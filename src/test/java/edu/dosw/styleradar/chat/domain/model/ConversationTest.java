package edu.dosw.styleradar.chat.domain.model;

import edu.dosw.styleradar.chat.domain.exception.SelfChatException;
import edu.dosw.styleradar.chat.domain.exception.ConversationClosedException;
import edu.dosw.styleradar.chat.domain.exception.ConversationNotFoundException;
import edu.dosw.styleradar.chat.domain.exception.ListingUnavailableException;
import edu.dosw.styleradar.chat.domain.exception.StoreUnavailableException;
import edu.dosw.styleradar.chat.domain.exception.DonationUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static edu.dosw.styleradar.chat.support.TestData.NOW;
import static edu.dosw.styleradar.chat.support.TestData.OUTSIDER;
import static edu.dosw.styleradar.chat.support.TestData.BUYER;
import static edu.dosw.styleradar.chat.support.TestData.LISTING;
import static edu.dosw.styleradar.chat.support.TestData.SELLER;
import static edu.dosw.styleradar.chat.support.TestData.conversation;
import static edu.dosw.styleradar.chat.support.TestData.listing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Rules of the Conversation aggregate. */
class ConversationTest {
    @Test
    void startsAnOpenConversationOnAnAvailableListing() {
        Conversation c = Conversation.start(listing(ListingStatus.AVAILABLE), BUYER, NOW);

        assertThat(c.getId()).isNull();
        assertThat(c.getListingId()).isEqualTo(LISTING);
        assertThat(c.getGarmentName()).isEqualTo("Chaqueta de jean");
        assertThat(c.getBuyerId()).isEqualTo(BUYER);
        assertThat(c.getSellerId()).isEqualTo(SELLER);
        assertThat(c.getStatus()).isEqualTo(ConversationStatus.OPEN);
        assertThat(c.getCreatedAt()).isEqualTo(NOW);
        assertThat(c.isOpen()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = ListingStatus.class, names = "AVAILABLE", mode = EnumSource.Mode.EXCLUDE)
    void doesNotStartOnUnavailableListing(ListingStatus status) {
        assertThatThrownBy(() -> Conversation.start(listing(status), BUYER, NOW))
                .isInstanceOf(ListingUnavailableException.class);
    }

    @Test
    void doesNotStartChatWithSelf() {
        assertThatThrownBy(() -> Conversation.start(listing(ListingStatus.AVAILABLE), SELLER, NOW))
                .isInstanceOf(SelfChatException.class);
    }

    @Test
    void buyerAndSellerAreParticipants() {
        Conversation c = conversation(7L, ConversationStatus.OPEN);
        assertThat(c.isParticipant(BUYER)).isTrue();
        assertThat(c.isParticipant(SELLER)).isTrue();
        assertThat(c.isParticipant(OUTSIDER)).isFalse();
    }

    @Test
    void recipientIsTheOtherParticipant() {
        Conversation c = conversation(7L, ConversationStatus.OPEN);
        assertThat(c.recipientOf(BUYER)).isEqualTo(SELLER);
        assertThat(c.recipientOf(SELLER)).isEqualTo(BUYER);
        assertThatThrownBy(() -> c.recipientOf(OUTSIDER)).isInstanceOf(ConversationNotFoundException.class);
    }

    @Test
    void conversationDoesNotExistForOutsider() {
        Conversation c = conversation(7L, ConversationStatus.OPEN);
        assertThatThrownBy(() -> c.verifyParticipant(OUTSIDER))
                .isInstanceOf(ConversationNotFoundException.class)
                .hasMessageContaining("7");
    }

    @Test
    void aParticipantWritesInAnOpenConversation() {
        Conversation c = conversation(7L, ConversationStatus.OPEN);

        Message m = c.write(SELLER, new MessageText("Sí, sigue disponible"), NOW);

        assertThat(m.getId()).isNull();
        assertThat(m.getConversationId()).isEqualTo(7L);
        assertThat(m.getAuthorId()).isEqualTo(SELLER);
        assertThat(m.getText().value()).isEqualTo("Sí, sigue disponible");
    }

    @Test
    void cannotWriteInClosedConversation() {
        Conversation c = conversation(7L, ConversationStatus.OPEN);
        c.close();
        c.close();

        assertThat(c.getStatus()).isEqualTo(ConversationStatus.CLOSED);
        assertThatThrownBy(() -> c.write(BUYER, new MessageText("Hola"), NOW))
                .isInstanceOf(ConversationClosedException.class);
    }

    @Test
    void anOutsiderCannotWrite() {
        Conversation c = conversation(7L, ConversationStatus.CLOSED);
        assertThatThrownBy(() -> c.write(OUTSIDER, new MessageText("Hola"), NOW))
                .isInstanceOf(ConversationNotFoundException.class);
    }

    @Test
    void requiresMandatoryData() {
        assertThatThrownBy(() -> new Conversation(1L, null, "x", BUYER, SELLER, ConversationStatus.OPEN, NOW))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Conversation(1L, LISTING, "x", null, SELLER, ConversationStatus.OPEN, NOW))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Conversation(1L, LISTING, "x", BUYER, null, ConversationStatus.OPEN, NOW))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Conversation(1L, LISTING, "x", BUYER, SELLER, null, NOW))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Conversation(1L, LISTING, "x", BUYER, SELLER, ConversationStatus.OPEN, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Conversation.start(null, BUYER, NOW)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Conversation.start(listing(ListingStatus.AVAILABLE), null, NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void startsStoreConversation() {
        StoreSummary store = new StoreSummary("900123-1", "Tienda Moda", true);
        Conversation c = Conversation.startStore(store, BUYER, null, NOW);

        assertThat(c.getType()).isEqualTo(ConversationType.ALMACEN);
        assertThat(c.getStoreNit()).isEqualTo("900123-1");
        assertThat(c.getBuyerId()).isEqualTo(BUYER);
        assertThat(c.getSellerId()).isNull();
        assertThat(c.recipientOf(BUYER)).isEqualTo(0L);

        c.assignSellerIfUnassigned(2L);
        assertThat(c.getSellerId()).isEqualTo(2L);
        assertThat(c.recipientOf(BUYER)).isEqualTo(2L);
    }

    @Test
    void rejectsStoreConversationWhenStoreNotAvailable() {
        StoreSummary store = new StoreSummary("900123-1", "Tienda Moda", false);
        assertThatThrownBy(() -> Conversation.startStore(store, BUYER, null, NOW))
                .isInstanceOf(StoreUnavailableException.class);
    }

    @Test
    void startsDonationConversation() {
        DonationSummary donation = new DonationSummary(50L, BUYER, 2L, "APPROVED");
        Conversation c = Conversation.startDonation(donation, BUYER, NOW);

        assertThat(c.getType()).isEqualTo(ConversationType.DONACION);
        assertThat(c.getDonationId()).isEqualTo(50L);
        assertThat(c.getBuyerId()).isEqualTo(BUYER);
        assertThat(c.getSellerId()).isEqualTo(2L);
    }

    @Test
    void rejectsDonationConversationWhenCompleted() {
        DonationSummary donation = new DonationSummary(50L, BUYER, 2L, "REJECTED");
        assertThatThrownBy(() -> Conversation.startDonation(donation, BUYER, NOW))
                .isInstanceOf(DonationUnavailableException.class);
    }

    @Test
    void rejectsDonationConversationWhenCallerNotParticipant() {
        DonationSummary donation = new DonationSummary(50L, BUYER, 2L, "APPROVED");
        assertThatThrownBy(() -> Conversation.startDonation(donation, OUTSIDER, NOW))
                .isInstanceOf(edu.dosw.styleradar.chat.domain.exception.AccessDeniedDomainException.class);
    }

    @Test
    void isParticipantReturnsFalseForNull() {
        Conversation c = conversation(7L, ConversationStatus.OPEN);
        assertThat(c.isParticipant(null)).isFalse();
    }

    @Test
    void realtimeAccessToAStoreRequiresTheBuyerOrTheMatchingStoreNit() {
        Conversation c = Conversation.startStore(new StoreSummary("900123-1", "Tienda Moda", true), BUYER, null, NOW);

        assertThat(c.allowsRealtimeAccess(BUYER, false, null)).isTrue();
        c.assignSellerIfUnassigned(SELLER);
        assertThat(c.allowsRealtimeAccess(SELLER, false, null)).isTrue();
        assertThat(c.allowsRealtimeAccess(OUTSIDER, false, null)).isFalse();
        assertThat(c.allowsRealtimeAccess(OUTSIDER, true, "900123-1")).isTrue();
        assertThat(c.allowsRealtimeAccess(OUTSIDER, true, "otro-nit")).isFalse();
        assertThat(c.allowsRealtimeAccess(OUTSIDER, true, null)).isFalse();
        assertThat(c.allowsRealtimeAccess(null, true, "900123-1")).isFalse();
        assertThat(c.belongsToStore("900123-1")).isTrue();
        assertThat(c.belongsToStore(null)).isFalse();
    }

    @Test
    void realtimeAccessOutsideAStoreRequiresParticipation() {
        Conversation c = conversation(7L, ConversationStatus.OPEN);

        assertThat(c.allowsRealtimeAccess(BUYER, false, null)).isTrue();
        assertThat(c.allowsRealtimeAccess(SELLER, false, null)).isTrue();
        assertThat(c.allowsRealtimeAccess(OUTSIDER, true, "900123-1")).isFalse();
        assertThat(c.belongsToStore("900123-1")).isFalse();
    }
}
