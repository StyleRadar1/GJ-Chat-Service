package edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Internal representation of a user's public profile from ms-usuarios. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PublicProfileResponse(Long id, String displayName) {
}
