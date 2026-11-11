package edu.dosw.styleradar.chat.infrastructure.adapter.out.messaging;

import edu.dosw.styleradar.chat.domain.event.MessageSentEvent;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.messaging.dto.EventEnvelope;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.messaging.dto.MessageSentData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;
import java.time.Clock;
import java.time.ZoneOffset;

import static edu.dosw.styleradar.chat.support.TestData.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/** Tests for the RabbitMQ publisher adapter. */
@ExtendWith(MockitoExtension.class)
class RabbitMqEventPublisherAdapterTest {
    private static final MessageSentEvent EVENT = new MessageSentEvent(20L, 7L, 10L, "Chaqueta de jean", 1L, 2L,
            "Hola", NOW);

    @Mock
    private RabbitTemplate rabbitTemplate;
    private RabbitMqEventPublisherAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new RabbitMqEventPublisherAdapter(rabbitTemplate, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    @SuppressWarnings("unchecked")
    void publishesTheEnvelopeWithTheContractRoutingKey() {
        adapter.publish(EVENT);

        ArgumentCaptor<Object> sent = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(eq("styleradar.events"), eq("chat.mensaje.enviado"), sent.capture());
        EventEnvelope<MessageSentData> envelope = (EventEnvelope<MessageSentData>) sent.getValue();
        assertThat(envelope.eventId()).isNotBlank();
        assertThat(envelope.eventType()).isEqualTo("chat.mensaje.enviado");
        assertThat(envelope.version()).isEqualTo(1);
        assertThat(envelope.timestamp()).isEqualTo(NOW);
        assertThat(envelope.producer()).isEqualTo("ms-chat");
        assertThat(envelope.correlationId()).isNotBlank();
        assertThat(envelope.data().messageId()).isEqualTo(20L);
        assertThat(envelope.data().recipientId()).isEqualTo(2L);
        assertThat(envelope.data().preview()).isEqualTo("Hola");
    }

    @Test
    void aBrokerFailureDoesNotBreakTheCaller() {
        doThrow(new AmqpConnectException(new IOException("down")))
                .when(rabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class));

        assertThatCode(() -> adapter.publish(EVENT)).doesNotThrowAnyException();
    }
}
