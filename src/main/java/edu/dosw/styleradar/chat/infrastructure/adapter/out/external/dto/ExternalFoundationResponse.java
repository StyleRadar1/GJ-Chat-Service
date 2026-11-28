package edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** DTO for foundation details queried from ms-tiendas. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalFoundationResponse(
        Long id,
        String nombre,
        Long representanteId
) {
}
