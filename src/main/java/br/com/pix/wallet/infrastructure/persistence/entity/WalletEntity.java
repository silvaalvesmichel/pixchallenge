package br.com.pix.wallet.infrastructure.persistence.entity;

import br.com.pix.wallet.domain.model.Wallet;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class WalletEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private BigDecimal balance;

    @Version
    private Long version;

    public Wallet toDomain() {
        return new Wallet(this.id, this.balance);
    }

    public static WalletEntity fromDomain(Wallet wallet) {
        return new WalletEntity(wallet.getId(), wallet.getBalance(), null);
    }
}
