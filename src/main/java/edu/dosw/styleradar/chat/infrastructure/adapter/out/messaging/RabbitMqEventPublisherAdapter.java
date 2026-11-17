package edu.dosw.styleradar.chat.infrastructure.adapter.out.messaging;

import edu.dosw.styleradar.chat.domain.event.MessageSentEvent;
import edu.dosw.styleradar.chat.domain.port.out.EventPublisherPort;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.messaging.dto.EventEnvelope;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.messaging.dto.MessageSentData;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.metrics.ChatMetrics;
import edu.dosw.styleradar.chat.infrastructure.config.RabbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.UUID;

/**
 * Publica los eventos del chat en RabbitMQ con confirmaciones del publicador. Si el broker no confirma o no
 * puede enrutar el evento, se registra el error y una métrica para que salte la alerta; nunca se rompe la llamada HTTP.
 */
@Slf4j
@Component
public class RabbitMqEventPublisherAdapter implements EventPublisherPort {
    private static final String PRODUCER = "ms-chat";

    private final RabbitTemplate rabbitTemplate;
    private final Clock clock;
    private final ChatMetrics metrics;

    public RabbitMqEventPublisherAdapter(RabbitTemplate rabbitTemplate, Clock clock, ChatMetrics metrics) {
        this.rabbitTemplate = rabbitTemplate;
        this.clock = clock;
        this.metrics = metrics;
    }

    @Override
    public void publish(MessageSentEvent event) {
        String correlationId = MDC.get("correlationId") != null ? MDC.get("correlationId") : UUID.randomUUID().toString();
        MessageSentData data = new MessageSentData(event.messageId(), event.conversationId(), event.listingId(),
                event.garmentName(), event.authorId(), event.recipientId(), event.preview(), event.sentAt());
        EventEnvelope<MessageSentData> envelope = new EventEnvelope<>(UUID.randomUUID().toString(),
                RabbitConfig.MESSAGE_SENT_ROUTING_KEY, 1, clock.instant(), PRODUCER, correlationId, data);
        CorrelationData correlation = new CorrelationData(envelope.eventId());
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EVENTS_EXCHANGE, RabbitConfig.MESSAGE_SENT_ROUTING_KEY,
                    envelope, correlation);
            correlation.getFuture().whenComplete((confirm, error) -> checkConfirmation(event, correlation, confirm, error));
        } catch (AmqpException e) {
            log.error("Could not publish {} for message {}", RabbitConfig.MESSAGE_SENT_ROUTING_KEY,
                    event.messageId(), e);
            metrics.recordEventPublishFailed("excepcion");
        }
    }

    private void checkConfirmation(MessageSentEvent event, CorrelationData correlation,
                                   CorrelationData.Confirm confirm, Throwable error) {
        if (error != null) {
            reportFailure("sin-confirmacion", event, error.getMessage());
        } else if (!confirm.isAck()) {
            reportFailure("nack", event, confirm.getReason());
        } else if (correlation.getReturned() != null) {
            reportFailure("sin-ruta", event, correlation.getReturned().getReplyText());
        }
    }

    private void reportFailure(String reason, MessageSentEvent event, String detail) {
        log.error("RabbitMQ no confirmó {} del mensaje {} ({}): {}", RabbitConfig.MESSAGE_SENT_ROUTING_KEY,
                event.messageId(), reason, detail);
        metrics.recordEventPublishFailed(reason);
    }
}
