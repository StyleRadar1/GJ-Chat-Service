package edu.dosw.styleradar.chat.application.usecase;

import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import edu.dosw.styleradar.chat.domain.model.ConversationType;
import edu.dosw.styleradar.chat.domain.model.StoreSummary;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.StoreQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetStoreConversationsUseCaseImplTest {
    private static final StoreSummary STORE = new StoreSummary("900123456-1", "Almacén Moda", true);

    @Mock
    private ConversationRepositoryPort conversations;
    @Mock
    private StoreQueryPort stores;

    private GetStoreConversationsUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetStoreConversationsUseCaseImpl(conversations, stores);
    }

    @Test
    void fetchesConversationsForStoreAdmin() {
        when(stores.fetchMyStore(2L)).thenReturn(STORE);
        Conversation conv = new Conversation(1L, ConversationType.ALMACEN, null, null, 5L,
                2L, "900123456-1", "Almacén Moda", null, ConversationStatus.OPEN, Instant.now());
        when(conversations.findByStore("900123456-1")).thenReturn(List.of(conv));

        List<Conversation> result = useCase.fetchForStoreAdmin(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStoreNit()).isEqualTo("900123456-1");
    }

    @Test
    void rejectsNullAdminId() {
        assertThatThrownBy(() -> useCase.fetchForStoreAdmin(null))
                .isInstanceOf(NullPointerException.class);
    }
}
