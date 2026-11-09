package edu.dosw.styleradar.chat.support;

import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import edu.dosw.styleradar.chat.domain.model.ListingStatus;
import edu.dosw.styleradar.chat.domain.model.Message;
import edu.dosw.styleradar.chat.domain.model.ListingSummary;
import edu.dosw.styleradar.chat.domain.model.MessageText;

import java.time.Instant;

/** Shared fixtures for the tests. */
public final class TestData {
    public static final Instant NOW = Instant.parse("2026-11-01T15:00:00Z");
    public static final Long LISTING = 10L;
    public static final Long BUYER = 1L;
    public static final Long SELLER = 2L;
    public static final Long OUTSIDER = 3L;

    private TestData() {
    }

    public static ListingSummary listing(ListingStatus status) {
        return new ListingSummary(LISTING, SELLER, "Chaqueta de jean", status);
    }

    public static Conversation conversation(Long id, ConversationStatus status) {
        return new Conversation(id, LISTING, "Chaqueta de jean", BUYER, SELLER, status, NOW);
    }

    public static Message message(Long id, Long conversationId, Long author, String text) {
        return new Message(id, conversationId, author, new MessageText(text), NOW);
    }
}
