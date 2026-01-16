package br.com.pix.wallet.infrastructure.gateway;

import br.com.pix.wallet.domain.gateway.LedgerGateway;
import br.com.pix.wallet.domain.model.LedgerEntry;
import br.com.pix.wallet.infrastructure.persistence.entity.LedgerEntity;
import br.com.pix.wallet.infrastructure.persistence.repository.LedgerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LedgerDatabaseGateway implements LedgerGateway {

    private final LedgerJpaRepository repository;

    @Override
    public void save(LedgerEntry entry) {
        repository.save(LedgerEntity.fromDomain(entry));
    }

    @Override
    public BigDecimal getBalanceAt(UUID walletId, LocalDateTime date) {
        return repository.calculateBalanceUntil(walletId, date);
    }
}
