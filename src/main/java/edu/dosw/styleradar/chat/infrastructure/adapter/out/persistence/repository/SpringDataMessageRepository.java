package edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.repository;

import edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.entity.MessageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/** Spring Data repository for messages. */
public interface SpringDataMessageRepository extends JpaRepository<MessageJpaEntity, Long> {
    List<MessageJpaEntity> findByConversationIdOrderBySentAtAscIdAsc(Long conversationId);

    Page<MessageJpaEntity> findByConversationIdOrderBySentAtAscIdAsc(Long conversationId, Pageable pageable);
}
