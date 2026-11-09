package edu.dosw.styleradar.chat.application.usecase;

import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.ListingSummary;
import edu.dosw.styleradar.chat.domain.port.in.StartConversationUseCase;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.ListingQueryPort;

import java.time.Clock;

/** Validates the listing and opens the conversation, or returns the existing one. */
public class StartConversationUseCaseImpl implements StartConversationUseCase {
    private final ConversationRepositoryPort conversations;
    private final ListingQueryPort listings;
    private final Clock clock;

    public StartConversationUseCaseImpl(ConversationRepositoryPort conversations,
                                          ListingQueryPort listings, Clock clock) {
        this.conversations = conversations;
        this.listings = listings;
        this.clock = clock;
    }

    @Override
    public Conversation start(Long buyerId, Long listingId) {
        ListingSummary listing = listings.fetch(listingId);
        Conversation created = Conversation.start(listing, buyerId, clock.instant());
        return conversations.findByListingAndBuyer(listingId, buyerId)
                .orElseGet(() -> conversations.save(created));
    }
}
