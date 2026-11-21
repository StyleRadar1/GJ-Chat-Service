package edu.dosw.styleradar.chat.application.usecase;

import edu.dosw.styleradar.chat.domain.exception.TooManyMessagesException;
import edu.dosw.styleradar.chat.domain.model.Message;
import edu.dosw.styleradar.chat.domain.model.MessageText;
import edu.dosw.styleradar.chat.domain.port.in.SendMessageUseCase;
import edu.dosw.styleradar.chat.domain.port.out.MessageRateLimiterPort;

import java.util.Objects;

/** Aplica el límite de mensajes por usuario antes de delegar el envío al caso de uso real. */
public class RateLimitedSendMessageUseCase implements SendMessageUseCase {
    private final SendMessageUseCase delegate;
    private final MessageRateLimiterPort rateLimiter;

    public RateLimitedSendMessageUseCase(SendMessageUseCase delegate, MessageRateLimiterPort rateLimiter) {
        this.delegate = Objects.requireNonNull(delegate, "El caso de uso de envío es obligatorio");
        this.rateLimiter = Objects.requireNonNull(rateLimiter, "El limitador de mensajes es obligatorio");
    }

    @Override
    public Message send(Long userId, Long conversationId, MessageText text) {
        if (!rateLimiter.tryAcquire(userId)) {
            throw new TooManyMessagesException("Envió demasiados mensajes; espere un momento e intente de nuevo");
        }
        return delegate.send(userId, conversationId, text);
    }
}
