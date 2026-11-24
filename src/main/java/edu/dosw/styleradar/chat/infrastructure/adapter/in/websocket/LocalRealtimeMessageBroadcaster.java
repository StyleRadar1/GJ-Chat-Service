package edu.dosw.styleradar.chat.infrastructure.adapter.in.websocket;

import edu.dosw.styleradar.chat.application.dto.response.MessageResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/** Uses Spring's in-process broker when Redis Pub/Sub is disabled. */
public class LocalRealtimeMessageBroadcaster implements RealtimeMessageBroadcaster {
    private final SimpMessagingTemplate messaging;

    public LocalRealtimeMessageBroadcaster(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    @Override
    public void broadcast(MessageResponse message) {
        messaging.convertAndSend(destination(message), message);
    }

    static String destination(MessageResponse message) {
        return "/topic/conversations/" + message.conversationId();
    }
}
