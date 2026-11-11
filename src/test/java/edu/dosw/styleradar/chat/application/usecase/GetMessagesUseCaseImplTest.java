package edu.dosw.styleradar.chat.application.usecase;

import edu.dosw.styleradar.chat.domain.exception.ConversationNotFoundException;
import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.MessageRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static edu.dosw.styleradar.chat.support.TestData.OUTSIDER;
import static edu.dosw.styleradar.chat.support.TestData.BUYER;
import static edu.dosw.styleradar.chat.support.TestData.conversation;
import static edu.dosw.styleradar.chat.support.TestData.message;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Tests for reading messages. */
@ExtendWith(MockitoExtension.class)
class GetMessagesUseCaseImplTest {
    @Mock
    private ConversationRepositoryPort conversations;
    @Mock
    private MessageRepositoryPort messages;
    private GetMessagesUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetMessagesUseCaseImpl(conversations, messages);
    }

    @Test
    void aParticipantSeesTheMessages() {
        when(conversations.findById(7L)).thenReturn(Optional.of(conversation(7L, ConversationStatus.OPEN)));
        when(messages.findByConversation(7L)).thenReturn(List.of(message(1L, 7L, BUYER, "Hola")));

        assertThat(useCase.fetch(BUYER, 7L)).extracting("id").containsExactly(1L);
    }

    @Test
    void missingConversationGives404() {
        when(conversations.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.fetch(BUYER, 7L))
                .isInstanceOf(ConversationNotFoundException.class);
    }

    @Test
    void anOutsiderCannotSeeMessages() {
        when(conversations.findById(7L)).thenReturn(Optional.of(conversation(7L, ConversationStatus.OPEN)));

        assertThatThrownBy(() -> useCase.fetch(OUTSIDER, 7L)).isInstanceOf(ConversationNotFoundException.class);
        verify(messages, never()).findByConversation(any());
    }
}
