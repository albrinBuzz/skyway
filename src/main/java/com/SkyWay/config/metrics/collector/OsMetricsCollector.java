package com.SkyWay.config.metrics.collector;

import com.sun.management.OperatingSystemMXBean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

@Component
public class OsMetricsCollector {

    private final OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private final File rootDisk = new File("/");
    private static final long MEGABYTE = 1024L * 1024L;
    private static final long GIGABYTE = 1024L * 1024L * 1024L;

    public Map<String, Object> collect() {
        Map<String, Object> data = new HashMap<>();

        double rawAppCpu = osBean.getProcessCpuLoad() * 100.0;
        double rawSysCpu = osBean.getCpuLoad() * 100.0;

        data.put("app_cpu", Double.isNaN(rawAppCpu) || rawAppCpu < 0 ? 0.0 : Math.round(rawAppCpu * 10.0) / 10.0);
        data.put("system_cpu", Double.isNaN(rawSysCpu) || rawSysCpu < 0 ? 0.0 : Math.round(rawSysCpu * 10.0) / 10.0);
        data.put("system_load_avg", osBean.getSystemLoadAverage());
        data.put("processors", osBean.getAvailableProcessors());

        // RAM & Swap
        long totalRamMb = osBean.getTotalMemorySize() / MEGABYTE;
        long usedRamMb = totalRamMb - (osBean.getFreeMemorySize() / MEGABYTE);
        data.put("total_ram_mb", totalRamMb);
        data.put("used_ram_mb", usedRamMb);
        data.put("free_swap_mb", osBean.getFreeSwapSpaceSize() / MEGABYTE);

        // Storage
        data.put("used_disk_gb", (rootDisk.getTotalSpace() - rootDisk.getFreeSpace()) / GIGABYTE);
        data.put("total_disk_gb", rootDisk.getTotalSpace() / GIGABYTE);
        data.put("uptime_ms", ManagementFactory.getRuntimeMXBean().getUptime());

        return data;
    }
}