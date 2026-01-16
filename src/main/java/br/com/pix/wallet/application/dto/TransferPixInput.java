package br.com.pix.wallet.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferPixInput(
        UUID walletId,
        String toPixKey,
        BigDecimal amount,
        String idempotencyKey
) {}
