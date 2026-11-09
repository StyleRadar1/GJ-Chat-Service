package edu.dosw.styleradar.chat.domain.port.out;

import edu.dosw.styleradar.chat.domain.model.ListingSummary;

/** Output port to query a second-hand listing. */
public interface ListingQueryPort {
    ListingSummary fetch(Long listingId);
}
