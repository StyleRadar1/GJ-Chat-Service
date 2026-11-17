package edu.dosw.styleradar.chat.application.usecase;

import edu.dosw.styleradar.chat.domain.exception.ConversationNotFoundException;
import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import edu.dosw.styleradar.chat.domain.model.ConversationType;
import edu.dosw.styleradar.chat.domain.model.MessageText;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.EventPublisherPort;
import edu.dosw.styleradar.chat.domain.port.out.ListingQueryPort;
import edu.dosw.styleradar.chat.domain.port.out.MessageRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.StoreQueryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Optional;

import static edu.dosw.styleradar.chat.support.TestData.NOW;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Un chat de donación sin fundación asignada no se le entrega a quien no participa. */
@ExtendWith(MockitoExtension.class)
class SendMessageDonationAccessTest {
    @Mock
    private ConversationRepositoryPort conversations;
    @Mock
    private MessageRepositoryPort messages;
    @Mock
    private ListingQueryPort listings;
    @Mock
    private StoreQueryPort stores;
    @Mock
    private EventPublisherPort events;

    @Test
    void anOutsiderIsNotAssignedAsSellerOfADonationChat() {
        Conversation donation = new Conversation(7L, ConversationType.DONACION, null, "Donación #1", 1L, null,
                null, null, 1L, ConversationStatus.OPEN, NOW);
        when(conversations.findById(7L)).thenReturn(Optional.of(donation));
        var useCase = new SendMessageUseCaseImpl(conversations, messages, listings, stores, events,
                Clock.fixed(NOW, ZoneOffset.UTC));

        assertThatThrownBy(() -> useCase.send(99L, 7L, new MessageText("Hola")))
                .isInstanceOf(ConversationNotFoundException.class);

        verify(conversations, never()).save(any());
        verifyNoInteractions(messages, events, stores);
    }
}
