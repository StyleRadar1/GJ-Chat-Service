package edu.dosw.styleradar.chat.domain.port.in;

import edu.dosw.styleradar.chat.domain.model.Conversation;

/** Inbound port to open or retrieve a chat conversation with an ally store (SR-RF-73 / GJ-56). */
public interface StartStoreConversationUseCase {
    Conversation start(Long buyerId, String storeNit);
}
