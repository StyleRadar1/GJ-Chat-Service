package edu.dosw.styleradar.chat.domain.exception;

/** Raised when a seller tries to chat about their own listing. */
public class SelfChatException extends ChatException {
    public SelfChatException(String message) {
        super(message);
    }
}
