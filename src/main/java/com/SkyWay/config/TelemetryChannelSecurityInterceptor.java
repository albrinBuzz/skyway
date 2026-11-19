package com.SkyWay.config;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interceptor STOMP que restringe las suscripciones a {@code /topic/metrics} exclusivamente
 * a usuarios autenticados con {@code ROLE_ADMIN} y rastrea sesiones activas mirando telemetría
 * para evitar consumo innecesario en Azure F1 cuando no hay observadores conectados.
 */
@Component
public class TelemetryChannelSecurityInterceptor implements ChannelInterceptor {

    private static final String TOPIC_METRICS = "/topic/metrics";
    private final Set<String> activeTelemetrySessions = ConcurrentHashMap.newKeySet();

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        String sessionId = accessor.getSessionId();

        if (StompCommand.SUBSCRIBE.equals(command)) {
            String destination = accessor.getDestination();
            if (destination != null && destination.startsWith(TOPIC_METRICS)) {
                if (!tieneRolAdmin(accessor.getUser())) {
                    throw new AccessDeniedException("Acceso denegado a canal de telemetría administrativa.");
                }
                if (sessionId != null) {
                    activeTelemetrySessions.add(sessionId);
                }
            }
        } else if (StompCommand.UNSUBSCRIBE.equals(command)) {
            String subId = accessor.getSubscriptionId();
            String dest = accessor.getDestination();
            if (sessionId != null && (dest == null || dest.startsWith(TOPIC_METRICS) || subId != null)) {
                // Si se desuscribe sin destino explícito y estaba viendo métricas, la removemos de forma conservadora si aplica
                if (dest != null && dest.startsWith(TOPIC_METRICS)) {
                    activeTelemetrySessions.remove(sessionId);
                }
            }
        } else if (StompCommand.DISCONNECT.equals(command)) {
            if (sessionId != null) {
                activeTelemetrySessions.remove(sessionId);
            }
        }

        return message;
    }

    @EventListener
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        if (event != null && event.getSessionId() != null) {
            activeTelemetrySessions.remove(event.getSessionId());
        }
    }

    public int getActiveViewerCount() {
        return activeTelemetrySessions.size();
    }

    private boolean tieneRolAdmin(Principal principal) {
        if (principal instanceof Authentication auth && auth.isAuthenticated() && auth.getAuthorities() != null) {
            for (GrantedAuthority authority : auth.getAuthorities()) {
                if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                    return true;
                }
            }
        }
        return false;
    }
}
