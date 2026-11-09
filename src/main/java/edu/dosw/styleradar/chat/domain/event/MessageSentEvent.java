package edu.dosw.styleradar.chat.domain.event;

import java.time.Instant;

/** Fact published after a message is stored, so other services can notify the recipient. */
public record MessageSentEvent(Long messageId, Long conversationId, Long listingId, String garmentName,
                               Long authorId, Long recipientId, String preview, Instant sentAt) {
}
