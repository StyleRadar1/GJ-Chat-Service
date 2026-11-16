package edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Minimal representation of VerificacionAlmacenResponseDTO from ms-tiendas. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalMyStoreResponse(
        String nit,
        String nombreComercial,
        String estadoVerificacion
) {
}
