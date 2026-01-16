package br.com.pix.wallet.infrastructure.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PixTransferRequest(UUID fromWalletId, String toPixKey, BigDecimal amount) {}
