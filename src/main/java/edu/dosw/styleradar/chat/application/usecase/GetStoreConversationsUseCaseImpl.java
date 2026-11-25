package edu.dosw.styleradar.chat.application.usecase;

import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.PageResult;
import edu.dosw.styleradar.chat.domain.model.StoreSummary;
import edu.dosw.styleradar.chat.domain.exception.AccessDeniedDomainException;
import edu.dosw.styleradar.chat.domain.exception.StoreNotFoundException;
import edu.dosw.styleradar.chat.domain.exception.StoreUnavailableException;
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
        StoreSummary myStore;
        try {
            myStore = stores.fetchMyStore(adminUserId);
        } catch (StoreNotFoundException | StoreUnavailableException e) {
            throw new AccessDeniedDomainException("El usuario autenticado no administra un almacén aliado");
        }
        return conversations.findByStore(myStore.nit());
    }

    @Override
    public PageResult<Conversation> fetchForStoreAdmin(Long adminUserId, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page debe ser >= 0 y size debe estar entre 1 y 100");
        }
        Objects.requireNonNull(adminUserId, "El identificador del administrador es obligatorio");
        StoreSummary myStore;
        try {
            myStore = stores.fetchMyStore(adminUserId);
        } catch (StoreNotFoundException | StoreUnavailableException e) {
            throw new AccessDeniedDomainException("El usuario autenticado no administra un almacén aliado");
        }
        return conversations.findByStore(myStore.nit(), page, size);
    }

    @Override
    public PageResult<Conversation> fetchForStore(Long adminUserId, String nit, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page debe ser >= 0 y size debe estar entre 1 y 100");
        }
        Objects.requireNonNull(adminUserId, "El identificador del administrador es obligatorio");
        StoreSummary myStore;
        try {
            myStore = stores.fetchMyStore(adminUserId);
        } catch (StoreNotFoundException | StoreUnavailableException e) {
            throw new AccessDeniedDomainException("El usuario autenticado no administra un almacén aliado");
        }
        if (nit != null && !nit.isBlank() && !nit.trim().equalsIgnoreCase(myStore.nit().trim())) {
            throw new AccessDeniedDomainException("El usuario autenticado no administra el almacén con NIT " + nit);
        }
        return conversations.findByStore(myStore.nit(), page, size);
    }
}
