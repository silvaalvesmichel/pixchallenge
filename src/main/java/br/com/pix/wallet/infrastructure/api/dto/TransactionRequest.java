package br.com.pix.wallet.infrastructure.api.dto;

import java.math.BigDecimal;

public record TransactionRequest(BigDecimal amount) {}