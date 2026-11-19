package com.SkyWay.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final TelemetryChannelSecurityInterceptor telemetrySecurityInterceptor;

    public WebSocketConfig(TelemetryChannelSecurityInterceptor telemetrySecurityInterceptor) {
        this.telemetrySecurityInterceptor = telemetrySecurityInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Canal broker donde se suscribirán los clientes JSF
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Canal público operacional para retención de asientos
        registry.addEndpoint("/ws-asientos").withSockJS();

        // Canal administrativo de telemetría protegido por SecurityConfig (ROLE_ADMIN) y ChannelInterceptor
        registry.addEndpoint("/ws-metrics").withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(telemetrySecurityInterceptor);
    }
}
