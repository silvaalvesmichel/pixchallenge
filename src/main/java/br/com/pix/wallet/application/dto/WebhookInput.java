package br.com.pix.wallet.application.dto;

import br.com.pix.wallet.domain.model.enums.PixEventType;

import java.time.LocalDateTime;

public record WebhookInput(
        String endToEndId,
        String eventId,
        PixEventType eventType,
        LocalDateTime occurredAt
) {}
