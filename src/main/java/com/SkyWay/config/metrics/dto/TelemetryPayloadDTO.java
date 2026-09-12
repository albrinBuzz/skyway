package com.SkyWay.config.metrics.dto;

import java.util.Map;

public record TelemetryPayloadDTO(
        long timestamp,
        Map<String, Object> jvm,
        Map<String, Object> os,
        Map<String, Object> db,
        Map<String, Object> jsfWeb
) {}