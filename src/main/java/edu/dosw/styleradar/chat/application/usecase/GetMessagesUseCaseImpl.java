package edu.dosw.styleradar.chat.application.usecase;

import edu.dosw.styleradar.chat.domain.exception.ConversationNotFoundException;
import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.ConversationType;
import edu.dosw.styleradar.chat.domain.model.Message;
import edu.dosw.styleradar.chat.domain.model.StoreSummary;
import edu.dosw.styleradar.chat.domain.port.in.GetMessagesUseCase;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.MessageRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.StoreQueryPort;

import java.util.List;

/** Returns the messages of a conversation to its participants only. */
public class GetMessagesUseCaseImpl implements GetMessagesUseCase {
    private final ConversationRepositoryPort conversations;
    private final MessageRepositoryPort messages;
    private final StoreQueryPort stores;

    public GetMessagesUseCaseImpl(ConversationRepositoryPort conversations, MessageRepositoryPort messages,
                                  StoreQueryPort stores) {
        this.conversations = conversations;
        this.messages = messages;
        this.stores = stores;
    }

    @Override
    public List<Message> fetch(Long userId, Long conversationId) {
        Conversation conversation = conversations.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        String storeNitOfUser = null;
        if (conversation.getType() == ConversationType.ALMACEN && !conversation.isParticipant(userId)) {
            try {
                StoreSummary myStore = stores.fetchMyStore(userId);
                if (myStore != null) {
                    storeNitOfUser = myStore.nit();
                }
            } catch (Exception ignored) {
                // Si el usuario no administra tienda o falla el servicio, storeNitOfUser permanece null
            }
        }

        if (!conversation.isParticipantOrStoreAdmin(userId, storeNitOfUser)) {
            throw new ConversationNotFoundException(conversationId);
        }

        return messages.findByConversation(conversationId);
    }
}
