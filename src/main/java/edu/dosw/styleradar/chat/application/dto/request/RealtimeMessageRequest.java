package edu.dosw.styleradar.chat.application.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** STOMP payload used to send a message in real time (supports both 'text' and 'texto'). */
public record RealtimeMessageRequest(
        @NotBlank(message = "El mensaje no puede estar vacío")
        @Size(max = 1000, message = "El mensaje no puede superar 1000 caracteres")
        @JsonProperty("text")
        @JsonAlias({"texto"})
        String text) {
}
