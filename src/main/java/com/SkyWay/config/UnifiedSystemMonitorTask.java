package com.SkyWay.config;

import com.SkyWay.config.health.HttpSessionMetricsListener;
import com.SkyWay.config.health.JSFMetricsTracker;
import com.SkyWay.util.Logger;
import com.sun.management.OperatingSystemMXBean;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.JavaVersion;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.lang.management.*;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class UnifiedSystemMonitorTask {

    private static final long MEGABYTE = 1024L * 1024L;
    private static final long GIGABYTE = 1024L * 1024L * 1024L;

    // ⚙️ OPTIMIZACIÓN CLOUD: Ajuste de intervalos para conservar CPU/RAM
    private static final int MONITOR_INTERVAL_SECONDS = 5; // Transmisión WebSocket cada 5s
    private static final int DB_PING_EVERY_N_TICKS = 6;     // Pings a DB cada 30 segundos (5s * 6)

    private final OperatingSystemMXBean osBean;
    private final ThreadMXBean threadBean;
    private final List<GarbageCollectorMXBean> gcBeans;
    private final ScheduledExecutorService scheduler;
    private final File rootDisk = new File("/");

    private MemoryPoolMXBean metaspacePool;
    private MemoryPoolMXBean codeCachePool;

    private int tickCounter = 0;
    private long lastKnownDbLatency = -1;

    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired(required = false)
    private DataSource dataSource;

    public UnifiedSystemMonitorTask() {
        this.osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        this.threadBean = ManagementFactory.getThreadMXBean();
        this.gcBeans = ManagementFactory.getGarbageCollectorMXBeans();

        // Guardar referencias a los pools de memoria una sola vez
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if ("Metaspace".equalsIgnoreCase(pool.getName())) {
                this.metaspacePool = pool;
            } else if (pool.getName().contains("CodeCache") || pool.getName().contains("Code Cache")) {
                this.codeCachePool = pool;
            }
        }

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "unified-system-monitor-thread");
            thread.setDaemon(true);
            return thread;
        });
    }

    @PostConstruct
    public void startMonitor() {
        Logger.logInfo("📊 Monitor Unificado Cloud iniciado (Intervalo: " + MONITOR_INTERVAL_SECONDS + "s)");

        this.scheduler.scheduleAtFixedRate(
                this::collectAndBroadcastMetrics,
                2,
                MONITOR_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );
    }

    //@Scheduled(fixedDelay = 5000)
    private void collectAndBroadcastMetrics() {
        try {
            Runtime runtime = Runtime.getRuntime();

            // 1. Memoria Heap JVM
            long maxHeapMb = runtime.maxMemory() / MEGABYTE;
            long totalHeapMb = runtime.totalMemory() / MEGABYTE;
            long usedHeapMb = (totalHeapMb - (runtime.freeMemory() / MEGABYTE));
            double ramUsageRatio = maxHeapMb > 0 ? ((double) usedHeapMb / maxHeapMb) * 100.0 : 0.0;

            // 2. Non-Heap Pools
            long metaspaceUsedMb = metaspacePool != null ? metaspacePool.getUsage().getUsed() / MEGABYTE : 0;
            long codeCacheUsedMb = codeCachePool != null ? codeCachePool.getUsage().getUsed() / MEGABYTE : 0;

            // 3. CPU
            double rawAppCpu = osBean.getProcessCpuLoad() * 100.0;
            double rawSysCpu = osBean.getCpuLoad() * 100.0;
            double appCpu = Double.isNaN(rawAppCpu) || rawAppCpu < 0 ? 0.0 : rawAppCpu;
            double systemCpu = Double.isNaN(rawSysCpu) || rawSysCpu < 0 ? 0.0 : rawSysCpu;

            // 4. HikariCP Pool
            int activeConns = 0, totalConns = 0, idleConns = 0, threadsAwaiting = 0;
            if (dataSource instanceof HikariDataSource hikari && hikari.getHikariPoolMXBean() != null) {
                var pool = hikari.getHikariPoolMXBean();
                activeConns = pool.getActiveConnections();
                totalConns = pool.getTotalConnections();
                idleConns = pool.getIdleConnections();
                threadsAwaiting = pool.getThreadsAwaitingConnection();
            }

            // 5. GC Metrics
            long gcCount = 0, gcTimeMs = 0;
            for (GarbageCollectorMXBean gc : gcBeans) {
                long count = gc.getCollectionCount();
                long time = gc.getCollectionTime();
                if (count > 0) gcCount += count;
                if (time > 0) gcTimeMs += time;
            }

            // 6. DB Latency (Muestreo espaciado)
            tickCounter++;
            if (tickCounter % DB_PING_EVERY_N_TICKS == 0) {
                lastKnownDbLatency = measureDbLatency();
                tickCounter = 0;
            }

            // HashMap con capacidad inicial óptima para evitar re-hashing
            Map<String, Object> payload = new HashMap<>(32);
            payload.put("timestamp", System.currentTimeMillis());

            // RAM
            payload.put("used_mb", usedHeapMb);
            payload.put("total_mb", totalHeapMb);
            payload.put("max_mb", maxHeapMb);
            payload.put("usage_pct", Math.round(ramUsageRatio * 10.0) / 10.0);

            // Non-Heap
            payload.put("metaspace_used_mb", metaspaceUsedMb);
            payload.put("codecache_used_mb", codeCacheUsedMb);

            // CPU & OS
            payload.put("app_cpu", Math.round(appCpu * 10.0) / 10.0);
            payload.put("system_cpu", Math.round(systemCpu * 10.0) / 10.0);
            payload.put("processors", runtime.availableProcessors());

            // DB
            payload.put("db_latency_ms", lastKnownDbLatency);
            payload.put("active_connections", activeConns);
            payload.put("total_connections", totalConns);
            payload.put("idle_connections", idleConns);
            payload.put("threads_awaiting", threadsAwaiting);

            // Threads & JSF
            payload.put("threads", threadBean.getThreadCount());
            payload.put("peak_threads", threadBean.getPeakThreadCount());
            payload.put("daemon_threads", threadBean.getDaemonThreadCount());
            payload.put("active_sessions", HttpSessionMetricsListener.getActiveSessions());
            payload.put("total_sessions_created", HttpSessionMetricsListener.getTotalCreatedSessions());

            payload.put("jsf_component_count", JSFMetricsTracker.getLastViewComponentCount());
            payload.put("jsf_render_time_ms", JSFMetricsTracker.getLastPhaseDurationMs());
            payload.put("primefaces_version", "14.0.0");
            payload.put("omnifaces_version", "4.3");
            payload.put("jsf_stage", "Production");

            // Disk, GC & Runtime
            payload.put("used_disk_gb", (rootDisk.getTotalSpace() - rootDisk.getFreeSpace()) / GIGABYTE);
            payload.put("total_disk_gb", rootDisk.getTotalSpace() / GIGABYTE);
            payload.put("uptime_ms", ManagementFactory.getRuntimeMXBean().getUptime());
            payload.put("gc_count", gcCount);
            payload.put("gc_time_ms", gcTimeMs);
            payload.put("active_profile", activeProfile);
            payload.put("java_vendor", JavaVersion.getJavaVersion().toString());

            messagingTemplate.convertAndSend("/topic/metrics", payload);

        } catch (Exception e) {
            Logger.logInfo("⚠️ Error recopilando métricas: " + e.getMessage());
        }
    }

    private long measureDbLatency() {
        if (dataSource == null) return -1;
        long start = System.currentTimeMillis();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.setQueryTimeout(1);
            stmt.execute("SELECT 1");
            return System.currentTimeMillis() - start;
        } catch (Exception e) {
            return -1;
        }
    }

    @PreDestroy
    public void stopMonitor() {
        this.scheduler.shutdown();
        try {
            if (!this.scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                this.scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            this.scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}