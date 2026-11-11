package edu.dosw.styleradar.chat.application.usecase;

import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.port.in.CloseConversationsOnSaleUseCase;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.ProcessedEventPort;

/** Closes the open conversations of a sold listing except the one with the buyer who got the item; ignores redelivered events. */
public class CloseConversationsOnSaleUseCaseImpl implements CloseConversationsOnSaleUseCase {
    private final ConversationRepositoryPort conversations;
    private final ProcessedEventPort processedEvents;

    public CloseConversationsOnSaleUseCaseImpl(ConversationRepositoryPort conversations,
                                               ProcessedEventPort processedEvents) {
        this.conversations = conversations;
        this.processedEvents = processedEvents;
    }

    @Override
    public void execute(String eventId, Long listingId, Long buyerId) {
        if (processedEvents.isProcessed(eventId)) {
            return;
        }
        for (Conversation conversation : conversations.findOpenByListing(listingId)) {
            if (!conversation.getBuyerId().equals(buyerId)) {
                conversation.close();
                conversations.save(conversation);
            }
        }
        processedEvents.markProcessed(eventId);
    }
}
