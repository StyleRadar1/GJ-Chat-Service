package edu.dosw.styleradar.chat.application.mapper.out;

import edu.dosw.styleradar.chat.application.dto.response.ConversationResponse;
import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import edu.dosw.styleradar.chat.domain.model.ConversationType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationDtoOutMapperTest {
    private final ConversationDtoOutMapper mapper = Mappers.getMapper(ConversationDtoOutMapper.class);
    private static final Instant NOW = Instant.parse("2026-11-01T15:00:00Z");

    @Test
    void mapsConversationWhenBuyerViewsSecondHand() {
        Conversation conversation = new Conversation(7L, 10L, "Chaqueta", 1L, 2L, ConversationStatus.OPEN, NOW);

        // Buyer views -> should resolve seller's name or fallback
        ConversationResponse withName = mapper.toResponse(conversation, 1L, "Sofía L.");
        assertThat(withName.participantDisplayName()).isEqualTo("Sofía L.");

        ConversationResponse withFallback = mapper.toResponse(conversation, 1L, null);
        assertThat(withFallback.participantDisplayName()).isEqualTo("Vendedor");
        assertThat(withFallback.participantDisplayName()).doesNotContain("Usuario #");
    }

    @Test
    void mapsConversationWhenSellerViewsSecondHand() {
        Conversation conversation = new Conversation(7L, 10L, "Chaqueta", 1L, 2L, ConversationStatus.OPEN, NOW);

        // Seller views -> should resolve buyer's name or fallback
        ConversationResponse withName = mapper.toResponse(conversation, 2L, "Carlos P.");
        assertThat(withName.participantDisplayName()).isEqualTo("Carlos P.");

        ConversationResponse withFallback = mapper.toResponse(conversation, 2L, null);
        assertThat(withFallback.participantDisplayName()).isEqualTo("Comprador interesado");
        assertThat(withFallback.participantDisplayName()).doesNotContain("Usuario #");
    }

    @Test
    void resolvesParticipantDisplayNameForStore() {
        Conversation storeConv = new Conversation(1L, ConversationType.ALMACEN, null, null, 10L, null,
                "900123456-1", "Atelier Urbano Chapinero", null, ConversationStatus.OPEN, NOW);

        // Buyer views store chat -> storeName
        assertThat(mapper.toResponse(storeConv, 10L, null).participantDisplayName())
                .isEqualTo("Atelier Urbano Chapinero");

        // Store admin views store chat -> buyer's name or fallback
        assertThat(mapper.toResponse(storeConv, 99L, "Laura M.").participantDisplayName())
                .isEqualTo("Laura M.");
        assertThat(mapper.toResponse(storeConv, 99L, null).participantDisplayName())
                .isEqualTo("Comprador interesado");
    }

    @Test
    void resolvesParticipantDisplayNameForDonation() {
        Conversation donationConv = new Conversation(2L, ConversationType.DONACION, null, "Donación #5", 10L, 50L,
                null, null, 5L, ConversationStatus.OPEN, NOW);

        // Donor views -> recipient name or fallback
        assertThat(mapper.toResponse(donationConv, 10L, "Fundación Niños").participantDisplayName())
                .isEqualTo("Fundación Niños");
        assertThat(mapper.toResponse(donationConv, 10L, null).participantDisplayName())
                .isEqualTo("Fundación receptora");

        // Foundation views -> donor name or fallback
        assertThat(mapper.toResponse(donationConv, 50L, "Andrés G.").participantDisplayName())
                .isEqualTo("Andrés G.");
        assertThat(mapper.toResponse(donationConv, 50L, null).participantDisplayName())
                .isEqualTo("Persona que dona");
    }

    @Test
    void batchMappingResolvesCounterparts() {
        Conversation c1 = new Conversation(7L, 10L, "Prenda", 1L, 2L, ConversationStatus.OPEN, NOW);
        Conversation c2 = new Conversation(8L, 11L, "Pantalón", 1L, 3L, ConversationStatus.OPEN, NOW);

        List<ConversationResponse> responses = mapper.toResponse(List.of(c1, c2), 1L, Map.of(2L, "Sofía L.", 3L, "Mario R."));
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).participantDisplayName()).isEqualTo("Sofía L.");
        assertThat(responses.get(1).participantDisplayName()).isEqualTo("Mario R.");
    }
}
