package edu.dosw.styleradar.chat.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Mensaje enviado en una conversación")
public record MensajeResponse(
        @Schema(example = "20") Long id,
        @Schema(example = "7") Long conversacionId,
        @Schema(example = "1") Long autorId,
        @Schema(example = "¿Sigue disponible? Podría recogerla el sábado en Chapinero.") String texto,
        @Schema(example = "2026-11-01T15:05:00Z") Instant fechaEnvio) {
}
