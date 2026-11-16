package edu.dosw.styleradar.chat.infrastructure.adapter.in.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.styleradar.chat.application.dto.response.MessageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RedisRealtimeMessageBroadcasterTest {
    private static final String CHANNEL = "styleradar.chat.realtime";
    private static final MessageResponse MESSAGE = new MessageResponse(20L, 7L, 1L, "Hola",
            Instant.parse("2026-11-12T20:00:00Z"));

    private StringRedisTemplate redis;
    private SimpMessagingTemplate messaging;
    private ObjectMapper objectMapper;
    private RedisRealtimeMessageBroadcaster broadcaster;

    @BeforeEach
    void setUp() {
        redis = mock(StringRedisTemplate.class);
        messaging = mock(SimpMessagingTemplate.class);
        objectMapper = new ObjectMapper().findAndRegisterModules();
        broadcaster = new RedisRealtimeMessageBroadcaster(redis, messaging, objectMapper, CHANNEL);
    }

    @Test
    void deliversLocallyAndPublishesThePersistedMessageToRedis() {
        broadcaster.broadcast(MESSAGE);

        verify(messaging).convertAndSend("/topic/conversations/7", MESSAGE);
        verify(redis).convertAndSend(eq(CHANNEL), startsWith("{\"originInstance\":"));
    }

    @Test
    void relaysMessagesPublishedByAnotherInstanceToLocalSubscribers() throws Exception {
        String envelope = objectMapper.writeValueAsString(new TestEnvelope("other-instance", MESSAGE));
        broadcaster.onMessage(new DefaultMessage(CHANNEL.getBytes(StandardCharsets.UTF_8),
                envelope.getBytes(StandardCharsets.UTF_8)), null);

        verify(messaging).convertAndSend("/topic/conversations/7", MESSAGE);
    }

    @Test
    void ignoresMessagesPublishedByThisInstanceToAvoidDuplicateDelivery() throws Exception {
        broadcaster.broadcast(MESSAGE);
        ArgumentCaptor<String> published = ArgumentCaptor.forClass(String.class);
        verify(redis).convertAndSend(eq(CHANNEL), published.capture());
        broadcaster.onMessage(new DefaultMessage(CHANNEL.getBytes(StandardCharsets.UTF_8),
                published.getValue().getBytes(StandardCharsets.UTF_8)), null);

        // A locally published message is already delivered by broadcast; Redis callbacks are origin-filtered.
        verify(messaging, times(1)).convertAndSend("/topic/conversations/7", MESSAGE);
    }

    @Test
    void keepsLocalDeliveryWhenRedisDistributionFails() {
        doThrow(new DataAccessResourceFailureException("Redis unavailable"))
                .when(redis).convertAndSend(eq(CHANNEL), startsWith("{\"originInstance\":"));

        broadcaster.broadcast(MESSAGE);

        verify(messaging).convertAndSend("/topic/conversations/7", MESSAGE);
    }

    private record TestEnvelope(String originInstance, MessageResponse message) { }
}
