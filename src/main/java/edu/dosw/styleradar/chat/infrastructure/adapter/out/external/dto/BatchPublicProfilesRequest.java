package edu.dosw.styleradar.chat.infrastructure.adapter.out.external.dto;

import java.util.List;

/** Request payload for batch querying public user profiles. */
public record BatchPublicProfilesRequest(List<Long> ids) {
}
