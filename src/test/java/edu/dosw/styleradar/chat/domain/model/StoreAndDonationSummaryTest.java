package edu.dosw.styleradar.chat.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StoreAndDonationSummaryTest {
    @Test
    void storeSummaryAvailability() {
        StoreSummary verified = new StoreSummary("900123-1", "Tienda Moda", true);
        assertThat(verified.isAvailable()).isTrue();
        assertThat(verified.nit()).isEqualTo("900123-1");
        assertThat(verified.tradeName()).isEqualTo("Tienda Moda");
        assertThat(verified.verified()).isTrue();

        StoreSummary unverified = new StoreSummary("900123-1", "Tienda Moda", false);
        assertThat(unverified.isAvailable()).isFalse();
    }

    @Test
    void donationSummaryAvailability() {
        DonationSummary pending = new DonationSummary(10L, 1L, 2L, "PENDING");
        assertThat(pending.isAvailable()).isTrue();
        assertThat(pending.id()).isEqualTo(10L);
        assertThat(pending.donorId()).isEqualTo(1L);
        assertThat(pending.foundationId()).isEqualTo(2L);
        assertThat(pending.status()).isEqualTo("PENDING");

        DonationSummary approved = new DonationSummary(10L, 1L, 2L, "approved");
        assertThat(approved.isAvailable()).isTrue();

        DonationSummary pickup = new DonationSummary(10L, 1L, 2L, "IN_PICKUP");
        assertThat(pickup.isAvailable()).isTrue();

        DonationSummary rejected = new DonationSummary(10L, 1L, 2L, "REJECTED");
        assertThat(rejected.isAvailable()).isFalse();

        DonationSummary nullStatus = new DonationSummary(10L, 1L, 2L, null);
        assertThat(nullStatus.isAvailable()).isFalse();
    }
}
