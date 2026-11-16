package edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Representación mínima de AlmacenResponseDTO de ms-tiendas. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalStoreResponse(
        String nit,
        String nombreComercial,
        String descripcion,
        Double reputacion
) {
}
