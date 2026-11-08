package edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.repository;

import edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.entity.ProcessedEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for consumed event ids. */
public interface SpringDataProcessedEventRepository extends JpaRepository<ProcessedEventJpaEntity, String> {
}
