package br.com.pix.wallet.domain.model;

import br.com.pix.wallet.domain.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record LedgerEntry(
        UUID id,
        UUID walletId,
        BigDecimal amount,
        TransactionType type,
        String description,
        LocalDateTime createdAt,
        String correlationId
) {
    public static LedgerEntry create(UUID walletId, BigDecimal amount, TransactionType type, String description, String correlationId) {
        return new LedgerEntry(
                UUID.randomUUID(),
                walletId,
                amount,
                type,
                description,
                LocalDateTime.now(),
                correlationId
        );
    }
}
