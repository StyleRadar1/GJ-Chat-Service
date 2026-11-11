package edu.dosw.styleradar.chat.application.usecase;

import edu.dosw.styleradar.chat.domain.event.MessageSentEvent;
import edu.dosw.styleradar.chat.domain.exception.ConversationNotFoundException;
import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.Message;
import edu.dosw.styleradar.chat.domain.model.MessageText;
import edu.dosw.styleradar.chat.domain.port.in.SendMessageUseCase;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.EventPublisherPort;
import edu.dosw.styleradar.chat.domain.port.out.MessageRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.ListingQueryPort;

import java.time.Clock;

/** Sends a message, closing the conversation first if its listing was withdrawn. */
public class SendMessageUseCaseImpl implements SendMessageUseCase {
    private static final int PREVIEW_LENGTH = 80;

    private final ConversationRepositoryPort conversations;
    private final MessageRepositoryPort messages;
    private final ListingQueryPort listings;
    private final EventPublisherPort events;
    private final Clock clock;

    public SendMessageUseCaseImpl(ConversationRepositoryPort conversations, MessageRepositoryPort messages,
                                    ListingQueryPort listings, EventPublisherPort events, Clock clock) {
        this.conversations = conversations;
        this.messages = messages;
        this.listings = listings;
        this.events = events;
        this.clock = clock;
    }

    private static String preview(MessageText text) {
        String value = text.value();
        return value.length() <= PREVIEW_LENGTH ? value : value.substring(0, PREVIEW_LENGTH);
    }

    @Override
    public Message send(Long userId, Long conversationId, MessageText text) {
        Conversation conversation = conversations.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));
        conversation.verifyParticipant(userId);
        if (conversation.isOpen() && listings.fetch(conversation.getListingId()).wasWithdrawn()) {
            conversation.close();
            conversations.save(conversation);
        }
        Message message = conversation.write(userId, text, clock.instant());
        Message saved = messages.save(message);
        events.publish(new MessageSentEvent(saved.getId(), conversationId, conversation.getListingId(),
                conversation.getGarmentName(), userId, conversation.recipientOf(userId), preview(text),
                saved.getSentAt()));
        return saved;
    }
}
