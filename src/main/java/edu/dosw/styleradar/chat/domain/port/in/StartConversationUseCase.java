package edu.dosw.styleradar.chat.domain.port.in;

import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.MessageText;

/** Input port to start a conversation about a listing. */
public interface StartConversationUseCase {
    Conversation start(Long buyerId, Long listingId, MessageText initialMessage);

    /** Compatibility entry point for internal callers that do not create a conversation through REST. */
    default Conversation start(Long buyerId, Long listingId) {
        return start(buyerId, listingId, null);
    }
}
