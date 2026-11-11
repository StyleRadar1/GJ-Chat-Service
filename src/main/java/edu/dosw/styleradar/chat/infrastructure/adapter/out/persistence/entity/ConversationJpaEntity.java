package edu.dosw.styleradar.chat.infrastructure.adapter.out.persistence.entity;

import edu.dosw.styleradar.chat.domain.model.ConversationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Convert;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** JPA row of the conversaciones table; holds no foreign keys because listings and users belong to other services. */
@Entity
@Table(name = "conversaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConversationJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "publicacion_id", nullable = false, updatable = false)
    private Long listingId;

    @Column(name = "prenda_nombre", length = 200)
    private String garmentName;

    @Column(name = "comprador_id", nullable = false, updatable = false)
    private Long buyerId;

    @Column(name = "vendedor_id", nullable = false, updatable = false)
    private Long sellerId;

    @Convert(converter = ConversationStatusConverter.class)
    @Column(name = "estado", nullable = false, length = 20)
    private ConversationStatus status;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant createdAt;
}
