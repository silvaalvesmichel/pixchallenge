package br.com.pix.wallet.infrastructure.persistence.entity;

import br.com.pix.wallet.domain.model.PixTransfer;
import br.com.pix.wallet.domain.model.enums.TransferStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pix_transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PixTransferEntity {

    @Id
    private UUID id;

    @Column(name = "end_to_end_id", nullable = false, unique = true)
    private String endToEndId;

    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "to_pix_key", nullable = false)
    private String toPixKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static PixTransferEntity fromDomain(PixTransfer domain) {
        return new PixTransferEntity(
                domain.getId(),
                domain.getEndToEndId(),
                domain.getWalletId(),
                domain.getAmount(),
                domain.getToPixKey(),
                domain.getStatus(),
                domain.getCreatedAt()
        );
    }

    public PixTransfer toDomain() {
        return new PixTransfer(id, endToEndId, walletId, amount, toPixKey, status, createdAt);
    }
}
