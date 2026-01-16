package br.com.pix.wallet.infrastructure.api.dto;

import java.time.LocalDateTime;

public record WebhookRequest(
        String endToEndId,
        String eventId,
        String eventType, // Recebemos como String e convertemos
        LocalDateTime occurredAt
) {}
