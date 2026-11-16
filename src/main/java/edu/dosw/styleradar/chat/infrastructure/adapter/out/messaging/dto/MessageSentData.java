package edu.dosw.styleradar.chat.infrastructure.adapter.out.messaging.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/** Data of the styleradar.chat.mensaje.enviado event; JSON names follow the event contract. */
public record MessageSentData(@JsonProperty("mensajeId") Long messageId,
                              @JsonProperty("conversacionId") Long conversationId,
                              @JsonProperty("publicacionId") Long listingId,
                              @JsonProperty("prendaNombre") String garmentName,
                              @JsonProperty("autorId") Long authorId,
                              @JsonProperty("destinatarioId") Long recipientId,
                              @JsonProperty("vistaPrevia") String preview,
                              @JsonProperty("fechaEnvio") Instant sentAt) {
}
