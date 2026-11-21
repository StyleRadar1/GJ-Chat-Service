package edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Minimal representation of ms-tiendas VerificacionAlmacenResponseDTO. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalMyStoreResponse(
        String nit,
        @JsonProperty("tradeName") String nombreComercial,
        @JsonProperty("verificationStatus") String estadoVerificacion,
        @JsonProperty("legacyUnverified") boolean legadoSinVerificacion
) {
    public ExternalMyStoreResponse(String nit, String nombreComercial) {
        this(nit, nombreComercial, "APPROVED", false);
    }
}
