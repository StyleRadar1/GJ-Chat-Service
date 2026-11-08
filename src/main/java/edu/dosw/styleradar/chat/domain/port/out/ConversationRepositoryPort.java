package edu.dosw.styleradar.chat.domain.port.out;

import edu.dosw.styleradar.chat.domain.model.Conversation;

import java.util.List;
import java.util.Optional;

/** Output port to store and find conversations. */
public interface ConversationRepositoryPort {
    Optional<Conversation> findById(Long id);

    List<Conversation> findOpenByListing(Long listingId);

    Optional<Conversation> findByListingAndBuyer(Long listingId, Long buyerId);

    List<Conversation> findByParticipant(Long userId);

    Conversation save(Conversation conversation);
}
