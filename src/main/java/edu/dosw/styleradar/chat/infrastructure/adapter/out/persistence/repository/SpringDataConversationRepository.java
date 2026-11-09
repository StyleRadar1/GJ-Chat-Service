package edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.repository;

import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.entity.ConversationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** Spring Data repository for conversations. */
public interface SpringDataConversationRepository extends JpaRepository<ConversationJpaEntity, Long> {
    List<ConversationJpaEntity> findByListingIdAndStatus(Long listingId, ConversationStatus status);

    Optional<ConversationJpaEntity> findByListingIdAndBuyerId(Long listingId, Long buyerId);

    @Query("select c from ConversationJpaEntity c where c.buyerId = :userId or c.sellerId = :userId "
            + "order by c.createdAt desc, c.id desc")
    List<ConversationJpaEntity> findByParticipant(@Param("userId") Long userId);
}
