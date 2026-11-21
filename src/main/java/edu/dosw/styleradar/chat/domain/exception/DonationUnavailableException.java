package edu.dosw.styleradar.chat.domain.exception;

/** Raised when a donation request is already finalized (REJECTED or DELIVERED) and cannot receive new chat. */
public class DonationUnavailableException extends ChatException {
    public DonationUnavailableException(String message) {
        super(message);
    }
}
