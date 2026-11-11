package edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.Message;
import edu.dosw.styleradar.chat.domain.model.MessageText;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.entity.ConversationJpaEntity;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.entity.MessageJpaEntity;
import org.springframework.stereotype.Component;

/** Translates JPA entities to the domain model and back, so the domain never sees JPA. */
@Component
public class PersistenceMapper {
    public Conversation toDomain(ConversationJpaEntity entity) {
        return new Conversation(entity.getId(), entity.getListingId(), entity.getGarmentName(),
                entity.getBuyerId(), entity.getSellerId(), entity.getStatus(), entity.getCreatedAt());
    }

    public ConversationJpaEntity toEntity(Conversation conversation) {
        return new ConversationJpaEntity(conversation.getId(), conversation.getListingId(),
                conversation.getGarmentName(), conversation.getBuyerId(), conversation.getSellerId(),
                conversation.getStatus(), conversation.getCreatedAt());
    }

    public Message toDomain(MessageJpaEntity entity) {
        return new Message(entity.getId(), entity.getConversationId(), entity.getAuthorId(),
                new MessageText(entity.getText()), entity.getSentAt());
    }

    public MessageJpaEntity toEntity(Message message) {
        return new MessageJpaEntity(message.getId(), message.getConversationId(), message.getAuthorId(),
                message.getText().value(), message.getSentAt());
    }
}
