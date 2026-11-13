package edu.dosw.styleradar.chat.infrastructure.adapter.in.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.styleradar.chat.application.dto.response.MessageResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/** Exercises message fan-out between two service instances through a real Redis Pub/Sub broker. */
@Testcontainers(disabledWithoutDocker = true)
class RedisRealtimeMultiInstanceTest {
    private static final String CHANNEL = "styleradar.chat.realtime.test";
    private static final MessageResponse MESSAGE = new MessageResponse(20L, 7L, 1L, "Hola",
            Instant.parse("2026-11-12T20:00:00Z"));

    @Container
    private static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    private static LettuceConnectionFactory connectionFactory;
    private static RedisMessageListenerContainer instanceBListener;
    private static SimpMessagingTemplate instanceAMessaging;
    private static SimpMessagingTemplate instanceBMessaging;
    private static RedisRealtimeMessageBroadcaster instanceA;

    @BeforeAll
    static void startRedisAndInstances() {
        connectionFactory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();
        StringRedisTemplate redis = new StringRedisTemplate(connectionFactory);
        redis.afterPropertiesSet();
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        instanceAMessaging = mock(SimpMessagingTemplate.class);
        instanceBMessaging = mock(SimpMessagingTemplate.class);
        instanceA = new RedisRealtimeMessageBroadcaster(redis, instanceAMessaging, objectMapper, CHANNEL);
        RedisRealtimeMessageBroadcaster instanceB = new RedisRealtimeMessageBroadcaster(
                redis, instanceBMessaging, objectMapper, CHANNEL);

        instanceBListener = new RedisMessageListenerContainer();
        instanceBListener.setConnectionFactory(connectionFactory);
        instanceBListener.addMessageListener(instanceB, new ChannelTopic(CHANNEL));
        instanceBListener.afterPropertiesSet();
        instanceBListener.start();
        await().atMost(Duration.ofSeconds(10)).until(instanceBListener::isListening);
    }

    @AfterAll
    static void stopRedisConnections() {
        if (instanceBListener != null) {
            instanceBListener.stop();
        }
        if (connectionFactory != null) {
            connectionFactory.destroy();
        }
    }

    @Test
    void deliversAMessageFromOneInstanceToSubscribersOnAnotherInstance() {
        instanceA.broadcast(MESSAGE);

        verify(instanceAMessaging, timeout(5_000)).convertAndSend("/topic/conversations/7", MESSAGE);
        verify(instanceBMessaging, timeout(5_000)).convertAndSend("/topic/conversations/7", MESSAGE);
    }
}
