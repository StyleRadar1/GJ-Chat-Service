package edu.dosw.styleradar.chat.infrastructure.adapter.in.websocket;

import edu.dosw.styleradar.chat.application.dto.response.MessageResponse;

/** Delivers a persisted chat message to connected subscribers. */
public interface RealtimeMessageBroadcaster {
    void broadcast(MessageResponse message);
}
