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

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StartStoreConversationUseCaseImplTest {
    private static final Instant NOW = Instant.parse("2026-11-01T15:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    private static final StoreSummary STORE = new StoreSummary("900123456-1", "Almacén Moda", true);

    @Mock
    private ConversationRepositoryPort conversations;
    @Mock
    private StoreQueryPort stores;

    private StartStoreConversationUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new StartStoreConversationUseCaseImpl(conversations, stores, CLOCK);
    }

    @Test
    void returnsExistingConversationIfAlreadyOpen() {
        Conversation existing = new Conversation(1L, ConversationType.ALMACEN, null, null, 5L,
                2L, "900123456-1", "Almacén Moda", null, ConversationStatus.OPEN, NOW);
        when(conversations.findByStoreAndBuyer("900123456-1", 5L)).thenReturn(Optional.of(existing));

        Conversation result = useCase.start(5L, "900123456-1");

        assertThat(result).isSameAs(existing);
    }

    @Test
    void createsNewConversationWhenNotExisting() {
        when(conversations.findByStoreAndBuyer("900123456-1", 5L)).thenReturn(Optional.empty());
        when(stores.fetch("900123456-1")).thenReturn(STORE);
        when(conversations.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Conversation result = useCase.start(5L, "900123456-1");

        assertThat(result.getType()).isEqualTo(ConversationType.ALMACEN);
        assertThat(result.getBuyerId()).isEqualTo(5L);
        assertThat(result.getStoreNit()).isEqualTo("900123456-1");
        verify(conversations).save(any());
    }

    @Test
    void rejectsNullArguments() {
        assertThatThrownBy(() -> useCase.start(null, "900123456-1"))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> useCase.start(5L, null))
                .isInstanceOf(NullPointerException.class);
    }
}
