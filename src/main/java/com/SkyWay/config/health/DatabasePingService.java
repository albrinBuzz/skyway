package com.SkyWay.config.health;

import com.SkyWay.util.Logger;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
public class DatabasePingService {

    @Autowired
    private DataSource dataSource;

    @EventListener(ApplicationReadyEvent.class)
    public void runDiagnosticsOnStartup() {
        printDetailedConnectionInfo();
        runLatencyTests();
    }

    /**
     * Imprime el informe estructurado con todos los parámetros de conexión,
     * versión de PostgreSQL, extensiones y telemetría de HikariCP.
     */
    public void printDetailedConnectionInfo() {
        Logger.logInfo("╔══════════════════════════════════════════════════════════════════════════════╗");
        Logger.logInfo("║                DIAGNÓSTICO INTEGRAL DE BASE DE DATOS SKYWAY                 ║");
        Logger.logInfo("╚══════════════════════════════════════════════════════════════════════════════╝");

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // -------------------------------------------------------------
            // BLOQUE 1: SERVIDOR POSTGRESQL (VERSIÓN EXACTA Y ENTORNO)
            // -------------------------------------------------------------
            Logger.logInfo("┌── [1. SERVIDOR & VERSIÓN POSTGRESQL] ────────────────────────────────────────");

            // Consulta directa de versión a PostgreSQL
            try (ResultSet rs = stmt.executeQuery("SELECT version(), current_setting('server_version') AS ver_corta")) {
                if (rs.next()) {
                    Logger.logInfo(String.format("│ Versión PostgreSQL : %s", rs.getString("ver_corta")));
                    Logger.logInfo(String.format("│ Build Completo     : %s", rs.getString(1)));
                }
            } catch (Exception e) {
                Logger.logWarn("│ No se pudo obtener la versión de PostgreSQL: " + e.getMessage());
            }

            // Datos operativos de la instancia
            try (ResultSet rs = stmt.executeQuery("""
                SELECT\s
                    current_database() AS db_nombre,
                    current_schema() AS esquema,
                    current_user AS usuario,
                    pg_backend_pid() AS pid,
                    current_setting('server_encoding') AS s_enc,
                    current_setting('client_encoding') AS c_enc,
                    current_setting('TimeZone') AS tz,
                    current_setting('ssl') AS ssl,
                    pg_size_pretty(pg_database_size(current_database())) AS tamano
           \s""")) {
                if (rs.next()) {
                    Logger.logInfo(String.format("│ Base de Datos      : %s (Espacio en disco: %s)", rs.getString("db_nombre"), rs.getString("tamano")));
                    Logger.logInfo(String.format("│ Esquema Activo     : %s", rs.getString("esquema")));
                    Logger.logInfo(String.format("│ Usuario Conectado  : %s", rs.getString("usuario")));
                    Logger.logInfo(String.format("│ Proceso Backend PID: %s", rs.getString("pid")));
                    Logger.logInfo(String.format("│ Encodings          : Servidor=[%s] | Cliente JDBC=[%s]", rs.getString("s_enc"), rs.getString("c_enc")));
                    Logger.logInfo(String.format("│ Zona Horaria Motor : %s", rs.getString("tz")));
                    Logger.logInfo(String.format("│ Cifrado SSL        : %s", "on".equalsIgnoreCase(rs.getString("ssl")) ? "HABILITADO (TLS)" : "INACTIVO (Texto plano)"));
                }
            }
            Logger.logInfo("└─────────────────────────────────────────────────────────────────────────────");

            // -------------------------------------------------------------
            // BLOQUE 2: CLIENTE JDBC & CONEXIÓN
            // -------------------------------------------------------------
            Logger.logInfo("┌── [2. CONEXIÓN & DRIVER JDBC] ──────────────────────────────────────────────");
            DatabaseMetaData meta = conn.getMetaData();
            Logger.logInfo(String.format("│ URL de Conexión    : %s", meta.getURL()));
            Logger.logInfo(String.format("│ Driver JDBC        : %s v%s", meta.getDriverName(), meta.getDriverVersion()));
            Logger.logInfo(String.format("│ Especificación JDBC: v%d.%d", meta.getJDBCMajorVersion(), meta.getJDBCMinorVersion()));
            Logger.logInfo(String.format("│ Nivel Aislamiento  : %s", getIsolationLevelName(conn.getTransactionIsolation())));
            Logger.logInfo(String.format("│ Transaccionalidad  : AutoCommit=%b | ReadOnly=%b", conn.getAutoCommit(), conn.isReadOnly()));
            Logger.logInfo("└─────────────────────────────────────────────────────────────────────────────");

            // -------------------------------------------------------------
            // BLOQUE 3: EXTENSIONES ESPACIALES (POSTGIS)
            // -------------------------------------------------------------
            Logger.logInfo("┌── [3. EXTENSIONES (POSTGIS)] ───────────────────────────────────────────────");
            try (ResultSet rs = stmt.executeQuery("SELECT postgis_full_version()")) {
                if (rs.next()) {
                    Logger.logInfo(String.format("│ PostGIS Info       : %s", rs.getString(1)));
                }
            } catch (Exception e) {
                Logger.logWarn("│ PostGIS            : NO DETECTADO O DESHABILITADO EN ESTE ESQUEMA");
            }
            Logger.logInfo("└─────────────────────────────────────────────────────────────────────────────");

            // -------------------------------------------------------------
            // BLOQUE 4: POOL DE CONEXIONES HIKARICP
            // -------------------------------------------------------------
            if (dataSource instanceof HikariDataSource hikari) {
                Logger.logInfo("┌── [4. TELEMETRÍA POOL HIKARICP] ────────────────────────────────────────────");
                Logger.logInfo(String.format("│ Nombre del Pool    : %s", hikari.getPoolName()));
                Logger.logInfo(String.format("│ Límites de Pool    : Min Idle=%d | Max Size=%d", hikari.getMinimumIdle(), hikari.getMaximumPoolSize()));
                Logger.logInfo(String.format("│ Timeouts           : Conexión=%d ms | Validación=%d ms | Vida Máx=%d ms",
                        hikari.getConnectionTimeout(), hikari.getValidationTimeout(), hikari.getMaxLifetime()));

                HikariPoolMXBean mxBean = hikari.getHikariPoolMXBean();
                if (mxBean != null) {
                    Logger.logInfo(String.format("│ Conexiones Físicas : Total=%d (Activas: %d | Inactivas: %d)",
                            mxBean.getTotalConnections(), mxBean.getActiveConnections(), mxBean.getIdleConnections()));
                    Logger.logInfo(String.format("│ Cola de Espera     : %d hilos esperando conexión", mxBean.getThreadsAwaitingConnection()));
                }
                Logger.logInfo("└─────────────────────────────────────────────────────────────────────────────");
            }

        } catch (Exception e) {
            Logger.logError("Error general durante la inspección de BD: " + e.getMessage());
        }
    }

    /**
     * Mide la latencia de red TCP pura y la latencia de ejecución SQL.
     */
    public void runLatencyTests() {
        Logger.logInfo("┌── [5. RENDIMIENTO & LATENCIAS] ─────────────────────────────────────────────");
        checkTcpNetworkLatencyMs(3000);
        checkDatabaseQueryLatencyMs();
        Logger.logInfo("└─────────────────────────────────────────────────────────────────────────────");
        Logger.logInfo("══════════════════════════════════════════════════════════════════════════════");
    }

    public long checkDatabaseQueryLatencyMs() {
        long start = System.currentTimeMillis();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            long duration = System.currentTimeMillis() - start;
            Logger.logInfo(String.format("│ Latencia Consulta (SELECT 1) : %d ms", duration));
            return duration;
        } catch (Exception e) {
            Logger.logError("│ Error en consulta SQL: " + e.getMessage());
            return -1;
        }
    }

    public long checkTcpNetworkLatencyMs(int timeoutMs) {
        try (Connection conn = dataSource.getConnection()) {
            String jdbcUrl = conn.getMetaData().getURL();
            String cleanUri = jdbcUrl.replaceFirst("^jdbc:", "");
            if (cleanUri.contains("?")) {
                cleanUri = cleanUri.substring(0, cleanUri.indexOf('?'));
            }

            URI uri = URI.create(cleanUri);
            String host = uri.getHost();
            int port = uri.getPort() != -1 ? uri.getPort() : 5432;

            long start = System.currentTimeMillis();
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), timeoutMs);
                long duration = System.currentTimeMillis() - start;
                Logger.logInfo(String.format("│ Latencia Socket TCP [%s:%d] : %d ms", host, port, duration));
                return duration;
            } catch (IOException e) {
                Logger.logError(String.format("│ Fallo Socket TCP [%s:%d]: %s", host, port, e.getMessage()));
                return -1;
            }
        } catch (Exception e) {
            Logger.logError("│ Error resolviendo host de DataSource: " + e.getMessage());
            return -1;
        }
    }

    private String getIsolationLevelName(int level) {
        return switch (level) {
            case Connection.TRANSACTION_NONE -> "NONE";
            case Connection.TRANSACTION_READ_UNCOMMITTED -> "READ_UNCOMMITTED";
            case Connection.TRANSACTION_READ_COMMITTED -> "READ_COMMITTED";
            case Connection.TRANSACTION_REPEATABLE_READ -> "REPEATABLE_READ";
            case Connection.TRANSACTION_SERIALIZABLE -> "SERIALIZABLE";
            default -> "NIVEL_DESCONOCIDO (" + level + ")";
        };
    }
}