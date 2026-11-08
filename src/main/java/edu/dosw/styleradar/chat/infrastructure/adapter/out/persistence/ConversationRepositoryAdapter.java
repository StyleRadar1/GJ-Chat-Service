package edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.repository.SpringDataConversationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Persistence adapter for conversations; resolves concurrent creation through the unique constraint. */
@Component
public class ConversationRepositoryAdapter implements ConversationRepositoryPort {
    private final SpringDataConversationRepository repository;
    private final PersistenceMapper mapper;

    public ConversationRepositoryAdapter(SpringDataConversationRepository repository, PersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Conversation> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Conversation> findOpenByListing(Long listingId) {
        return repository.findByListingIdAndStatus(listingId, ConversationStatus.OPEN).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Conversation> findByListingAndBuyer(Long listingId, Long buyerId) {
        return repository.findByListingIdAndBuyerId(listingId, buyerId).map(mapper::toDomain);
    }

    @Override
    public List<Conversation> findByParticipant(Long userId) {
        return repository.findByParticipant(userId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Conversation save(Conversation conversation) {
        try {
            return mapper.toDomain(repository.saveAndFlush(mapper.toEntity(conversation)));
        } catch (DataIntegrityViolationException e) {
            return repository.findByListingIdAndBuyerId(conversation.getListingId(),
                            conversation.getBuyerId())
                    .map(mapper::toDomain)
                    .orElseThrow(() -> e);
        }
    }
}
