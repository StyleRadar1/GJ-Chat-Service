package edu.dosw.styleradar.chat.domain.port.in;

import edu.dosw.styleradar.chat.domain.model.Message;
import edu.dosw.styleradar.chat.domain.model.MessageText;

/** Input port to send a message. */
public interface SendMessageUseCase {
    Message send(Long userId, Long conversationId, MessageText text);

    /** Sends a message after an inbound real-time adapter has already resolved the store NIT. */
    default Message send(Long userId, Long conversationId, MessageText text, String authorizedStoreNit) {
        return send(userId, conversationId, text);
    }
}
