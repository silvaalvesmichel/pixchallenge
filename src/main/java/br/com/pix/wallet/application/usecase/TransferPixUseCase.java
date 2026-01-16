package br.com.pix.wallet.application.usecase;

import br.com.pix.wallet.application.dto.TransferPixInput;
import br.com.pix.wallet.application.dto.TransferPixOutput;
import br.com.pix.wallet.domain.exception.WalletNotFoundException;
import br.com.pix.wallet.domain.gateway.LedgerGateway;
import br.com.pix.wallet.domain.gateway.PixGateway;
import br.com.pix.wallet.domain.gateway.PixKeyGateway;
import br.com.pix.wallet.domain.gateway.WalletGateway;
import br.com.pix.wallet.domain.model.LedgerEntry;
import br.com.pix.wallet.domain.model.PixKey;
import br.com.pix.wallet.domain.model.PixTransfer;
import br.com.pix.wallet.domain.model.enums.TransactionType;
import br.com.pix.wallet.infrastructure.persistence.entity.IdempotencyKeyEntity;
import br.com.pix.wallet.infrastructure.persistence.repository.IdempotencyKeyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferPixUseCase {

    private final WalletGateway walletGateway;
    private final LedgerGateway ledgerGateway;
    private final PixGateway pixGateway;
    private final IdempotencyKeyRepository idempotencyRepository;
    private final ObjectMapper objectMapper;
    private final PixKeyGateway pixKeyGateway;
    // Injetando para controle manual da transação
    private final TransactionTemplate transactionTemplate;

    public TransferPixOutput execute(TransferPixInput input) {

        // 1. Verificação de Idempotência (Cache) - Fora da Transação
        if (input.idempotencyKey() != null) {
            Optional<IdempotencyKeyEntity> cached = idempotencyRepository.findById(input.idempotencyKey());
            if (cached.isPresent()) {
                log.info("Retornando resposta cacheada para chave: {}", input.idempotencyKey());
                try {
                    return objectMapper.readValue(cached.get().getBody(), TransferPixOutput.class);
                } catch (Exception e) {
                    throw new RuntimeException("Erro ao ler cache de idempotência", e);
                }
            }
        }

        // 2. Execução da Lógica de Negócio (DENTRO DA TRANSAÇÃO)
        // Usamos o template para garantir que o Lock Pessimista funcione
        TransferPixOutput output = transactionTemplate.execute(status -> {
            return processTransferLogic(input);
        });

        // 3. Salvar Idempotência - Fora da Transação Principal
        // Isso garante que se houver erro aqui, não revertemos o PIX (embora seja raro)
        // Ou, mais importante: permite tratar a Race Condition do "Duplo Disparo"
        if (input.idempotencyKey() != null && output != null) {
            try {
                saveIdempotency(input.idempotencyKey(), output);
            } catch (DataIntegrityViolationException e) {
                // Race Condition: Outra thread salvou a chave milissegundos antes.
                return execute(input);
            }
        }

        return output;
    }

    protected TransferPixOutput processTransferLogic(TransferPixInput input) {
        log.info("Processando transferencia. WalletOrigem: {}", input.walletId());

        // Busca a chave no banco
        Optional<PixKey> destinationKey = pixKeyGateway.findByKey(input.toPixKey());

        // --- LOG DE DEBUG ---
        if (destinationKey.isEmpty()) {
            log.warn("ATENCAO: Chave Pix '{}' NAO encontrada no banco local. Processando como externa.", input.toPixKey());
            // Se o requisito for ESTRITAMENTE transferência interna, descomente a linha abaixo:
            // throw new IllegalArgumentException("Chave Pix nao encontrada no sistema.");
        } else {
            log.info("Chave Pix encontrada. Dono da chave: {}", destinationKey.get().getWalletId());
        }
        // --------------------

        if (destinationKey.isPresent()) {
            // Se a carteira dona da chave for IGUAL à carteira de origem -> BLOQUEIA
            if (destinationKey.get().getWalletId().equals(input.walletId())) {
                log.error("Tentativa de auto-transferencia bloqueada.");
                throw new IllegalArgumentException("Nao e permitido fazer transferencia para a propria carteira.");
            }
        }

        var wallet = walletGateway.findByIdForUpdate(input.walletId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

        // Debitar (Valida Saldo)
        wallet.debit(input.amount());
        walletGateway.save(wallet);

        // Criar Transferência Pix (PENDING)
        var transfer = PixTransfer.create(input.walletId(), input.amount(), input.toPixKey());
        pixGateway.save(transfer);

        //Registrar no Ledger (Com EndToEndId)
        var ledger = LedgerEntry.create(
                wallet.getId(),
                input.amount(),
                TransactionType.DEBIT,
                "PIX_SENT_PENDING",
                transfer.getEndToEndId()
        );
        ledgerGateway.save(ledger);

        return new TransferPixOutput(transfer.getEndToEndId(), transfer.getStatus());
    }

    private void saveIdempotency(String key, TransferPixOutput output) {
        try {
            var entity = new IdempotencyKeyEntity();
            entity.setKey(key);
            entity.setStatus(200);
            entity.setBody(objectMapper.writeValueAsString(output));
            entity.setCreatedAt(LocalDateTime.now());
            idempotencyRepository.save(entity);
        } catch (Exception e) {
            // Se for constraint violation, relança para o catch do execute lidar
            if (e instanceof DataIntegrityViolationException) throw (DataIntegrityViolationException) e;
            throw new RuntimeException("Erro ao salvar chave idempotencia", e);
        }
    }
}
