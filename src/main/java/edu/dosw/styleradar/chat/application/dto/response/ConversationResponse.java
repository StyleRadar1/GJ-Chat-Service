package edu.dosw.styleradar.chat.application.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** REST view of a conversation; supports both D1 English names and legacy Spanish names. */
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Conversation between participants (second-hand, partner store, or donation)")
public record ConversationResponse(
        @Schema(example = "7") Long id,
        @Schema(example = "STORE", description = "Conversation type: SECOND_HAND, STORE, or DONATION")
        @JsonProperty("type") String type,
        @Schema(example = "10", description = "Second-hand publication associated with the conversation")
        @JsonProperty("publicationId") Long listingId,
        @Schema(example = "Chaqueta de jean")
        @JsonProperty("garmentName") String garmentName,
        @Schema(example = "1")
        @JsonProperty("buyerId") Long buyerId,
        @Schema(example = "2")
        @JsonProperty("sellerId") Long sellerId,
        @Schema(example = "900123456-1")
        @JsonProperty("storeNit") String storeNit,
        @Schema(example = "Atelier Urbano Chapinero")
        @JsonProperty("storeName") String storeName,
        @Schema(example = "5")
        @JsonProperty("donationId") Long donationId,
        @Schema(example = "OPEN", description = "Conversation status: OPEN / ABIERTA, or CLOSED / CERRADA")
        @JsonProperty("status") String status,
        @Schema(example = "2026-11-01T15:00:00Z")
        @JsonProperty("createdAt") Instant createdAt) {

    /** Backwards-compatible 7-argument constructor. */
    public ConversationResponse(Long id, Long listingId, String garmentName, Long buyerId, Long sellerId,
                                String status, Instant createdAt) {
        this(id, "SEGUNDA_MANO", listingId, garmentName, buyerId, sellerId, null, null, null, status, createdAt);
    }

    // --- English alias ---
    @JsonProperty(value = "postId", access = JsonProperty.Access.READ_ONLY)
    public Long postId() {
        return listingId;
    }

    // --- Legacy Spanish aliases for full backwards compatibility (serialization only) ---
    @JsonProperty(value = "tipo", access = JsonProperty.Access.READ_ONLY)
    public String tipo() {
        return type;
    }

    @JsonProperty(value = "publicacionId", access = JsonProperty.Access.READ_ONLY)
    public Long publicacionId() {
        return listingId;
    }

    @JsonProperty(value = "prendaNombre", access = JsonProperty.Access.READ_ONLY)
    public String prendaNombre() {
        return garmentName;
    }

    @JsonProperty(value = "compradorId", access = JsonProperty.Access.READ_ONLY)
    public Long compradorId() {
        return buyerId;
    }

    @JsonProperty(value = "vendedorId", access = JsonProperty.Access.READ_ONLY)
    public Long vendedorId() {
        return sellerId;
    }

    @JsonProperty(value = "almacenNit", access = JsonProperty.Access.READ_ONLY)
    public String almacenNit() {
        return storeNit;
    }

    @JsonProperty(value = "almacenNombre", access = JsonProperty.Access.READ_ONLY)
    public String almacenNombre() {
        return storeName;
    }

    @JsonProperty(value = "donacionId", access = JsonProperty.Access.READ_ONLY)
    public Long donacionId() {
        return donationId;
    }

    @JsonProperty(value = "estado", access = JsonProperty.Access.READ_ONLY)
    public String estado() {
        return status;
    }

    @JsonProperty(value = "fechaCreacion", access = JsonProperty.Access.READ_ONLY)
    public Instant fechaCreacion() {
        return createdAt;
    }
}
