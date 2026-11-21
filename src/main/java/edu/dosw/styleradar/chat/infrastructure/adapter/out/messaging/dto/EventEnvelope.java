package edu.dosw.styleradar.chat.infrastructure.adapter.out.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/** Common envelope of every event on the bus; only the data changes between events. */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EventEnvelope<T>(String eventId, String eventType, int version, Instant timestamp, String producer,
                               String correlationId, T data) {

    @JsonProperty("eventoId")
    public String eventoId() {
        return eventId;
    }

    @JsonProperty("mensajeId")
    public Long mensajeId() {
        return data instanceof MessageSentData m ? m.messageId() : null;
    }

    @JsonProperty("conversacionId")
    public Long conversacionId() {
        return data instanceof MessageSentData m ? m.conversationId() : null;
    }

    @JsonProperty("remitenteId")
    public Long remitenteId() {
        return data instanceof MessageSentData m ? m.authorId() : null;
    }

    @JsonProperty("nombreRemitente")
    public String nombreRemitente() {
        return data instanceof MessageSentData m && m.authorId() != null ? "Usuario " + m.authorId() : null;
    }

    @JsonProperty("destinatarioId")
    public Long destinatarioId() {
        return data instanceof MessageSentData m ? m.recipientId() : null;
    }

    @JsonProperty("resumenContenido")
    public String resumenContenido() {
        return data instanceof MessageSentData m ? m.preview() : null;
    }

    @JsonProperty("fechaEnvio")
    public Instant fechaEnvio() {
        return data instanceof MessageSentData m ? m.sentAt() : null;
    }
}
