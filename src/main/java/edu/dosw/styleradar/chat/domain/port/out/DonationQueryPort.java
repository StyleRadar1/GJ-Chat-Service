package edu.dosw.styleradar.chat.domain.port.out;

import edu.dosw.styleradar.chat.domain.model.DonationSummary;

/** Output port to query donation request information from ms-donaciones. */
public interface DonationQueryPort {
    DonationSummary fetch(Long donationId);
}
