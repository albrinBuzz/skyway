package com.SkyWay.config.health;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@WebListener
public class HttpSessionMetricsListener implements HttpSessionListener {

    private static final AtomicInteger activeSessions = new AtomicInteger(0);
    private static final AtomicInteger totalCreatedSessions = new AtomicInteger(0);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        activeSessions.incrementAndGet();
        totalCreatedSessions.incrementAndGet();
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        if (activeSessions.get() > 0) {
            activeSessions.decrementAndGet();
        }
    }

    public static int getActiveSessions() {
        return activeSessions.get();
    }

    public static int getTotalCreatedSessions() {
        return totalCreatedSessions.get();
    }
}