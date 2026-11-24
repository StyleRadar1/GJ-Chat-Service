package edu.dosw.styleradar.chat.application.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** REST view of a message; supports both D1 English names and legacy Spanish names. */
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Message sent in a conversation")
public record MessageResponse(
        @Schema(example = "20") Long id,
        @Schema(example = "7") @JsonProperty("conversationId") Long conversationId,
        @Schema(example = "1") @JsonProperty("authorId") Long authorId,
        @Schema(example = "Is this still available? Could I pick it up on Saturday?") @JsonProperty("text") String text,
        @Schema(example = "2026-11-01T15:05:00Z") @JsonProperty("sentAt") Instant sentAt) {

    // --- Legacy Spanish aliases for full backwards compatibility (serialization only) ---
    @JsonProperty(value = "conversacionId", access = JsonProperty.Access.READ_ONLY)
    public Long conversacionId() {
        return conversationId;
    }

    @JsonProperty(value = "autorId", access = JsonProperty.Access.READ_ONLY)
    public Long autorId() {
        return authorId;
    }

    @JsonProperty(value = "texto", access = JsonProperty.Access.READ_ONLY)
    public String texto() {
        return text;
    }

    @JsonProperty(value = "fechaEnvio", access = JsonProperty.Access.READ_ONLY)
    public Instant fechaEnvio() {
        return sentAt;
    }
}
