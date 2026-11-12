package edu.dosw.styleradar.chat.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** STOMP payload used to send a message in real time. */
public record RealtimeMessageRequest(
        @NotBlank(message = "El mensaje no puede estar vacío")
        @Size(max = 1000, message = "El mensaje no puede superar 1000 caracteres")
        String text) {
}
