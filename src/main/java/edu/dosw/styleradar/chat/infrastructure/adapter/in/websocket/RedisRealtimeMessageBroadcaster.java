package edu.dosw.styleradar.chat.infrastructure.adapter.in.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.styleradar.chat.application.dto.response.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Relays persisted messages between service instances through Redis Pub/Sub. */
public class RedisRealtimeMessageBroadcaster implements RealtimeMessageBroadcaster, MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisRealtimeMessageBroadcaster.class);

    private final StringRedisTemplate redis;
    private final SimpMessagingTemplate messaging;
    private final ObjectMapper objectMapper;
    private final String channel;
    private final String instanceId = UUID.randomUUID().toString();

    public RedisRealtimeMessageBroadcaster(StringRedisTemplate redis, SimpMessagingTemplate messaging,
                                           ObjectMapper objectMapper, String channel) {
        this.redis = redis;
        this.messaging = messaging;
        this.objectMapper = objectMapper;
        this.channel = channel;
    }

    @Override
    public void broadcast(MessageResponse message) {
        deliverLocally(message);
        try {
            redis.convertAndSend(channel, objectMapper.writeValueAsString(new RealtimeEnvelope(instanceId, message)));
        } catch (JsonProcessingException | DataAccessException exception) {
            LOGGER.warn("No fue posible distribuir por Redis el mensaje {} de la conversación {}. "
                            + "El mensaje quedó persistido y se puede recuperar por REST.",
                    message.id(), message.conversationId(), exception);
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);
            RealtimeEnvelope envelope = objectMapper.readValue(payload, RealtimeEnvelope.class);
            if (envelope.originInstance() != null && !instanceId.equals(envelope.originInstance())) {
                deliverLocally(envelope.message());
            }
        } catch (Exception exception) {
            LOGGER.warn("Se descartó un mensaje Redis de chat con formato inválido", exception);
        }
    }

    private void deliverLocally(MessageResponse message) {
        messaging.convertAndSend(LocalRealtimeMessageBroadcaster.destination(message), message);
    }

    private record RealtimeEnvelope(String originInstance, MessageResponse message) { }
}
