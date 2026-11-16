package edu.dosw.styleradar.chat.domain.port.in;

import edu.dosw.styleradar.chat.domain.model.Conversation;

/** Inbound port to open or retrieve a chat conversation for a donation request (SR-RF-44 / GJ-57). */
public interface StartDonationConversationUseCase {
    Conversation start(Long callerId, Long donationId);
}
