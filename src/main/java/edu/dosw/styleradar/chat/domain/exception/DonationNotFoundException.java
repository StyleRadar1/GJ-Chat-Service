package edu.dosw.styleradar.chat.domain.exception;

/** Raised when a donation request cannot be found in ms-donaciones. */
public class DonationNotFoundException extends ChatException {
    public DonationNotFoundException(String message) {
        super(message);
    }
}
