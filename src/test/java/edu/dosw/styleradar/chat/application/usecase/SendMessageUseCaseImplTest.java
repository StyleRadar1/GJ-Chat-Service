package edu.dosw.styleradar.chat.application.usecase;

import edu.dosw.styleradar.chat.domain.event.MessageSentEvent;
import edu.dosw.styleradar.chat.domain.exception.ConversationClosedException;
import edu.dosw.styleradar.chat.domain.exception.ConversationNotFoundException;
import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import edu.dosw.styleradar.chat.domain.model.ListingStatus;
import edu.dosw.styleradar.chat.domain.model.Message;
import edu.dosw.styleradar.chat.domain.model.MessageText;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.EventPublisherPort;
import edu.dosw.styleradar.chat.domain.port.out.MessageRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.ListingQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Optional;

import static edu.dosw.styleradar.chat.support.TestData.NOW;
import static edu.dosw.styleradar.chat.support.TestData.OUTSIDER;
import static edu.dosw.styleradar.chat.support.TestData.SELLER;
import static edu.dosw.styleradar.chat.support.TestData.BUYER;
import static edu.dosw.styleradar.chat.support.TestData.LISTING;
import static edu.dosw.styleradar.chat.support.TestData.conversation;
import static edu.dosw.styleradar.chat.support.TestData.message;
import static edu.dosw.styleradar.chat.support.TestData.listing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Tests for sending messages. */
@ExtendWith(MockitoExtension.class)
class SendMessageUseCaseImplTest {
    private static final MessageText TEXT = new MessageText("¿Podemos vernos el sábado?");

    @Mock
    private ConversationRepositoryPort conversations;
    @Mock
    private MessageRepositoryPort messages;
    @Mock
    private ListingQueryPort listings;
    @Mock
    private EventPublisherPort events;
    private SendMessageUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new SendMessageUseCaseImpl(conversations, messages, listings, events, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void savesAParticipantsMessage() {
        when(conversations.findById(7L)).thenReturn(Optional.of(conversation(7L, ConversationStatus.OPEN)));
        when(listings.fetch(LISTING)).thenReturn(listing(ListingStatus.SOLD));
        when(messages.save(any())).thenReturn(message(20L, 7L, BUYER, TEXT.value()));

        Message result = useCase.send(BUYER, 7L, TEXT);

        ArgumentCaptor<Message> stored = ArgumentCaptor.forClass(Message.class);
        verify(messages).save(stored.capture());
        assertThat(stored.getValue().getAuthorId()).isEqualTo(BUYER);
        assertThat(stored.getValue().getSentAt()).isEqualTo(NOW);
        assertThat(result.getId()).isEqualTo(20L);
        verify(conversations, never()).save(any());
        ArgumentCaptor<MessageSentEvent> published = ArgumentCaptor.forClass(MessageSentEvent.class);
        verify(events).publish(published.capture());
        assertThat(published.getValue().messageId()).isEqualTo(20L);
        assertThat(published.getValue().conversationId()).isEqualTo(7L);
        assertThat(published.getValue().listingId()).isEqualTo(LISTING);
        assertThat(published.getValue().authorId()).isEqualTo(BUYER);
        assertThat(published.getValue().recipientId()).isEqualTo(SELLER);
        assertThat(published.getValue().preview()).isEqualTo(TEXT.value());
    }

    @Test
    void cutsThePreviewAtEightyCharacters() {
        MessageText longText = new MessageText("x".repeat(200));
        when(conversations.findById(7L)).thenReturn(Optional.of(conversation(7L, ConversationStatus.OPEN)));
        when(listings.fetch(LISTING)).thenReturn(listing(ListingStatus.AVAILABLE));
        when(messages.save(any())).thenReturn(message(21L, 7L, SELLER, longText.value()));

        useCase.send(SELLER, 7L, longText);

        ArgumentCaptor<MessageSentEvent> published = ArgumentCaptor.forClass(MessageSentEvent.class);
        verify(events).publish(published.capture());
        assertThat(published.getValue().preview()).hasSize(80);
        assertThat(published.getValue().recipientId()).isEqualTo(BUYER);
    }

    @Test
    void ifListingWasWithdrawnClosesConversationAndRejectsMessage() {
        when(conversations.findById(7L)).thenReturn(Optional.of(conversation(7L, ConversationStatus.OPEN)));
        when(listings.fetch(LISTING)).thenReturn(listing(ListingStatus.WITHDRAWN));

        assertThatThrownBy(() -> useCase.send(BUYER, 7L, TEXT)).isInstanceOf(ConversationClosedException.class);

        ArgumentCaptor<Conversation> closed = ArgumentCaptor.forClass(Conversation.class);
        verify(conversations).save(closed.capture());
        assertThat(closed.getValue().getStatus()).isEqualTo(ConversationStatus.CLOSED);
        verify(messages, never()).save(any());
        verifyNoInteractions(events);
    }

    @Test
    void anAlreadyClosedConversationDoesNotQueryTheListing() {
        when(conversations.findById(7L)).thenReturn(Optional.of(conversation(7L, ConversationStatus.CLOSED)));

        assertThatThrownBy(() -> useCase.send(BUYER, 7L, TEXT)).isInstanceOf(ConversationClosedException.class);
        verifyNoInteractions(listings);
    }

    @Test
    void missingConversationGives404() {
        when(conversations.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.send(BUYER, 7L, TEXT))
                .isInstanceOf(ConversationNotFoundException.class);
    }

    @Test
    void anOutsiderCannotWriteNorTriggerQueries() {
        when(conversations.findById(7L)).thenReturn(Optional.of(conversation(7L, ConversationStatus.OPEN)));

        assertThatThrownBy(() -> useCase.send(OUTSIDER, 7L, TEXT)).isInstanceOf(ConversationNotFoundException.class);
        verifyNoInteractions(listings, messages);
    }
}
