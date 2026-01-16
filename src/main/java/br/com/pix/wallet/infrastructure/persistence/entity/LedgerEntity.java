package br.com.pix.wallet.infrastructure.persistence.entity;

import br.com.pix.wallet.domain.model.LedgerEntry;
import br.com.pix.wallet.domain.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ledger")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class LedgerEntity {

    @Id
    private UUID id;

    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    private String description;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static LedgerEntity fromDomain(LedgerEntry entry) {
        return new LedgerEntity(
                entry.id(),
                entry.walletId(),
                entry.amount(),
                entry.type(),
                entry.description(),
                entry.correlationId(),
                entry.createdAt()
        );
    }
}

