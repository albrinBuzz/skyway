package com.SkyWay.config.health;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class DatabasePerformanceTracker {

    @Autowired(required = false)
    private DataSource dataSource;

    private final AtomicLong totalQueriesExecuted = new AtomicLong(0);
    private final AtomicLong totalConnectionFailures = new AtomicLong(0);
    private final AtomicLong totalPingTimeMs = new AtomicLong(0);
    private final AtomicLong maxAcquireTimeMs = new AtomicLong(0);
    private final AtomicBoolean refrescoEnCurso = new AtomicBoolean(false);

    private volatile String cachedProductName;
    private volatile String cachedProductVersion;
    private volatile String cachedDriverName;
    private volatile String cachedDriverVersion;
    private volatile String cachedCatalog;
    private volatile String cachedPostgisVersion;
    private volatile String cachedSharedBuffers;
    private volatile String cachedWorkMem;
    private volatile String cachedServerEncoding;
    private volatile String cachedPgTimezone;
    private volatile String cachedPgEndpointHost;
    private volatile String cachedPgServerAddr;

    private static final long PG_STATS_CACHE_TTL_MS = 15_000L;
    private volatile long lastPgStatsTimestamp = 0L;
    private volatile Map<String, Object> cachedPgStats = Map.of();
    private volatile Map<String, Object> lastCompleteMetrics = Map.of();

    public boolean necesitaRefresco() {
        return lastPgStatsTimestamp == 0L
                || lastCompleteMetrics.isEmpty()
                || (System.currentTimeMillis() - lastPgStatsTimestamp) >= PG_STATS_CACHE_TTL_MS;
    }

    public boolean refrescarDesdeBd(boolean forzarRefresco) {
        if (!refrescoEnCurso.compareAndSet(false, true)) {
            return false;
        }
        try {
            collectMetrics(true, forzarRefresco);
            return true;
        } finally {
            refrescoEnCurso.set(false);
        }
    }

    public Map<String, Object> getCachedOrQuickMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>(lastCompleteMetrics);
        if (dataSource instanceof HikariDataSource hikari) {
            llenarInfoPoolHikari(hikari, metrics);
            metrics.putIfAbsent("pg_endpoint_host", cachedPgEndpointHost != null ? cachedPgEndpointHost : extraerEndpointJdbc(hikari.getJdbcUrl()));
        } else {
            metrics.putIfAbsent("db_status", "NO_HIKARI");
        }
        metrics.putIfAbsent("db_status", lastCompleteMetrics.isEmpty() ? "WARMING_UP" : "UP");
        metrics.putIfAbsent("latency_ping_ms", 0L);
        metrics.putIfAbsent("avg_ping_ms", 0.0);
        metrics.putIfAbsent("acquire_time_ms", 0L);
        metrics.putIfAbsent("max_acquire_time_ms", maxAcquireTimeMs.get());
        metrics.putIfAbsent("db_product_name", cachedProductName != null ? cachedProductName : "PostgreSQL");
        metrics.putIfAbsent("db_product_version", cachedProductVersion != null ? cachedProductVersion : "");
        metrics.putIfAbsent("driver_name", cachedDriverName != null ? cachedDriverName : "PostgreSQL JDBC");
        metrics.putIfAbsent("driver_version", cachedDriverVersion != null ? cachedDriverVersion : "");
        metrics.putIfAbsent("catalog", cachedCatalog != null ? cachedCatalog : "aerolinea_db");
        metrics.putIfAbsent("postgis_version", cachedPostgisVersion != null ? cachedPostgisVersion : "N/A");
        metrics.putIfAbsent("pg_shared_buffers", cachedSharedBuffers != null ? cachedSharedBuffers : "-");
        metrics.putIfAbsent("pg_work_mem", cachedWorkMem != null ? cachedWorkMem : "-");
        metrics.putIfAbsent("pg_encoding", cachedServerEncoding != null ? cachedServerEncoding : "-");
        metrics.putIfAbsent("pg_timezone", cachedPgTimezone != null ? cachedPgTimezone : "-");
        metrics.putIfAbsent("pg_server_addr", cachedPgServerAddr != null ? cachedPgServerAddr : "-");
        metrics.put("total_queries_executed", totalQueriesExecuted.get());
        metrics.put("total_connection_failures", totalConnectionFailures.get());
        return metrics;
    }

    private void llenarInfoPoolHikari(HikariDataSource hikari, Map<String, Object> metrics) {
        var pool = hikari.getHikariPoolMXBean();
        metrics.put("pool_name", hikari.getPoolName());
        metrics.put("max_pool_size", hikari.getMaximumPoolSize());
        metrics.put("min_idle", hikari.getMinimumIdle());
        metrics.put("connection_timeout_ms", hikari.getConnectionTimeout());
        metrics.put("idle_timeout_ms", hikari.getIdleTimeout());

        if (pool != null) {
            metrics.put("active_connections", pool.getActiveConnections());
            metrics.put("idle_connections", pool.getIdleConnections());
            metrics.put("total_connections", pool.getTotalConnections());
            metrics.put("threads_awaiting", pool.getThreadsAwaitingConnection());
        } else {
            metrics.put("active_connections", 0);
            metrics.put("idle_connections", 0);
            metrics.put("total_connections", 0);
            metrics.put("threads_awaiting", 0);
        }
    }

    public Map<String, Object> collectMetrics(boolean incluirEstadisticasPostgres) {
        return collectMetrics(incluirEstadisticasPostgres, false);
    }

    public Map<String, Object> collectMetrics(boolean incluirEstadisticasPostgres, boolean forzarRefresco) {
        Map<String, Object> metrics = new LinkedHashMap<>();

        if (!(dataSource instanceof HikariDataSource hikari)) {
            metrics.put("db_status", "NO_HIKARI");
            return metrics;
        }

        llenarInfoPoolHikari(hikari, metrics);

        long startAcquire = System.nanoTime();
        try (Connection conn = dataSource.getConnection()) {
            long acquireDurationMs = Math.max(0L, (System.nanoTime() - startAcquire) / 1_000_000L);
            actualizarMaximoAcquire(acquireDurationMs);

            if (cachedProductName == null) {
                cargarMetadatos(conn);
            }

            long startQuery = System.nanoTime();
            try (Statement stmt = conn.createStatement()) {
                stmt.setQueryTimeout(2);
                stmt.execute("SELECT 1");
            }
            long queryDurationMs = Math.max(0L, (System.nanoTime() - startQuery) / 1_000_000L);

            long execCount = totalQueriesExecuted.incrementAndGet();
            long sumPing = totalPingTimeMs.addAndGet(queryDurationMs);

            metrics.put("latency_ping_ms", queryDurationMs);
            metrics.put("avg_ping_ms", Math.round(((double) sumPing / execCount) * 10.0) / 10.0);
            metrics.put("acquire_time_ms", acquireDurationMs);
            metrics.put("max_acquire_time_ms", maxAcquireTimeMs.get());
            metrics.put("db_status", "UP");

            metrics.put("db_product_name", cachedProductName);
            metrics.put("db_product_version", cachedProductVersion);
            metrics.put("driver_name", cachedDriverName);
            metrics.put("driver_version", cachedDriverVersion);
            metrics.put("catalog", cachedCatalog);
            metrics.put("postgis_version", cachedPostgisVersion != null ? cachedPostgisVersion : "N/A");
            metrics.put("pg_shared_buffers", cachedSharedBuffers != null ? cachedSharedBuffers : "-");
            metrics.put("pg_work_mem", cachedWorkMem != null ? cachedWorkMem : "-");
            metrics.put("pg_encoding", cachedServerEncoding != null ? cachedServerEncoding : "-");
            metrics.put("pg_timezone", cachedPgTimezone != null ? cachedPgTimezone : "-");
            metrics.put("pg_endpoint_host", cachedPgEndpointHost != null ? cachedPgEndpointHost : extraerEndpointJdbc(hikari.getJdbcUrl()));
            metrics.put("pg_server_addr", cachedPgServerAddr != null ? cachedPgServerAddr : "-");

            if (incluirEstadisticasPostgres) {
                long ahora = System.currentTimeMillis();
                if (forzarRefresco || cachedPgStats.isEmpty() || (ahora - lastPgStatsTimestamp) >= PG_STATS_CACHE_TTL_MS) {
                    Map<String, Object> pgStats = new LinkedHashMap<>();
                    consultarEstadisticasPostgres(conn, pgStats);
                    cachedPgStats = pgStats;
                    lastPgStatsTimestamp = ahora;
                }
                metrics.putAll(cachedPgStats);
            }
        } catch (Exception e) {
            totalConnectionFailures.incrementAndGet();
            metrics.put("latency_ping_ms", -1);
            metrics.put("avg_ping_ms", 0.0);
            metrics.put("acquire_time_ms", -1);
            metrics.put("max_acquire_time_ms", maxAcquireTimeMs.get());
            metrics.put("db_status", "DOWN_OR_TIMEOUT");
            metrics.put("last_error", e.getMessage());
            metrics.put("db_product_name", cachedProductName != null ? cachedProductName : "PostgreSQL");
            metrics.put("db_product_version", cachedProductVersion != null ? cachedProductVersion : "");
            metrics.put("driver_name", cachedDriverName != null ? cachedDriverName : "PostgreSQL JDBC");
            metrics.put("driver_version", cachedDriverVersion != null ? cachedDriverVersion : "");
            metrics.put("catalog", cachedCatalog != null ? cachedCatalog : "aerolinea_db");
            metrics.put("postgis_version", cachedPostgisVersion != null ? cachedPostgisVersion : "N/A");
            metrics.put("pg_shared_buffers", cachedSharedBuffers != null ? cachedSharedBuffers : "-");
            metrics.put("pg_work_mem", cachedWorkMem != null ? cachedWorkMem : "-");
            metrics.put("pg_encoding", cachedServerEncoding != null ? cachedServerEncoding : "-");
            metrics.put("pg_timezone", cachedPgTimezone != null ? cachedPgTimezone : "-");
            metrics.put("pg_endpoint_host", cachedPgEndpointHost != null ? cachedPgEndpointHost : extraerEndpointJdbc(hikari.getJdbcUrl()));
            metrics.put("pg_server_addr", cachedPgServerAddr != null ? cachedPgServerAddr : "-");
        }

        metrics.put("total_queries_executed", totalQueriesExecuted.get());
        metrics.put("total_connection_failures", totalConnectionFailures.get());
        lastCompleteMetrics = new LinkedHashMap<>(metrics);
        return metrics;
    }

    public Map<String, Object> collectMetrics() {
        return collectMetrics(true);
    }

    private void cargarMetadatos(Connection conn) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            cachedProductName = meta.getDatabaseProductName();
            cachedProductVersion = meta.getDatabaseProductVersion();
            cachedDriverName = meta.getDriverName();
            cachedDriverVersion = meta.getDriverVersion();
            cachedCatalog = conn.getCatalog();
        } catch (Exception ignored) {
            cachedProductName = "PostgreSQL";
            cachedProductVersion = "";
            cachedDriverName = "JDBC";
            cachedDriverVersion = "";
            cachedCatalog = "aerolinea_db";
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT PostGIS_Lib_Version()")) {
            if (rs.next()) {
                cachedPostgisVersion = rs.getString(1);
            }
        } catch (Exception ignored) {
            cachedPostgisVersion = "N/A";
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT current_setting('shared_buffers', true), current_setting('work_mem', true), "
                             + "current_setting('server_encoding', true), current_setting('TimeZone', true), "
                             + "COALESCE(inet_server_addr()::text, 'local') || ':' || COALESCE(inet_server_port()::text, '5432')")) {
            if (rs.next()) {
                cachedSharedBuffers = rs.getString(1);
                cachedWorkMem = rs.getString(2);
                cachedServerEncoding = rs.getString(3);
                cachedPgTimezone = rs.getString(4);
                cachedPgServerAddr = rs.getString(5);
            }
        } catch (Exception ignored) {
        }

        if (dataSource instanceof HikariDataSource hikari) {
            cachedPgEndpointHost = extraerEndpointJdbc(hikari.getJdbcUrl());
        }
    }

    private String extraerEndpointJdbc(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            return "-";
        }
        String limpia = jdbcUrl.replace("jdbc:postgresql://", "");
        int slash = limpia.indexOf('/');
        int query = limpia.indexOf('?');
        int corte = slash > 0 ? slash : (query > 0 ? query : limpia.length());
        return limpia.substring(0, corte);
    }

    private void consultarEstadisticasPostgres(Connection conn, Map<String, Object> metrics) {
        String sqlGeneral = """
                SELECT
                    ROUND((pg_database_size(current_database()) / (1024.0 * 1024.0))::numeric, 1) AS db_size_mb,
                    TO_CHAR(pg_postmaster_start_time() AT TIME ZONE COALESCE(current_setting('TimeZone', true), 'UTC'), 'DD/MM/YYYY HH24:MI:SS') AS pg_start_fmt,
                    COALESCE(EXTRACT(EPOCH FROM (NOW() - pg_postmaster_start_time()))::bigint, 0) AS pg_uptime_sec,
                    (SELECT setting::int FROM pg_settings WHERE name = 'max_connections') AS max_conns,
                    (SELECT COUNT(*)::int FROM pg_stat_activity WHERE datname = current_database()) AS total_backends,
                    (SELECT COUNT(*)::int FROM pg_stat_activity WHERE datname = current_database() AND state = 'active') AS active_backends,
                    (SELECT COUNT(*)::int FROM pg_stat_activity WHERE datname = current_database() AND state = 'idle in transaction') AS idle_tx_backends,
                    (SELECT COUNT(*)::int FROM pg_locks WHERE granted) AS granted_locks,
                    (SELECT COUNT(*)::int FROM pg_locks WHERE NOT granted) AS waiting_locks,
                    COALESCE((
                        SELECT ROUND((blks_hit * 100.0 / NULLIF(blks_hit + blks_read, 0))::numeric, 2)
                        FROM pg_stat_database WHERE datname = current_database()
                    ), 100.0) AS cache_hit_pct,
                    COALESCE((SELECT xact_commit FROM pg_stat_database WHERE datname = current_database()), 0) AS xact_commit,
                    COALESCE((SELECT xact_rollback FROM pg_stat_database WHERE datname = current_database()), 0) AS xact_rollback,
                    COALESCE((SELECT tup_returned FROM pg_stat_database WHERE datname = current_database()), 0) AS tup_returned,
                    COALESCE((SELECT tup_fetched FROM pg_stat_database WHERE datname = current_database()), 0) AS tup_fetched,
                    COALESCE((SELECT tup_inserted FROM pg_stat_database WHERE datname = current_database()), 0) AS tup_inserted,
                    COALESCE((SELECT tup_updated FROM pg_stat_database WHERE datname = current_database()), 0) AS tup_updated,
                    COALESCE((SELECT tup_deleted FROM pg_stat_database WHERE datname = current_database()), 0) AS tup_deleted,
                    COALESCE((SELECT deadlocks FROM pg_stat_database WHERE datname = current_database()), 0) AS pg_deadlocks,
                    COALESCE((SELECT temp_files FROM pg_stat_database WHERE datname = current_database()), 0) AS temp_files,
                    COALESCE((SELECT ROUND((temp_bytes / (1024.0 * 1024.0))::numeric, 2) FROM pg_stat_database WHERE datname = current_database()), 0) AS temp_mb,
                    COALESCE((
                        SELECT ROUND((SUM(idx_scan) * 100.0 / NULLIF(SUM(idx_scan + seq_scan), 0))::numeric, 1)
                        FROM pg_stat_user_tables
                    ), 100.0) AS idx_scan_pct,
                    COALESCE((SELECT SUM(n_dead_tup)::bigint FROM pg_stat_user_tables), 0) AS total_dead_tuples
                """;

        try (Statement stmt = conn.createStatement()) {
            stmt.setQueryTimeout(2);
            try (ResultSet rs = stmt.executeQuery(sqlGeneral)) {
                if (rs.next()) {
                    metrics.put("pg_database_size_mb", rs.getDouble("db_size_mb"));
                    metrics.put("pg_start_time", rs.getString("pg_start_fmt"));
                    metrics.put("pg_uptime_sec", rs.getLong("pg_uptime_sec"));
                    metrics.put("pg_max_connections", rs.getInt("max_conns"));
                    metrics.put("pg_total_backends", rs.getInt("total_backends"));
                    metrics.put("pg_active_backends", rs.getInt("active_backends"));
                    metrics.put("pg_idle_in_tx", rs.getInt("idle_tx_backends"));
                    metrics.put("pg_granted_locks", rs.getInt("granted_locks"));
                    metrics.put("pg_waiting_locks", rs.getInt("waiting_locks"));
                    metrics.put("pg_cache_hit_ratio_pct", rs.getDouble("cache_hit_pct"));
                    metrics.put("pg_xact_commit", rs.getLong("xact_commit"));
                    metrics.put("pg_xact_rollback", rs.getLong("xact_rollback"));
                    metrics.put("pg_tup_returned", rs.getLong("tup_returned"));
                    metrics.put("pg_tup_fetched", rs.getLong("tup_fetched"));
                    metrics.put("pg_tup_inserted", rs.getLong("tup_inserted"));
                    metrics.put("pg_tup_updated", rs.getLong("tup_updated"));
                    metrics.put("pg_tup_deleted", rs.getLong("tup_deleted"));
                    metrics.put("pg_deadlocks", rs.getLong("pg_deadlocks"));
                    metrics.put("pg_temp_files", rs.getLong("temp_files"));
                    metrics.put("pg_temp_mb", rs.getDouble("temp_mb"));
                    metrics.put("pg_idx_scan_pct", rs.getDouble("idx_scan_pct"));
                    metrics.put("pg_total_dead_tuples", rs.getLong("total_dead_tuples"));
                }
            }
        } catch (Exception ignored) {
        }

        // Top 8 tablas por tamaño total + tuplas vivas/muertas
        String sqlTopTablas = """
                SELECT
                    c.relname AS tabla,
                    GREATEST(c.reltuples::bigint, 0) AS filas_est,
                    ROUND((pg_total_relation_size(c.oid) / (1024.0 * 1024.0))::numeric, 2) AS size_mb,
                    COALESCE(s.n_dead_tup, 0) AS dead_tup,
                    COALESCE(s.idx_scan, 0) AS idx_scans,
                    COALESCE(s.seq_scan, 0) AS seq_scans
                FROM pg_class c
                JOIN pg_namespace n ON n.oid = c.relnamespace
                LEFT JOIN pg_stat_user_tables s ON s.relid = c.oid
                WHERE n.nspname = 'public' AND c.relkind = 'r'
                ORDER BY pg_total_relation_size(c.oid) DESC
                LIMIT 8
                """;
        List<Map<String, Object>> topTablas = new ArrayList<>();
        Map<String, Long> tableEstimates = new LinkedHashMap<>();
        try (Statement stmt = conn.createStatement()) {
            stmt.setQueryTimeout(1);
            try (ResultSet rs = stmt.executeQuery(sqlTopTablas)) {
                while (rs.next()) {
                    String tabla = rs.getString("tabla");
                    long filas = rs.getLong("filas_est");
                    tableEstimates.put(tabla, filas);

                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("table", tabla);
                    row.put("rows_est", filas);
                    row.put("size_mb", rs.getDouble("size_mb"));
                    row.put("dead_tup", rs.getLong("dead_tup"));
                    row.put("idx_scans", rs.getLong("idx_scans"));
                    row.put("seq_scans", rs.getLong("seq_scans"));
                    topTablas.add(row);
                }
            }
        } catch (Exception ignored) {
        }
        metrics.put("table_estimates", tableEstimates);
        metrics.put("top_tables", topTablas);

        // Sesiones activas actuales en PostgreSQL
        String sqlSesiones = """
                SELECT
                    pid,
                    COALESCE(usename, '-') AS usuario,
                    COALESCE(client_addr::text, 'local') AS client_ip,
                    COALESCE(NULLIF(application_name, ''), '-') AS app_name,
                    COALESCE(state, '-') AS estado,
                    COALESCE(wait_event, '-') AS wait_event,
                    COALESCE(ROUND(EXTRACT(EPOCH FROM (NOW() - query_start)) * 1000)::bigint, 0) AS duracion_ms,
                    LEFT(REGEXP_REPLACE(COALESCE(query, ''), '\\s+', ' ', 'g'), 100) AS query_txt
                FROM pg_stat_activity
                WHERE datname = current_database()
                  AND pid <> pg_backend_pid()
                  AND state IS NOT NULL
                ORDER BY CASE WHEN state = 'active' THEN 0 ELSE 1 END, query_start DESC NULLS LAST
                LIMIT 6
                """;
        List<Map<String, Object>> sesiones = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            stmt.setQueryTimeout(1);
            try (ResultSet rs = stmt.executeQuery(sqlSesiones)) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("pid", rs.getInt("pid"));
                    item.put("user", rs.getString("usuario"));
                    item.put("client_ip", rs.getString("client_ip"));
                    item.put("app_name", rs.getString("app_name"));
                    item.put("state", rs.getString("estado"));
                    item.put("wait_event", rs.getString("wait_event"));
                    item.put("duration_ms", Math.max(0L, rs.getLong("duracion_ms")));
                    item.put("query", rs.getString("query_txt"));
                    sesiones.add(item);
                }
            }
        } catch (Exception ignored) {
        }
        metrics.put("active_sessions_detail", sesiones);
    }

    private void actualizarMaximoAcquire(long candidato) {
        long actual;
        do {
            actual = maxAcquireTimeMs.get();
            if (candidato <= actual) {
                return;
            }
        } while (!maxAcquireTimeMs.compareAndSet(actual, candidato));
    }
}
