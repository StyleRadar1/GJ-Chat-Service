package edu.dosw.styleradar.chat.domain.model;

import edu.dosw.styleradar.chat.domain.exception.SelfChatException;
import edu.dosw.styleradar.chat.domain.exception.ConversationClosedException;
import edu.dosw.styleradar.chat.domain.exception.ConversationNotFoundException;
import edu.dosw.styleradar.chat.domain.exception.ListingUnavailableException;

import java.time.Instant;
import java.util.Objects;

/** Chat between the buyer and the seller of a second-hand listing, with its open/closed lifecycle. */
public final class Conversation {
    private final Long id;
    private final Long listingId;
    private final String garmentName;
    private final Long buyerId;
    private final Long sellerId;
    private final Instant createdAt;
    private ConversationStatus status;

    public Conversation(Long id, Long listingId, String garmentName, Long buyerId, Long sellerId,
                        ConversationStatus status, Instant createdAt) {
        this.id = id;
        this.listingId = Objects.requireNonNull(listingId, "La publicación es obligatoria");
        this.garmentName = garmentName;
        this.buyerId = Objects.requireNonNull(buyerId, "El comprador es obligatorio");
        this.sellerId = Objects.requireNonNull(sellerId, "El vendedor es obligatorio");
        this.status = Objects.requireNonNull(status, "El estado es obligatorio");
        this.createdAt = Objects.requireNonNull(createdAt, "La fecha de creación es obligatoria");
    }

    public static Conversation start(ListingSummary listing, Long buyerId, Instant now) {
        Objects.requireNonNull(listing, "La publicación es obligatoria");
        Objects.requireNonNull(buyerId, "El comprador es obligatorio");
        if (!listing.isAvailable()) {
            throw new ListingUnavailableException("Solo se puede iniciar un chat sobre una publicación disponible");
        }
        if (listing.sellerId().equals(buyerId)) {
            throw new SelfChatException("No puede iniciar un chat sobre su propia publicación");
        }
        return new Conversation(null, listing.id(), listing.garmentName(), buyerId,
                listing.sellerId(), ConversationStatus.OPEN, now);
    }

    public boolean isParticipant(Long userId) {
        return buyerId.equals(userId) || sellerId.equals(userId);
    }

    public void verifyParticipant(Long userId) {
        if (!isParticipant(userId)) {
            throw new ConversationNotFoundException(id);
        }
    }

    public Long recipientOf(Long authorId) {
        verifyParticipant(authorId);
        return buyerId.equals(authorId) ? sellerId : buyerId;
    }

    public boolean isOpen() {
        return status == ConversationStatus.OPEN;
    }

    public void close() {
        status = ConversationStatus.CLOSED;
    }

    public Message write(Long authorId, MessageText text, Instant now) {
        verifyParticipant(authorId);
        if (!isOpen()) {
            throw new ConversationClosedException("La publicación fue retirada; la conversación está cerrada");
        }
        return new Message(null, id, authorId, text, now);
    }

    public Long getId() {
        return id;
    }

    public Long getListingId() {
        return listingId;
    }

    public String getGarmentName() {
        return garmentName;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public ConversationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
