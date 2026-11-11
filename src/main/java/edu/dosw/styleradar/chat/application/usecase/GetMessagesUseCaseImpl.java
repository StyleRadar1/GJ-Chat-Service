package edu.dosw.styleradar.chat.application.usecase;

import edu.dosw.styleradar.chat.domain.exception.ConversationNotFoundException;
import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.Message;
import edu.dosw.styleradar.chat.domain.port.in.GetMessagesUseCase;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.domain.port.out.MessageRepositoryPort;

import java.util.List;

/** Returns the messages of a conversation to its participants only. */
public class GetMessagesUseCaseImpl implements GetMessagesUseCase {
    private final ConversationRepositoryPort conversations;
    private final MessageRepositoryPort messages;

    public GetMessagesUseCaseImpl(ConversationRepositoryPort conversations, MessageRepositoryPort messages) {
        this.conversations = conversations;
        this.messages = messages;
    }

    @Override
    public List<Message> fetch(Long userId, Long conversationId) {
        Conversation conversation = conversations.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));
        conversation.verifyParticipant(userId);
        return messages.findByConversation(conversationId);
    }
}
