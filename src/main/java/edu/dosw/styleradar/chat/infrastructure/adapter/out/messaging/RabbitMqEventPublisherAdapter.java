package edu.dosw.styleradar.chat.infrastructure.adapter.out.messaging;

import edu.dosw.styleradar.chat.domain.event.MessageSentEvent;
import edu.dosw.styleradar.chat.domain.port.out.EventPublisherPort;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.messaging.dto.EventEnvelope;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.messaging.dto.MessageSentData;
import edu.dosw.styleradar.chat.infrastructure.config.RabbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.UUID;

/** Publishes chat events to RabbitMQ; a broker failure is logged and never breaks the HTTP call. */
@Slf4j
@Component
public class RabbitMqEventPublisherAdapter implements EventPublisherPort {
    private static final String PRODUCER = "ms-chat";

    private final RabbitTemplate rabbitTemplate;
    private final Clock clock;

    public RabbitMqEventPublisherAdapter(RabbitTemplate rabbitTemplate, Clock clock) {
        this.rabbitTemplate = rabbitTemplate;
        this.clock = clock;
    }

    @Override
    public void publish(MessageSentEvent event) {
        String correlationId = MDC.get("correlationId") != null ? MDC.get("correlationId") : UUID.randomUUID().toString();
        MessageSentData data = new MessageSentData(event.messageId(), event.conversationId(), event.listingId(),
                event.garmentName(), event.authorId(), event.recipientId(), event.preview(), event.sentAt());
        EventEnvelope<MessageSentData> envelope = new EventEnvelope<>(UUID.randomUUID().toString(),
                RabbitConfig.MESSAGE_SENT_ROUTING_KEY, 1, clock.instant(), PRODUCER, correlationId, data);
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EVENTS_EXCHANGE, RabbitConfig.MESSAGE_SENT_ROUTING_KEY, envelope);
        } catch (AmqpException e) {
            log.error("Could not publish {} for message {}", RabbitConfig.MESSAGE_SENT_ROUTING_KEY,
                    event.messageId(), e);
        }
    }
}
