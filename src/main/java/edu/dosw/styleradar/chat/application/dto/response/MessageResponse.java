package edu.dosw.styleradar.chat.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** REST view of a message; JSON names keep the monolith contract. */
@Schema(description = "Mensaje enviado en una conversación")
public record MessageResponse(
        @Schema(example = "20") Long id,
        @Schema(example = "7") @JsonProperty("conversacionId") Long conversationId,
        @Schema(example = "1") @JsonProperty("autorId") Long authorId,
        @Schema(example = "¿Sigue disponible? Podría recogerla el sábado en Chapinero.") @JsonProperty("texto") String text,
        @Schema(example = "2026-11-01T15:05:00Z") @JsonProperty("fechaEnvio") Instant sentAt) {
}
