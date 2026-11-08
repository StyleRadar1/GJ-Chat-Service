package edu.dosw.styleradar.chat.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** REST view of a conversation; JSON names keep the monolith contract. */
@Schema(description = "Conversación entre el comprador y el vendedor de una publicación de segunda mano")
public record ConversationResponse(
        @Schema(example = "7") Long id,
        @Schema(example = "10", description = "Publicación de segunda mano sobre la que se conversa") @JsonProperty("publicacionId") Long listingId,
        @Schema(example = "Chaqueta de jean") @JsonProperty("prendaNombre") String garmentName,
        @Schema(example = "1") @JsonProperty("compradorId") Long buyerId,
        @Schema(example = "2") @JsonProperty("vendedorId") Long sellerId,
        @Schema(example = "ABIERTA", description = "CERRADA si la publicación fue retirada") @JsonProperty("estado") String status,
        @Schema(example = "2026-11-01T15:00:00Z") @JsonProperty("fechaCreacion") Instant createdAt) {
}
