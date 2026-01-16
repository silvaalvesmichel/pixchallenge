package br.com.pix.wallet.domain.model.enums;

public enum TransferStatus {
    CREATED,
    PENDING,   // Enviado, aguardando webhook
    CONFIRMED, // Sucesso final
    REJECTED   // Falha (estorno necessário)
}
