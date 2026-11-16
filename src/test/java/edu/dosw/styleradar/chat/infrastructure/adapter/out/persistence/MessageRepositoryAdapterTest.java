package edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.chat.domain.model.Message;
import edu.dosw.styleradar.chat.domain.model.MessageText;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.entity.MessageJpaEntity;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.repository.SpringDataMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageRepositoryAdapterTest {
    @Mock
    private SpringDataMessageRepository repository;
    private final PersistenceMapper mapper = new PersistenceMapper();
    private MessageRepositoryAdapter adapter;

    private static final Instant NOW = Instant.parse("2026-11-01T15:00:00Z");
    private static final Message MESSAGE = new Message(100L, 7L, 1L, new MessageText("Hola"), NOW);
    private static final MessageJpaEntity ENTITY = new MessageJpaEntity(100L, 7L, 1L, "Hola", NOW);

    @BeforeEach
    void setUp() {
        adapter = new MessageRepositoryAdapter(repository, mapper);
    }

    @Test
    void savesMessage() {
        when(repository.save(any())).thenReturn(ENTITY);

        Message saved = adapter.save(MESSAGE);

        assertThat(saved.getId()).isEqualTo(100L);
        assertThat(saved.getText().value()).isEqualTo("Hola");
        verify(repository).save(any());
    }

    @Test
    void findsMessagesByConversation() {
        when(repository.findByConversationIdOrderBySentAtAscIdAsc(7L)).thenReturn(List.of(ENTITY));

        List<Message> results = adapter.findByConversation(7L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(100L);
    }
}
