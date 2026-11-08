package edu.dosw.styleradar.chat.domain.model;

import java.util.Objects;

/** Minimal listing data the chat needs to open and keep a conversation. */
public record ListingSummary(Long id, Long sellerId, String garmentName, ListingStatus status) {
    public ListingSummary {
        Objects.requireNonNull(id, "El id de la publicación es obligatorio");
        Objects.requireNonNull(sellerId, "El vendedor de la publicación es obligatorio");
        Objects.requireNonNull(status, "El estado de la publicación es obligatorio");
    }

    public boolean isAvailable() {
        return status == ListingStatus.AVAILABLE;
    }

    public boolean wasWithdrawn() {
        return status == ListingStatus.WITHDRAWN;
    }
}
