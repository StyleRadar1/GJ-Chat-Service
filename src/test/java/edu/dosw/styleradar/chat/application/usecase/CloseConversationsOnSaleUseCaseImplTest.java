package edu.dosw.styleradar.chat.application.usecase;

import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.ProcessedEventPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static edu.dosw.styleradar.chat.support.TestData.BUYER;
import static edu.dosw.styleradar.chat.support.TestData.LISTING;
import static edu.dosw.styleradar.chat.support.TestData.conversation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Tests for closing conversations when a listing is sold. */
@ExtendWith(MockitoExtension.class)
class CloseConversationsOnSaleUseCaseImplTest {
    private static final Long OTHER_BUYER = 99L;

    @Mock
    private ConversationRepositoryPort conversations;
    @Mock
    private ProcessedEventPort processedEvents;
    private CloseConversationsOnSaleUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CloseConversationsOnSaleUseCaseImpl(conversations, processedEvents);
    }

    private static Conversation conversationWith(Long id, Long buyerId) {
        return new Conversation(id, LISTING, "Chaqueta de jean", buyerId, 2L, ConversationStatus.OPEN,
                conversation(id, ConversationStatus.OPEN).getCreatedAt());
    }

    @Test
    void closesTheConversationsOfOtherBuyersAndKeepsTheWinnerOpen() {
        Conversation winner = conversationWith(1L, BUYER);
        Conversation loser = conversationWith(2L, OTHER_BUYER);
        when(processedEvents.isProcessed("evt-1")).thenReturn(false);
        when(conversations.findOpenByListing(LISTING)).thenReturn(List.of(winner, loser));

        useCase.execute("evt-1", LISTING, BUYER);

        ArgumentCaptor<Conversation> saved = ArgumentCaptor.forClass(Conversation.class);
        verify(conversations).save(saved.capture());
        assertThat(saved.getValue().getId()).isEqualTo(2L);
        assertThat(saved.getValue().getStatus()).isEqualTo(ConversationStatus.CLOSED);
        assertThat(winner.getStatus()).isEqualTo(ConversationStatus.OPEN);
        verify(processedEvents).markProcessed("evt-1");
    }

    @Test
    void ignoresARedeliveredEvent() {
        when(processedEvents.isProcessed("evt-1")).thenReturn(true);

        useCase.execute("evt-1", LISTING, BUYER);

        verify(conversations, never()).findOpenByListing(any());
        verify(conversations, never()).save(any());
        verify(processedEvents, never()).markProcessed(any());
    }

    @Test
    void marksTheEventEvenWhenThereIsNothingToClose() {
        when(processedEvents.isProcessed("evt-2")).thenReturn(false);
        when(conversations.findOpenByListing(LISTING)).thenReturn(List.of());

        useCase.execute("evt-2", LISTING, BUYER);

        verify(conversations, never()).save(any());
        verify(processedEvents).markProcessed("evt-2");
    }
}
