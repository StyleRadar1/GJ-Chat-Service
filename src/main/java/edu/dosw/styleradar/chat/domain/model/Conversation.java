package edu.dosw.styleradar.chat.domain.model;

import edu.dosw.styleradar.chat.domain.exception.SelfChatException;
import edu.dosw.styleradar.chat.domain.exception.ConversationClosedException;
import edu.dosw.styleradar.chat.domain.exception.ConversationNotFoundException;
import edu.dosw.styleradar.chat.domain.exception.ListingUnavailableException;
import edu.dosw.styleradar.chat.domain.exception.StoreUnavailableException;
import edu.dosw.styleradar.chat.domain.exception.DonationUnavailableException;

import java.time.Instant;
import java.util.Objects;

/** Chat conversation between participants across second-hand, store allies, or donations. */
public final class Conversation {
    private final Long id;
    private final ConversationType type;
    private final Long listingId;
    private final String garmentName;
    private final Long buyerId;
    private Long sellerId;
    private final String storeNit;
    private final String storeName;
    private final Long donationId;
    private final Instant createdAt;
    private ConversationStatus status;

    public Conversation(Long id, ConversationType type, Long listingId, String garmentName, Long buyerId, Long sellerId,
                        String storeNit, String storeName, Long donationId,
                        ConversationStatus status, Instant createdAt) {
        this.id = id;
        this.type = type != null ? type : ConversationType.SEGUNDA_MANO;
        this.listingId = listingId;
        this.garmentName = garmentName;
        this.buyerId = Objects.requireNonNull(buyerId, "El comprador/iniciador es obligatorio");
        this.sellerId = sellerId;
        this.storeNit = storeNit;
        this.storeName = storeName;
        this.donationId = donationId;
        this.status = Objects.requireNonNull(status, "El estado es obligatorio");
        this.createdAt = Objects.requireNonNull(createdAt, "La fecha de creación es obligatoria");
    }

    /** Backwards-compatible constructor for second-hand listings. */
    public Conversation(Long id, Long listingId, String garmentName, Long buyerId, Long sellerId,
                        ConversationStatus status, Instant createdAt) {
        this(id, ConversationType.SEGUNDA_MANO,
                Objects.requireNonNull(listingId, "La publicación es obligatoria"),
                garmentName, buyerId,
                Objects.requireNonNull(sellerId, "El vendedor es obligatorio"),
                null, null, null, status, createdAt);
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

    public static Conversation startStore(StoreSummary store, Long buyerId, Long storeAdminId, Instant now) {
        Objects.requireNonNull(store, "El almacén es obligatorio");
        Objects.requireNonNull(buyerId, "El comprador es obligatorio");
        if (!store.isAvailable()) {
            throw new StoreUnavailableException("Este almacén no está disponible para chat");
        }
        return new Conversation(null, ConversationType.ALMACEN, null, null, buyerId,
                storeAdminId, store.nit(), store.tradeName(), null, ConversationStatus.OPEN, now);
    }

    public static Conversation startDonation(DonationSummary donation, Long callerId, Instant now) {
        Objects.requireNonNull(donation, "La donación es obligatoria");
        Objects.requireNonNull(callerId, "El usuario es obligatorio");
        if (!donation.isAvailable()) {
            throw new DonationUnavailableException("La donación ya finalizó; no se puede iniciar un chat");
        }
        Long donorId = donation.donorId();
        Long foundationId = donation.foundationId();
        if (!callerId.equals(donorId) && (foundationId == null || !callerId.equals(foundationId))) {
            throw new SelfChatException("Solo el donante o la fundación receptora pueden participar en este chat");
        }
        return new Conversation(null, ConversationType.DONACION, null, "Donación #" + donation.id(),
                donorId, foundationId, null, null, donation.id(), ConversationStatus.OPEN, now);
    }

    public boolean isParticipant(Long userId) {
        if (userId == null) {
            return false;
        }
        return buyerId.equals(userId) || (sellerId != null && sellerId.equals(userId));
    }

    public void verifyParticipant(Long userId) {
        if (!isParticipant(userId)) {
            throw new ConversationNotFoundException(id);
        }
    }

    public Long recipientOf(Long authorId) {
        verifyParticipant(authorId);
        if (buyerId.equals(authorId)) {
            return sellerId != null ? sellerId : 0L;
        }
        return buyerId;
    }

    public void assignSellerIfUnassigned(Long adminId) {
        if (this.sellerId == null && adminId != null) {
            this.sellerId = adminId;
        }
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
            throw new ConversationClosedException("La conversación está cerrada");
        }
        return new Message(null, id, authorId, text, now);
    }

    public Long getId() {
        return id;
    }

    public ConversationType getType() {
        return type;
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

    public String getStoreNit() {
        return storeNit;
    }

    public String getStoreName() {
        return storeName;
    }

    public Long getDonationId() {
        return donationId;
    }

    public ConversationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
