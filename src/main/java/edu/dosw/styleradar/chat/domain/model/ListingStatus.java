package edu.dosw.styleradar.chat.domain.model;

/** Listing states published by the listings service; the code is the value on the wire. */
public enum ListingStatus {
    AVAILABLE("DISPONIBLE"),
    SOLD("VENDIDA"),
    WITHDRAWN("RETIRADA"),
    IN_DONATION("EN_DONACION"),
    DONATED("DONADA");

    private final String code;

    ListingStatus(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static ListingStatus fromCode(String code) {
        for (ListingStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown listing status: " + code);
    }
}
