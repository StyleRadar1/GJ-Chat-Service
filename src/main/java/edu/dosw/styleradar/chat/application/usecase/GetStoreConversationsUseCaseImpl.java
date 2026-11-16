package edu.dosw.styleradar.chat.application.usecase;

import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.StoreSummary;
import edu.dosw.styleradar.chat.domain.port.in.GetStoreConversationsUseCase;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.StoreQueryPort;

import java.util.List;
import java.util.Objects;

/** Retrieves all conversations associated with the ally store managed by the admin user. */
public class GetStoreConversationsUseCaseImpl implements GetStoreConversationsUseCase {
    private final ConversationRepositoryPort conversations;
    private final StoreQueryPort stores;

    public GetStoreConversationsUseCaseImpl(ConversationRepositoryPort conversations, StoreQueryPort stores) {
        this.conversations = conversations;
        this.stores = stores;
    }

    @Override
    public List<Conversation> fetchForStoreAdmin(Long adminUserId) {
        Objects.requireNonNull(adminUserId, "El identificador del administrador es obligatorio");
        StoreSummary myStore = stores.fetchMyStore(adminUserId);
        return conversations.findByStore(myStore.nit());
    }
}
