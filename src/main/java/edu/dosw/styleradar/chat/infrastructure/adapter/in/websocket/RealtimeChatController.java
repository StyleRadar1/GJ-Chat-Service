package edu.dosw.styleradar.chat.infrastructure.adapter.in.websocket;

import edu.dosw.styleradar.chat.application.dto.request.RealtimeMessageRequest;
import edu.dosw.styleradar.chat.application.dto.response.MessageResponse;
import edu.dosw.styleradar.chat.domain.exception.ConversationClosedException;
import edu.dosw.styleradar.chat.domain.exception.TooManyMessagesException;
import edu.dosw.styleradar.chat.domain.model.Message;
import edu.dosw.styleradar.chat.domain.model.MessageText;
import edu.dosw.styleradar.chat.domain.port.in.SendMessageUseCase;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.metrics.ChatMetrics;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import java.security.Principal;

/** STOMP adapter that persists messages before broadcasting them to both participants. */
@Controller
@Validated
public class RealtimeChatController {
    private final SendMessageUseCase sendMessage;
    private final RealtimeMessageBroadcaster broadcaster;
    private final SimpMessagingTemplate messaging;
    private final ChatMetrics metrics;

    public RealtimeChatController(SendMessageUseCase sendMessage, RealtimeMessageBroadcaster broadcaster,
                                  SimpMessagingTemplate messaging, ChatMetrics metrics) {
        this.sendMessage = sendMessage;
        this.broadcaster = broadcaster;
        this.messaging = messaging;
        this.metrics = metrics;
    }

    @MessageMapping("/conversations/{conversationId}/messages")
    public void send(@DestinationVariable Long conversationId, @Valid @Payload RealtimeMessageRequest request,
                     Principal principal) {
        AuthenticatedUser user = authenticatedUser(principal);
        long start = System.currentTimeMillis();
        Message saved = sendMessage.send(user.userId(), conversationId, new MessageText(request.text()));
        metrics.recordMessageLatency(System.currentTimeMillis() - start);
        metrics.recordRealtimeMessageSent();
        MessageResponse response = new MessageResponse(saved.getId(), saved.getConversationId(), saved.getAuthorId(),
                saved.getText().value(), saved.getSentAt());
        messaging.convertAndSendToUser(user.userId().toString(), "/queue/conversations/acks", response);
        broadcaster.broadcast(response);
    }

    @MessageExceptionHandler(ConversationClosedException.class)
    @SendToUser(value = "/queue/conversations/errors", broadcast = false)
    public RealtimeErrorResponse handleClosedConversation(ConversationClosedException exception) {
        return new RealtimeErrorResponse(409, "conflicto", exception.getMessage());
    }

    @MessageExceptionHandler(TooManyMessagesException.class)
    @SendToUser(value = "/queue/conversations/errors", broadcast = false)
    public RealtimeErrorResponse handleTooManyMessages(TooManyMessagesException exception) {
        return new RealtimeErrorResponse(429, "demasiadas-solicitudes", exception.getMessage());
    }

    private AuthenticatedUser authenticatedUser(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken authentication
                && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user;
        }
        throw new MessageDeliveryException("La conexión WebSocket no está autenticada");
    }
}
