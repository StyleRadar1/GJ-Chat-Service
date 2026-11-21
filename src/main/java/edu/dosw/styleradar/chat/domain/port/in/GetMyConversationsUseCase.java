package edu.dosw.styleradar.chat.domain.port.in;

import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.PageResult;

import java.util.List;

/** Input port to list the conversations of a user. */
public interface GetMyConversationsUseCase {
    List<Conversation> fetch(Long userId);

    PageResult<Conversation> fetch(Long userId, int page, int size);

    /** Lista las conversaciones de {@code ownerId}; solo el propio usuario puede consultarlas. */
    List<Conversation> fetchOwn(Long requesterId, Long ownerId);
}
