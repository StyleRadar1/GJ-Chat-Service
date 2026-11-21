package edu.dosw.styleradar.chat.application.mapper.out;

import edu.dosw.styleradar.chat.application.dto.response.ConversationResponse;
import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationDtoOutMapperTest {
    private final ConversationDtoOutMapper mapper = Mappers.getMapper(ConversationDtoOutMapper.class);

    @Test
    void mapsConversationAndStatus() {
        Instant now = Instant.parse("2026-11-01T15:00:00Z");
        Conversation conversation = new Conversation(7L, 10L, "Prenda", 1L, 2L, ConversationStatus.OPEN, now);

        ConversationResponse response = mapper.toResponse(conversation);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.status()).isEqualTo("ABIERTA");

        List<ConversationResponse> list = mapper.toResponse(List.of(conversation));
        assertThat(list).hasSize(1);
        assertThat(list.get(0).status()).isEqualTo("ABIERTA");

        assertThat(mapper.toCode(ConversationStatus.CLOSED)).isEqualTo("CERRADA");
    }
}
