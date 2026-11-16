package edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.entity;

import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JpaEntitiesTest {
    private static final Instant NOW = Instant.parse("2026-11-01T15:00:00Z");

    @Test
    void conversationJpaEntityGettersAndSetters() {
        ConversationJpaEntity entity = new ConversationJpaEntity();
        entity.setId(1L);
        entity.setListingId(2L);
        entity.setGarmentName("Camisa");
        entity.setBuyerId(3L);
        entity.setSellerId(4L);
        entity.setStatus(ConversationStatus.OPEN);
        entity.setCreatedAt(NOW);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getListingId()).isEqualTo(2L);
        assertThat(entity.getGarmentName()).isEqualTo("Camisa");
        assertThat(entity.getBuyerId()).isEqualTo(3L);
        assertThat(entity.getSellerId()).isEqualTo(4L);
        assertThat(entity.getStatus()).isEqualTo(ConversationStatus.OPEN);
        assertThat(entity.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void messageJpaEntityGettersAndSetters() {
        MessageJpaEntity entity = new MessageJpaEntity();
        entity.setId(10L);
        entity.setConversationId(20L);
        entity.setAuthorId(30L);
        entity.setText("Mensaje");
        entity.setSentAt(NOW);

        assertThat(entity.getId()).isEqualTo(10L);
        assertThat(entity.getConversationId()).isEqualTo(20L);
        assertThat(entity.getAuthorId()).isEqualTo(30L);
        assertThat(entity.getText()).isEqualTo("Mensaje");
        assertThat(entity.getSentAt()).isEqualTo(NOW);
    }

    @Test
    void processedEventJpaEntityGettersAndSetters() {
        ProcessedEventJpaEntity entity = new ProcessedEventJpaEntity();
        entity.setEventId("evt-123");
        entity.setProcessedAt(NOW);

        assertThat(entity.getEventId()).isEqualTo("evt-123");
        assertThat(entity.getProcessedAt()).isEqualTo(NOW);

        ProcessedEventJpaEntity allArgs = new ProcessedEventJpaEntity("evt-456", NOW);
        assertThat(allArgs.getEventId()).isEqualTo("evt-456");
        assertThat(allArgs.getProcessedAt()).isEqualTo(NOW);
    }

    @Test
    void conversationStatusConverterTests() {
        ConversationStatusConverter converter = new ConversationStatusConverter();

        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToDatabaseColumn(ConversationStatus.OPEN)).isEqualTo("ABIERTA");
        assertThat(converter.convertToDatabaseColumn(ConversationStatus.CLOSED)).isEqualTo("CERRADA");

        assertThat(converter.convertToEntityAttribute(null)).isNull();
        assertThat(converter.convertToEntityAttribute("ABIERTA")).isEqualTo(ConversationStatus.OPEN);
        assertThat(converter.convertToEntityAttribute("CERRADA")).isEqualTo(ConversationStatus.CLOSED);

        assertThatThrownBy(() -> converter.convertToEntityAttribute("DESCONOCIDO"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
