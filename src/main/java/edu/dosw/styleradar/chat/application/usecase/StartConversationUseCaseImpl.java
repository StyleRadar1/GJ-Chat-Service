package edu.dosw.styleradar.chat.application.usecase;

import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.ListingSummary;
import edu.dosw.styleradar.chat.domain.port.in.StartConversationUseCase;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.ListingQueryPort;
import edu.dosw.styleradar.chat.domain.port.in.SendMessageUseCase;
import edu.dosw.styleradar.chat.domain.model.MessageText;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/** Validates the listing and opens the conversation, or returns the existing one. */
public class StartConversationUseCaseImpl implements StartConversationUseCase {
    private final ConversationRepositoryPort conversations;
    private final ListingQueryPort listings;
    private final Clock clock;
    private final SendMessageUseCase sendMessage;

    public StartConversationUseCaseImpl(ConversationRepositoryPort conversations,
                                          ListingQueryPort listings, Clock clock, SendMessageUseCase sendMessage) {
        this.conversations = conversations;
        this.listings = listings;
        this.clock = clock;
        this.sendMessage = sendMessage;
    }

    public StartConversationUseCaseImpl(ConversationRepositoryPort conversations,
                                        ListingQueryPort listings, Clock clock) {
        this(conversations, listings, clock, (userId, conversationId, text) -> null);
    }

    @Override
    @Transactional
    public Conversation start(Long buyerId, Long listingId, MessageText initialMessage) {
        var existing = conversations.findByListingAndBuyer(listingId, buyerId);
        if (existing.isPresent()) {
            return existing.get();
        }
        ListingSummary listing = listings.fetch(listingId);
        Conversation created = Conversation.start(listing, buyerId, clock.instant());
        Conversation saved = conversations.save(created);
        if (initialMessage != null) {
            sendMessage.send(buyerId, saved.getId(), initialMessage);
        }
        return saved;
    }
}
