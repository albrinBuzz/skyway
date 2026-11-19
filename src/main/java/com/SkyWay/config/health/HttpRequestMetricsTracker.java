package com.SkyWay.config.health;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Filtro ligero de telemetría HTTP y red en memoria para medir tráfico, IPs cliente,
 * cabeceras de proxy/Azure, dispositivos, endpoints más activos y errores recientes.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class HttpRequestMetricsTracker extends OncePerRequestFilter {

    private static final long VENTANA_RPM_MS = 60_000L;
    private static final long VENTANA_RPS_MS = 5_000L;
    private static final int MAX_ENDPOINTS_RASTREADOS = 150;
    private static final int MAX_IPS_RASTREADAS = 300;
    private static final int MAX_ERRORES_RECIENTES = 6;

    private final AtomicInteger activeRequests = new AtomicInteger(0);
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong status2xx = new AtomicLong(0);
    private final AtomicLong status3xx = new AtomicLong(0);
    private final AtomicLong status4xx = new AtomicLong(0);
    private final AtomicLong status5xx = new AtomicLong(0);
    private final AtomicLong totalLatencyMs = new AtomicLong(0);
    private final AtomicLong maxLatencyMs = new AtomicLong(0);
    private final AtomicLong totalRequestBytes = new AtomicLong(0);

    private final AtomicLong hitsDesktop = new AtomicLong(0);
    private final AtomicLong hitsMobile = new AtomicLong(0);
    private final AtomicLong hitsBotOrApi = new AtomicLong(0);

    private final AtomicReference<String> lastMethod = new AtomicReference<>("-");
    private final AtomicReference<String> lastUri = new AtomicReference<>("-");
    private final AtomicReference<Long> lastLatencyMs = new AtomicReference<>(0L);
    private final AtomicReference<String> lastClientIp = new AtomicReference<>("-");
    private final AtomicReference<String> lastForwardedChain = new AtomicReference<>("-");
    private final AtomicReference<String> lastHostHeader = new AtomicReference<>("-");
    private final AtomicReference<String> lastSchemeProto = new AtomicReference<>("-");

    private final Deque<Long> recentRequestTimestamps = new ConcurrentLinkedDeque<>();
    private final ConcurrentHashMap<String, EndpointAccumulator> endpointStats = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ClientIpAccumulator> clientIpStats = new ConcurrentHashMap<>();
    private final Deque<Map<String, Object>> recentErrors = new ConcurrentLinkedDeque<>();

    private static final class EndpointAccumulator {
        final AtomicLong hits = new AtomicLong(0);
        final AtomicLong totalLatencyMs = new AtomicLong(0);
        final AtomicLong maxLatencyMs = new AtomicLong(0);
        final AtomicLong errors = new AtomicLong(0);

        void record(long latencyMs, boolean isError) {
            hits.incrementAndGet();
            totalLatencyMs.addAndGet(latencyMs);
            if (isError) {
                errors.incrementAndGet();
            }
            long currentMax;
            do {
                currentMax = maxLatencyMs.get();
                if (latencyMs <= currentMax) {
                    break;
                }
            } while (!maxLatencyMs.compareAndSet(currentMax, latencyMs));
        }
    }

    private static final class ClientIpAccumulator {
        final AtomicLong hits = new AtomicLong(0);
        final AtomicLong lastSeenMs = new AtomicLong(0);
        final AtomicReference<String> lastUri = new AtomicReference<>("-");
        final AtomicReference<String> device = new AtomicReference<>("Desktop");

        void record(long ahora, String uri, String deviceCategory) {
            hits.incrementAndGet();
            lastSeenMs.set(ahora);
            lastUri.set(uri);
            device.set(deviceCategory);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) {
            return true;
        }
        return uri.startsWith("/jakarta.faces.resource")
                || uri.startsWith("/resources/")
                || uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/images/")
                || uri.startsWith("/ws-metrics")
                || uri.startsWith("/ws-asientos")
                || uri.startsWith("/api/admin/telemetry");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startNs = System.nanoTime();
        activeRequests.incrementAndGet();
        try {
            filterChain.doFilter(request, response);
        } finally {
            activeRequests.decrementAndGet();
            long latencyMs = Math.max(0L, (System.nanoTime() - startNs) / 1_000_000L);
            long ahora = System.currentTimeMillis();

            totalRequests.incrementAndGet();
            totalLatencyMs.addAndGet(latencyMs);
            lastLatencyMs.set(latencyMs);
            actualizarMaximo(latencyMs);

            long contentLen = request.getContentLengthLong();
            if (contentLen > 0) {
                totalRequestBytes.addAndGet(contentLen);
            }

            String method = request.getMethod() != null ? request.getMethod() : "GET";
            String uri = request.getRequestURI() != null ? request.getRequestURI() : "/";
            lastMethod.set(method);
            lastUri.set(uri);

            String clientIp = resolverIpReal(request);
            String forwarded = resolverCadenaForwarded(request);
            String host = request.getHeader("Host");
            String proto = request.getHeader("X-Forwarded-Proto");
            String scheme = StringUtils.hasText(proto) ? proto : request.getScheme();
            String deviceCategory = clasificarDispositivo(request.getHeader("User-Agent"));

            lastClientIp.set(clientIp);
            lastForwardedChain.set(forwarded);
            lastHostHeader.set(StringUtils.hasText(host) ? host : "-");
            lastSchemeProto.set((scheme != null ? scheme.toUpperCase(Locale.ROOT) : "HTTP")
                    + " (" + (request.getProtocol() != null ? request.getProtocol() : "-") + ")");

            if ("Mobile".equals(deviceCategory)) {
                hitsMobile.incrementAndGet();
            } else if ("Bot/API".equals(deviceCategory)) {
                hitsBotOrApi.incrementAndGet();
            } else {
                hitsDesktop.incrementAndGet();
            }

            int status = response.getStatus();
            boolean esError = status >= 400;
            if (status >= 500) {
                status5xx.incrementAndGet();
            } else if (status >= 400) {
                status4xx.incrementAndGet();
            } else if (status >= 300) {
                status3xx.incrementAndGet();
            } else {
                status2xx.incrementAndGet();
            }

            String uriNormalizada = normalizarUri(uri);
            registrarEndpoint(method + " " + uriNormalizada, latencyMs, esError);
            registrarClienteIp(clientIp, ahora, uriNormalizada, deviceCategory);
            if (esError) {
                registrarError(ahora, method, uri, status, latencyMs, clientIp);
            }

            recentRequestTimestamps.addLast(ahora);
            depurarVentana(ahora);
        }
    }

    public Map<String, Object> collectMetrics() {
        long ahora = System.currentTimeMillis();
        depurarVentana(ahora);

        long total = totalRequests.get();
        long s4xx = status4xx.get();
        long s5xx = status5xx.get();
        double avgLatency = total > 0 ? Math.round(((double) totalLatencyMs.get() / total) * 10.0) / 10.0 : 0.0;
        double errorRate = total > 0 ? Math.round((((double) (s4xx + s5xx) / total) * 100.0) * 100.0) / 100.0 : 0.0;

        long limiteRps = ahora - VENTANA_RPS_MS;
        int hitsUltimos5s = 0;
        for (Long ts : recentRequestTimestamps) {
            if (ts >= limiteRps) {
                hitsUltimos5s++;
            }
        }
        double rps = Math.round((hitsUltimos5s / 5.0) * 10.0) / 10.0;

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("active_requests", Math.max(0, activeRequests.get()));
        metrics.put("total_requests", total);
        metrics.put("requests_per_min", recentRequestTimestamps.size());
        metrics.put("requests_per_sec", rps);
        metrics.put("status_2xx", status2xx.get());
        metrics.put("status_3xx", status3xx.get());
        metrics.put("status_4xx", s4xx);
        metrics.put("status_5xx", s5xx);
        metrics.put("error_rate_pct", errorRate);
        metrics.put("avg_latency_ms", avgLatency);
        metrics.put("max_latency_ms", maxLatencyMs.get());
        metrics.put("last_latency_ms", lastLatencyMs.get());
        metrics.put("last_method", lastMethod.get());
        metrics.put("last_uri", lastUri.get());

        // Red, IPs y Dispositivos
        metrics.put("last_client_ip", lastClientIp.get());
        metrics.put("last_forwarded_for", lastForwardedChain.get());
        metrics.put("last_host_header", lastHostHeader.get());
        metrics.put("last_scheme_proto", lastSchemeProto.get());
        metrics.put("unique_client_ips", clientIpStats.size());
        metrics.put("total_request_kb", Math.round((totalRequestBytes.get() / 1024.0) * 10.0) / 10.0);
        metrics.put("device_desktop_hits", hitsDesktop.get());
        metrics.put("device_mobile_hits", hitsMobile.get());
        metrics.put("device_api_hits", hitsBotOrApi.get());

        metrics.put("top_client_ips", obtenerTopIps());
        metrics.put("top_endpoints", obtenerTopEndpoints());
        metrics.put("recent_errors", new ArrayList<>(recentErrors));
        return metrics;
    }

    private String resolverIpReal(HttpServletRequest request) {
        String azureClient = request.getHeader("X-Azure-ClientIP");
        if (StringUtils.hasText(azureClient)) {
            return limpiarPuertoIp(azureClient);
        }
        String xff = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            int coma = xff.indexOf(',');
            String primera = coma > 0 ? xff.substring(0, coma) : xff;
            return limpiarPuertoIp(primera);
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return limpiarPuertoIp(realIp);
        }
        return limpiarPuertoIp(request.getRemoteAddr());
    }

    private String resolverCadenaForwarded(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            return xff.length() > 90 ? xff.substring(0, 90) : xff;
        }
        String azureClient = request.getHeader("X-Azure-ClientIP");
        if (StringUtils.hasText(azureClient)) {
            return "Azure: " + azureClient;
        }
        return "Conexión directa (" + limpiarPuertoIp(request.getRemoteAddr()) + ")";
    }

    private String limpiarPuertoIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return "-";
        }
        String s = ip.trim();
        // Caso IPv4 con puerto (ej: 190.160.12.4:54321)
        int colon = s.indexOf(':');
        if (colon > 0 && s.indexOf(':', colon + 1) == -1) {
            return s.substring(0, colon);
        }
        return s.length() > 45 ? s.substring(0, 45) : s;
    }

    private String clasificarDispositivo(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return "Bot/API";
        }
        String ua = userAgent.toLowerCase(Locale.ROOT);
        if (ua.contains("bot") || ua.contains("crawl") || ua.contains("curl") || ua.contains("postman") || ua.contains("python")) {
            return "Bot/API";
        }
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone") || ua.contains("ipad")) {
            return "Mobile";
        }
        return "Desktop";
    }

    private void registrarClienteIp(String ip, long ahora, String uri, String deviceCategory) {
        ClientIpAccumulator acc = clientIpStats.get(ip);
        if (acc == null) {
            if (clientIpStats.size() >= MAX_IPS_RASTREADAS) {
                return;
            }
            acc = clientIpStats.computeIfAbsent(ip, k -> new ClientIpAccumulator());
        }
        acc.record(ahora, uri, deviceCategory);
    }

    private List<Map<String, Object>> obtenerTopIps() {
        return clientIpStats.entrySet().stream()
                .sorted(Comparator.comparingLong((Map.Entry<String, ClientIpAccumulator> e) -> e.getValue().hits.get()).reversed())
                .limit(6)
                .map(entry -> {
                    ClientIpAccumulator acc = entry.getValue();
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("ip", entry.getKey());
                    item.put("hits", acc.hits.get());
                    item.put("device", acc.device.get());
                    item.put("last_uri", acc.lastUri.get());
                    item.put("last_seen", acc.lastSeenMs.get());
                    return item;
                })
                .toList();
    }

    private void registrarEndpoint(String key, long latencyMs, boolean isError) {
        EndpointAccumulator acc = endpointStats.get(key);
        if (acc == null) {
            if (endpointStats.size() >= MAX_ENDPOINTS_RASTREADOS) {
                return;
            }
            acc = endpointStats.computeIfAbsent(key, k -> new EndpointAccumulator());
        }
        acc.record(latencyMs, isError);
    }

    private void registrarError(long timestamp, String method, String uri, int status, long latencyMs, String clientIp) {
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("timestamp", timestamp);
        err.put("method", method);
        err.put("uri", uri);
        err.put("status", status);
        err.put("latency_ms", latencyMs);
        err.put("ip", clientIp);
        recentErrors.addFirst(err);
        while (recentErrors.size() > MAX_ERRORES_RECIENTES) {
            recentErrors.pollLast();
        }
    }

    private List<Map<String, Object>> obtenerTopEndpoints() {
        return endpointStats.entrySet().stream()
                .sorted(Comparator.comparingLong((Map.Entry<String, EndpointAccumulator> e) -> e.getValue().hits.get()).reversed())
                .limit(6)
                .map(entry -> {
                    EndpointAccumulator acc = entry.getValue();
                    long hits = acc.hits.get();
                    double avg = hits > 0 ? Math.round(((double) acc.totalLatencyMs.get() / hits) * 10.0) / 10.0 : 0.0;
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("endpoint", entry.getKey());
                    item.put("hits", hits);
                    item.put("avg_ms", avg);
                    item.put("max_ms", acc.maxLatencyMs.get());
                    item.put("errors", acc.errors.get());
                    return item;
                })
                .toList();
    }

    private String normalizarUri(String uri) {
        int semi = uri.indexOf(';');
        String limpia = semi >= 0 ? uri.substring(0, semi) : uri;
        return limpia.length() > 80 ? limpia.substring(0, 80) : limpia;
    }

    private void actualizarMaximo(long candidato) {
        long actual;
        do {
            actual = maxLatencyMs.get();
            if (candidato <= actual) {
                return;
            }
        } while (!maxLatencyMs.compareAndSet(actual, candidato));
    }

    private void depurarVentana(long ahora) {
        long limite = ahora - VENTANA_RPM_MS;
        while (true) {
            Long head = recentRequestTimestamps.peekFirst();
            if (head == null || head >= limite) {
                break;
            }
            recentRequestTimestamps.pollFirst();
        }
    }
}
