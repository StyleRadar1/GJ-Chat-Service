package edu.dosw.styleradar.chat.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** REST view of a conversation; JSON names keep the monolith contract. */
@Schema(description = "Conversación entre participantes (segunda mano, almacén aliado o donación)")
public record ConversationResponse(
        @Schema(example = "7") Long id,
        @Schema(example = "SEGUNDA_MANO", description = "Tipo de conversación: SEGUNDA_MANO, ALMACEN o DONACION") @JsonProperty("tipo") String type,
        @Schema(example = "10", description = "Publicación de segunda mano sobre la que se conversa") @JsonProperty("publicacionId") Long listingId,
        @Schema(example = "Chaqueta de jean") @JsonProperty("prendaNombre") String garmentName,
        @Schema(example = "1") @JsonProperty("compradorId") Long buyerId,
        @Schema(example = "2") @JsonProperty("vendedorId") Long sellerId,
        @Schema(example = "900123456-1") @JsonProperty("almacenNit") String storeNit,
        @Schema(example = "Atelier Urbano Chapinero") @JsonProperty("almacenNombre") String storeName,
        @Schema(example = "5") @JsonProperty("donacionId") Long donationId,
        @Schema(example = "ABIERTA", description = "CERRADA si la publicación fue retirada o finalizada") @JsonProperty("estado") String status,
        @Schema(example = "2026-11-01T15:00:00Z") @JsonProperty("fechaCreacion") Instant createdAt) {

    /** Backwards-compatible 7-argument constructor. */
    public ConversationResponse(Long id, Long listingId, String garmentName, Long buyerId, Long sellerId,
                                String status, Instant createdAt) {
        this(id, "SEGUNDA_MANO", listingId, garmentName, buyerId, sellerId, null, null, null, status, createdAt);
    }
}
