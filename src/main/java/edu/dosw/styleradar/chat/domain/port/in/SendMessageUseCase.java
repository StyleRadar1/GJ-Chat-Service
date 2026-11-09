package edu.dosw.styleradar.chat.domain.port.in;

import edu.dosw.styleradar.chat.domain.model.Message;
import edu.dosw.styleradar.chat.domain.model.MessageText;

/** Input port to send a message. */
public interface SendMessageUseCase {
    Message send(Long userId, Long conversationId, MessageText text);
}
