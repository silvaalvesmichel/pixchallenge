package br.com.pix.wallet.application.dto;

import br.com.pix.wallet.domain.model.enums.PixKeyType;

import java.util.UUID;
public record RegisterPixKeyInput(UUID walletId, PixKeyType type, String key) {}
