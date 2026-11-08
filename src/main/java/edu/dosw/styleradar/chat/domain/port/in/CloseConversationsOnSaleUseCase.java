package edu.dosw.styleradar.chat.domain.port.in;

/** Input port to close the conversations of a listing that was sold to someone else. */
public interface CloseConversationsOnSaleUseCase {
    void execute(String eventId, Long listingId, Long buyerId);
}
