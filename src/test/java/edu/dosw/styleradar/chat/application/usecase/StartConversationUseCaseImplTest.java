package edu.dosw.styleradar.chat.application.usecase;

import edu.dosw.styleradar.chat.domain.exception.SelfChatException;
import edu.dosw.styleradar.chat.domain.exception.ListingUnavailableException;
import edu.dosw.styleradar.chat.domain.exception.ListingNotFoundException;
import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import edu.dosw.styleradar.chat.domain.model.ListingStatus;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.ListingQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Optional;

import static edu.dosw.styleradar.chat.support.TestData.NOW;
import static edu.dosw.styleradar.chat.support.TestData.BUYER;
import static edu.dosw.styleradar.chat.support.TestData.LISTING;
import static edu.dosw.styleradar.chat.support.TestData.SELLER;
import static edu.dosw.styleradar.chat.support.TestData.conversation;
import static edu.dosw.styleradar.chat.support.TestData.listing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Tests for starting conversations. */
@ExtendWith(MockitoExtension.class)
class StartConversationUseCaseImplTest {
    @Mock
    private ConversationRepositoryPort conversations;
    @Mock
    private ListingQueryPort listings;
    private StartConversationUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new StartConversationUseCaseImpl(conversations, listings, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsTheConversationIfMissing() {
        when(listings.fetch(LISTING)).thenReturn(listing(ListingStatus.AVAILABLE));
        when(conversations.findByListingAndBuyer(LISTING, BUYER)).thenReturn(Optional.empty());
        when(conversations.save(any())).thenReturn(conversation(7L, ConversationStatus.OPEN));

        Conversation result = useCase.start(BUYER, LISTING);

        ArgumentCaptor<Conversation> saved = ArgumentCaptor.forClass(Conversation.class);
        verify(conversations).save(saved.capture());
        assertThat(saved.getValue().getSellerId()).isEqualTo(SELLER);
        assertThat(saved.getValue().getCreatedAt()).isEqualTo(NOW);
        assertThat(result.getId()).isEqualTo(7L);
    }

    @Test
    void returnsExistingConversationWithoutCreatingAnother() {
        when(listings.fetch(LISTING)).thenReturn(listing(ListingStatus.AVAILABLE));
        when(conversations.findByListingAndBuyer(LISTING, BUYER))
                .thenReturn(Optional.of(conversation(7L, ConversationStatus.OPEN)));

        assertThat(useCase.start(BUYER, LISTING).getId()).isEqualTo(7L);
        verify(conversations, never()).save(any());
    }

    @Test
    void doesNotStartIfListingIsNotAvailable() {
        when(listings.fetch(LISTING)).thenReturn(listing(ListingStatus.SOLD));

        assertThatThrownBy(() -> useCase.start(BUYER, LISTING))
                .isInstanceOf(ListingUnavailableException.class);
        verify(conversations, never()).save(any());
    }

    @Test
    void doesNotStartOnOwnListing() {
        when(listings.fetch(LISTING)).thenReturn(listing(ListingStatus.AVAILABLE));

        assertThatThrownBy(() -> useCase.start(SELLER, LISTING)).isInstanceOf(SelfChatException.class);
    }

    @Test
    void propagatesListingNotFound() {
        when(listings.fetch(LISTING)).thenThrow(new ListingNotFoundException("no existe"));

        assertThatThrownBy(() -> useCase.start(BUYER, LISTING))
                .isInstanceOf(ListingNotFoundException.class);
    }
}
