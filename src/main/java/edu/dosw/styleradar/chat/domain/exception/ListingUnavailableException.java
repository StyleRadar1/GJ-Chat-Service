package edu.dosw.styleradar.chat.domain.exception;

/** Raised when a chat is requested on a listing that is not available. */
public class ListingUnavailableException extends ChatException {
    public ListingUnavailableException(String message) {
        super(message);
    }
}
