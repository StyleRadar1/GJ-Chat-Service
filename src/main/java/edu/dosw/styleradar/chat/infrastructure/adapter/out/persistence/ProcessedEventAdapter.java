package edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.chat.domain.port.out.ProcessedEventPort;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.entity.ProcessedEventJpaEntity;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.repository.SpringDataProcessedEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.Clock;

/** Persistence adapter that tracks consumed event ids; a concurrent duplicate insert counts as already processed. */
@Slf4j
@Component
public class ProcessedEventAdapter implements ProcessedEventPort {
    private final SpringDataProcessedEventRepository repository;
    private final Clock clock;

    public ProcessedEventAdapter(SpringDataProcessedEventRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public boolean isProcessed(String eventId) {
        return repository.existsById(eventId);
    }

    @Override
    public void markProcessed(String eventId) {
        try {
            repository.saveAndFlush(new ProcessedEventJpaEntity(eventId, clock.instant()));
        } catch (DataIntegrityViolationException e) {
            log.debug("Event {} was already marked as processed", eventId);
        }
    }
}
