package com.SkyWay.config.metrics;

import com.SkyWay.config.metrics.collector.*;
import com.SkyWay.config.metrics.dto.TelemetryPayloadDTO;
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.JavaVersion;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class SystemObservabilityEngine {

    private final JvmMetricsCollector jvmCollector;
    private final OsMetricsCollector osCollector;
    private final DatabaseMetricsCollector dbCollector;
    private final JsfWebMetricsCollector jsfCollector;
    private final SimpMessagingTemplate messagingTemplate;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "skyway-apm-engine");
        t.setDaemon(true);
        return t;
    });

    private int tickCounter = 0;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    public SystemObservabilityEngine(
            JvmMetricsCollector jvmCollector,
            OsMetricsCollector osCollector,
            DatabaseMetricsCollector dbCollector,
            JsfWebMetricsCollector jsfCollector,
            SimpMessagingTemplate messagingTemplate) {
        this.jvmCollector = jvmCollector;
        this.osCollector = osCollector;
        this.dbCollector = dbCollector;
        this.jsfCollector = jsfCollector;
        this.messagingTemplate = messagingTemplate;
    }

    @PostConstruct
    public void init() {
        Logger.logInfo("🚀 SkyWay Enterprise Observability Engine iniciado");
        scheduler.scheduleAtFixedRate(this::broadcast, 1, 3, TimeUnit.SECONDS);
    }

    @Scheduled(fixedRate = 1000)
    private void broadcast() {
        try {
            tickCounter++;
            boolean pingDb = (tickCounter % 10 == 0); // Ping cada 30 segundos

            var jvmData = jvmCollector.collect();
            var osData = osCollector.collect();
            var dbData = dbCollector.collect(pingDb);
            var jsfData = jsfCollector.collect();

            // Atributos Globales
            osData.put("profile", activeProfile);
            osData.put("java_version", JavaVersion.getJavaVersion().toString());

            TelemetryPayloadDTO payload = new TelemetryPayloadDTO(
                    System.currentTimeMillis(),
                    jvmData,
                    osData,
                    dbData,
                    jsfData
            );

            messagingTemplate.convertAndSend("/topic/metrics", payload);
        } catch (Exception e) {
            Logger.logInfo("⚠️ Error en recopilación APM: " + e.getMessage());
        }
    }

    @PreDestroy
    public void stop() {
        scheduler.shutdown();
    }
}