package edu.dosw.styleradar.chat.domain.exception;

/** Raised when a conversation does not exist or the caller is not a participant. */
public class ConversationNotFoundException extends ChatException {
    public ConversationNotFoundException(Long conversationId) {
        super("No existe la conversación " + conversationId);
    }
}
