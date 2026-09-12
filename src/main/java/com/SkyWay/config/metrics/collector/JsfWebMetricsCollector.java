package com.SkyWay.config.metrics.collector;

import com.SkyWay.config.health.HttpSessionMetricsListener;
import com.SkyWay.config.health.JSFMetricsTracker;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

@Component
public class JsfWebMetricsCollector {

    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

    public Map<String, Object> collect() {
        Map<String, Object> data = new HashMap<>();

        // Threads
        data.put("thread_count", threadBean.getThreadCount());
        data.put("peak_thread_count", threadBean.getPeakThreadCount());
        data.put("daemon_thread_count", threadBean.getDaemonThreadCount());

        // Sessions HTTP
        data.put("active_sessions", HttpSessionMetricsListener.getActiveSessions());
        data.put("total_sessions", HttpSessionMetricsListener.getTotalCreatedSessions());

        // JSF & UI Specs
        data.put("jsf_components", JSFMetricsTracker.getLastViewComponentCount());
        data.put("jsf_render_ms", JSFMetricsTracker.getLastPhaseDurationMs());
        data.put("primefaces_version", "14.0.0");
        data.put("omnifaces_version", "4.3");

        return data;
    }
}