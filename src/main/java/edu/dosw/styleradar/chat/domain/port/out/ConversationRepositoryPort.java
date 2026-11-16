package edu.dosw.styleradar.chat.domain.port.out;

import edu.dosw.styleradar.chat.domain.model.Conversation;

import java.util.List;
import java.util.Optional;

/** Output port to store and find conversations. */
public interface ConversationRepositoryPort {
    Optional<Conversation> findById(Long id);

    List<Conversation> findOpenByListing(Long listingId);

    Optional<Conversation> findByListingAndBuyer(Long listingId, Long buyerId);

    Optional<Conversation> findByStoreAndBuyer(String storeNit, Long buyerId);

    Optional<Conversation> findByDonationAndBuyer(Long donationId, Long buyerId);

    Optional<Conversation> findFirstByDonation(Long donationId);

    List<Conversation> findByStore(String storeNit);

    List<Conversation> findByDonation(Long donationId);

    List<Conversation> findByParticipant(Long userId);

    Conversation save(Conversation conversation);
}
