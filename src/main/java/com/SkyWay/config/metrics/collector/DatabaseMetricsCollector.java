package com.SkyWay.config.metrics.collector;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class DatabaseMetricsCollector {

    @Autowired(required = false)
    private DataSource dataSource;

    private long lastLatency = -1;

    public Map<String, Object> collect(boolean pingDb) {
        Map<String, Object> data = new HashMap<>();

        int active = 0, total = 0, idle = 0, awaiting = 0;
        if (dataSource instanceof HikariDataSource hikari && hikari.getHikariPoolMXBean() != null) {
            var pool = hikari.getHikariPoolMXBean();
            active = pool.getActiveConnections();
            total = pool.getTotalConnections();
            idle = pool.getIdleConnections();
            awaiting = pool.getThreadsAwaitingConnection();
        }

        if (pingDb || lastLatency == -1) {
            lastLatency = ping();
        }

        data.put("latency_ms", lastLatency);
        data.put("active_conns", active);
        data.put("total_conns", total);
        data.put("idle_conns", idle);
        data.put("awaiting_threads", awaiting);
        data.put("status", lastLatency >= 0 ? "HEALTHY" : "DEGRADED");

        return data;
    }

    private long ping() {
        if (dataSource == null) return -1;
        long start = System.nanoTime();
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.setQueryTimeout(2);
            stmt.execute("SELECT 1");
            return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        } catch (Exception e) {
            return -1;
        }
    }
}