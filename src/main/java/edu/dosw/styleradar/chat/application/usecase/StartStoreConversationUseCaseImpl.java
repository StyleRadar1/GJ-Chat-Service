package edu.dosw.styleradar.chat.application.usecase;

import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.MessageText;
import edu.dosw.styleradar.chat.domain.port.in.SendMessageUseCase;
import edu.dosw.styleradar.chat.domain.model.StoreSummary;
import edu.dosw.styleradar.chat.domain.port.in.StartStoreConversationUseCase;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.StoreQueryPort;

import java.time.Clock;
import java.util.Objects;
import org.springframework.transaction.annotation.Transactional;

/** Validates the store and opens the conversation, or returns the existing one (GJ-56 / SR-RF-73). */
public class StartStoreConversationUseCaseImpl implements StartStoreConversationUseCase {
    private final ConversationRepositoryPort conversations;
    private final StoreQueryPort stores;
    private final SendMessageUseCase sendMessage;
    private final Clock clock;

    public StartStoreConversationUseCaseImpl(ConversationRepositoryPort conversations,
                                             StoreQueryPort stores, SendMessageUseCase sendMessage, Clock clock) {
        this.conversations = conversations;
        this.stores = stores;
        this.sendMessage = sendMessage;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Conversation start(Long buyerId, String storeNit, MessageText initialMessage) {
        Objects.requireNonNull(buyerId, "El comprador es obligatorio");
        Objects.requireNonNull(storeNit, "El NIT del almacén es obligatorio");
        Objects.requireNonNull(initialMessage, "El mensaje inicial es obligatorio");

        StoreSummary store = stores.fetch(storeNit);
        Conversation conversation = conversations.findByStoreAndBuyer(storeNit, buyerId).orElseGet(() -> {
            Conversation created = Conversation.startStore(store, buyerId, null, clock.instant());
            return conversations.save(created);
        });
        sendMessage.send(buyerId, conversation.getId(), initialMessage);
        return conversation;
    }
}
