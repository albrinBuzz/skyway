package com.SkyWay.config;

import com.SkyWay.util.Logger;
import com.sun.management.OperatingSystemMXBean;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class MemoryMonitorTask {

    private static final long MEGABYTE = 1024L * 1024L;
    private static final long GIGABYTE = 1024L * 1024L * 1024L;
    private static final int INITIAL_DELAY_SECONDS = 2;
    private static final int MONITOR_INTERVAL_SECONDS = 2;

    private final OperatingSystemMXBean osBean;
    private final ScheduledExecutorService scheduler;

    public MemoryMonitorTask() {
        this.osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "resource-monitor-thread");
            thread.setDaemon(true); // Evita que bloquee el apagado de la JVM
            return thread;
        });
    }

    @PostConstruct
    public void startMonitor() {
        Logger.logInfo("📊 Monitor de recursos iniciado (Intervalo: " + MONITOR_INTERVAL_SECONDS + "s)");

        // Programación limpia sin bucles while(true) ni Thread.sleep()
        this.scheduler.scheduleAtFixedRate(
                this::collectAndLogMetrics,
                INITIAL_DELAY_SECONDS,
                MONITOR_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );
    }

    private void collectAndLogMetrics() {
        try {
            SystemMetrics metrics = captureCurrentMetrics();
            logMetrics(metrics);
        } catch (Exception e) {
            Logger.logInfo("⚠️ Error al obtener métricas del sistema: " + e.getMessage());
        }
    }

    private SystemMetrics captureCurrentMetrics() {
        Runtime runtime = Runtime.getRuntime();

        // 1. Métricas de Memoria JVM
        long maxRamMb = runtime.maxMemory() / MEGABYTE;
        long totalRamMb = runtime.totalMemory() / MEGABYTE;
        long usedRamMb = (runtime.totalMemory() - runtime.freeMemory()) / MEGABYTE;

        // 2. Métricas de CPU (Manejo de NaN en lecturas iniciales)
        double appCpu = Math.max(0.0, osBean.getProcessCpuLoad() * 100.0);
        double systemCpu = Math.max(0.0, osBean.getCpuLoad() * 100.0);

        // 3. Métricas de Disco
        File root = new File("/");
        long totalDiskGb = root.getTotalSpace() / GIGABYTE;
        long usedDiskGb = (root.getTotalSpace() - root.getFreeSpace()) / GIGABYTE;

        return new SystemMetrics(usedRamMb, totalRamMb, maxRamMb, appCpu, systemCpu, usedDiskGb, totalDiskGb);
    }

    private void logMetrics(SystemMetrics m) {
        Logger.logInfo(String.format(
                "💻 [RECURSOS] RAM: %dMB/%dMB (Max: %dMB) | CPU App: %.1f%% | CPU Total: %.1f%% | Disco: %dGB/%dGB",
                m.usedRamMb(), m.totalRamMb(), m.maxRamMb(),
                m.appCpu(), m.systemCpu(),
                m.usedDiskGb(), m.totalDiskGb()
        ));
    }

    @PreDestroy
    public void stopMonitor() {
        Logger.logInfo("🛑 Deteniendo el monitor de recursos...");
        this.scheduler.shutdown();
        try {
            if (!this.scheduler.awaitTermination(3, TimeUnit.SECONDS)) {
                this.scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            this.scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        Logger.logInfo("✅ Monitor de recursos detenido exitosamente.");
    }

    // Record interno para transporte inmutable de datos de métricas
    private record SystemMetrics(
            long usedRamMb,
            long totalRamMb,
            long maxRamMb,
            double appCpu,
            double systemCpu,
            long usedDiskGb,
            long totalDiskGb
    ) {}
}