package edu.dosw.styleradar.chat.domain.exception;

/** Raised when the listing does not exist in the listings service. */
public class ListingNotFoundException extends ChatException {
    public ListingNotFoundException(String message) {
        super(message);
    }
}
