package com.SkyWay.config.metrics.collector;

import org.springframework.stereotype.Component;
import java.lang.management.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JvmMetricsCollector {

    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final ClassLoadingMXBean classLoadingBean = ManagementFactory.getClassLoadingMXBean();
    private final List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    private final List<MemoryPoolMXBean> poolBeans = ManagementFactory.getMemoryPoolMXBeans();

    private static final long MEGABYTE = 1024L * 1024L;

    public Map<String, Object> collect() {
        Map<String, Object> data = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();

        long maxHeapMb = runtime.maxMemory() / MEGABYTE;
        long totalHeapMb = runtime.totalMemory() / MEGABYTE;
        long usedHeapMb = totalHeapMb - (runtime.freeMemory() / MEGABYTE);

        data.put("used_heap_mb", usedHeapMb);
        data.put("total_heap_mb", totalHeapMb);
        data.put("max_heap_mb", maxHeapMb);
        data.put("heap_pct", maxHeapMb > 0 ? Math.round(((double) usedHeapMb / maxHeapMb) * 1000.0) / 10.0 : 0.0);

        // Desglose por Espacios de Memoria (Eden, Survivor, Old Gen)
        long edenMb = 0, survivorMb = 0, oldGenMb = 0, metaspaceMb = 0, codeCacheMb = 0;
        for (MemoryPoolMXBean pool : poolBeans) {
            long used = pool.getUsage().getUsed() / MEGABYTE;
            String name = pool.getName().toLowerCase();
            if (name.contains("eden")) edenMb = used;
            else if (name.contains("survivor")) survivorMb = used;
            else if (name.contains("old") || name.contains("tenured")) oldGenMb = used;
            else if (name.contains("metaspace")) metaspaceMb = used;
            else if (name.contains("codecache") || name.contains("code cache")) codeCacheMb = used;
        }

        data.put("eden_mb", edenMb);
        data.put("survivor_mb", survivorMb);
        data.put("old_gen_mb", oldGenMb);
        data.put("metaspace_mb", metaspaceMb);
        data.put("codecache_mb", codeCacheMb);

        // Classloading
        data.put("loaded_classes", classLoadingBean.getLoadedClassCount());
        data.put("unloaded_classes", classLoadingBean.getUnloadedClassCount());

        // GC Info
        long gcCount = 0, gcTimeMs = 0;
        StringBuilder gcNames = new StringBuilder();
        for (GarbageCollectorMXBean gc : gcBeans) {
            long c = gc.getCollectionCount();
            long t = gc.getCollectionTime();
            if (c > 0) gcCount += c;
            if (t > 0) gcTimeMs += t;
            if (!gcNames.isEmpty()) gcNames.append(", ");
            gcNames.append(gc.getName());
        }

        data.put("gc_count", gcCount);
        data.put("gc_time_ms", gcTimeMs);
        data.put("gc_algorithm", !gcNames.isEmpty() ? gcNames.toString() : "G1 / Standard");

        return data;
    }
}