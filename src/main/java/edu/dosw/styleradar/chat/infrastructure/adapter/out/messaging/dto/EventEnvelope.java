package edu.dosw.styleradar.chat.infrastructure.adapter.out.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

/** Common envelope of every event on the bus; only the data changes between events. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EventEnvelope<T>(String eventId, String eventType, int version, Instant timestamp, String producer,
                               String correlationId, T data) {
}
