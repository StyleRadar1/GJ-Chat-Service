package edu.dosw.styleradar.chat.application.usecase;

import edu.dosw.styleradar.chat.domain.exception.AccessDeniedDomainException;
import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.port.in.GetMyConversationsUseCase;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;

import java.util.List;

/** Lists the conversations where the user is buyer or seller. */
public class GetMyConversationsUseCaseImpl implements GetMyConversationsUseCase {
    private final ConversationRepositoryPort conversations;

    public GetMyConversationsUseCaseImpl(ConversationRepositoryPort conversations) {
        this.conversations = conversations;
    }

    @Override
    public List<Conversation> fetch(Long userId) {
        return conversations.findByParticipant(userId);
    }

    @Override
    public List<Conversation> fetchOwn(Long requesterId, Long ownerId) {
        if (requesterId == null || !requesterId.equals(ownerId)) {
            throw new AccessDeniedDomainException("Solo puede consultar sus propias conversaciones");
        }
        return conversations.findByParticipant(ownerId);
    }
}
