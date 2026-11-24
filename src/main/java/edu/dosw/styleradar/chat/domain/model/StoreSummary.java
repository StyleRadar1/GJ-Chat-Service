package edu.dosw.styleradar.chat.domain.model;

import java.util.Objects;

/** Summary of an ally store retrieved from ms-tiendas (SR-RF-73). */
public record StoreSummary(String nit, String tradeName, boolean verified) {
    public StoreSummary {
        Objects.requireNonNull(nit, "El NIT del almacén es obligatorio");
    }

    public boolean isAvailable() {
        return verified;
    }
}
