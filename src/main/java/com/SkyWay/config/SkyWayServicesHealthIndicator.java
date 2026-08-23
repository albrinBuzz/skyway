package com.SkyWay.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class SkyWayServicesHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        boolean apiVuelosDisponible = comprobarApiVuelos();

        if (!apiVuelosDisponible) {
            return Health.down()
                    .withDetail("API Vuelos", "No disponible / Timeout")
                    .build();
        }

        return Health.up()
                .withDetail("API Vuelos", "Operacional")
                .build();
    }

    private boolean comprobarApiVuelos() {
        // Tu lógica de verificación de servicios
        return true;
    }
}