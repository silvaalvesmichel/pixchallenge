package br.com.pix.wallet.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateWalletOutput(UUID id, BigDecimal balance) {}

