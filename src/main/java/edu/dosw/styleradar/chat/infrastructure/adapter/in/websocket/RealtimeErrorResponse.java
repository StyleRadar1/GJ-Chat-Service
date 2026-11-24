package edu.dosw.styleradar.chat.infrastructure.adapter.in.websocket;

/** Private STOMP error delivered to the authenticated sender. */
public record RealtimeErrorResponse(int status, String code, String detail) {
}
