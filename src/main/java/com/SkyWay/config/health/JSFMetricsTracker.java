package com.SkyWay.config.health;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class JSFMetricsTracker implements PhaseListener {

    private static final AtomicLong lastPhaseDurationMs = new AtomicLong(0);
    private static final AtomicInteger lastViewComponentCount = new AtomicInteger(0);

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
                lastPhaseDurationMs.set(System.currentTimeMillis() - startTime);
            }

            if (context.getViewRoot() != null) {
                lastViewComponentCount.set(countComponents(context.getViewRoot()));
            }
        }
    }

    private int countComponents(UIComponent component) {
        int count = 1; // El componente actual
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
}