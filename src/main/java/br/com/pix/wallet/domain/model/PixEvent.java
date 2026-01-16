package br.com.pix.wallet.domain.model;

import br.com.pix.wallet.domain.model.enums.PixEventType;

import java.time.LocalDateTime;

public record PixEvent(
        String eventId,
        String endToEndId,
        PixEventType type,
        LocalDateTime occurredAt,
        LocalDateTime processedAt
) {}
