package br.com.pix.wallet.application.usecase;

import br.com.pix.wallet.application.dto.WebhookInput;
import br.com.pix.wallet.domain.exception.WalletNotFoundException;
import br.com.pix.wallet.domain.gateway.LedgerGateway;
import br.com.pix.wallet.domain.gateway.PixEventGateway;
import br.com.pix.wallet.domain.gateway.PixGateway;
import br.com.pix.wallet.domain.gateway.WalletGateway;
import br.com.pix.wallet.domain.model.*;
import br.com.pix.wallet.domain.model.enums.PixEventType;
import br.com.pix.wallet.domain.model.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessWebhookUseCase {

    private final PixGateway pixGateway;
    private final WalletGateway walletGateway;
    private final LedgerGateway ledgerGateway;
    private final PixEventGateway pixEventGateway;

    @Transactional
    public void execute(WebhookInput input) {
        log.info("Recebido Webhook Pix: {} - Type: {}", input.endToEndId(), input.eventType());

        // 1. Idempotência de Evento (Deduplicação)
        if (pixEventGateway.existsByEventId(input.eventId())) {
            log.warn("Evento duplicado ignorado: {}", input.eventId());
            return;
        }

        // 2. Busca Transferência
        var transfer = pixGateway.findByEndToEndId(input.endToEndId())
                .orElseThrow(() -> new IllegalArgumentException("Transferencia nao encontrada para e2e: " + input.endToEndId()));

        // 3. Verifica Máquina de Estados (Se já finalizou, não mexe mais)
        if (transfer.isTerminalState()) {
            log.warn("Transferencia {} ja em estado terminal {}. Evento ignorado.", transfer.getEndToEndId(), transfer.getStatus());
            saveEventLog(input); // Logamos que recebemos, mesmo ignorando
            return;
        }

        // 4. Aplica Lógica
        if (input.eventType() == PixEventType.CONFIRMED) {
            handleConfirmation(transfer);
        } else if (input.eventType() == PixEventType.REJECTED) {
            handleRejection(transfer);
        }

        // 5. Persiste Mudanças e Loga Evento (Atomicamente)
        pixGateway.save(transfer);
        saveEventLog(input);
    }

    private void handleConfirmation(PixTransfer transfer) {
        log.info("Confirmando transferencia {}", transfer.getEndToEndId());
        transfer.confirm();
        // Nenhuma movimentação financeira necessária (o dinheiro já saiu no PENDING)
    }

    private void handleRejection(PixTransfer transfer) {
        log.info("Rejeitando transferencia {} e estornando valores.", transfer.getEndToEndId());
        transfer.reject();

        // LÓGICA DE ESTORNO (Compensating Transaction)
        var wallet = walletGateway.findByIdForUpdate(transfer.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet nao encontrada para estorno"));

        wallet.credit(transfer.getAmount());
        walletGateway.save(wallet);

        // Registra Estorno no Ledger
        var ledgerEntry = LedgerEntry.create(
                wallet.getId(),
                transfer.getAmount(),
                TransactionType.CREDIT,
                "PIX_REFUND", // Descrição de estorno
                transfer.getEndToEndId()
        );
        ledgerGateway.save(ledgerEntry);
    }

    private void saveEventLog(WebhookInput input) {
        var event = new PixEvent(
                input.eventId(),
                input.endToEndId(),
                input.eventType(),
                input.occurredAt(),
                LocalDateTime.now()
        );
        pixEventGateway.save(event);
    }
}
