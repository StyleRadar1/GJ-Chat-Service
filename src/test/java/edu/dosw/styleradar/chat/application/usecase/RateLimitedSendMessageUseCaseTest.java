package edu.dosw.styleradar.chat.application.usecase;

import edu.dosw.styleradar.chat.domain.exception.TooManyMessagesException;
import edu.dosw.styleradar.chat.domain.model.Message;
import edu.dosw.styleradar.chat.domain.model.MessageText;
import edu.dosw.styleradar.chat.domain.port.in.SendMessageUseCase;
import edu.dosw.styleradar.chat.domain.port.out.MessageRateLimiterPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static edu.dosw.styleradar.chat.support.TestData.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Pruebas del límite de mensajes por usuario. */
@ExtendWith(MockitoExtension.class)
class RateLimitedSendMessageUseCaseTest {
    private static final MessageText TEXT = new MessageText("Hola");

    @Mock
    private SendMessageUseCase delegate;
    @Mock
    private MessageRateLimiterPort limiter;

    @Test
    void delegatesWhenTheUserIsWithinTheLimit() {
        Message sent = new Message(20L, 7L, 1L, TEXT, NOW);
        when(limiter.tryAcquire(1L)).thenReturn(true);
        when(delegate.send(1L, 7L, TEXT)).thenReturn(sent);

        Message result = new RateLimitedSendMessageUseCase(delegate, limiter).send(1L, 7L, TEXT);

        assertThat(result).isSameAs(sent);
    }

    @Test
    void rejectsWithoutCallingTheDelegateWhenTheLimitIsExceeded() {
        when(limiter.tryAcquire(1L)).thenReturn(false);
        RateLimitedSendMessageUseCase useCase = new RateLimitedSendMessageUseCase(delegate, limiter);

        assertThatThrownBy(() -> useCase.send(1L, 7L, TEXT)).isInstanceOf(TooManyMessagesException.class);

        verifyNoInteractions(delegate);
    }

    @Test
    void theUnlimitedLimiterNeverRejects() {
        Message sent = new Message(21L, 7L, 1L, TEXT, NOW);
        when(delegate.send(1L, 7L, TEXT)).thenReturn(sent);

        Message result = new RateLimitedSendMessageUseCase(delegate, MessageRateLimiterPort.UNLIMITED)
                .send(1L, 7L, TEXT);

        assertThat(result).isSameAs(sent);
        verify(delegate, never()).send(2L, 7L, TEXT);
    }

    @Test
    void requiresItsDependencies() {
        assertThatThrownBy(() -> new RateLimitedSendMessageUseCase(null, limiter))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new RateLimitedSendMessageUseCase(delegate, null))
                .isInstanceOf(NullPointerException.class);
    }
}
