package edu.dosw.styleradar.chat.application.usecase;

import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.StoreSummary;
import edu.dosw.styleradar.chat.domain.port.in.StartStoreConversationUseCase;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.StoreQueryPort;

import java.time.Clock;
import java.util.Objects;

/** Validates the store and opens the conversation, or returns the existing one (GJ-56 / SR-RF-73). */
public class StartStoreConversationUseCaseImpl implements StartStoreConversationUseCase {
    private final ConversationRepositoryPort conversations;
    private final StoreQueryPort stores;
    private final Clock clock;

    public StartStoreConversationUseCaseImpl(ConversationRepositoryPort conversations,
                                             StoreQueryPort stores, Clock clock) {
        this.conversations = conversations;
        this.stores = stores;
        this.clock = clock;
    }

    @Override
    public Conversation start(Long buyerId, String storeNit) {
        Objects.requireNonNull(buyerId, "El comprador es obligatorio");
        Objects.requireNonNull(storeNit, "El NIT del almacén es obligatorio");

        return conversations.findByStoreAndBuyer(storeNit, buyerId).orElseGet(() -> {
            StoreSummary store = stores.fetch(storeNit);
            Conversation created = Conversation.startStore(store, buyerId, null, clock.instant());
            return conversations.save(created);
        });
    }
}
