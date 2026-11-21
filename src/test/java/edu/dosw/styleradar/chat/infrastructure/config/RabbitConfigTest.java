package edu.dosw.styleradar.chat.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.MessageConverter;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitConfigTest {
    private final RabbitConfig config = new RabbitConfig();

    @Test
    void declaresExchangesQueuesAndBindings() {
        TopicExchange eventsExchange = config.eventsExchange();
        assertThat(eventsExchange.getName()).isEqualTo(RabbitConfig.EVENTS_EXCHANGE);

        DirectExchange deadLetterExchange = config.deadLetterExchange();
        assertThat(deadLetterExchange.getName()).isEqualTo(RabbitConfig.DEAD_LETTER_EXCHANGE);

        Queue queue = config.secondHandQueue();
        assertThat(queue.getName()).isEqualTo(RabbitConfig.SECOND_HAND_QUEUE);

        Queue dlq = config.secondHandDeadLetterQueue();
        assertThat(dlq.getName()).isEqualTo(RabbitConfig.SECOND_HAND_DEAD_LETTER_QUEUE);

        Binding listingSoldBinding = config.listingSoldBinding(queue, eventsExchange);
        assertThat(listingSoldBinding.getRoutingKey()).isEqualTo(RabbitConfig.LISTING_SOLD_ROUTING_KEY);

        Binding dlqBinding = config.deadLetterBinding(dlq, deadLetterExchange);
        assertThat(dlqBinding.getRoutingKey()).isEqualTo(RabbitConfig.SECOND_HAND_DEAD_LETTER_QUEUE);

        MessageConverter converter = config.jsonMessageConverter(new ObjectMapper());
        assertThat(converter).isNotNull();
    }
}
