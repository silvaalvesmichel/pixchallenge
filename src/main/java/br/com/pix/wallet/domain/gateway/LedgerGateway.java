package br.com.pix.wallet.domain.gateway;

import br.com.pix.wallet.domain.model.LedgerEntry;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface LedgerGateway {
    void save(LedgerEntry entry);
    BigDecimal getBalanceAt(UUID walletId, LocalDateTime date);
}
