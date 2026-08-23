package com.SkyWay.config.health;

import org.springframework.boot.actuate.availability.LivenessStateHealthIndicator;
import org.springframework.boot.actuate.availability.ReadinessStateHealthIndicator;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HealthProbesConfig {

    @Bean
    public LivenessStateHealthIndicator livenessStateHealthIndicator(ApplicationAvailability availability) {
        return new LivenessStateHealthIndicator(availability);
    }

    @Bean
    public ReadinessStateHealthIndicator readinessStateHealthIndicator(ApplicationAvailability availability) {
        return new ReadinessStateHealthIndicator(availability);
    }
}