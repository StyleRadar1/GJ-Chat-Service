package edu.dosw.styleradar.chat.infrastructure.adapter.in.websocket;

import edu.dosw.styleradar.chat.application.dto.request.RealtimeMessageRequest;
import edu.dosw.styleradar.chat.application.dto.response.MessageResponse;
import edu.dosw.styleradar.chat.domain.exception.ConversationClosedException;
import edu.dosw.styleradar.chat.domain.model.Message;
import edu.dosw.styleradar.chat.domain.model.MessageText;
import edu.dosw.styleradar.chat.domain.port.in.SendMessageUseCase;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RealtimeChatControllerTest {
    private static final AuthenticatedUser BUYER = new AuthenticatedUser(1L, Set.of("COMPRADOR"));
    private static final Message SAVED = new Message(20L, 7L, 1L, new MessageText("Hola"),
            Instant.parse("2026-11-12T20:00:00Z"));
    private static final MessageResponse RESPONSE = new MessageResponse(20L, 7L, 1L, "Hola",
            Instant.parse("2026-11-12T20:00:00Z"));

    @Mock
    private SendMessageUseCase sendMessage;
    @Mock
    private RealtimeMessageBroadcaster broadcaster;
    @Mock
    private SimpMessagingTemplate messaging;

    private RealtimeChatController controller;

    @BeforeEach
    void setUp() {
        controller = new RealtimeChatController(sendMessage, broadcaster, messaging);
    }

    @Test
    void confirmsTheSavedMessagePrivatelyThenBroadcastsItToTheConversation() {
        when(sendMessage.send(1L, 7L, new MessageText("Hola"))).thenReturn(SAVED);

        controller.send(7L, new RealtimeMessageRequest("Hola"), authenticatedBuyer());

        InOrder order = inOrder(sendMessage, messaging, broadcaster);
        order.verify(sendMessage).send(1L, 7L, new MessageText("Hola"));
        order.verify(messaging).convertAndSendToUser("1", "/queue/conversations/acks", RESPONSE);
        order.verify(broadcaster).broadcast(RESPONSE);
    }

    @Test
    void doesNotAcknowledgeOrBroadcastWhenTheConversationIsClosed() {
        ConversationClosedException closed = new ConversationClosedException("La conversación está cerrada");
        when(sendMessage.send(1L, 7L, new MessageText("Hola"))).thenThrow(closed);

        assertThatThrownBy(() -> controller.send(7L, new RealtimeMessageRequest("Hola"), authenticatedBuyer()))
                .isInstanceOf(ConversationClosedException.class);

        verify(messaging, never()).convertAndSendToUser("1", "/queue/conversations/acks", RESPONSE);
        verify(broadcaster, never()).broadcast(RESPONSE);

        org.assertj.core.api.Assertions.assertThat(controller.handleClosedConversation(closed))
                .isEqualTo(new RealtimeErrorResponse(409, "conflicto", "La conversación está cerrada"));
    }

    private UsernamePasswordAuthenticationToken authenticatedBuyer() {
        return UsernamePasswordAuthenticationToken.authenticated(BUYER, null, BUYER.authorities());
    }
}
