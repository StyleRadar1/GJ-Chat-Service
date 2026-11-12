package edu.dosw.styleradar.chat.infrastructure.adapter.in.amqp;

import edu.dosw.styleradar.chat.domain.port.in.CloseConversationsOnSaleUseCase;
import edu.dosw.styleradar.chat.infrastructure.adapter.in.amqp.dto.ListingSoldData;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.messaging.dto.EventEnvelope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static edu.dosw.styleradar.chat.support.TestData.NOW;
import static org.mockito.Mockito.verify;

/** Tests for the listing sold consumer. */
@ExtendWith(MockitoExtension.class)
class ListingSoldListenerTest {
    @Mock
    private CloseConversationsOnSaleUseCase closeConversationsOnSale;
    @InjectMocks
    private ListingSoldListener listener;

    @Test
    void passesTheEventIdAndTheBuyerToTheUseCase() {
        EventEnvelope<ListingSoldData> envelope = new EventEnvelope<>("evt-9",
                "styleradar.segundavida.prenda.vendida", 1, NOW,
                "ms-catalogo", "corr-1", new ListingSoldData(10L, 2L, 1L));

        listener.onListingSold(envelope);

        verify(closeConversationsOnSale).execute("evt-9", 10L, 1L);
    }
}
