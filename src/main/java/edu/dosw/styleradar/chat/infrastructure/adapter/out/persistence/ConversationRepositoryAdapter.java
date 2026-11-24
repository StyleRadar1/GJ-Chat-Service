package edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import edu.dosw.styleradar.chat.domain.model.PageResult;
import edu.dosw.styleradar.chat.domain.port.out.ConversationRepositoryPort;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.repository.SpringDataConversationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.PageRequest;

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
    public Optional<Conversation> findByStoreAndBuyer(String storeNit, Long buyerId) {
        return repository.findByStoreNitAndBuyerId(storeNit, buyerId).map(mapper::toDomain);
    }

    @Override
    public Optional<Conversation> findByDonationAndBuyer(Long donationId, Long buyerId) {
        return repository.findByDonationIdAndBuyerId(donationId, buyerId).map(mapper::toDomain);
    }

    @Override
    public Optional<Conversation> findFirstByDonation(Long donationId) {
        return repository.findFirstByDonationIdOrderByCreatedAtAsc(donationId).map(mapper::toDomain);
    }

    @Override
    public List<Conversation> findByStore(String storeNit) {
        return repository.findByStoreNitOrderByCreatedAtDesc(storeNit).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public List<Conversation> findByDonation(Long donationId) {
        return repository.findByDonationIdOrderByCreatedAtDesc(donationId).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public List<Conversation> findByParticipant(Long userId) {
        return repository.findByParticipant(userId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public PageResult<Conversation> findByParticipant(Long userId, int page, int size) {
        var result = repository.findByParticipant(userId, PageRequest.of(page, size));
        return new PageResult<>(result.getContent().stream().map(mapper::toDomain).toList(), page, size,
                result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public PageResult<Conversation> findByStore(String storeNit, int page, int size) {
        var result = repository.findByStoreNitOrderByCreatedAtDescIdDesc(storeNit, PageRequest.of(page, size));
        return new PageResult<>(result.getContent().stream().map(mapper::toDomain).toList(), page, size,
                result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public Conversation save(Conversation conversation) {
        try {
            return mapper.toDomain(repository.saveAndFlush(mapper.toEntity(conversation)));
        } catch (DataIntegrityViolationException e) {
            if (conversation.getListingId() != null) {
                return repository.findByListingIdAndBuyerId(conversation.getListingId(), conversation.getBuyerId())
                        .map(mapper::toDomain)
                        .orElseThrow(() -> e);
            }
            if (conversation.getStoreNit() != null) {
                return repository.findByStoreNitAndBuyerId(conversation.getStoreNit(), conversation.getBuyerId())
                        .map(mapper::toDomain)
                        .orElseThrow(() -> e);
            }
            if (conversation.getDonationId() != null) {
                return repository.findByDonationIdAndBuyerId(conversation.getDonationId(), conversation.getBuyerId())
                        .map(mapper::toDomain)
                        .orElseThrow(() -> e);
            }
            throw e;
        }
    }
}
