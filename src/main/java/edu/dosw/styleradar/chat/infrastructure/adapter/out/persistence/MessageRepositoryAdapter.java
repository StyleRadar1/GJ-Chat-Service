package edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.chat.domain.model.Message;
import edu.dosw.styleradar.chat.domain.port.out.MessageRepositoryPort;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.repository.SpringDataMessageRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/** Persistence adapter for messages. */
@Component
public class MessageRepositoryAdapter implements MessageRepositoryPort {
    private final SpringDataMessageRepository repository;
    private final PersistenceMapper mapper;

    public MessageRepositoryAdapter(SpringDataMessageRepository repository, PersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Message save(Message message) {
        return mapper.toDomain(repository.save(mapper.toEntity(message)));
    }

    @Override
    public List<Message> findByConversation(Long conversationId) {
        return repository.findByConversationIdOrderBySentAtAscIdAsc(conversationId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
