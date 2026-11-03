package edu.dosw.styleradar.chat.application.dto.response;

import edu.dosw.styleradar.chat.domain.model.EstadoConversacion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** Mismos campos que el monolito, más el estado para que el cliente sepa si puede escribir. */
@Schema(description = "Conversación entre el comprador y el vendedor de una publicación de segunda mano")
public record ConversacionResponse(
        @Schema(example = "7") Long id,
        @Schema(example = "10", description = "Publicación de segunda mano sobre la que se conversa") Long publicacionId,
        @Schema(example = "Chaqueta de jean") String prendaNombre,
        @Schema(example = "1") Long compradorId,
        @Schema(example = "2") Long vendedorId,
        @Schema(example = "ABIERTA", description = "CERRADA si la publicación fue retirada") EstadoConversacion estado,
        @Schema(example = "2026-11-01T15:00:00Z") Instant fechaCreacion) {
}
