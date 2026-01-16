package br.com.pix.wallet.domain.model;

import br.com.pix.wallet.domain.model.enums.PixKeyType;

import java.time.LocalDateTime;
import java.util.UUID;

public class PixKey {
    private final UUID id;
    private final UUID walletId;
    private final PixKeyType type;
    private final String value;
    private final LocalDateTime createdAt;

    public PixKey(UUID id, UUID walletId, PixKeyType type, String value, LocalDateTime createdAt) {
        this.id = id;
        this.walletId = walletId;
        this.type = type;
        this.value = value;
        this.createdAt = createdAt;
    }

    public static PixKey create(UUID walletId, PixKeyType type, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Valor da chave Pix não pode ser vazio");
        }
        return new PixKey(UUID.randomUUID(), walletId, type, value, LocalDateTime.now());
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getWalletId() { return walletId; }
    public PixKeyType getType() { return type; }
    public String getValue() { return value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
