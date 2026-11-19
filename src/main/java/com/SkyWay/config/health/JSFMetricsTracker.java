package com.SkyWay.config.health;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class JSFMetricsTracker implements PhaseListener {

    private static final AtomicLong lastPhaseDurationMs = new AtomicLong(0);
    private static final AtomicLong totalRenderTimeMs = new AtomicLong(0);
    private static final AtomicLong maxRenderTimeMs = new AtomicLong(0);
    private static final AtomicLong totalJsfRequests = new AtomicLong(0);
    private static final AtomicLong totalPostbacks = new AtomicLong(0);
    private static final AtomicInteger lastViewComponentCount = new AtomicInteger(0);
    private static final AtomicReference<String> lastViewId = new AtomicReference<>("-");
    private static final AtomicBoolean lastRequestWasPostback = new AtomicBoolean(false);

    @Override
    public void beforePhase(PhaseEvent event) {
        if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
            event.getFacesContext().getAttributes().put("jsf_start_time", System.currentTimeMillis());
        }
    }

    @Override
    public void afterPhase(PhaseEvent event) {
        if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
            FacesContext context = event.getFacesContext();
            Long startTime = (Long) context.getAttributes().get("jsf_start_time");

            if (startTime != null) {
                long duration = Math.max(0L, System.currentTimeMillis() - startTime);
                lastPhaseDurationMs.set(duration);
                totalRenderTimeMs.addAndGet(duration);
                actualizarMaximoRender(duration);
            }

            totalJsfRequests.incrementAndGet();
            boolean postback = context.isPostback();
            lastRequestWasPostback.set(postback);
            if (postback) {
                totalPostbacks.incrementAndGet();
            }

            if (context.getViewRoot() != null) {
                String viewId = context.getViewRoot().getViewId();
                if (viewId != null) {
                    lastViewId.set(viewId);
                }
                lastViewComponentCount.set(countComponents(context.getViewRoot()));
            }
        }
    }

    private void actualizarMaximoRender(long duration) {
        long actual;
        do {
            actual = maxRenderTimeMs.get();
            if (duration <= actual) {
                return;
            }
        } while (!maxRenderTimeMs.compareAndSet(actual, duration));
    }

    private int countComponents(UIComponent component) {
        int count = 1;
        for (UIComponent child : component.getChildren()) {
            count += countComponents(child);
        }
        return count;
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    public static long getLastPhaseDurationMs() {
        return lastPhaseDurationMs.get();
    }

    public static int getLastViewComponentCount() {
        return lastViewComponentCount.get();
    }

    public static Map<String, Object> collectMetrics() {
        long requests = totalJsfRequests.get();
        long totalMs = totalRenderTimeMs.get();
        double avgMs = requests > 0 ? Math.round(((double) totalMs / requests) * 10.0) / 10.0 : 0.0;

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("last_component_count", lastViewComponentCount.get());
        metrics.put("last_render_time_ms", lastPhaseDurationMs.get());
        metrics.put("total_render_time_ms", totalMs);
        metrics.put("avg_render_time_ms", avgMs);
        metrics.put("max_render_time_ms", maxRenderTimeMs.get());
        metrics.put("total_jsf_requests", requests);
        metrics.put("total_postbacks", totalPostbacks.get());
        metrics.put("last_view_id", lastViewId.get());
        metrics.put("last_was_postback", lastRequestWasPostback.get());
        metrics.put("primefaces_version", "14.0.0");
        metrics.put("omnifaces_version", "4.3");
        return metrics;
    }
}
