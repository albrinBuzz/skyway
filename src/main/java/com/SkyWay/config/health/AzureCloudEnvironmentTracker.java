package com.SkyWay.config.health;

import com.sun.management.OperatingSystemMXBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.lang.management.BufferPoolMXBean;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class AzureCloudEnvironmentTracker {

    private static final long AZURE_F1_MAX_RAM_MB = 1024L;
    private static final long MEGABYTE = 1024L * 1024L;
    private static final long GIGABYTE = 1024L * 1024L * 1024L;

    private final OperatingSystemMXBean osBean;
    private final ClassLoadingMXBean classLoadingBean;
    private final File rootDisk;
    private final String hostname;
    private final String hostIp;
    private final Map<String, Object> cachedAzureMetadata;
    private final List<Map<String, Object>> cachedNetworkInterfaces;

    public AzureCloudEnvironmentTracker() {
        this.osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        this.classLoadingBean = ManagementFactory.getClassLoadingMXBean();

        File disco = new File("/");
        try {
            Path root = Path.of(".").toAbsolutePath().getRoot();
            if (root != null) {
                disco = root.toFile();
            }
        } catch (Exception ignored) {
        }
        this.rootDisk = disco;

        String host = "localhost";
        String ip = "127.0.0.1";
        try {
            InetAddress local = InetAddress.getLocalHost();
            host = local.getHostName();
            ip = local.getHostAddress();
        } catch (Exception ignored) {
        }
        this.hostname = host;
        this.hostIp = ip;

        this.cachedAzureMetadata = detectarMetadatosInfraestructura();
        this.cachedNetworkInterfaces = detectarInterfacesDeRed();
    }

    public Map<String, Object> collectMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        Runtime runtime = Runtime.getRuntime();

        long totalHeapMb = runtime.totalMemory() / MEGABYTE;
        long freeHeapMb = runtime.freeMemory() / MEGABYTE;
        long usedHeapMb = Math.max(0L, totalHeapMb - freeHeapMb);
        long maxHeapMb = Math.max(1L, runtime.maxMemory() / MEGABYTE);

        long metaspaceMb = 0, codeCacheMb = 0, edenMb = 0, survivorMb = 0, oldGenMb = 0;

        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            String name = pool.getName().toLowerCase();
            long used = pool.getUsage() != null ? pool.getUsage().getUsed() / MEGABYTE : 0L;
            if (name.contains("metaspace")) metaspaceMb = used;
            else if (name.contains("codecache") || name.contains("code cache")) codeCacheMb = used;
            else if (name.contains("eden")) edenMb = used;
            else if (name.contains("survivor")) survivorMb = used;
            else if (name.contains("old") || name.contains("tenured")) oldGenMb = used;
        }

        long directBuffersMb = 0L;
        long mappedBuffersMb = 0L;
        for (BufferPoolMXBean bp : ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class)) {
            String name = bp.getName().toLowerCase();
            long usedMb = Math.max(0L, bp.getMemoryUsed() / MEGABYTE);
            if (name.contains("direct")) {
                directBuffersMb = usedMb;
            } else if (name.contains("mapped")) {
                mappedBuffersMb = usedMb;
            }
        }

        long totalJvmProcessRamMb = usedHeapMb + metaspaceMb + codeCacheMb + directBuffersMb;
        double azureF1PressurePct = ((double) totalJvmProcessRamMb / AZURE_F1_MAX_RAM_MB) * 100.0;
        double heapUsagePct = ((double) usedHeapMb / maxHeapMb) * 100.0;

        // Memoria Proceso y Pools
        metrics.put("jvm_process_ram_mb", totalJvmProcessRamMb);
        metrics.put("heap_used_mb", usedHeapMb);
        metrics.put("heap_total_mb", totalHeapMb);
        metrics.put("heap_max_mb", maxHeapMb);
        metrics.put("heap_usage_pct", redondear1(heapUsagePct));
        metrics.put("eden_mb", edenMb);
        metrics.put("survivor_mb", survivorMb);
        metrics.put("oldgen_mb", oldGenMb);
        metrics.put("metaspace_mb", metaspaceMb);
        metrics.put("codecache_mb", codeCacheMb);
        metrics.put("direct_buffers_mb", directBuffersMb);
        metrics.put("mapped_buffers_mb", mappedBuffersMb);

        // Clases cargadas
        metrics.put("loaded_classes", classLoadingBean.getLoadedClassCount());
        metrics.put("unloaded_classes", classLoadingBean.getUnloadedClassCount());

        // Almacenamiento & Disco raíz + volúmenes montados
        long totalDiskGb = rootDisk.getTotalSpace() / GIGABYTE;
        long freeDiskGb = rootDisk.getFreeSpace() / GIGABYTE;
        long usedDiskGb = Math.max(0L, totalDiskGb - freeDiskGb);
        metrics.put("disk_path", rootDisk.getAbsolutePath());
        metrics.put("total_disk_gb", totalDiskGb);
        metrics.put("free_disk_gb", freeDiskGb);
        metrics.put("used_disk_gb", usedDiskGb);
        metrics.put("disk_usage_pct", totalDiskGb > 0 ? redondear1(((double) usedDiskGb / totalDiskGb) * 100.0) : 0.0);
        metrics.put("disk_volumes", leerVolumenesDisco());

        // Memoria Física Servidor Host & Swap
        long totalPhysRam = Math.max(0L, osBean.getTotalMemorySize() / MEGABYTE);
        long freePhysRam = Math.max(0L, osBean.getFreeMemorySize() / MEGABYTE);
        long totalSwap = Math.max(0L, osBean.getTotalSwapSpaceSize() / MEGABYTE);
        long freeSwap = Math.max(0L, osBean.getFreeSwapSpaceSize() / MEGABYTE);
        metrics.put("physical_ram_total_mb", totalPhysRam);
        metrics.put("physical_ram_free_mb", freePhysRam);
        metrics.put("physical_ram_used_mb", Math.max(0L, totalPhysRam - freePhysRam));
        metrics.put("swap_total_mb", totalSwap);
        metrics.put("swap_used_mb", Math.max(0L, totalSwap - freeSwap));

        // Cuota Azure F1
        metrics.put("azure_f1_limit_mb", AZURE_F1_MAX_RAM_MB);
        metrics.put("azure_f1_pressure_pct", redondear1(azureF1PressurePct));

        // CPU & Red Host
        double rawSysCpu = osBean.getCpuLoad() * 100.0;
        double rawProcCpu = osBean.getProcessCpuLoad() * 100.0;
        double sysCpu = Double.isNaN(rawSysCpu) || rawSysCpu < 0 ? 0.0 : rawSysCpu;
        double procCpu = Double.isNaN(rawProcCpu) || rawProcCpu < 0 ? 0.0 : rawProcCpu;

        metrics.put("available_processors", runtime.availableProcessors());
        metrics.put("system_cpu_load", redondear1(sysCpu));
        metrics.put("process_cpu_load", redondear1(procCpu));
        metrics.put("system_load_average", redondear1(Math.max(0.0, osBean.getSystemLoadAverage())));
        metrics.put("os_name", osBean.getName() + " (" + osBean.getArch() + " v" + osBean.getVersion() + ")");
        metrics.put("hostname", hostname);
        metrics.put("host_ip", hostIp);

        metrics.put("azure_infra", cachedAzureMetadata);
        metrics.put("network_interfaces_detail", cachedNetworkInterfaces);

        return metrics;
    }

    private Map<String, Object> detectarMetadatosInfraestructura() {
        Map<String, Object> infra = new LinkedHashMap<>();
        String siteName = envOr("WEBSITE_SITE_NAME", null);
        String region = envOr("REGION_NAME", null);
        String sku = envOr("WEBSITE_SKU", null);
        String instanceId = envOr("WEBSITE_INSTANCE_ID", null);
        String azureHostname = envOr("WEBSITE_HOSTNAME", null);
        boolean enDocker = Files.exists(Path.of("/.dockerenv"));

        String entorno = StringUtils.hasText(siteName)
                ? "Azure App Service"
                : (enDocker ? "Contenedor Docker" : "Host Local (" + System.getProperty("os.name", "OS") + ")");

        infra.put("environment_type", entorno);
        infra.put("site_name", StringUtils.hasText(siteName) ? siteName : "skyway-local");
        infra.put("region", StringUtils.hasText(region) ? region : "Local / On-Prem");
        infra.put("sku", StringUtils.hasText(sku) ? sku : "Dev / Local");
        infra.put("instance_id", StringUtils.hasText(instanceId)
                ? (instanceId.length() > 12 ? instanceId.substring(0, 12) + "..." : instanceId)
                : "-");
        infra.put("public_hostname", StringUtils.hasText(azureHostname) ? azureHostname : hostname);
        infra.put("is_azure", StringUtils.hasText(siteName));
        infra.put("is_docker", enDocker);
        return infra;
    }

    private List<Map<String, Object>> detectarInterfacesDeRed() {
        List<Map<String, Object>> lista = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            while (nics != null && nics.hasMoreElements()) {
                NetworkInterface nic = nics.nextElement();
                if (!nic.isUp() || nic.isLoopback()) {
                    continue;
                }
                List<String> ipv4s = new ArrayList<>();
                List<String> ipv6s = new ArrayList<>();
                for (InterfaceAddress ifAddr : nic.getInterfaceAddresses()) {
                    InetAddress addr = ifAddr.getAddress();
                    if (addr instanceof Inet4Address) {
                        ipv4s.add(addr.getHostAddress() + "/" + ifAddr.getNetworkPrefixLength());
                    } else if (addr instanceof Inet6Address && !addr.isLinkLocalAddress()) {
                        ipv6s.add(addr.getHostAddress() + "/" + ifAddr.getNetworkPrefixLength());
                    }
                }
                if (ipv4s.isEmpty() && ipv6s.isEmpty()) {
                    continue;
                }
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("name", nic.getName());
                item.put("display_name", nic.getDisplayName());
                item.put("mtu", nic.getMTU());
                item.put("mac", formatearMac(nic.getHardwareAddress()));
                item.put("ipv4", String.join(", ", ipv4s));
                item.put("ipv6", ipv6s.isEmpty() ? "-" : String.join(", ", ipv6s));
                lista.add(item);
            }
        } catch (Exception ignored) {
        }
        return lista;
    }

    private List<Map<String, Object>> leerVolumenesDisco() {
        List<Map<String, Object>> vols = new ArrayList<>();
        try {
            File[] roots = File.listRoots();
            if (roots != null) {
                for (File f : roots) {
                    long totalGb = f.getTotalSpace() / GIGABYTE;
                    if (totalGb <= 0) {
                        continue;
                    }
                    long freeGb = f.getFreeSpace() / GIGABYTE;
                    long usedGb = Math.max(0L, totalGb - freeGb);
                    Map<String, Object> v = new LinkedHashMap<>();
                    v.put("path", f.getAbsolutePath());
                    v.put("used_gb", usedGb);
                    v.put("free_gb", freeGb);
                    v.put("total_gb", totalGb);
                    v.put("usage_pct", redondear1(((double) usedGb / totalGb) * 100.0));
                    vols.add(v);
                }
            }
        } catch (Exception ignored) {
        }
        return vols;
    }

    private String formatearMac(byte[] mac) {
        if (mac == null || mac.length == 0) {
            return "-";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            if (i > 0) sb.append(':');
            sb.append(String.format("%02X", mac[i]));
        }
        return sb.toString();
    }

    private String envOr(String key, String fallback) {
        String val = System.getenv(key);
        return StringUtils.hasText(val) ? val.trim() : fallback;
    }

    private double redondear1(double val) {
        return Math.round(val * 10.0) / 10.0;
    }
}
