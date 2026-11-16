package edu.dosw.styleradar.chat.domain.exception;

/** Exception thrown when an authenticated user attempts to access a conversation they are not permitted to. */
public class AccessDeniedDomainException extends RuntimeException {
    public AccessDeniedDomainException(String message) {
        super(message);
    }
}
