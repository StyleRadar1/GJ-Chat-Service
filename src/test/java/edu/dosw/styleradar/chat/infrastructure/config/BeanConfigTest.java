package edu.dosw.styleradar.chat.infrastructure.config;

import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.EventPublisherPort;
import edu.dosw.styleradar.chat.domain.port.out.ListingQueryPort;
import edu.dosw.styleradar.chat.domain.port.out.MessageRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.ProcessedEventPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BeanConfigTest {
    private final BeanConfig config = new BeanConfig();

    @Mock
    private ConversationRepositoryPort conversations;
    @Mock
    private ListingQueryPort listings;
    @Mock
    private MessageRepositoryPort messages;
    @Mock
    private EventPublisherPort events;
    @Mock
    private ProcessedEventPort processedEvents;
    @Mock
    private edu.dosw.styleradar.chat.domain.port.out.StoreQueryPort stores;
    @Mock
    private edu.dosw.styleradar.chat.domain.port.out.DonationQueryPort donations;

    @Test
    void wiresAllUseCasesAndClock() {
        Clock clock = config.clock();
        assertThat(clock).isNotNull();

        assertThat(config.startConversationUseCase(conversations, listings, clock)).isNotNull();
        assertThat(config.startDonationConversationUseCase(conversations, donations, stores, clock)).isNotNull();
        assertThat(config.startDonationConversationUseCase(conversations, donations, clock)).isNotNull();
        assertThat(config.getMyConversationsUseCase(conversations)).isNotNull();
        assertThat(config.getMessagesUseCase(conversations, messages, stores)).isNotNull();
        assertThat(config.sendMessageUseCase(conversations, messages, listings, stores, events, clock)).isNotNull();
        assertThat(config.closeConversationsOnSaleUseCase(conversations, processedEvents)).isNotNull();
    }
}
