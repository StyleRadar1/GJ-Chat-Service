package edu.dosw.styleradar.chat.infrastructure.adapter.in.websocket;

import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.security.AuthenticatedUser;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.security.JwtTokenAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtStompChannelInterceptorTest {
    private static final AuthenticatedUser PARTICIPANT = new AuthenticatedUser(1L, Set.of("COMPRADOR"));
    private static final Conversation CONVERSATION = new Conversation(7L, 10L, "Prenda", 1L, 2L,
            ConversationStatus.OPEN, Instant.parse("2026-01-01T00:00:00Z"));

    @Mock
    private JwtTokenAdapter tokens;
    @Mock
    private ConversationRepositoryPort conversations;
    private JwtStompChannelInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new JwtStompChannelInterceptor(tokens, conversations);
    }

    @Test
    void authenticatesAStompConnectionFromItsBearerHeader() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer valid-token");
        when(tokens.validate("valid-token")).thenReturn(PARTICIPANT);

        Message<?> result = interceptor.preSend(message(accessor), mock(MessageChannel.class));

        StompHeaderAccessor authenticated = StompHeaderAccessor.wrap(result);
        assertThat(authenticated.getUser()).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        verify(tokens).validate("valid-token");
    }

    @Test
    void rejectsAConnectionWithoutJwt() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);

        assertThatThrownBy(() -> interceptor.preSend(message(accessor), mock(MessageChannel.class)))
                .isInstanceOf(MessagingException.class);
    }

    @Test
    void allowsOnlyAConversationParticipantToSubscribe() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/conversations/7");
        accessor.setUser(UsernamePasswordAuthenticationToken.authenticated(PARTICIPANT, null,
                PARTICIPANT.authorities()));
        when(conversations.findById(7L)).thenReturn(Optional.of(CONVERSATION));

        interceptor.preSend(message(accessor), mock(MessageChannel.class));

        verify(conversations).findById(7L);
    }

    @Test
    void rejectsSubscriptionsToAnotherParticipantsConversation() {
        AuthenticatedUser stranger = new AuthenticatedUser(3L, Set.of("COMPRADOR"));
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/conversations/7");
        accessor.setUser(UsernamePasswordAuthenticationToken.authenticated(stranger, null,
                stranger.authorities()));
        when(conversations.findById(7L)).thenReturn(Optional.of(CONVERSATION));

        assertThatThrownBy(() -> interceptor.preSend(message(accessor), mock(MessageChannel.class)))
                .isInstanceOf(MessagingException.class);
    }

    private Message<byte[]> message(StompHeaderAccessor accessor) {
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
