package edu.dosw.styleradar.chat.infrastructure.adapter.out.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.dosw.styleradar.chat.domain.event.MessageSentEvent;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.messaging.dto.EventEnvelope;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.messaging.dto.MessageSentData;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.metrics.ChatMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
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

/** Pruebas del publicador de RabbitMQ con confirmaciones. */
@ExtendWith(MockitoExtension.class)
class RabbitMqEventPublisherAdapterTest {
    private static final MessageSentEvent EVENT = new MessageSentEvent(20L, 7L, 10L, "Chaqueta de jean", 1L, 2L,
            null, "Hola", NOW);

    @Mock
    private RabbitTemplate rabbitTemplate;
    private SimpleMeterRegistry registry;
    private RabbitMqEventPublisherAdapter adapter;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        adapter = new RabbitMqEventPublisherAdapter(rabbitTemplate, Clock.fixed(NOW, ZoneOffset.UTC),
                new ChatMetrics(registry));
    }

    private CorrelationData publishAndCaptureCorrelation() {
        adapter.publish(EVENT);
        ArgumentCaptor<CorrelationData> correlation = ArgumentCaptor.forClass(CorrelationData.class);
        verify(rabbitTemplate).convertAndSend(eq("styleradar.exchange"), eq("styleradar.chat.mensaje.enviado"),
                any(Object.class), correlation.capture());
        return correlation.getValue();
    }

    private double failures(String reason) {
        return registry.get("chat.events.publish.failed").tag("reason", reason).counter().count();
    }

    @Test
    @SuppressWarnings("unchecked")
    void publishesTheEnvelopeWithTheContractRoutingKey() {
        adapter.publish(EVENT);

        ArgumentCaptor<Object> sent = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<CorrelationData> correlation = ArgumentCaptor.forClass(CorrelationData.class);
        verify(rabbitTemplate).convertAndSend(eq("styleradar.exchange"),
                eq("styleradar.chat.mensaje.enviado"), sent.capture(), correlation.capture());
        EventEnvelope<MessageSentData> envelope = (EventEnvelope<MessageSentData>) sent.getValue();
        assertThat(envelope.eventId()).isNotBlank();
        assertThat(envelope.eventType()).isEqualTo("styleradar.chat.mensaje.enviado");
        assertThat(envelope.version()).isEqualTo(1);
        assertThat(envelope.timestamp()).isEqualTo(NOW);
        assertThat(envelope.producer()).isEqualTo("ms-chat");
        assertThat(envelope.correlationId()).isNotBlank();
        assertThat(envelope.data().messageId()).isEqualTo(20L);
        assertThat(envelope.data().recipientId()).isEqualTo(2L);
        assertThat(envelope.data().preview()).isEqualTo("Hola");
        assertThat(envelope.destinatarioId()).isEqualTo(2L);
        assertThat(envelope.conversacionId()).isEqualTo(7L);
        assertThat(envelope.remitenteId()).isEqualTo(1L);
        assertThat(envelope.nombreRemitente()).isEqualTo("Usuario 1");
        assertThat(envelope.resumenContenido()).isEqualTo("Hola");
        assertThat(envelope.eventoId()).isEqualTo(envelope.eventId());
        assertThat(correlation.getValue().getId()).isEqualTo(envelope.eventId());
    }

    @Test
    void serializesHybridPayloadWithEnvelopeAndFlatFields() throws Exception {
        adapter.publish(EVENT);

        ArgumentCaptor<Object> sent = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(any(String.class), any(String.class),
                sent.capture(), any(CorrelationData.class));

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String json = mapper.writeValueAsString(sent.getValue());
        JsonNode root = mapper.readTree(json);

        // Standard envelope
        assertThat(root.has("eventId")).isTrue();
        assertThat(root.has("data")).isTrue();
        assertThat(root.get("data").get("mensajeId").asLong()).isEqualTo(20L);

        // Flat fields expected by ms-notificaciones
        assertThat(root.has("destinatarioId")).isTrue();
        assertThat(root.get("destinatarioId").asLong()).isEqualTo(2L);
        assertThat(root.has("conversacionId")).isTrue();
        assertThat(root.get("conversacionId").asLong()).isEqualTo(7L);
        assertThat(root.get("remitenteId").asLong()).isEqualTo(1L);
        assertThat(root.get("nombreRemitente").asText()).isEqualTo("Usuario 1");
        assertThat(root.get("resumenContenido").asText()).isEqualTo("Hola");
        assertThat(root.has("eventoId")).isTrue();
    }

    @Test
    void aBrokerFailureDoesNotBreakTheCaller() {
        doThrow(new AmqpConnectException(new IOException("down")))
                .when(rabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class),
                        any(CorrelationData.class));

        assertThatCode(() -> adapter.publish(EVENT)).doesNotThrowAnyException();
        assertThat(failures("excepcion")).isEqualTo(1.0);
    }

    @Test
    void aConfirmedAndRoutedEventIsNotReportedAsFailure() {
        CorrelationData correlation = publishAndCaptureCorrelation();

        correlation.getFuture().complete(new CorrelationData.Confirm(true, null));

        assertThat(registry.find("chat.events.publish.failed").counter()).isNull();
    }

    @Test
    void aNackedEventIsReportedAsFailure() {
        CorrelationData correlation = publishAndCaptureCorrelation();

        correlation.getFuture().complete(new CorrelationData.Confirm(false, "broker lleno"));

        assertThat(failures("nack")).isEqualTo(1.0);
    }

    @Test
    void anUnroutableEventIsReportedAsFailure() {
        CorrelationData correlation = publishAndCaptureCorrelation();
        correlation.setReturned(new ReturnedMessage(new Message(new byte[0]), 312, "NO_ROUTE",
                "styleradar.exchange", "styleradar.chat.mensaje.enviado"));

        correlation.getFuture().complete(new CorrelationData.Confirm(true, null));

        assertThat(failures("sin-ruta")).isEqualTo(1.0);
    }

    @Test
    void aMissingConfirmationIsReportedAsFailure() {
        CorrelationData correlation = publishAndCaptureCorrelation();

        correlation.getFuture().completeExceptionally(new IllegalStateException("tiempo agotado"));

        assertThat(failures("sin-confirmacion")).isEqualTo(1.0);
    }
}
