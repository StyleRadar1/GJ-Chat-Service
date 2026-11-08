package edu.dosw.styleradar.chat.domain.model;

import org.junit.jupiter.api.Test;

import static edu.dosw.styleradar.chat.support.TestData.listing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Tests for the listing summary value object. */
class ListingSummaryTest {
    @Test
    void onlyAvailableIsAvailable() {
        assertThat(listing(ListingStatus.AVAILABLE).isAvailable()).isTrue();
        assertThat(listing(ListingStatus.SOLD).isAvailable()).isFalse();
    }

    @Test
    void recognizesAWithdrawnListing() {
        assertThat(listing(ListingStatus.WITHDRAWN).wasWithdrawn()).isTrue();
        assertThat(listing(ListingStatus.DONATED).wasWithdrawn()).isFalse();
    }

    @Test
    void requiresIdSellerAndStatus() {
        assertThatThrownBy(() -> new ListingSummary(null, 2L, "x", ListingStatus.AVAILABLE))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ListingSummary(1L, null, "x", ListingStatus.AVAILABLE))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ListingSummary(1L, 2L, "x", null))
                .isInstanceOf(NullPointerException.class);
    }
}
