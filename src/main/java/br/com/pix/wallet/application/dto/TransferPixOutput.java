package br.com.pix.wallet.application.dto;

import br.com.pix.wallet.domain.model.enums.TransferStatus;

public record TransferPixOutput(String endToEndId, TransferStatus status) {}
