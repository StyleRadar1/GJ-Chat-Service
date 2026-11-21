package edu.dosw.styleradar.chat.domain.model;

import java.util.Objects;

/** Summary of a donation request retrieved from ms-donaciones (SR-RF-44 / GJ-57). */
public record DonationSummary(Long id, Long donorId, Long foundationId, String status) {
    public DonationSummary {
        Objects.requireNonNull(id, "El identificador de la donación es obligatorio");
        Objects.requireNonNull(donorId, "El donante es obligatorio");
    }

    public boolean isAvailable() {
        if (status == null) {
            return false;
        }
        String normalized = status.trim().toUpperCase();
        return "PENDING".equals(normalized)
                || "APPROVED".equals(normalized)
                || "IN_PICKUP".equals(normalized);
    }
}
