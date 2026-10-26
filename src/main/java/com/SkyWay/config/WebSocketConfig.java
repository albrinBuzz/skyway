package com.SkyWay.config;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /** Origenes extra permitidos (separados por coma). Vacio = solo el mismo origen. */
    @Value("${skyway.ws.allowed-origins:}")
    private String[] allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        var asientos = registry.addEndpoint("/ws-asientos");
        var metrics = registry.addEndpoint("/ws-metrics");
        if (allowedOrigins != null && allowedOrigins.length > 0 && !allowedOrigins[0].isBlank()) {
            asientos.setAllowedOrigins(allowedOrigins);
            metrics.setAllowedOrigins(allowedOrigins);
        }
        asientos.withSockJS();
        metrics.withSockJS();
    }

    /** Exige usuario autenticado en CONNECT/SUBSCRIBE; /topic/metrics solo ADMIN. */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (acc == null || acc.getCommand() == null) return message;
                StompCommand cmd = acc.getCommand();
                if (cmd == StompCommand.CONNECT || cmd == StompCommand.SUBSCRIBE || cmd == StompCommand.SEND) {
                    Principal user = acc.getUser();
                    if (user == null || user instanceof AnonymousAuthenticationToken) {
                        throw new MessagingException("No autenticado");
                    }
                    String dest = acc.getDestination();
                    if (cmd == StompCommand.SUBSCRIBE && dest != null && dest.startsWith("/topic/metrics")) {
                        boolean admin = user instanceof Authentication a
                                && a.getAuthorities().stream().anyMatch(g -> "ROLE_ADMIN".equals(g.getAuthority()));
                        if (!admin) throw new MessagingException("Prohibido");
                    }
                }
                return message;
            }
        });
    }
}
