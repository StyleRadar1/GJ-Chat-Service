package edu.dosw.styleradar.chat.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body to send a message in a conversation. */
@Schema(description = "Mensaje que un participante envía en la conversación")
public record SendMessageRequest(
        @Schema(description = "Texto del mensaje (1 a 1000 caracteres)",
                example = "¿Sigue disponible? Podría recogerla el sábado en Chapinero.")
        @NotBlank(message = "El mensaje no puede estar vacío")
        @Size(max = 1000, message = "El mensaje no puede superar 1000 caracteres")
        @JsonProperty("texto") String text) {
}
