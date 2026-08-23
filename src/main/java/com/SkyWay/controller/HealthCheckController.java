package com.SkyWay.controller;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.JavaVersion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.io.File;
import java.lang.management.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    @Autowired(required = false)
    private DataSource dataSource;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    private static final int DB_TIMEOUT_SECONDS = 3;
    private static final long MEGABYTE = 1024 * 1024;

    @GetMapping("/liveness")
    public ResponseEntity<Map<String, String>> liveness() {
        return ResponseEntity.ok(Map.of("status", "UP", "probe", "liveness"));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> components = new LinkedHashMap<>();

        // 1. Audit del Entorno y Sistema
        checkSystemEnvironment(response);

        // 2. Evaluaciones de Salud
        boolean dbOk = checkDatabase(components);
        boolean memoryOk = checkJvmMemory(components);
        boolean poolOk = checkHikariPool(components);

        // 3. Métricas extras de diagnóstico
        checkGarbageCollector(components);
        checkThreads(components);
        checkDiskSpace(components);

        boolean systemHealthy = dbOk && memoryOk && poolOk;

        response.put("status", systemHealthy ? "UP" : "DOWN");
        response.put("components", components);

        HttpStatus status = systemHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return new ResponseEntity<>(response, status);
    }

    private void checkSystemEnvironment(Map<String, Object> response) {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        long uptimeMs = runtimeBean.getUptime();

        Map<String, Object> envDetails = new LinkedHashMap<>();
        envDetails.put("active_profile", activeProfile);
        envDetails.put("java_version", JavaVersion.getJavaVersion().toString() + " (" + System.getProperty("java.vendor") + ")");
        envDetails.put("os_architecture", System.getProperty("os.name") + " " + System.getProperty("os.arch") + " (v" + System.getProperty("os.version") + ")");
        envDetails.put("available_processors", Runtime.getRuntime().availableProcessors());
        envDetails.put("start_time", DateTimeFormatter.ISO_OFFSET_DATE_TIME
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(runtimeBean.getStartTime())));
        envDetails.put("uptime_formatted", formatUptime(uptimeMs));
        envDetails.put("uptime_ms", uptimeMs);

        response.put("application", envDetails);
    }

    private boolean checkDatabase(Map<String, Object> components) {
        Map<String, Object> dbDetails = new LinkedHashMap<>();
        if (dataSource == null) {
            dbDetails.put("status", "DOWN");
            dbDetails.put("error", "DataSource no configurado");
            components.put("database", dbDetails);
            return false;
        }

        long start = System.currentTimeMillis();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.setQueryTimeout(DB_TIMEOUT_SECONDS);
            stmt.execute("SELECT 1");
            long latency = System.currentTimeMillis() - start;

            DatabaseMetaData metaData = conn.getMetaData();
            dbDetails.put("status", "UP");
            dbDetails.put("latency_ms", latency);
            dbDetails.put("database_product", metaData.getDatabaseProductName());
            dbDetails.put("database_version", metaData.getDatabaseProductVersion());
            dbDetails.put("driver_name", metaData.getDriverName());
            dbDetails.put("driver_version", metaData.getDriverVersion());
            dbDetails.put("read_only", conn.isReadOnly());

            components.put("database", dbDetails);
            return true;

        } catch (Exception e) {
            dbDetails.put("status", "DOWN");
            dbDetails.put("error_type", e.getClass().getName());
            dbDetails.put("error_message", e.getMessage());
            components.put("database", dbDetails);
            return false;
        }
    }

    private boolean checkJvmMemory(Map<String, Object> components) {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();

        Map<String, Object> memDetails = new LinkedHashMap<>();

        // HEAP
        long usedHeap = heapUsage.getUsed() / MEGABYTE;
        long maxHeap = heapUsage.getMax() / MEGABYTE;
        long committedHeap = heapUsage.getCommitted() / MEGABYTE;
        double heapRatio = (double) heapUsage.getUsed() / heapUsage.getMax();

        Map<String, Object> heapMap = new LinkedHashMap<>();
        heapMap.put("used_mb", usedHeap);
        heapMap.put("committed_mb", committedHeap);
        heapMap.put("max_mb", maxHeap);
        heapMap.put("usage_pct", String.format("%.2f%%", heapRatio * 100));

        // METASPACE & NON-HEAP
        Map<String, Object> nonHeapMap = new LinkedHashMap<>();
        nonHeapMap.put("used_mb", nonHeapUsage.getUsed() / MEGABYTE);
        nonHeapMap.put("committed_mb", nonHeapUsage.getCommitted() / MEGABYTE);

        // Intento de obtener detalle de Metaspace
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if ("Metaspace".equalsIgnoreCase(pool.getName())) {
                MemoryUsage metaUsage = pool.getUsage();
                Map<String, Object> metaMap = new LinkedHashMap<>();
                metaMap.put("used_mb", metaUsage.getUsed() / MEGABYTE);
                metaMap.put("committed_mb", metaUsage.getCommitted() / MEGABYTE);
                metaMap.put("max_mb", metaUsage.getMax() > 0 ? metaUsage.getMax() / MEGABYTE : "UNLIMITED");
                nonHeapMap.put("metaspace_detail", metaMap);
            }
        }

        boolean memHealthy = heapRatio < 0.92;
        memDetails.put("status", memHealthy ? "OK" : "CRITICAL");
        memDetails.put("heap", heapMap);
        memDetails.put("non_heap", nonHeapMap);

        components.put("jvm_memory", memDetails);
        return memHealthy;
    }

    private boolean checkHikariPool(Map<String, Object> components) {
        if (dataSource instanceof HikariDataSource hikari) {
            Map<String, Object> poolDetails = new LinkedHashMap<>();
            var poolBean = hikari.getHikariPoolMXBean();

            int active = poolBean.getActiveConnections();
            int idle = poolBean.getIdleConnections();
            int total = poolBean.getTotalConnections();
            int waiting = poolBean.getThreadsAwaitingConnection();

            poolDetails.put("pool_name", hikari.getPoolName());
            poolDetails.put("active_connections", active);
            poolDetails.put("idle_connections", idle);
            poolDetails.put("total_connections", total);
            poolDetails.put("threads_awaiting", waiting);
            poolDetails.put("maximum_pool_size", hikari.getMaximumPoolSize());
            poolDetails.put("connection_timeout_ms", hikari.getConnectionTimeout());

            boolean poolHealthy = waiting <= 5;
            poolDetails.put("status", poolHealthy ? "OK" : "SATURATED");
            components.put("hikari_pool", poolDetails);

            return poolHealthy;
        }
        return true;
    }

    private void checkGarbageCollector(Map<String, Object> components) {
        List<Map<String, Object>> gcList = new ArrayList<>();
        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            Map<String, Object> gcMap = new LinkedHashMap<>();
            gcMap.put("name", gcBean.getName());
            gcMap.put("collection_count", gcBean.getCollectionCount());
            gcMap.put("collection_time_ms", gcBean.getCollectionTime());
            gcList.add(gcMap);
        }
        components.put("garbage_collector", gcList);
    }

    private void checkThreads(Map<String, Object> components) {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        Map<String, Object> threadDetails = new LinkedHashMap<>();
        threadDetails.put("live_threads", threadBean.getThreadCount());
        threadDetails.put("peak_threads", threadBean.getPeakThreadCount());
        threadDetails.put("daemon_threads", threadBean.getDaemonThreadCount());
        threadDetails.put("total_started_threads", threadBean.getTotalStartedThreadCount());

        components.put("threads", threadDetails);
    }

    private void checkDiskSpace(Map<String, Object> components) {
        File root = new File(".");
        Map<String, Object> diskDetails = new LinkedHashMap<>();
        long freeBytes = root.getFreeSpace();
        long totalBytes = root.getTotalSpace();

        diskDetails.put("free_mb", freeBytes / MEGABYTE);
        diskDetails.put("total_mb", totalBytes / MEGABYTE);
        diskDetails.put("usable_pct", String.format("%.2f%%", ((double) freeBytes / totalBytes) * 100));
        diskDetails.put("status", freeBytes > (50 * MEGABYTE) ? "OK" : "LOW_SPACE");

        components.put("disk_space", diskDetails);
    }

    private String formatUptime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        return String.format("%d d, %d h, %d m, %d s",
                days, hours % 24, minutes % 60, seconds % 60);
    }
}