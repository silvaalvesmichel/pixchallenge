package br.com.pix.wallet.application.dto;

import br.com.pix.wallet.domain.model.enums.PixKeyType;

import java.time.LocalDateTime;
import java.util.UUID;
public record RegisterPixKeyOutput(UUID id, PixKeyType type, String key, LocalDateTime createdAt) {}