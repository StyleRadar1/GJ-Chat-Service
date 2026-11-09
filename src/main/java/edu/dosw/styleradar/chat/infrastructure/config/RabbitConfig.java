package edu.dosw.styleradar.chat.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Declares the exchanges, queues and dead-letter routing used by the chat; messages are JSON. */
@Configuration
public class RabbitConfig {
    public static final String EVENTS_EXCHANGE = "styleradar.events";
    public static final String DEAD_LETTER_EXCHANGE = "styleradar.dlx";
    public static final String SECOND_HAND_QUEUE = "q.chat.segunda-mano";
    public static final String SECOND_HAND_DEAD_LETTER_QUEUE = "q.chat.segunda-mano.dlq";
    public static final String MESSAGE_SENT_ROUTING_KEY = "chat.mensaje.enviado";
    public static final String LISTING_SOLD_ROUTING_KEY = "segunda-mano.prenda.vendida";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue secondHandQueue() {
        return QueueBuilder.durable(SECOND_HAND_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(SECOND_HAND_DEAD_LETTER_QUEUE)
                .build();
    }

    @Bean
    public Queue secondHandDeadLetterQueue() {
        return QueueBuilder.durable(SECOND_HAND_DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding listingSoldBinding(Queue secondHandQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(secondHandQueue).to(eventsExchange).with(LISTING_SOLD_ROUTING_KEY);
    }

    @Bean
    public Binding deadLetterBinding(Queue secondHandDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(secondHandDeadLetterQueue).to(deadLetterExchange)
                .with(SECOND_HAND_DEAD_LETTER_QUEUE);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
