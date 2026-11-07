package com.SkyWay.modules.auditoria.application;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LogAuditoriaService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Registro estándar para operaciones ordinarias del sistema.
     */
    @Transactional
    public void registrarLog(String identificadorUsuario, String accion, String tablaAfectada, Integer idRegistroAfectado, String detalleBase, HttpServletRequest request) {
        registrarLogExhaustivo(identificadorUsuario, accion, "INFO", tablaAfectada, idRegistroAfectado, null, null, detalleBase, null, request);
    }

    /**
     * Registro especializado para excepciones y fallos del sistema.
     */
    @Transactional
    public void registrarLogError(String identificadorUsuario, String accion, String tablaAfectada, Integer idRegistroAfectado, Throwable excepcion, HttpServletRequest request) {
        registrarLogExhaustivo(identificadorUsuario, accion, "ERROR", tablaAfectada, idRegistroAfectado, null, null, excepcion != null ? excepcion.getMessage() : "Error no especificado", excepcion, request);
    }

    /**
     * Método principal de persistencia de auditoría inmutable vía procedimiento almacenado.
     */
    @Transactional
    public void registrarLogExhaustivo(String identificadorUsuario, String accion, String nivel, String tablaAfectada,
                                       Integer idRegistroAfectado, String datosAnterioresJson, String datosNuevosJson,
                                       String detalleBase, Throwable excepcion, HttpServletRequest request) {

        String ip = obtenerIpCliente(request);
        String rutObtenido = obtenerRutPorIdentificador(identificadorUsuario);
        String userAgent = (request != null) ? request.getHeader("User-Agent") : "Desconocido";
        if (userAgent != null && userAgent.length() > 255) {
            userAgent = userAgent.substring(0, 255);
        }

        String detalleTrazable = construirReporteTrazable(detalleBase, excepcion, request);

        entityManager.createNativeQuery(
                        "CALL sp_registrar_log_auditoria(" +
                                "CAST(:rut AS VARCHAR), " +
                                "CAST(:accion AS VARCHAR), " +
                                "CAST(:nivel AS VARCHAR), " +
                                "CAST(:tabla AS VARCHAR), " +
                                "CAST(:idRegistro AS INTEGER), " +
                                "CAST(:datosAnt AS JSONB), " +
                                "CAST(:datosNuev AS JSONB), " +
                                "CAST(:detalle AS TEXT), " +
                                "CAST(:ip AS VARCHAR), " +
                                "CAST(:userAgent AS VARCHAR))"
                )
                .setParameter("rut", rutObtenido)
                .setParameter("accion", accion)
                .setParameter("nivel", (nivel != null) ? nivel : "INFO")
                .setParameter("tabla", tablaAfectada)
                .setParameter("idRegistro", idRegistroAfectado)
                .setParameter("datosAnt", datosAnterioresJson)
                .setParameter("datosNuev", datosNuevosJson)
                .setParameter("detalle", detalleTrazable)
                .setParameter("ip", ip)
                .setParameter("userAgent", userAgent)
                .executeUpdate();
    }

    /**
     * Genera la traza técnica y operativa en formato estandarizado de pares CLAVE: VALOR.
     */
    private String construirReporteTrazable(String detalleBase, Throwable excepcion, HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        // 1. IDENTIFICACIÓN DEL EVENTO
        sb.append("[EVENTO DE AUDITORIA]\n");
        sb.append("DESCRIPCION   : ").append(detalleBase != null ? detalleBase.trim() : "Sin descripción").append("\n");
        sb.append("TIMESTAMP_UTC : ").append(LocalDateTime.now(ZoneId.of("UTC")).format(formatter)).append(" UTC\n");
        sb.append("TIMESTAMP_LOC : ").append(LocalDateTime.now().format(formatter)).append("\n\n");

        // 2. CONTEXTO DE SEGURIDAD Y ROLES
        sb.append("[SEGURIDAD & AUTENTICACION]\n");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            sb.append("PRINCIPAL     : ").append(auth.getName()).append("\n");
            sb.append("AUTENTICADO   : SÍ\n");
            sb.append("ROLES_ASIGNAD : ").append(auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(", "))).append("\n");
            sb.append("TIPO_AUTH_CLASS: ").append(auth.getClass().getSimpleName()).append("\n\n");
        } else {
            sb.append("PRINCIPAL     : ANONYMOUS\n");
            sb.append("AUTENTICADO   : NO\n");
            sb.append("ROLES_ASIGNAD : SIN_ROLES\n\n");
        }

        if (request != null) {
            // 3. CONTEXTO HTTP Y CANAL DE ACCESO AEROLÍNEA
            sb.append("[PETICION HTTP & CANAL]\n");
            sb.append("METODO        : ").append(request.getMethod()).append(" ").append(request.getRequestURI()).append("\n");
            sb.append("CANAL_ACCESO  : ").append(determinarCanal(request)).append("\n");
            sb.append("URL_COMPLETA  : ").append(request.getRequestURL().toString()).append("\n");
            sb.append("QUERY_STRING  : ").append(request.getQueryString() != null ? request.getQueryString() : "N/A").append("\n");
            sb.append("PROTOCOLO     : ").append(request.getScheme().toUpperCase()).append(" / ").append(request.getProtocol()).append("\n");
            sb.append("PUERTO_SERVER : ").append(request.getServerName()).append(":").append(request.getServerPort()).append("\n");
            sb.append("DIREC_LOCAL   : ").append(request.getLocalAddr()).append(":").append(request.getLocalPort()).append("\n\n");

            // 4. PARÁMETROS Y DATOS ENVIADOS
            sb.append("[PARAMETROS SOLICITUD]\n");
            Map<String, String[]> paramMap = request.getParameterMap();
            if (paramMap.isEmpty()) {
                sb.append("  (Sin parámetros)\n\n");
            } else {
                for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
                    String paramName = entry.getKey();
                    if (esCampoSensible(paramName)) {
                        sb.append("  - ").append(paramName).append(" = [PROTEGIDO/MASKED]\n");
                    } else {
                        sb.append("  - ").append(paramName).append(" = ").append(String.join(", ", entry.getValue())).append("\n");
                    }
                }
                sb.append("\n");
            }

            // 5. ESTADO DE LA SESIÓN HTTP
            sb.append("[SESION DE USUARIO]\n");
            HttpSession session = request.getSession(false);
            if (session != null) {
                sb.append("ID_SESION     : ").append(session.getId()).append("\n");
                sb.append("ES_NUEVA      : ").append(session.isNew() ? "SÍ" : "NO").append("\n");
                sb.append("CREACION      : ").append(LocalDateTime.ofInstant(Instant.ofEpochMilli(session.getCreationTime()), ZoneId.systemDefault()).format(formatter)).append("\n");
                sb.append("ULTIMO_ACCESO : ").append(LocalDateTime.ofInstant(Instant.ofEpochMilli(session.getLastAccessedTime()), ZoneId.systemDefault()).format(formatter)).append("\n");
                sb.append("TIMEOUT_MAX   : ").append(session.getMaxInactiveInterval()).append(" segundos\n\n");
            } else {
                sb.append("ESTADO        : Sin sesión activa (Stateless / API)\n\n");
            }

            // 6. CLIENTE, NAVEGADOR Y DISPOSITIVO
            sb.append("[CLIENTE & DISPOSITIVO]\n");
            sb.append("USER_AGENT    : ").append(request.getHeader("User-Agent") != null ? request.getHeader("User-Agent") : "N/A").append("\n");
            sb.append("SISTEMA_NAV   : ").append(extraerInfoCliente(request.getHeader("User-Agent"))).append("\n");
            sb.append("IDIOMA        : ").append(request.getHeader("Accept-Language") != null ? request.getHeader("Accept-Language") : "N/A").append("\n");
            sb.append("REFERER       : ").append(request.getHeader("Referer") != null ? request.getHeader("Referer") : "Directo").append("\n");
            sb.append("SEC_CH_UA_PLAT: ").append(request.getHeader("Sec-Ch-Ua-Platform") != null ? request.getHeader("Sec-Ch-Ua-Platform") : "N/A").append("\n\n");

            // 7. TRAZA DE RED Y PROXIES (PRODUCCIÓN CLOUD)
            sb.append("[TRAZA DE RED & PROXIES]\n");
            sb.append("X_FORWARD_FOR : ").append(request.getHeader("X-Forwarded-For") != null ? request.getHeader("X-Forwarded-For") : "N/A").append("\n");
            sb.append("X_REAL_IP     : ").append(request.getHeader("X-Real-IP") != null ? request.getHeader("X-Real-IP") : "N/A").append("\n");
            sb.append("CF_CONN_IP    : ").append(request.getHeader("CF-Connecting-IP") != null ? request.getHeader("CF-Connecting-IP") : "N/A").append("\n\n");

            // 8. COOKIES RECIBIDAS
            sb.append("[COOKIES]\n");
            Cookie[] cookies = request.getCookies();
            if (cookies != null && cookies.length > 0) {
                for (Cookie c : cookies) {
                    sb.append("  - ").append(c.getName()).append(" (MaxAge: ").append(c.getMaxAge()).append("s)\n");
                }
                sb.append("\n");
            } else {
                sb.append("  (Sin cookies)\n\n");
            }
        }

        // 9. NODO DE INFRAESTRUCTURA Y SALUD JVM
        sb.append("[INFRAESTRUCTURA & RUNTIME]\n");
        sb.append("HOST_INSTANCIA: ").append(obtenerHostName()).append("\n");
        sb.append("PID_PROCESO   : ").append(ProcessHandle.current().pid()).append("\n");

        Runtime runtime = Runtime.getRuntime();
        long maxMem = runtime.maxMemory() / (1024 * 1024);
        long totalMem = runtime.totalMemory() / (1024 * 1024);
        long freeMem = runtime.freeMemory() / (1024 * 1024);
        long usedMem = totalMem - freeMem;
        long porcentajeUso = (usedMem * 100) / (maxMem > 0 ? maxMem : totalMem);

        sb.append("MEMORIA_USADA : ").append(usedMem).append(" MB / ").append(totalMem).append(" MB (Uso: ").append(porcentajeUso).append("%, Máx: ").append(maxMem).append(" MB)\n");
        sb.append("CPU_CORES     : ").append(runtime.availableProcessors()).append(" Cores asignados\n");
        sb.append("UPTIME_SERVER : ").append(ManagementFactory.getRuntimeMXBean().getUptime() / 1000).append(" segundos\n");

        Thread currentThread = Thread.currentThread();
        sb.append("HILO_EJECUCION: ").append(currentThread.getName()).append(" (ID: ").append(currentThread.getId()).append(")\n");

        // 10. DIAGNÓSTICO DE EXCEPCIÓN
        if (excepcion != null) {
            sb.append("\n[DIAGNOSTICO DE EXCEPCION]\n");
            sb.append("CLASE_ERROR   : ").append(excepcion.getClass().getName()).append("\n");
            sb.append("MENSAJE_ERROR : ").append(excepcion.getMessage()).append("\n");
            sb.append("STACKTRACE    :\n");

            StackTraceElement[] stackTrace = excepcion.getStackTrace();
            int limit = Math.min(stackTrace.length, 15);
            for (int i = 0; i < limit; i++) {
                sb.append("   at ").append(stackTrace[i].toString()).append("\n");
            }
        }

        return sb.toString();
    }

    private boolean esCampoSensible(String campo) {
        if (campo == null) return false;
        String lower = campo.toLowerCase();
        return lower.contains("password") || lower.contains("contrasena") ||
                lower.contains("token") || lower.contains("cvv") ||
                lower.contains("tarjeta") || lower.contains("secret");
    }

    private String determinarCanal(HttpServletRequest request) {
        String channelHeader = request.getHeader("X-SkyWay-Channel");
        if (channelHeader != null && !channelHeader.isBlank()) {
            return channelHeader.toUpperCase();
        }
        String ua = request.getHeader("User-Agent");
        if (ua != null) {
            if (ua.contains("Android") || ua.contains("iPhone")) return "APP_MOVIL";
        }
        return "WEB_PORTAL";
    }

    private String obtenerHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "UNKNOWN_HOST";
        }
    }

    private String obtenerRutPorIdentificador(String identificador) {
        if (identificador == null || identificador.trim().isEmpty()) {
            return null;
        }

        String querySql = "SELECT u.rut FROM usuario u WHERE u.correo_electronico = :val OR u.rut = :val LIMIT 1";

        try {
            Object result = entityManager.createNativeQuery(querySql)
                    .setParameter("val", identificador.trim())
                    .getSingleResult();

            return result != null ? result.toString() : null;
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        if (request == null) return "127.0.0.1";

        String[] cabecerasIp = {
                "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP", "FORWARDED_FOR", "FORWARDED", "CF-Connecting-IP", "X-Real-IP"
        };

        for (String header : cabecerasIp) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.contains(",") ? ip.split(",")[0].trim() : ip.trim();
            }
        }

        return request.getRemoteAddr();
    }

    private String extraerInfoCliente(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) return "Desconocido";

        String browser = "Navegador Desconocido";
        String os = "SO Desconocido";

        if (userAgent.contains("Windows")) os = "Windows";
        else if (userAgent.contains("Mac")) os = userAgent.contains("OS X") ? "MacOS" : "Mac";
        else if (userAgent.contains("X11") || userAgent.contains("Linux")) os = "Linux";
        else if (userAgent.contains("Android")) os = "Android";
        else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) os = "iOS";

        if (userAgent.contains("Edg")) browser = "Edge";
        else if (userAgent.contains("Chrome") && !userAgent.contains("Edg")) browser = "Chrome";
        else if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) browser = "Safari";
        else if (userAgent.contains("Firefox")) browser = "Firefox";

        return os + " / " + browser;
    }
}