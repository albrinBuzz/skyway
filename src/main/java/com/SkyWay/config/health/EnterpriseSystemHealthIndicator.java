package com.SkyWay.config.health;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

@Component("skywaySystemHealth")
public class EnterpriseSystemHealthIndicator implements HealthIndicator {

    @Autowired(required = false)
    private DataSource dataSource;

    private static final double MEMORY_THRESHOLD_WARNING = 0.88; // 88% de uso de RAM
    private static final int DB_TIMEOUT_SECONDS = 3;

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        boolean isHealthy = true;

        // 1. Chequeo de Memoria JVM (Heap)
        boolean memoryOk = checkJvmMemory(details);

        // 2. Chequeo de Base de Datos y Latencia
        boolean dbOk = checkDatabaseConnectivity(details);

        // 3. Chequeo del Pool de Conexiones Hikari
        boolean poolOk = checkHikariPoolStatus(details);

        isHealthy = memoryOk && dbOk && poolOk;

        Health.Builder builder = isHealthy ? Health.up() : Health.down();

        // Si la BD falla la app entra en DOWN, pero si solo la memoria está alta enviamos un estado custom WARNING
        if (isHealthy && "HIGH_USAGE".equals(details.get("jvm_memory_status"))) {
            builder.status(new Status("WARNING", "Alto consumo de memoria detectado"));
        }

        return builder.withDetails(details).build();
    }

    private boolean checkJvmMemory(Map<String, Object> details) {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long maxHeap = memoryBean.getHeapMemoryUsage().getMax();
        long usedHeap = memoryBean.getHeapMemoryUsage().getUsed();

        double usageRatio = (double) usedHeap / maxHeap;

        Map<String, Object> memDetails = new HashMap<>();
        memDetails.put("max_mb", maxHeap / (1024 * 1024));
        memDetails.put("used_mb", usedHeap / (1024 * 1024));
        memDetails.put("usage_percentage", String.format("%.2f%%", usageRatio * 100));

        if (usageRatio > MEMORY_THRESHOLD_WARNING) {
            memDetails.put("status", "HIGH_USAGE");
            details.put("jvm_memory", memDetails);
            // No marcamos DOWN para evitar reinicios agresivos, pero alertamos en logs
            return true;
        }

        memDetails.put("status", "OK");
        details.put("jvm_memory", memDetails);
        return true;
    }

    private boolean checkDatabaseConnectivity(Map<String, Object> details) {
        Map<String, Object> dbDetails = new HashMap<>();
        if (dataSource == null) {
            dbDetails.put("error", "No DataSource configurado");
            details.put("database", dbDetails);
            return false;
        }

        long startTime = System.currentTimeMillis();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            statement.setQueryTimeout(DB_TIMEOUT_SECONDS);
            statement.execute("SELECT 1"); // Validation Query liviana

            long latency = System.currentTimeMillis() - startTime;

            dbDetails.put("status", "UP");
            dbDetails.put("response_time_ms", latency);
            details.put("database", dbDetails);
            return true;

        } catch (Exception e) {
            dbDetails.put("status", "DOWN");
            dbDetails.put("error", e.getMessage());
            details.put("database", dbDetails);
            return false;
        }
    }

    private boolean checkHikariPoolStatus(Map<String, Object> details) {
        if (dataSource instanceof HikariDataSource hikariDS) {
            Map<String, Object> poolDetails = new HashMap<>();

            int activeConnections = hikariDS.getHikariPoolMXBean().getActiveConnections();
            int totalConnections = hikariDS.getHikariPoolMXBean().getTotalConnections();
            int threadsAwaiting = hikariDS.getHikariPoolMXBean().getThreadsAwaitingConnection();

            poolDetails.put("active_connections", activeConnections);
            poolDetails.put("total_connections", totalConnections);
            poolDetails.put("threads_awaiting", threadsAwaiting);

            // Si hay hilos bloqueados esperando conexiones de BD
            if (threadsAwaiting > 5) {
                poolDetails.put("status", "SATURATED");
                details.put("hikari_pool", poolDetails);
                return false;
            }

            poolDetails.put("status", "OK");
            details.put("hikari_pool", poolDetails);
        }
        return true;
    }
}