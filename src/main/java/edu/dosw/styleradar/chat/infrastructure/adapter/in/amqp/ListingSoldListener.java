package edu.dosw.styleradar.chat.infrastructure.adapter.in.amqp;

import edu.dosw.styleradar.chat.domain.port.in.CloseConversationsOnSaleUseCase;
import edu.dosw.styleradar.chat.infrastructure.adapter.in.amqp.dto.ListingSoldData;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.messaging.dto.EventEnvelope;
import edu.dosw.styleradar.chat.infrastructure.config.RabbitConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Consumes the listing sold event; a thrown error is retried and then sent to the dead-letter queue. */
@Component
public class ListingSoldListener {
    private final CloseConversationsOnSaleUseCase closeConversationsOnSale;

    public ListingSoldListener(CloseConversationsOnSaleUseCase closeConversationsOnSale) {
        this.closeConversationsOnSale = closeConversationsOnSale;
    }

    @RabbitListener(queues = RabbitConfig.SECOND_HAND_QUEUE)
    public void onListingSold(EventEnvelope<ListingSoldData> envelope) {
        ListingSoldData data = envelope.data();
        closeConversationsOnSale.execute(envelope.eventId(), data.listingId(), data.buyerId());
    }
}
