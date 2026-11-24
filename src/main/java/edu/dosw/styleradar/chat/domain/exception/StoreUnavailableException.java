package edu.dosw.styleradar.chat.domain.exception;

/** Raised when a store ally exists but is not active or verified for chat (SR-RF-73). */
public class StoreUnavailableException extends ChatException {
    public StoreUnavailableException(String message) {
        super(message);
    }
}
