package edu.dosw.styleradar.chat.domain.port.out;

import edu.dosw.styleradar.chat.domain.model.Message;
import edu.dosw.styleradar.chat.domain.model.PageResult;

import java.util.List;

/** Output port to store and find messages. */
public interface MessageRepositoryPort {
    Message save(Message message);

    List<Message> findByConversation(Long conversationId);

    PageResult<Message> findByConversation(Long conversationId, int page, int size);
}
