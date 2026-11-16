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
        return switch (code) {
            case "DISPONIBLE", "AVAILABLE" -> AVAILABLE;
            case "VENDIDA", "SOLD" -> SOLD;
            case "RETIRADA", "RETIRED", "WITHDRAWN" -> WITHDRAWN;
            case "EN_DONACION", "IN_DONATION" -> IN_DONATION;
            case "DONADA", "DONATED" -> DONATED;
            default -> throw new IllegalArgumentException("Unknown listing status: " + code);
        };
    }
}
