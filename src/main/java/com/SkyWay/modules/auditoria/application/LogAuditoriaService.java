package com.SkyWay.modules.auditoria.application;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.Map;

@Service
public class LogAuditoriaService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Sobrecarga estándar para llamadas rápidas desde Controllers o Beans.
     */
    @Transactional
    public void registrarLog(String identificadorUsuario, String accion, String tablaAfectada, Integer idRegistroAfectado, String detalleBase, HttpServletRequest request) {
        registrarLogExhaustivo(identificadorUsuario, accion, "INFO", tablaAfectada, idRegistroAfectado, null, null, detalleBase, null, request);
    }

    /**
     * Sobrecarga para registrar errores con captura de StackTrace.
     */
    @Transactional
    public void registrarLogError(String identificadorUsuario, String accion, String tablaAfectada, Integer idRegistroAfectado, Throwable excepcion, HttpServletRequest request) {
        registrarLogExhaustivo(identificadorUsuario, accion, "ERROR", tablaAfectada, idRegistroAfectado, null, null, excepcion != null ? excepcion.getMessage() : "Error no especificado", excepcion, request);
    }

    /**
     * Método principal que empaqueta la máxima cantidad de información posible.
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

        // Construir reporte exhaustivo de auditoría
        String detalleExtendido = construirReporteExhaustivo(detalleBase, excepcion, request);

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
                .setParameter("detalle", detalleExtendido)
                .setParameter("ip", ip)
                .setParameter("userAgent", userAgent)
                .executeUpdate();
    }

    /**
     * Genera un bloque estructurado en texto con todos los metadatos técnicos imaginables.
     */
    private String construirReporteExhaustivo(String detalleBase, Throwable excepcion, HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        // 1. EVENTO Y NEGOCIO
        sb.append("=================================================================\n");
        sb.append("                      DETALLE DE AUDITORÍA                      \n");
        sb.append("=================================================================\n");
        sb.append("DESCRIPCIÓN    : ").append(detalleBase != null ? detalleBase.trim() : "Sin descripción").append("\n");
        sb.append("TIMESTAMP APP  : ").append(LocalDateTime.now().format(formatter)).append("\n\n");

        if (request != null) {
            // 2. CONTEXTO HTTP Y RED
            sb.append("-----------------------------------------------------------------\n");
            sb.append("1. CONTEXTO DE SOLICITUD HTTP (REQUEST)\n");
            sb.append("-----------------------------------------------------------------\n");
            sb.append("MÉTODO HTTP    : ").append(request.getMethod()).append("\n");
            sb.append("URI SOLICITADA : ").append(request.getRequestURI()).append("\n");
            sb.append("URL COMPLETA   : ").append(request.getRequestURL().toString()).append("\n");
            sb.append("QUERY STRING   : ").append(request.getQueryString() != null ? request.getQueryString() : "N/A").append("\n");
            sb.append("ESQUEMA/PROTOCO: ").append(request.getScheme()).append(" / ").append(request.getProtocol()).append("\n");
            sb.append("PUERTO SERVIDOR: ").append(request.getServerName()).append(":").append(request.getServerPort()).append("\n");
            sb.append("DIRECCIÓN LOCAL: ").append(request.getLocalAddr()).append(":").append(request.getLocalPort()).append("\n");

            // 3. PARÁMETROS RECIBIDOS EN LA PETICIÓN
            sb.append("\n-----------------------------------------------------------------\n");
            sb.append("2. PARÁMETROS DE LA PETICIÓN (HTTP PARAMETERS)\n");
            sb.append("-----------------------------------------------------------------\n");
            Map<String, String[]> paramMap = request.getParameterMap();
            if (paramMap.isEmpty()) {
                sb.append("Sin parámetros adicionales en la solicitud.\n");
            } else {
                for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
                    String paramName = entry.getKey();
                    // Ocultar valores sensibles
                    if ("password".equalsIgnoreCase(paramName) || "contrasena".equalsIgnoreCase(paramName) || "token".equalsIgnoreCase(paramName)) {
                        sb.append("  - ").append(paramName).append(" = [PROTEGIDO]\n");
                    } else {
                        sb.append("  - ").append(paramName).append(" = ").append(String.join(", ", entry.getValue())).append("\n");
                    }
                }
            }

            // 4. INFORMACIÓN COMPLETA DE SESIÓN
            sb.append("\n-----------------------------------------------------------------\n");
            sb.append("3. ESTADO DE LA SESIÓN HTTP\n");
            sb.append("-----------------------------------------------------------------\n");
            HttpSession session = request.getSession(false);
            if (session != null) {
                sb.append("SESSION ID     : ").append(session.getId()).append("\n");
                sb.append("ES NUEVA       : ").append(session.isNew() ? "SÍ" : "NO").append("\n");
                sb.append("CREACIÓN       : ").append(LocalDateTime.ofInstant(Instant.ofEpochMilli(session.getCreationTime()), ZoneId.systemDefault()).format(formatter)).append("\n");
                sb.append("ÚLTIMO ACCESO  : ").append(LocalDateTime.ofInstant(Instant.ofEpochMilli(session.getLastAccessedTime()), ZoneId.systemDefault()).format(formatter)).append("\n");
                sb.append("INACTIVIDAD MAX: ").append(session.getMaxInactiveInterval()).append(" segundos\n");
            } else {
                sb.append("Estado         : Sin Sesión Activa (Stateless / Anon)\n");
            }

            // 5. ENCABEZADOS DE CLIENTE Y NAVEGADOR (HEADERS)
            sb.append("\n-----------------------------------------------------------------\n");
            sb.append("4. NAVEGADOR Y PLATAFORMA CLIENTE\n");
            sb.append("-----------------------------------------------------------------\n");
            sb.append("USER AGENT     : ").append(request.getHeader("User-Agent")).append("\n");
            sb.append("RESUMEN OS/NAV : ").append(extraerInfoCliente(request.getHeader("User-Agent"))).append("\n");
            sb.append("IDIOMA CLIENTE : ").append(request.getHeader("Accept-Language")).append("\n");
            sb.append("REFERER        : ").append(request.getHeader("Referer") != null ? request.getHeader("Referer") : "Directo / Sin Referer").append("\n");
            sb.append("SEC-CH-UA      : ").append(request.getHeader("Sec-Ch-Ua")).append("\n");
            sb.append("SEC-CH-UA-PLAT : ").append(request.getHeader("Sec-Ch-Ua-Platform")).append("\n");
            sb.append("SEC-CH-UA-MOB  : ").append(request.getHeader("Sec-Ch-Ua-Mobile")).append("\n");

            // 6. DETECCION DE PROXIES / RED
            sb.append("\n-----------------------------------------------------------------\n");
            sb.append("5. TRAZA DE RED Y PROXIES\n");
            sb.append("-----------------------------------------------------------------\n");
            sb.append("X-Forwarded-For: ").append(request.getHeader("X-Forwarded-For")).append("\n");
            sb.append("X-Real-IP      : ").append(request.getHeader("X-Real-IP")).append("\n");
            sb.append("CF-Conn-IP     : ").append(request.getHeader("CF-Connecting-IP")).append("\n");

            // 7. COOKIES PRESENTES EN LA SOLICITUD
            sb.append("\n-----------------------------------------------------------------\n");
            sb.append("6. COOKIES RECIBIDAS\n");
            sb.append("-----------------------------------------------------------------\n");
            Cookie[] cookies = request.getCookies();
            if (cookies != null && cookies.length > 0) {
                for (Cookie c : cookies) {
                    sb.append("  - ").append(c.getName()).append(" (Domain: ").append(c.getDomain()).append(", MaxAge: ").append(c.getMaxAge()).append("s)\n");
                }
            } else {
                sb.append("No se recibieron cookies en la petición.\n");
            }
        }

        // 8. MÉTRICAS DE LA MÁQUINA VIRTUAL DE JAVA (JVM)
        sb.append("\n-----------------------------------------------------------------\n");
        sb.append("7. ESTADO Y MÉTRICAS DE SERVIDOR (JVM)\n");
        sb.append("-----------------------------------------------------------------\n");
        Runtime runtime = Runtime.getRuntime();
        long maxMem = runtime.maxMemory() / (1024 * 1024);
        long totalMem = runtime.totalMemory() / (1024 * 1024);
        long freeMem = runtime.freeMemory() / (1024 * 1024);
        long usedMem = totalMem - freeMem;

        sb.append("MEMORIA USADA  : ").append(usedMem).append(" MB / ").append(totalMem).append(" MB (Max: ").append(maxMem).append(" MB)\n");
        sb.append("PROCESADORES   : ").append(runtime.availableProcessors()).append(" Cores asignados\n");

        // 9. TRAZABILIDAD DEL HILO DE EJECUCIÓN (THREAD)
        Thread currentThread = Thread.currentThread();
        sb.append("HILO (THREAD)  : ").append(currentThread.getName()).append(" (ID: ").append(currentThread.getId()).append(", Prioridad: ").append(currentThread.getPriority()).append(")\n");

        // 10. CAPTURA DE EXCEPCIÓN Y STACK TRACE (SI EXISTE)
        if (excepcion != null) {
            sb.append("\n=================================================================\n");
            sb.append("8. EXCEPCIÓN Y TRAZA DE ERROR (STACKTRACE)\n");
            sb.append("=================================================================\n");
            sb.append("CLASE EXCEPCIÓN: ").append(excepcion.getClass().getName()).append("\n");
            sb.append("MENSAJE ERROR  : ").append(excepcion.getMessage()).append("\n");
            sb.append("TRAZA PRINCIPAL:\n");

            StackTraceElement[] stackTrace = excepcion.getStackTrace();
            int limit = Math.min(stackTrace.length, 12); // Guardar los 12 primeros frames relevantes
            for (int i = 0; i < limit; i++) {
                sb.append("   at ").append(stackTrace[i].toString()).append("\n");
            }
        }

        return sb.toString();
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
        else if (userAgent.contains("Mac")) os = "MacOS";
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