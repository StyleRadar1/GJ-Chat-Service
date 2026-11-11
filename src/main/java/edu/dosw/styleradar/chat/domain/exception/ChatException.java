package edu.dosw.styleradar.chat.domain.exception;

/** Base type of every business error raised by the chat domain. */
public abstract class ChatException extends RuntimeException {
    protected ChatException(String message) {
        super(message);
    }
}
