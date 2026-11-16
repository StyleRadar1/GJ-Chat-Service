package edu.dosw.styleradar.chat.infrastructure.adapter.in.websocket;

import edu.dosw.styleradar.chat.domain.exception.AccessDeniedDomainException;
import edu.dosw.styleradar.chat.domain.exception.ConversationNotFoundException;
import edu.dosw.styleradar.chat.domain.exception.StoreNotFoundException;
import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import edu.dosw.styleradar.chat.domain.model.ConversationType;
import edu.dosw.styleradar.chat.domain.model.StoreSummary;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.StoreQueryPort;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.security.AuthenticatedUser;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.security.JwtTokenAdapter;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
    @Mock
    private StoreQueryPort stores;
    private JwtStompChannelInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new JwtStompChannelInterceptor(tokens, conversations, stores);
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
    void authenticatesCommandStompFromItsBearerHeader() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.STOMP);
        accessor.addNativeHeader("Authorization", "Bearer valid-token");
        when(tokens.validate("valid-token")).thenReturn(PARTICIPANT);

        Message<?> result = interceptor.preSend(message(accessor), mock(MessageChannel.class));

        StompHeaderAccessor authenticated = StompHeaderAccessor.wrap(result);
        assertThat(authenticated.getUser()).isInstanceOf(UsernamePasswordAuthenticationToken.class);
    }

    @Test
    void ignoresMessagesWithoutCommand() {
        Message<byte[]> original = MessageBuilder.withPayload(new byte[0]).build();

        Message<?> result = interceptor.preSend(original, mock(MessageChannel.class));

        assertThat(result).isSameAs(original);
    }

    @Test
    void rejectsAConnectionWithoutJwt() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);

        assertThatThrownBy(() -> interceptor.preSend(message(accessor), mock(MessageChannel.class)))
                .isInstanceOf(MessagingException.class)
                .hasMessageContaining("Se requiere un JWT Bearer");
    }

    @Test
    void rejectsAConnectionWithInvalidAuthorizationHeaderFormat() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Basic 12345");

        assertThatThrownBy(() -> interceptor.preSend(message(accessor), mock(MessageChannel.class)))
                .isInstanceOf(MessagingException.class)
                .hasMessageContaining("Se requiere un JWT Bearer");
    }

    @Test
    void rejectsAConnectionWithInvalidJwt() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer bad-token");
        when(tokens.validate("bad-token")).thenThrow(new JwtException("Invalid token"));

        assertThatThrownBy(() -> interceptor.preSend(message(accessor), mock(MessageChannel.class)))
                .isInstanceOf(MessagingException.class)
                .hasMessageContaining("Token JWT inválido para WebSocket");
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
    void allowsAConversationParticipantToSendMessages() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setDestination("/app/conversations/7/messages");
        accessor.setUser(UsernamePasswordAuthenticationToken.authenticated(PARTICIPANT, null,
                PARTICIPANT.authorities()));
        when(conversations.findById(7L)).thenReturn(Optional.of(CONVERSATION));

        interceptor.preSend(message(accessor), mock(MessageChannel.class));

        verify(conversations).findById(7L);
    }

    @Test
    void rejectsSendWhenNotAuthenticated() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setDestination("/app/conversations/7/messages");

        assertThatThrownBy(() -> interceptor.preSend(message(accessor), mock(MessageChannel.class)))
                .isInstanceOf(MessagingException.class)
                .hasMessageContaining("La conexión WebSocket no está autenticada");
    }

    @Test
    void rejectsInvalidDestinationForSend() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setDestination("/topic/conversations/7");
        accessor.setUser(UsernamePasswordAuthenticationToken.authenticated(PARTICIPANT, null,
                PARTICIPANT.authorities()));

        assertThatThrownBy(() -> interceptor.preSend(message(accessor), mock(MessageChannel.class)))
                .isInstanceOf(MessagingException.class)
                .hasMessageContaining("Destino STOMP no permitido");
    }

    @Test
    void rejectsInvalidDestinationForSubscribe() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/app/conversations/7/messages");
        accessor.setUser(UsernamePasswordAuthenticationToken.authenticated(PARTICIPANT, null,
                PARTICIPANT.authorities()));

        assertThatThrownBy(() -> interceptor.preSend(message(accessor), mock(MessageChannel.class)))
                .isInstanceOf(MessagingException.class)
                .hasMessageContaining("Destino STOMP no permitido");
    }

    @Test
    void rejectsUnknownDestinationPattern() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/unknown/path");
        accessor.setUser(UsernamePasswordAuthenticationToken.authenticated(PARTICIPANT, null,
                PARTICIPANT.authorities()));

        assertThatThrownBy(() -> interceptor.preSend(message(accessor), mock(MessageChannel.class)))
                .isInstanceOf(MessagingException.class)
                .hasMessageContaining("Destino STOMP no permitido");
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
                .isInstanceOf(MessagingException.class)
                .hasMessageContaining("No tiene acceso a esta conversación")
                .hasCauseInstanceOf(AccessDeniedDomainException.class);
    }

    @Test
    void allowsTheSellerOfASecondHandConversationToSubscribe() {
        AuthenticatedUser seller = new AuthenticatedUser(2L, Set.of("COMPRADOR"));
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/conversations/7");
        accessor.setUser(UsernamePasswordAuthenticationToken.authenticated(seller, null, seller.authorities()));
        when(conversations.findById(7L)).thenReturn(Optional.of(CONVERSATION));

        interceptor.preSend(message(accessor), mock(MessageChannel.class));

        verify(stores, never()).fetchMyStore(any());
    }

    @Test
    void storeAdminSubscribesOnlyWhenTheStoreNitMatches() {
        AuthenticatedUser admin = new AuthenticatedUser(9L, Set.of("REPRESENTANTE_ALMACEN"));
        Conversation storeConversation = storeConversation(null);
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/conversations/8");
        accessor.setUser(UsernamePasswordAuthenticationToken.authenticated(admin, null, admin.authorities()));
        when(conversations.findById(8L)).thenReturn(Optional.of(storeConversation));
        when(stores.fetchMyStore(9L)).thenReturn(new StoreSummary("900123456-1", "Atelier", true));

        interceptor.preSend(message(accessor), mock(MessageChannel.class));

        assertThat(storeConversation.getSellerId()).isEqualTo(9L);
        verify(conversations).save(storeConversation);
    }

    @Test
    void storeAdminCannotSubscribeToAnotherStoresConversation() {
        AuthenticatedUser admin = new AuthenticatedUser(9L, Set.of("ADMIN_ALMACEN"));
        Conversation storeConversation = storeConversation(null);
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setDestination("/app/conversations/8/messages");
        accessor.setUser(UsernamePasswordAuthenticationToken.authenticated(admin, null, admin.authorities()));
        when(conversations.findById(8L)).thenReturn(Optional.of(storeConversation));
        when(stores.fetchMyStore(9L)).thenReturn(new StoreSummary("111111-1", "Otra tienda", true));

        assertThatThrownBy(() -> interceptor.preSend(message(accessor), mock(MessageChannel.class)))
                .isInstanceOf(MessagingException.class)
                .hasMessageContaining("No tiene acceso a esta conversación")
                .hasCauseInstanceOf(AccessDeniedDomainException.class);
        verify(conversations, never()).save(any());
        assertThat(storeConversation.getSellerId()).isNull();
    }

    @Test
    void storeAdminIsDeniedWhenTheirStoreCannotBeResolved() {
        AuthenticatedUser admin = new AuthenticatedUser(9L, Set.of("TIENDA"));
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/conversations/8");
        accessor.setUser(UsernamePasswordAuthenticationToken.authenticated(admin, null, admin.authorities()));
        when(conversations.findById(8L)).thenReturn(Optional.of(storeConversation(null)));
        when(stores.fetchMyStore(9L)).thenThrow(new StoreNotFoundException("El usuario no administra ningún almacén"));

        assertThatThrownBy(() -> interceptor.preSend(message(accessor), mock(MessageChannel.class)))
                .isInstanceOf(MessagingException.class)
                .hasCauseInstanceOf(AccessDeniedDomainException.class);
    }

    @Test
    void buyerCannotSubscribeToAnotherBuyersStoreConversation() {
        AuthenticatedUser buyer = new AuthenticatedUser(3L, Set.of("COMPRADOR"));
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/conversations/8");
        accessor.setUser(UsernamePasswordAuthenticationToken.authenticated(buyer, null, buyer.authorities()));
        when(conversations.findById(8L)).thenReturn(Optional.of(storeConversation(4L)));

        assertThatThrownBy(() -> interceptor.preSend(message(accessor), mock(MessageChannel.class)))
                .isInstanceOf(MessagingException.class)
                .hasCauseInstanceOf(AccessDeniedDomainException.class);
        verify(stores, never()).fetchMyStore(any());
    }

    @Test
    void rejectsWhenConversationNotFound() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/conversations/99");
        accessor.setUser(UsernamePasswordAuthenticationToken.authenticated(PARTICIPANT, null,
                PARTICIPANT.authorities()));
        when(conversations.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interceptor.preSend(message(accessor), mock(MessageChannel.class)))
                .isInstanceOf(ConversationNotFoundException.class);
    }

    private Message<byte[]> message(StompHeaderAccessor accessor) {
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private static Conversation storeConversation(Long sellerId) {
        return new Conversation(8L, ConversationType.ALMACEN, null, null, 1L, sellerId,
                "900123456-1", "Atelier", null, ConversationStatus.OPEN, Instant.parse("2026-01-01T00:00:00Z"));
    }
}
