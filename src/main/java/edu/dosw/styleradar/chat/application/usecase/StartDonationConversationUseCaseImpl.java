package edu.dosw.styleradar.chat.application.usecase;

import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.DonationSummary;
import edu.dosw.styleradar.chat.domain.port.in.StartDonationConversationUseCase;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.DonationQueryPort;

import java.time.Clock;
import java.util.Objects;

/** Validates the donation request and opens the conversation, or returns the existing one (GJ-57). */
public class StartDonationConversationUseCaseImpl implements StartDonationConversationUseCase {
    private final ConversationRepositoryPort conversations;
    private final DonationQueryPort donations;
    private final Clock clock;

    public StartDonationConversationUseCaseImpl(ConversationRepositoryPort conversations,
                                                DonationQueryPort donations, Clock clock) {
        this.conversations = conversations;
        this.donations = donations;
        this.clock = clock;
    }

    @Override
    public Conversation start(Long callerId, Long donationId) {
        Objects.requireNonNull(callerId, "El usuario participante es obligatorio");
        Objects.requireNonNull(donationId, "El identificador de la donación es obligatorio");

        return conversations.findByDonationAndBuyer(donationId, callerId).orElseGet(() -> {
            DonationSummary donation = donations.fetch(donationId);
            Conversation created = Conversation.startDonation(donation, callerId, clock.instant());
            return conversations.save(created);
        });
    }
}
