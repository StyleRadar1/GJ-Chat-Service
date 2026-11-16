package edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Minimal representation of DonationRequestResponseDTO from ms-donaciones. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalDonationResponse(
        Long id,
        Long publicationId,
        Long donorId,
        Long foundationId,
        String message,
        String status
) {
}
