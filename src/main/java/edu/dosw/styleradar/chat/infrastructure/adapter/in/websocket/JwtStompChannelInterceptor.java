package edu.dosw.styleradar.chat.infrastructure.adapter.in.websocket;

import edu.dosw.styleradar.chat.domain.exception.ConversationNotFoundException;
import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.security.AuthenticatedUser;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.security.JwtTokenAdapter;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Authenticates STOMP connections and restricts sends/subscriptions to conversation participants. */
@Component
public class JwtStompChannelInterceptor implements ChannelInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtStompChannelInterceptor.class);
    private static final String ACK_SUBSCRIPTION = "/user/queue/conversations/acks";
    private static final String ERROR_SUBSCRIPTION = "/user/queue/conversations/errors";
    private static final Pattern CONVERSATION_DESTINATION =
            Pattern.compile("^/(?:app/conversations|topic/conversations)/([1-9]\\d{0,18})(?:/messages)?$");
    private final JwtTokenAdapter tokens;
    private final ConversationRepositoryPort conversations;

    public JwtStompChannelInterceptor(JwtTokenAdapter tokens, ConversationRepositoryPort conversations) {
        this.tokens = tokens;
        this.conversations = conversations;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (accessor.getCommand() == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (command == StompCommand.CONNECT || command == StompCommand.STOMP) {
            authenticate(accessor);
        } else if (command == StompCommand.SEND || command == StompCommand.SUBSCRIBE) {
            AuthenticatedUser user = authenticatedUser(accessor);
            if (command == StompCommand.SUBSCRIBE
                    && (ACK_SUBSCRIPTION.equals(accessor.getDestination())
                    || ERROR_SUBSCRIPTION.equals(accessor.getDestination()))) {
                return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
            }
            Long conversationId = conversationId(accessor.getDestination(), command);
            verifyParticipant(user, conversationId);
        }
        return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (authorization == null || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            LOGGER.warn("Conexión STOMP rechazada: falta el token Bearer");
            throw new MessagingException("Se requiere un JWT Bearer para conectar al chat");
        }
        try {
            AuthenticatedUser user = tokens.validate(authorization.substring(7).trim());
            accessor.setUser(UsernamePasswordAuthenticationToken.authenticated(user, null, user.authorities()));
        } catch (JwtException | IllegalArgumentException exception) {
            LOGGER.warn("Conexión STOMP rechazada: JWT inválido o expirado");
            throw new MessagingException("Token JWT inválido para WebSocket", exception);
        }
    }

    private AuthenticatedUser authenticatedUser(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof UsernamePasswordAuthenticationToken authentication
                && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user;
        }
        throw new MessagingException("La conexión WebSocket no está autenticada");
    }

    private Long conversationId(String destination, StompCommand command) {
        Matcher matcher = destination == null ? null : CONVERSATION_DESTINATION.matcher(destination);
        if (matcher == null || !matcher.matches()
                || command == StompCommand.SEND
                && (!destination.startsWith("/app/") || !destination.endsWith("/messages"))
                || command == StompCommand.SUBSCRIBE
                && (!destination.startsWith("/topic/") || destination.endsWith("/messages"))) {
            LOGGER.warn("Frame STOMP rechazado: destino no permitido, comando={}", command);
            throw new MessagingException("Destino STOMP no permitido");
        }
        try {
            return Long.valueOf(matcher.group(1));
        } catch (NumberFormatException exception) {
            throw new MessagingException("Identificador de conversación inválido", exception);
        }
    }

    private void verifyParticipant(AuthenticatedUser user, Long conversationId) {
        Conversation conversation = conversations.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));
        if (!conversation.isParticipant(user.userId())) {
            LOGGER.warn("Acceso STOMP rechazado: userId={} no participa en conversationId={}",
                    user.userId(), conversationId);
            throw new MessagingException("No tiene acceso a esta conversación");
        }
    }
}
