package com.SkyWay.controller;

import com.SkyWay.config.UnifiedSystemMonitorTask;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador REST administrativo para consultar y controlar la telemetría en vivo en tiempo de ejecución.
 */
@RestController
@RequestMapping("/api/admin/telemetry")
public class AdminTelemetryController {

    private final UnifiedSystemMonitorTask monitorTask;

    public AdminTelemetryController(UnifiedSystemMonitorTask monitorTask) {
        this.monitorTask = monitorTask;
    }

    @GetMapping("/snapshot")
    public ResponseEntity<Map<String, Object>> snapshot(
            @RequestParam(defaultValue = "true") boolean quick
    ) {
        if (quick) {
            return ResponseEntity.ok(monitorTask.construirSnapshotInicialRapido());
        }
        return ResponseEntity.ok(monitorTask.construirSnapshotCompleto(true, true));
    }

    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggle(
            @RequestParam boolean active,
            @RequestParam(required = false) Integer interval
    ) {
        return ResponseEntity.ok(monitorTask.setTelemetriaActiva(active, interval));
    }

    @PostMapping("/interval")
    public ResponseEntity<Map<String, Object>> updateInterval(@RequestParam int seconds) {
        return ResponseEntity.ok(monitorTask.cambiarIntervalo(seconds));
    }

    @PostMapping("/gc")
    public ResponseEntity<Map<String, Object>> triggerGc() {
        return ResponseEntity.ok(monitorTask.solicitarGcYEmitir());
    }
}
