package edu.dosw.styleradar.chat.domain.port.in;

import edu.dosw.styleradar.chat.domain.model.Conversation;

import java.util.List;

/** Inbound port for store administrators to list conversations directed to their store. */
public interface GetStoreConversationsUseCase {
    List<Conversation> fetchForStoreAdmin(Long adminUserId);
}
