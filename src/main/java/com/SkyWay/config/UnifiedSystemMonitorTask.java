package com.SkyWay.config;

import com.SkyWay.config.health.AirlineOperationalTelemetryTracker;
import com.SkyWay.config.health.AzureCloudEnvironmentTracker;
import com.SkyWay.config.health.DatabasePerformanceTracker;
import com.SkyWay.config.health.HttpRequestMetricsTracker;
import com.SkyWay.config.health.HttpSessionMetricsListener;
import com.SkyWay.config.health.JSFMetricsTracker;
import com.SkyWay.modules.reservaasiento.domain.service.AsientoCacheService;
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.system.JavaVersion;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Monitor unificado de telemetría Full-Stack en tiempo real vía WebSocket ({@code /topic/metrics}).
 * Activación configurable por variable de entorno ({@code SKYWAY_TELEMETRIA_ACTIVA})
 * o dinámicamente en tiempo de ejecución desde el panel administrativo.
 */
@Service
public class UnifiedSystemMonitorTask {

    private static final long MEGABYTE = 1024L * 1024L;
    private static final int INTERVALO_MIN_SEG = 1;
    private static final int INTERVALO_MAX_SEG = 60;
    private static final int MAX_HISTORIAL_PUNTOS = 30;

    private final ThreadMXBean threadBean;
    private final List<GarbageCollectorMXBean> gcBeans;
    private final RuntimeMXBean runtimeBean;
    private final ScheduledExecutorService scheduler;
    private final ExecutorService dbRefreshExecutor;
    private final Object controlLock = new Object();
    private final AtomicBoolean dbRefreshEnCurso = new AtomicBoolean(false);
    private final Deque<Map<String, Object>> historyBuffer = new ConcurrentLinkedDeque<>();

    private final SimpMessagingTemplate messagingTemplate;
    private final AzureCloudEnvironmentTracker cloudTracker;
    private final DatabasePerformanceTracker dbTracker;
    private final HttpRequestMetricsTracker httpTracker;
    private final AirlineOperationalTelemetryTracker opsTracker;
    private final ApplicationContext applicationContext;
    private final ObjectProvider<AsientoCacheService> asientoCacheProvider;
    private final ObjectProvider<WebSocketMessageBrokerStats> wsStatsProvider;
    private final TelemetryChannelSecurityInterceptor telemetrySecurityInterceptor;

    private final boolean configuradaActivaPorEntorno;
    private final long autoPausaMs;
    private final AtomicBoolean telemetriaActiva;
    private final AtomicInteger intervaloSegundos;
    private final AtomicLong ticksEmitidos = new AtomicLong(0);
    private volatile long ultimoTickTimestamp = 0L;
    private volatile long activadaEnTimestamp = 0L;
    private ScheduledFuture<?> tareaProgramada;

    private final Map<String, Object> cachedRuntimeInfo;

    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    @Value("${server.port:8080}")
    private String serverPort;

    public UnifiedSystemMonitorTask(
            SimpMessagingTemplate messagingTemplate,
            AzureCloudEnvironmentTracker cloudTracker,
            DatabasePerformanceTracker dbTracker,
            HttpRequestMetricsTracker httpTracker,
            AirlineOperationalTelemetryTracker opsTracker,
            ApplicationContext applicationContext,
            ObjectProvider<AsientoCacheService> asientoCacheProvider,
            ObjectProvider<WebSocketMessageBrokerStats> wsStatsProvider,
            TelemetryChannelSecurityInterceptor telemetrySecurityInterceptor,
            @Value("${app.telemetria.activa:false}") boolean activaPorEntorno,
            @Value("${app.telemetria.intervalo-segundos:5}") int intervaloInicialSeg,
            @Value("${app.telemetria.auto-pausa-minutos:15}") long autoPausaMinutos
    ) {
        this.messagingTemplate = messagingTemplate;
        this.cloudTracker = cloudTracker;
        this.dbTracker = dbTracker;
        this.httpTracker = httpTracker;
        this.opsTracker = opsTracker;
        this.applicationContext = applicationContext;
        this.asientoCacheProvider = asientoCacheProvider;
        this.wsStatsProvider = wsStatsProvider;
        this.telemetrySecurityInterceptor = telemetrySecurityInterceptor;

        this.configuradaActivaPorEntorno = activaPorEntorno;
        this.autoPausaMs = Math.max(0L, autoPausaMinutos) * 60_000L;
        this.telemetriaActiva = new AtomicBoolean(activaPorEntorno);
        this.intervaloSegundos = new AtomicInteger(normalizarIntervalo(intervaloInicialSeg));

        this.threadBean = ManagementFactory.getThreadMXBean();
        this.gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        this.runtimeBean = ManagementFactory.getRuntimeMXBean();
        this.cachedRuntimeInfo = construirRuntimeInfoEstatica();

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "unified-system-monitor-thread");
            thread.setDaemon(true);
            return thread;
        });

        this.dbRefreshExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "telemetry-db-refresh-thread");
            thread.setDaemon(true);
            return thread;
        });
    }

    @PostConstruct
    public void startMonitor() {
        synchronized (controlLock) {
            if (telemetriaActiva.get()) {
                this.activadaEnTimestamp = System.currentTimeMillis();
                programarTransmision();
                Logger.logInfo("📡 Telemetría en vivo ACTIVADA por configuración (intervalo: " + intervaloSegundos.get() + "s)");
            } else {
                Logger.logInfo("⏸️ Telemetría en vivo en espera (activar vía SKYWAY_TELEMETRIA_ACTIVA=true o desde /admin/metrics-ws.xhtml)");
            }
        }
    }

    public Map<String, Object> setTelemetriaActiva(boolean activar, Integer nuevoIntervaloSeg) {
        synchronized (controlLock) {
            if (nuevoIntervaloSeg != null) {
                this.intervaloSegundos.set(normalizarIntervalo(nuevoIntervaloSeg));
            }
            this.telemetriaActiva.set(activar);

            if (activar) {
                this.activadaEnTimestamp = System.currentTimeMillis();
                programarTransmisionConRetrasoInicial(intervaloSegundos.get());
                Logger.logInfo("📡 Telemetría en vivo ACTIVADA en tiempo de ejecución (intervalo: " + intervaloSegundos.get() + "s)");
            } else {
                this.activadaEnTimestamp = 0L;
                cancelarTareaActiva();
                Logger.logInfo("⏸️ Telemetría en vivo PAUSADA en tiempo de ejecución.");
            }
        }

        Map<String, Object> payload = emitirSnapshotRapido(true);
        if (activar) {
            dispararRefrescoBdAsync(false);
        }
        return payload;
    }

    public Map<String, Object> cambiarIntervalo(int segundos) {
        synchronized (controlLock) {
            this.intervaloSegundos.set(normalizarIntervalo(segundos));
            if (telemetriaActiva.get()) {
                this.activadaEnTimestamp = System.currentTimeMillis();
                programarTransmisionConRetrasoInicial(intervaloSegundos.get());
            }
        }
        return emitirSnapshotRapido(true);
    }

    public Map<String, Object> emitirSnapshotInmediato() {
        return emitirSnapshotRapido(true);
    }

    public Map<String, Object> emitirSnapshotRapido(boolean incluirHistorial) {
        Map<String, Object> payload = construirSnapshotRapido(incluirHistorial);
        difundirPayloadWs(payload);
        return payload;
    }

    public Map<String, Object> emitirSnapshotInmediato(boolean incluirHistorial) {
        Map<String, Object> payload = construirSnapshotCompleto(incluirHistorial, true);
        difundirPayloadWs(payload);
        return payload;
    }

    public Map<String, Object> solicitarGcYEmitir() {
        System.gc();
        return emitirSnapshotRapido(true);
    }

    public Map<String, Object> construirSnapshotInicialRapido() {
        Map<String, Object> payload = construirSnapshotRapido(true);
        dispararRefrescoBdAsync(false);
        return payload;
    }

    public Map<String, Object> construirSnapshotRapido(boolean incluirHistorial) {
        long ahora = System.currentTimeMillis();
        return ensamblarPayload(
                ahora,
                incluirHistorial,
                false,
                dbTracker.getCachedOrQuickMetrics(),
                opsTracker.getCachedOrQuickMetrics()
        );
    }

    public Map<String, Object> construirSnapshotCompleto() {
        return construirSnapshotCompleto(true, true);
    }

    public Map<String, Object> construirSnapshotCompleto(boolean incluirHistorial, boolean forzarRefrescoBd) {
        long ahora = System.currentTimeMillis();
        if (telemetriaActiva.get()) {
            ticksEmitidos.incrementAndGet();
        }
        ultimoTickTimestamp = ahora;

        return ensamblarPayload(
                ahora,
                incluirHistorial,
                true,
                dbTracker.collectMetrics(true, forzarRefrescoBd),
                opsTracker.collectMetrics(forzarRefrescoBd)
        );
    }

    private Map<String, Object> ensamblarPayload(
            long ahora,
            boolean incluirHistorial,
            boolean registrarHistorial,
            Map<String, Object> db,
            Map<String, Object> ops
    ) {
        Map<String, Object> cloud = cloudTracker.collectMetrics();
        Map<String, Object> threads = recopilarHilos();
        Map<String, Object> http = httpTracker.collectMetrics();

        Map<String, Object> payload = new LinkedHashMap<>(15);
        payload.put("timestamp", ahora);
        payload.put("telemetry", recopilarEstadoTelemetria(ahora));
        payload.put("cloud", cloud);
        payload.put("memory_pools", recopilarMemoryPools());
        payload.put("db", db);
        payload.put("gc", recopilarGarbageCollector());
        payload.put("threads", threads);
        payload.put("http", http);
        payload.put("jsf", JSFMetricsTracker.collectMetrics());
        payload.put("app", recopilarAplicacion());
        payload.put("ops", ops);
        payload.put("tomcat", recopilarTomcatJmx());
        payload.put("ws_broker", recopilarWebSocketStats());
        payload.put("runtime_info", cachedRuntimeInfo);
        payload.put("alerts", evaluarAlertasSalud(cloud, db, threads, http));

        if (registrarHistorial) {
            registrarPuntoHistorial(ahora, cloud, db, http);
        }
        if (incluirHistorial) {
            payload.put("history", new ArrayList<>(historyBuffer));
        }
        return payload;
    }

    public void dispararRefrescoBdAsync(boolean forzar) {
        if (!forzar && !dbTracker.necesitaRefresco() && !opsTracker.necesitaRefresco()) {
            return;
        }
        if (!dbRefreshEnCurso.compareAndSet(false, true)) {
            return;
        }
        dbRefreshExecutor.submit(() -> {
            try {
                boolean actualizado = false;
                if (forzar || dbTracker.necesitaRefresco()) {
                    actualizado |= dbTracker.refrescarDesdeBd(forzar);
                }
                if (forzar || opsTracker.necesitaRefresco()) {
                    opsTracker.refrescarDesdeBd();
                    actualizado = true;
                }
                if (actualizado && telemetrySecurityInterceptor.getActiveViewerCount() > 0) {
                    difundirPayloadWs(construirSnapshotRapido(false));
                }
            } catch (Exception e) {
                Logger.logInfo("⚠️ Error refrescando métricas de BD en segundo plano: " + e.getMessage());
            } finally {
                dbRefreshEnCurso.set(false);
            }
        });
    }

    private void difundirPayloadWs(Map<String, Object> payload) {
        try {
            messagingTemplate.convertAndSend("/topic/metrics", payload);
        } catch (Exception e) {
            Logger.logInfo("⚠️ Error transmitiendo snapshot WebSocket: " + e.getMessage());
        }
    }

    private void registrarPuntoHistorial(
            long timestamp,
            Map<String, Object> cloud,
            Map<String, Object> db,
            Map<String, Object> http
    ) {
        Map<String, Object> punto = new LinkedHashMap<>();
        punto.put("ts", timestamp);
        punto.put("ram_mb", cloud.getOrDefault("jvm_process_ram_mb", 0));
        punto.put("heap_used_mb", cloud.getOrDefault("heap_used_mb", 0));
        punto.put("heap_total_mb", cloud.getOrDefault("heap_total_mb", 0));
        punto.put("sql_ms", toDouble(db.get("latency_ping_ms")) >= 0 ? db.get("latency_ping_ms") : 0);
        punto.put("cpu_pct", cloud.getOrDefault("process_cpu_load", 0));
        punto.put("http_rpm", http.getOrDefault("requests_per_min", 0));
        punto.put("http_avg_ms", http.getOrDefault("avg_latency_ms", 0));
        historyBuffer.addLast(punto);
        while (historyBuffer.size() > MAX_HISTORIAL_PUNTOS) {
            historyBuffer.pollFirst();
        }
    }

    public boolean isTelemetriaActiva() {
        return telemetriaActiva.get();
    }

    public int getIntervaloSegundos() {
        return intervaloSegundos.get();
    }

    private void programarTransmision() {
        programarTransmisionConRetrasoInicial(0);
    }

    private void programarTransmisionConRetrasoInicial(int initialDelaySec) {
        synchronized (controlLock) {
            cancelarTareaActiva();
            int intervalo = intervaloSegundos.get();
            this.tareaProgramada = this.scheduler.scheduleAtFixedRate(
                    this::collectAndBroadcastMetrics,
                    Math.max(0, initialDelaySec),
                    intervalo,
                    TimeUnit.SECONDS
            );
        }
    }

    private void cancelarTareaActiva() {
        synchronized (controlLock) {
            if (this.tareaProgramada != null && !this.tareaProgramada.isCancelled()) {
                this.tareaProgramada.cancel(false);
                this.tareaProgramada = null;
            }
        }
    }

    private void collectAndBroadcastMetrics() {
        if (!telemetriaActiva.get()) {
            return;
        }
        long ahora = System.currentTimeMillis();
        if (autoPausaMs > 0 && !configuradaActivaPorEntorno && activadaEnTimestamp > 0L
                && (ahora - activadaEnTimestamp) >= autoPausaMs) {
            setTelemetriaActiva(false, null);
            Logger.logInfo("🛡️ Telemetría en vivo auto-pausada tras alcanzar el límite protector de Azure F1.");
            return;
        }
        // En Azure F1 / Free Tier evitamos ejecutar SQL y serialización si no hay ningún admin viendo /topic/metrics
        if (telemetrySecurityInterceptor.getActiveViewerCount() <= 0) {
            return;
        }
        try {
            Map<String, Object> payload = construirSnapshotCompleto(false, false);
            messagingTemplate.convertAndSend("/topic/metrics", payload);
        } catch (Exception e) {
            Logger.logInfo("⚠️ Error recopilando métricas: " + e.getMessage());
        }
    }

    private Map<String, Object> recopilarEstadoTelemetria(long ahora) {
        long autoPauseRemainingSec = -1L;
        if (telemetriaActiva.get() && autoPausaMs > 0 && !configuradaActivaPorEntorno && activadaEnTimestamp > 0L) {
            long restanteMs = autoPausaMs - (ahora - activadaEnTimestamp);
            autoPauseRemainingSec = Math.max(0L, restanteMs / 1000L);
        }

        Map<String, Object> status = new LinkedHashMap<>();
        status.put("enabled", telemetriaActiva.get());
        status.put("interval_sec", intervaloSegundos.get());
        status.put("env_default_enabled", configuradaActivaPorEntorno);
        status.put("active_viewers", telemetrySecurityInterceptor.getActiveViewerCount());
        status.put("auto_pause_remaining_sec", autoPauseRemainingSec);
        status.put("ticks_emitted", ticksEmitidos.get());
        status.put("last_tick_ts", ultimoTickTimestamp);
        status.put("mode_label", telemetriaActiva.get()
                ? "ACTIVA (" + intervaloSegundos.get() + "s)"
                : "PAUSADA");
        return status;
    }

    private List<Map<String, Object>> recopilarMemoryPools() {
        List<Map<String, Object>> pools = new ArrayList<>();
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            MemoryUsage usage = pool.getUsage();
            if (usage == null) {
                continue;
            }
            long usedMb = Math.max(0L, usage.getUsed() / MEGABYTE);
            long committedMb = Math.max(0L, usage.getCommitted() / MEGABYTE);
            long maxMb = usage.getMax() > 0 ? usage.getMax() / MEGABYTE : -1L;
            double pct = maxMb > 0 ? Math.round(((double) usedMb / maxMb) * 1000.0) / 10.0 : 0.0;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", pool.getName());
            item.put("type", pool.getType().name());
            item.put("used_mb", usedMb);
            item.put("committed_mb", committedMb);
            item.put("max_mb", maxMb);
            item.put("usage_pct", pct);
            pools.add(item);
        }
        return pools;
    }

    private Map<String, Object> recopilarGarbageCollector() {
        long gcCount = 0;
        long gcTimeMs = 0;
        List<Map<String, Object>> collectors = new ArrayList<>();

        for (GarbageCollectorMXBean gc : gcBeans) {
            long count = Math.max(0L, gc.getCollectionCount());
            long time = Math.max(0L, gc.getCollectionTime());
            gcCount += count;
            gcTimeMs += time;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", gc.getName());
            item.put("count", count);
            item.put("time_ms", time);
            collectors.add(item);
        }

        Map<String, Object> gcMap = new LinkedHashMap<>();
        gcMap.put("total_collections", gcCount);
        gcMap.put("total_time_ms", gcTimeMs);
        gcMap.put("collectors", collectors);
        return gcMap;
    }

    private Map<String, Object> recopilarHilos() {
        int runnable = 0, waiting = 0, timedWaiting = 0, blocked = 0;
        long[] ids = threadBean.getAllThreadIds();
        ThreadInfo[] infos = threadBean.getThreadInfo(ids, 0);
        if (infos != null) {
            for (ThreadInfo info : infos) {
                if (info == null) {
                    continue;
                }
                switch (info.getThreadState()) {
                    case RUNNABLE -> runnable++;
                    case WAITING -> waiting++;
                    case TIMED_WAITING -> timedWaiting++;
                    case BLOCKED -> blocked++;
                    default -> {
                    }
                }
            }
        }

        long[] deadlocked = threadBean.findDeadlockedThreads();
        boolean cpuTimeSupported = threadBean.isThreadCpuTimeSupported() && threadBean.isThreadCpuTimeEnabled();
        List<Map<String, Object>> topThreads = new ArrayList<>();
        if (infos != null) {
            List<Map<String, Object>> candidatos = new ArrayList<>();
            for (ThreadInfo info : infos) {
                if (info == null) continue;
                long cpuMs = cpuTimeSupported ? Math.max(0L, threadBean.getThreadCpuTime(info.getThreadId()) / 1_000_000L) : 0L;
                Map<String, Object> t = new LinkedHashMap<>();
                t.put("id", info.getThreadId());
                t.put("name", info.getThreadName());
                t.put("state", info.getThreadState().name());
                t.put("daemon", info.isDaemon());
                t.put("cpu_ms", cpuMs);
                candidatos.add(t);
            }
            topThreads = candidatos.stream()
                    .sorted(Comparator.comparingLong((Map<String, Object> m) -> toLong(m.get("cpu_ms"))).reversed())
                    .limit(6)
                    .toList();
        }

        Map<String, Object> threads = new LinkedHashMap<>();
        threads.put("total", threadBean.getThreadCount());
        threads.put("peak", threadBean.getPeakThreadCount());
        threads.put("daemon", threadBean.getDaemonThreadCount());
        threads.put("total_started", threadBean.getTotalStartedThreadCount());
        threads.put("runnable", runnable);
        threads.put("waiting", waiting);
        threads.put("timed_waiting", timedWaiting);
        threads.put("blocked", blocked);
        threads.put("deadlocked", deadlocked != null ? deadlocked.length : 0);
        threads.put("top_threads", topThreads);
        return threads;
    }

    private Map<String, Object> recopilarTomcatJmx() {
        Map<String, Object> tomcat = new LinkedHashMap<>();
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> threadPools = mBeanServer.queryNames(new ObjectName("Tomcat:type=ThreadPool,*"), null);
            if (!threadPools.isEmpty()) {
                ObjectName pool = threadPools.iterator().next();
                tomcat.put("connector_name", pool.getKeyProperty("name"));
                tomcat.put("max_threads", mBeanServer.getAttribute(pool, "maxThreads"));
                tomcat.put("current_threads", mBeanServer.getAttribute(pool, "currentThreadCount"));
                tomcat.put("threads_busy", mBeanServer.getAttribute(pool, "currentThreadsBusy"));
            }
            Set<ObjectName> processors = mBeanServer.queryNames(new ObjectName("Tomcat:type=GlobalRequestProcessor,*"), null);
            if (!processors.isEmpty()) {
                ObjectName proc = processors.iterator().next();
                long bytesRec = toLong(mBeanServer.getAttribute(proc, "bytesReceived"));
                long bytesSent = toLong(mBeanServer.getAttribute(proc, "bytesSent"));
                tomcat.put("request_count", mBeanServer.getAttribute(proc, "requestCount"));
                tomcat.put("error_count", mBeanServer.getAttribute(proc, "errorCount"));
                tomcat.put("bytes_received_mb", Math.round((bytesRec / (double) MEGABYTE) * 100.0) / 100.0);
                tomcat.put("bytes_sent_mb", Math.round((bytesSent / (double) MEGABYTE) * 100.0) / 100.0);
            }
        } catch (Exception ignored) {
        }
        return tomcat;
    }

    private Map<String, Object> recopilarAplicacion() {
        AsientoCacheService cacheService = asientoCacheProvider.getIfAvailable();
        int vuelosEnCache = cacheService != null ? cacheService.getCantidadVuelosEnCache() : 0;
        int asientosRetenidos = cacheService != null ? cacheService.getCantidadAsientosRetenidos() : 0;

        Map<String, Object> app = new LinkedHashMap<>();
        app.put("active_sessions", HttpSessionMetricsListener.getActiveSessions());
        app.put("total_created_sessions", HttpSessionMetricsListener.getTotalCreatedSessions());
        app.put("spring_beans_count", applicationContext.getBeanDefinitionCount());
        app.put("uptime_ms", runtimeBean.getUptime());
        app.put("start_time_iso", DateTimeFormatter.ISO_OFFSET_DATE_TIME
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(runtimeBean.getStartTime())));
        app.put("active_profile", activeProfile);
        app.put("server_port", serverPort);
        app.put("pid", ProcessHandle.current().pid());
        app.put("java_version", JavaVersion.getJavaVersion().toString());
        app.put("java_vendor", System.getProperty("java.vendor", "Java"));
        app.put("jvm_name", runtimeBean.getVmName());
        app.put("spring_boot_version", SpringBootVersion.getVersion());
        app.put("cached_flight_seatmaps", vuelosEnCache);
        app.put("active_seat_locks", asientosRetenidos);
        return app;
    }

    private Map<String, Object> recopilarWebSocketStats() {
        Map<String, Object> ws = new LinkedHashMap<>();
        WebSocketMessageBrokerStats stats = wsStatsProvider.getIfAvailable();
        if (stats != null) {
            ws.put("session_stats", stats.getWebSocketSessionStatsInfo());
            ws.put("stomp_relay_stats", stats.getStompSubProtocolStatsInfo());
            ws.put("inbound_stats", stats.getClientInboundExecutorStatsInfo());
            ws.put("outbound_stats", stats.getClientOutboundExecutorStatsInfo());
        } else {
            ws.put("session_stats", "N/A");
            ws.put("stomp_relay_stats", "N/A");
            ws.put("inbound_stats", "N/A");
            ws.put("outbound_stats", "N/A");
        }
        return ws;
    }

    private Map<String, Object> construirRuntimeInfoEstatica() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("timezone", TimeZone.getDefault().getID());
        info.put("charset", Charset.defaultCharset().name());
        info.put("java_runtime_version", System.getProperty("java.runtime.version", "-"));
        info.put("jvm_flags", String.join(" ", runtimeBean.getInputArguments()));

        List<String> interfaces = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            while (nics != null && nics.hasMoreElements()) {
                NetworkInterface nic = nics.nextElement();
                if (!nic.isUp() || nic.isLoopback()) {
                    continue;
                }
                Enumeration<InetAddress> addrs = nic.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (addr instanceof Inet4Address) {
                        interfaces.add(nic.getDisplayName() + " (" + addr.getHostAddress() + ")");
                    }
                }
            }
        } catch (Exception ignored) {
        }
        info.put("network_interfaces", interfaces);
        return info;
    }

    private List<Map<String, String>> evaluarAlertasSalud(
            Map<String, Object> cloud,
            Map<String, Object> db,
            Map<String, Object> threads,
            Map<String, Object> http
    ) {
        List<Map<String, String>> alertas = new ArrayList<>();

        double f1Pct = toDouble(cloud.get("azure_f1_pressure_pct"));
        if (f1Pct >= 85.0) {
            alertas.add(crearAlerta("danger", "Presión de memoria JVM alta (" + f1Pct + "% vs 1024 MB). Considera ejecutar GC."));
        } else if (f1Pct >= 70.0) {
            alertas.add(crearAlerta("warning", "Consumo JVM moderado-alto (" + f1Pct + "%)."));
        }

        if (!"UP".equals(String.valueOf(db.get("db_status")))) {
            alertas.add(crearAlerta("danger", "Base de datos PostgreSQL sin respuesta (" + db.get("last_error") + ")."));
        } else if (toDouble(db.get("latency_ping_ms")) > 100.0) {
            alertas.add(crearAlerta("warning", "Latencia SQL elevada (" + db.get("latency_ping_ms") + " ms)."));
        }

        if (toLong(db.get("pg_waiting_locks")) > 0 || toLong(db.get("threads_awaiting")) > 0) {
            alertas.add(crearAlerta("warning", "Contención detectada en BD/Hikari (locks en espera o hilos aguardando conexión)."));
        }

        if (toLong(threads.get("deadlocked")) > 0) {
            alertas.add(crearAlerta("danger", "¡Deadlock detectado en hilos JVM!"));
        }

        if (toLong(http.get("status_5xx")) > 0) {
            alertas.add(crearAlerta("warning", "Se registraron respuestas HTTP 5xx (" + http.get("status_5xx") + "). Revisa la pestaña Tráfico HTTP."));
        }

        if (alertas.isEmpty()) {
            alertas.add(crearAlerta("ok", "Todos los subsistemas (JVM, PostgreSQL, HikariCP, Hilos y HTTP) operan en rangos nominales."));
        }
        return alertas;
    }

    private Map<String, String> crearAlerta(String nivel, String mensaje) {
        return Map.of("level", nivel, "message", mensaje);
    }

    private double toDouble(Object v) {
        return v instanceof Number n ? n.doubleValue() : 0.0;
    }

    private long toLong(Object v) {
        return v instanceof Number n ? n.longValue() : 0L;
    }

    private int normalizarIntervalo(int segundos) {
        return Math.max(INTERVALO_MIN_SEG, Math.min(INTERVALO_MAX_SEG, segundos));
    }

    @PreDestroy
    public void stopMonitor() {
        synchronized (controlLock) {
            cancelarTareaActiva();
        }
        this.scheduler.shutdown();
        this.dbRefreshExecutor.shutdown();
        try {
            if (!this.scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                this.scheduler.shutdownNow();
            }
            if (!this.dbRefreshExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                this.dbRefreshExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            this.scheduler.shutdownNow();
            this.dbRefreshExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
