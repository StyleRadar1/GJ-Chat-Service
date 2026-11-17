package edu.dosw.styleradar.chat.domain.exception;

/** Se lanza cuando un usuario supera el límite de mensajes permitido por ventana de tiempo. */
public class TooManyMessagesException extends ChatException {
    public TooManyMessagesException(String message) {
        super(message);
    }
}
