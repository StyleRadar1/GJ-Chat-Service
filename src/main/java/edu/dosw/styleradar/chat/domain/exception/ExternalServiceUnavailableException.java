package edu.dosw.styleradar.chat.domain.exception;

/** Raised when the listings service fails or answers something unusable. */
public class ExternalServiceUnavailableException extends ChatException {
    public ExternalServiceUnavailableException(String message) {
        super(message);
    }
}
