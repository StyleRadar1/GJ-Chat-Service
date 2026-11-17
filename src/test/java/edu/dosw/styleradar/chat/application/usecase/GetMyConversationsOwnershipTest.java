package edu.dosw.styleradar.chat.application.usecase;

import edu.dosw.styleradar.chat.domain.exception.AccessDeniedDomainException;
import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static edu.dosw.styleradar.chat.support.TestData.BUYER;
import static edu.dosw.styleradar.chat.support.TestData.SELLER;
import static edu.dosw.styleradar.chat.support.TestData.conversation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Pruebas de GET /api/v1/users/{id}/conversations: solo el dueño del id puede consultar. */
@ExtendWith(MockitoExtension.class)
class GetMyConversationsOwnershipTest {
    @Mock
    private ConversationRepositoryPort conversations;

    @Test
    void theOwnerListsItsConversations() {
        when(conversations.findByParticipant(SELLER))
                .thenReturn(List.of(conversation(7L, ConversationStatus.OPEN)));

        var result = new GetMyConversationsUseCaseImpl(conversations).fetchOwn(SELLER, SELLER);

        assertThat(result).extracting("id").containsExactly(7L);
    }

    @Test
    void anotherUserGetsAccessDeniedWithoutQueryingTheRepository() {
        var useCase = new GetMyConversationsUseCaseImpl(conversations);

        assertThatThrownBy(() -> useCase.fetchOwn(BUYER, SELLER)).isInstanceOf(AccessDeniedDomainException.class);

        verifyNoInteractions(conversations);
    }

    @Test
    void aMissingRequesterGetsAccessDenied() {
        var useCase = new GetMyConversationsUseCaseImpl(conversations);

        assertThatThrownBy(() -> useCase.fetchOwn(null, SELLER)).isInstanceOf(AccessDeniedDomainException.class);
    }
}
