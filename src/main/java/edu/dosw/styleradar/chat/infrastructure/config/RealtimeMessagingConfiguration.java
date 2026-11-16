package edu.dosw.styleradar.chat.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.styleradar.chat.infrastructure.adapter.in.websocket.LocalRealtimeMessageBroadcaster;
import edu.dosw.styleradar.chat.infrastructure.adapter.in.websocket.RealtimeMessageBroadcaster;
import edu.dosw.styleradar.chat.infrastructure.adapter.in.websocket.RedisRealtimeMessageBroadcaster;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/** Selects a local broker for single-instance use or Redis Pub/Sub for multi-instance use. */
@Configuration
public class RealtimeMessagingConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "styleradar.websocket.redis", name = "enabled", havingValue = "false",
            matchIfMissing = true)
    public RealtimeMessageBroadcaster localRealtimeMessageBroadcaster(SimpMessagingTemplate messaging) {
        return new LocalRealtimeMessageBroadcaster(messaging);
    }

    @Bean
    @ConditionalOnProperty(prefix = "styleradar.websocket.redis", name = "enabled", havingValue = "true")
    public RedisRealtimeMessageBroadcaster redisRealtimeMessageBroadcaster(
            StringRedisTemplate redis, SimpMessagingTemplate messaging, ObjectMapper objectMapper,
            @Value("${styleradar.websocket.redis.channel}") String channel) {
        return new RedisRealtimeMessageBroadcaster(redis, messaging, objectMapper, channel);
    }

    @Bean
    @ConditionalOnProperty(prefix = "styleradar.websocket.redis", name = "enabled", havingValue = "true")
    public RedisMessageListenerContainer realtimeRedisListenerContainer(
            RedisConnectionFactory connectionFactory, RedisRealtimeMessageBroadcaster listener,
            @Value("${styleradar.websocket.redis.channel}") String channel) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listener, new ChannelTopic(channel));
        return container;
    }
}
