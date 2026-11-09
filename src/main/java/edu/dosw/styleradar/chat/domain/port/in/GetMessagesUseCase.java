package edu.dosw.styleradar.chat.domain.port.in;

import edu.dosw.styleradar.chat.domain.model.Message;

import java.util.List;

/** Input port to read the messages of a conversation. */
public interface GetMessagesUseCase {
    List<Message> fetch(Long userId, Long conversationId);
}
