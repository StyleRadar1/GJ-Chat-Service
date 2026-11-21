package edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import edu.dosw.styleradar.chat.domain.model.Message;
import edu.dosw.styleradar.chat.domain.model.MessageText;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.entity.ConversationJpaEntity;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.entity.MessageJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PersistenceMapperTest {
    private final PersistenceMapper mapper = new PersistenceMapper();
    private static final Instant NOW = Instant.parse("2026-11-01T15:00:00Z");

    @Test
    void mapsConversationToDomainAndBack() {
        Conversation conversation = new Conversation(7L, 10L, "Prenda", 1L, 2L, ConversationStatus.OPEN, NOW);
        ConversationJpaEntity entity = mapper.toEntity(conversation);

        assertThat(entity.getId()).isEqualTo(7L);
        assertThat(entity.getListingId()).isEqualTo(10L);
        assertThat(entity.getGarmentName()).isEqualTo("Prenda");
        assertThat(entity.getBuyerId()).isEqualTo(1L);
        assertThat(entity.getSellerId()).isEqualTo(2L);
        assertThat(entity.getStatus()).isEqualTo(ConversationStatus.OPEN);
        assertThat(entity.getCreatedAt()).isEqualTo(NOW);

        Conversation domain = mapper.toDomain(entity);
        assertThat(domain.getId()).isEqualTo(7L);
        assertThat(domain.getListingId()).isEqualTo(10L);
        assertThat(domain.getGarmentName()).isEqualTo("Prenda");
        assertThat(domain.getBuyerId()).isEqualTo(1L);
        assertThat(domain.getSellerId()).isEqualTo(2L);
        assertThat(domain.getStatus()).isEqualTo(ConversationStatus.OPEN);
        assertThat(domain.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void mapsMessageToDomainAndBack() {
        Message message = new Message(100L, 7L, 1L, new MessageText("Hola"), NOW);
        MessageJpaEntity entity = mapper.toEntity(message);

        assertThat(entity.getId()).isEqualTo(100L);
        assertThat(entity.getConversationId()).isEqualTo(7L);
        assertThat(entity.getAuthorId()).isEqualTo(1L);
        assertThat(entity.getText()).isEqualTo("Hola");
        assertThat(entity.getSentAt()).isEqualTo(NOW);

        Message domain = mapper.toDomain(entity);
        assertThat(domain.getId()).isEqualTo(100L);
        assertThat(domain.getConversationId()).isEqualTo(7L);
        assertThat(domain.getAuthorId()).isEqualTo(1L);
        assertThat(domain.getText().value()).isEqualTo("Hola");
        assertThat(domain.getSentAt()).isEqualTo(NOW);
    }
}
