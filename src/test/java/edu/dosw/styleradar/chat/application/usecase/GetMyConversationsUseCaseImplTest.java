package edu.dosw.styleradar.chat.application.usecase;

import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static edu.dosw.styleradar.chat.support.TestData.SELLER;
import static edu.dosw.styleradar.chat.support.TestData.conversation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** Tests for listing conversations. */
@ExtendWith(MockitoExtension.class)
class GetMyConversationsUseCaseImplTest {
    @Mock
    private ConversationRepositoryPort conversations;

    @Test
    void returnsConversationsWhereUserParticipates() {
        when(conversations.findByParticipant(SELLER))
                .thenReturn(List.of(conversation(7L, ConversationStatus.OPEN)));

        var result = new GetMyConversationsUseCaseImpl(conversations).fetch(SELLER);

        assertThat(result).extracting("id").containsExactly(7L);
    }
}
