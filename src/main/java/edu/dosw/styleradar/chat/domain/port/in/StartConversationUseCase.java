package edu.dosw.styleradar.chat.domain.port.in;

import edu.dosw.styleradar.chat.domain.model.Conversation;

/** Input port to start a conversation about a listing. */
public interface StartConversationUseCase {
    Conversation start(Long buyerId, Long listingId);
}
