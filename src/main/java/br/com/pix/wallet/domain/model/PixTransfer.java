package br.com.pix.wallet.domain.model;

import br.com.pix.wallet.domain.model.enums.TransferStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PixTransfer {
    private final UUID id;
    private final String endToEndId;
    private final UUID walletId;
    private final BigDecimal amount;
    private final String toPixKey;
    private TransferStatus status;
    private final LocalDateTime createdAt;

    public PixTransfer(UUID id, String endToEndId, UUID walletId, BigDecimal amount, String toPixKey, TransferStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.endToEndId = endToEndId;
        this.walletId = walletId;
        this.amount = amount;
        this.toPixKey = toPixKey;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static PixTransfer create(UUID walletId, BigDecimal amount, String toPixKey) {
        // Gera um EndToEndId simples (simulação de padrão Bacen: E + 31 chars)
        String e2e = "E" + UUID.randomUUID().toString().replace("-", "") + "000";
        return new PixTransfer(
                UUID.randomUUID(),
                e2e,
                walletId,
                amount,
                toPixKey,
                TransferStatus.PENDING, // Nasce como PENDING
                LocalDateTime.now()
        );
    }

    public boolean isTerminalState() {
        return this.status == TransferStatus.CONFIRMED || this.status == TransferStatus.REJECTED;
    }

    public void confirm() {
        if (isTerminalState()) return; // Ignora se já finalizado
        this.status = TransferStatus.CONFIRMED;
    }

    public void reject() {
        if (isTerminalState()) return; // Ignora se já finalizado
        this.status = TransferStatus.REJECTED;
    }

    public UUID getId() { return id; }
    public String getEndToEndId() { return endToEndId; }
    public UUID getWalletId() { return walletId; }
    public BigDecimal getAmount() { return amount; }
    public String getToPixKey() { return toPixKey; }
    public TransferStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
