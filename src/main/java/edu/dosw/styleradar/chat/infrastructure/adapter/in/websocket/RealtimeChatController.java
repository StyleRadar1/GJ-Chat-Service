package edu.dosw.styleradar.chat.infrastructure.adapter.in.websocket;

import edu.dosw.styleradar.chat.application.dto.request.RealtimeMessageRequest;
import edu.dosw.styleradar.chat.application.dto.response.MessageResponse;
import edu.dosw.styleradar.chat.domain.model.Message;
import edu.dosw.styleradar.chat.domain.model.MessageText;
import edu.dosw.styleradar.chat.domain.port.in.SendMessageUseCase;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
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
    private final SimpMessagingTemplate messaging;

    public RealtimeChatController(SendMessageUseCase sendMessage, SimpMessagingTemplate messaging) {
        this.sendMessage = sendMessage;
        this.messaging = messaging;
    }

    @MessageMapping("/conversations/{conversationId}/messages")
    public void send(@DestinationVariable Long conversationId, @Valid @Payload RealtimeMessageRequest request,
                     Principal principal) {
        AuthenticatedUser user = authenticatedUser(principal);
        Message saved = sendMessage.send(user.userId(), conversationId, new MessageText(request.text()));
        MessageResponse response = new MessageResponse(saved.getId(), saved.getConversationId(), saved.getAuthorId(),
                saved.getText().value(), saved.getSentAt());
        messaging.convertAndSend("/topic/conversations/" + conversationId, response);
    }

    private AuthenticatedUser authenticatedUser(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken authentication
                && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user;
        }
        throw new MessageDeliveryException("La conexión WebSocket no está autenticada");
    }
}
