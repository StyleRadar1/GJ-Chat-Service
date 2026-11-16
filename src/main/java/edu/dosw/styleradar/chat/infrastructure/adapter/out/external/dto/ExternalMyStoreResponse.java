package edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Representación mínima de la respuesta de mi-almacen de ms-tiendas (solo NIT y nombre comercial). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalMyStoreResponse(
        String nit,
        String nombreComercial
) {
}
