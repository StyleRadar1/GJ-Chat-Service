package edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.entity.ProcessedEventJpaEntity;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.repository.SpringDataProcessedEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Clock;
import java.time.ZoneOffset;

import static edu.dosw.styleradar.chat.support.TestData.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Tests for the processed events adapter. */
@ExtendWith(MockitoExtension.class)
class ProcessedEventAdapterTest {
    @Mock
    private SpringDataProcessedEventRepository repository;
    private ProcessedEventAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ProcessedEventAdapter(repository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void knowsWhichEventsWereProcessed() {
        when(repository.existsById("evt-1")).thenReturn(true);
        when(repository.existsById("evt-2")).thenReturn(false);

        assertThat(adapter.isProcessed("evt-1")).isTrue();
        assertThat(adapter.isProcessed("evt-2")).isFalse();
    }

    @Test
    void storesTheEventIdWithTheCurrentInstant() {
        adapter.markProcessed("evt-1");

        ArgumentCaptor<ProcessedEventJpaEntity> saved = ArgumentCaptor.forClass(ProcessedEventJpaEntity.class);
        verify(repository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getEventId()).isEqualTo("evt-1");
        assertThat(saved.getValue().getProcessedAt()).isEqualTo(NOW);
    }

    @Test
    void aDuplicateInsertIsNotAnError() {
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatCode(() -> adapter.markProcessed("evt-1")).doesNotThrowAnyException();
    }
}
