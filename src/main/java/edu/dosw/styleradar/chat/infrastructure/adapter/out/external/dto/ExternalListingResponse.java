package edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Subset of the second-hand listing payload that the chat needs. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalListingResponse(Long id, @JsonProperty("usuarioId") Long userId,
                                      @JsonProperty("prenda") Garment garment,
                                      @JsonProperty("estado") String status) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Garment(@JsonProperty("nombre") String name) {
    }
}
