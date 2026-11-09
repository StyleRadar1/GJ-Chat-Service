package edu.dosw.styleradar.chat.domain.exception;

/** Raised when a message text is empty or too long. */
public class InvalidMessageTextException extends ChatException {
    public InvalidMessageTextException(String message) {
        super(message);
    }
}
