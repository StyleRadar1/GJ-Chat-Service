package edu.dosw.styleradar.chat.domain.model;

import java.time.Instant;
import java.util.Objects;

/** Message written by a participant of a conversation. */
public final class Message {
    private final Long id;
    private final Long conversationId;
    private final Long authorId;
    private final MessageText text;
    private final Instant sentAt;

    public Message(Long id, Long conversationId, Long authorId, MessageText text, Instant sentAt) {
        this.id = id;
        this.conversationId = Objects.requireNonNull(conversationId, "La conversación es obligatoria");
        this.authorId = Objects.requireNonNull(authorId, "El autor es obligatorio");
        this.text = Objects.requireNonNull(text, "El texto es obligatorio");
        this.sentAt = Objects.requireNonNull(sentAt, "La fecha de envío es obligatoria");
    }

    public Long getId() {
        return id;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public MessageText getText() {
        return text;
    }

    public Instant getSentAt() {
        return sentAt;
    }
}
