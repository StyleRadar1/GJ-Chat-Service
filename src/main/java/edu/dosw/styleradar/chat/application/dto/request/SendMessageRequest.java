package edu.dosw.styleradar.chat.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body to send a message in a conversation. */
@Schema(description = "Message sent by a participant in the conversation")
public record SendMessageRequest(
        @Schema(description = "Message text (1 to 1000 characters)",
                example = "Is this still available? Could I pick it up on Saturday?")
        @NotBlank(message = "El mensaje no puede estar vacío")
        @Size(max = 1000, message = "El mensaje no puede superar 1000 caracteres")
        @JsonProperty("texto") String text) {
}
