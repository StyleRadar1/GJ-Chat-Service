package edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Minimal representation of ms-tiendas AlmacenResponseDTO. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalStoreResponse(
        String nit,
        @JsonProperty("tradeName") String nombreComercial,
        @JsonProperty("description") String descripcion,
        @JsonProperty("rating") Double reputacion
) {
}
