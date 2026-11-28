package edu.dosw.styleradar.chat.application.usecase;

import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.DonationSummary;
import edu.dosw.styleradar.chat.domain.port.in.StartDonationConversationUseCase;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.DonationQueryPort;
import edu.dosw.styleradar.chat.domain.port.out.StoreQueryPort;

import java.time.Clock;
import java.util.Objects;

/** Validates the donation request and opens the conversation, or returns the existing one (GJ-57). */
public class StartDonationConversationUseCaseImpl implements StartDonationConversationUseCase {
    private final ConversationRepositoryPort conversations;
    private final DonationQueryPort donations;
    private final StoreQueryPort stores;
    private final Clock clock;

    public StartDonationConversationUseCaseImpl(ConversationRepositoryPort conversations,
                                                DonationQueryPort donations, Clock clock) {
        this(conversations, donations, null, clock);
    }

    public StartDonationConversationUseCaseImpl(ConversationRepositoryPort conversations,
                                                DonationQueryPort donations,
                                                StoreQueryPort stores,
                                                Clock clock) {
        this.conversations = conversations;
        this.donations = donations;
        this.stores = stores;
        this.clock = clock;
    }

    @Override
    public Conversation start(Long callerId, Long donationId) {
        Objects.requireNonNull(callerId, "El usuario participante es obligatorio");
        Objects.requireNonNull(donationId, "El identificador de la donación es obligatorio");

        return conversations.findFirstByDonation(donationId)
                .map(existing -> {
                    existing.verifyParticipant(callerId);
                    return existing;
                })
                .orElseGet(() -> {
                    DonationSummary donation = donations.fetch(donationId);
                    Long representativeId = null;
                    if (donation.foundationId() != null && stores != null) {
                        representativeId = stores.fetchFoundationRepresentativeId(donation.foundationId());
                    }
                    Conversation created = Conversation.startDonation(
                            donation, representativeId, callerId, clock.instant());
                    return conversations.save(created);
                });
    }
}
