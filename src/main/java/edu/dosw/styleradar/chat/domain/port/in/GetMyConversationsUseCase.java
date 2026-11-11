package edu.dosw.styleradar.chat.domain.port.in;

import edu.dosw.styleradar.chat.domain.model.Conversation;

import java.util.List;

/** Input port to list the conversations of a user. */
public interface GetMyConversationsUseCase {
    List<Conversation> fetch(Long userId);
}
