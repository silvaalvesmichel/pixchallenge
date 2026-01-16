package br.com.pix.wallet.infrastructure.persistence.entity;

import br.com.pix.wallet.domain.model.PixEvent;
import br.com.pix.wallet.domain.model.enums.PixEventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "pix_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PixEventEntity {

    @Id
    @Column(name = "event_id")
    private String eventId;

    @Column(name = "end_to_end_id", nullable = false)
    private String endToEndId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private PixEventType type;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    public static PixEventEntity fromDomain(PixEvent domain) {
        return new PixEventEntity(
                domain.eventId(),
                domain.endToEndId(),
                domain.type(),
                domain.occurredAt(),
                domain.processedAt()
        );
    }
}

