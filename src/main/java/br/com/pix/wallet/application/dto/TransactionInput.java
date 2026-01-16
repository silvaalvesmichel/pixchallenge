package br.com.pix.wallet.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionInput(UUID walletId, BigDecimal amount, String idempotencyKey) {}
