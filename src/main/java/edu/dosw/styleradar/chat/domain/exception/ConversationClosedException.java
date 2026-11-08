package edu.dosw.styleradar.chat.domain.exception;

/** Raised when writing in a conversation whose listing was withdrawn. */
public class ConversationClosedException extends ChatException {
    public ConversationClosedException(String message) {
        super(message);
    }
}
