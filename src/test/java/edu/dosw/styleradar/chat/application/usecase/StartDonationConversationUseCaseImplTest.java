package edu.dosw.styleradar.chat.application.usecase;

import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import edu.dosw.styleradar.chat.domain.model.ConversationType;
import edu.dosw.styleradar.chat.domain.model.DonationSummary;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.DonationQueryPort;
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
class StartDonationConversationUseCaseImplTest {
    private static final Instant NOW = Instant.parse("2026-11-01T15:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    private static final DonationSummary DONATION = new DonationSummary(20L, 1L, 2L, "APPROVED");

    @Mock
    private ConversationRepositoryPort conversations;
    @Mock
    private DonationQueryPort donations;

    private StartDonationConversationUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new StartDonationConversationUseCaseImpl(conversations, donations, CLOCK);
    }

    @Test
    void returnsExistingConversationIfAlreadyOpen() {
        Conversation existing = new Conversation(1L, ConversationType.DONACION, null, "Donación #20",
                1L, 2L, null, null, 20L, ConversationStatus.OPEN, NOW);
        when(conversations.findByDonationAndBuyer(20L, 1L)).thenReturn(Optional.of(existing));

        Conversation result = useCase.start(1L, 20L);

        assertThat(result).isSameAs(existing);
    }

    @Test
    void createsNewConversationWhenNotExisting() {
        when(conversations.findByDonationAndBuyer(20L, 1L)).thenReturn(Optional.empty());
        when(donations.fetch(20L)).thenReturn(DONATION);
        when(conversations.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Conversation result = useCase.start(1L, 20L);

        assertThat(result.getType()).isEqualTo(ConversationType.DONACION);
        assertThat(result.getDonationId()).isEqualTo(20L);
        assertThat(result.getBuyerId()).isEqualTo(1L);
        verify(conversations).save(any());
    }

    @Test
    void rejectsNullArguments() {
        assertThatThrownBy(() -> useCase.start(null, 20L))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> useCase.start(1L, null))
                .isInstanceOf(NullPointerException.class);
    }
}
