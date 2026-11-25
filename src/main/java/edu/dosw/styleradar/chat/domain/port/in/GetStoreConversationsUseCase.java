package edu.dosw.styleradar.chat.domain.port.in;

import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.PageResult;

import java.util.List;

/** Inbound port for store administrators to list conversations directed to their store. */
public interface GetStoreConversationsUseCase {
    List<Conversation> fetchForStoreAdmin(Long adminUserId);

    PageResult<Conversation> fetchForStoreAdmin(Long adminUserId, int page, int size);

    PageResult<Conversation> fetchForStore(Long adminUserId, String nit, int page, int size);
}
