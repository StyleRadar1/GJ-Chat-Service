package edu.dosw.styleradar.chat.domain.port.in;

import edu.dosw.styleradar.chat.domain.model.Message;
import edu.dosw.styleradar.chat.domain.model.PageResult;

import java.util.List;

/** Input port to read the messages of a conversation. */
public interface GetMessagesUseCase {
    List<Message> fetch(Long userId, Long conversationId);

    PageResult<Message> fetch(Long userId, Long conversationId, int page, int size);
}
