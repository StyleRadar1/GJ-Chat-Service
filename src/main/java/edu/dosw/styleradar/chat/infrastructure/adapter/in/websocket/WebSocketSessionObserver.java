package edu.dosw.styleradar.chat.infrastructure.adapter.in.websocket;

import edu.dosw.styleradar.chat.infrastructure.adapter.out.security.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Records STOMP connection lifecycle without logging credentials or message contents. */
@Component
public class WebSocketSessionObserver {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketSessionObserver.class);
    private final Set<String> activeSessions = ConcurrentHashMap.newKeySet();

    @EventListener
    public void onConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        if (sessionId != null && activeSessions.add(sessionId)) {
            LOGGER.info("Conexión STOMP establecida: sessionId={}, userId={}, sesionesActivas={}",
                    sessionId, userId(accessor.getUser()), activeSessions.size());
        }
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        if (sessionId != null && activeSessions.remove(sessionId)) {
            LOGGER.info("Conexión STOMP cerrada: sessionId={}, sesionesActivas={}",
                    sessionId, activeSessions.size());
        }
    }

    private String userId(Principal principal) {
        if (principal instanceof Authentication authentication
                && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user.userId().toString();
        }
        return "desconocido";
    }
}
