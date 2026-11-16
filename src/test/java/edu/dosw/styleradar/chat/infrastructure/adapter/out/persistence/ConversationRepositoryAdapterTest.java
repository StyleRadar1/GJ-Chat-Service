package edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.chat.domain.model.Conversation;
import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.entity.ConversationJpaEntity;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.repository.SpringDataConversationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationRepositoryAdapterTest {
    @Mock
    private SpringDataConversationRepository repository;
    private final PersistenceMapper mapper = new PersistenceMapper();
    private ConversationRepositoryAdapter adapter;

    private static final Instant NOW = Instant.parse("2026-11-01T15:00:00Z");
    private static final Conversation CONVERSATION = new Conversation(7L, 10L, "Chaqueta", 1L, 2L,
            ConversationStatus.OPEN, NOW);
    private static final ConversationJpaEntity ENTITY = new ConversationJpaEntity(7L, 10L, "Chaqueta", 1L, 2L,
            ConversationStatus.OPEN, NOW);

    @BeforeEach
    void setUp() {
        adapter = new ConversationRepositoryAdapter(repository, mapper);
    }

    @Test
    void findsById() {
        when(repository.findById(7L)).thenReturn(Optional.of(ENTITY));

        Optional<Conversation> result = adapter.findById(7L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(7L);
        assertThat(result.get().getGarmentName()).isEqualTo("Chaqueta");
    }

    @Test
    void findsOpenByListing() {
        when(repository.findByListingIdAndStatus(10L, ConversationStatus.OPEN)).thenReturn(List.of(ENTITY));

        List<Conversation> result = adapter.findOpenByListing(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(7L);
    }

    @Test
    void findsByListingAndBuyer() {
        when(repository.findByListingIdAndBuyerId(10L, 1L)).thenReturn(Optional.of(ENTITY));

        Optional<Conversation> result = adapter.findByListingAndBuyer(10L, 1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(7L);
    }

    @Test
    void findsByParticipant() {
        when(repository.findByParticipant(1L)).thenReturn(List.of(ENTITY));

        List<Conversation> result = adapter.findByParticipant(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBuyerId()).isEqualTo(1L);
    }

    @Test
    void savesSuccessfully() {
        when(repository.saveAndFlush(any())).thenReturn(ENTITY);

        Conversation saved = adapter.save(CONVERSATION);

        assertThat(saved.getId()).isEqualTo(7L);
        verify(repository).saveAndFlush(any());
    }

    @Test
    void handlesConcurrentCreationOnUniqueConstraint() {
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate"));
        when(repository.findByListingIdAndBuyerId(10L, 1L)).thenReturn(Optional.of(ENTITY));

        Conversation saved = adapter.save(CONVERSATION);

        assertThat(saved.getId()).isEqualTo(7L);
    }

    @Test
    void rethrowsDataIntegrityViolationWhenNotFound() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("duplicate");
        when(repository.saveAndFlush(any())).thenThrow(exception);
        when(repository.findByListingIdAndBuyerId(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.save(CONVERSATION)).isSameAs(exception);
    }
}
