package br.com.pix.wallet.infrastructure.persistence.entity;

import br.com.pix.wallet.domain.model.PixKey;
import br.com.pix.wallet.domain.model.enums.PixKeyType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pix_keys")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PixKeyEntity {
    @Id
    private UUID id;

    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PixKeyType type;

    @Column(name = "key_value", nullable = false, unique = true)
    private String value;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static PixKeyEntity fromDomain(PixKey domain) {
        return new PixKeyEntity(
                domain.getId(),
                domain.getWalletId(),
                domain.getType(),
                domain.getValue(),
                domain.getCreatedAt()
        );
    }

    public PixKey toDomain() {
        return new PixKey(id, walletId, type, value, createdAt);
    }
}
