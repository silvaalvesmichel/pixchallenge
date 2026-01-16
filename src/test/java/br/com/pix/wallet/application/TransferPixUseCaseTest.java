package br.com.pix.wallet.application;

import br.com.pix.wallet.application.dto.TransferPixInput;
import br.com.pix.wallet.application.dto.TransferPixOutput;
import br.com.pix.wallet.application.usecase.TransferPixUseCase;
import br.com.pix.wallet.domain.gateway.LedgerGateway;
import br.com.pix.wallet.domain.gateway.PixGateway;
import br.com.pix.wallet.domain.gateway.PixKeyGateway;
import br.com.pix.wallet.domain.gateway.WalletGateway;
import br.com.pix.wallet.domain.model.enums.TransferStatus;
import br.com.pix.wallet.domain.model.Wallet;
import br.com.pix.wallet.infrastructure.persistence.entity.IdempotencyKeyEntity;
import br.com.pix.wallet.infrastructure.persistence.repository.IdempotencyKeyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferPixUseCaseTest {

    @Mock
    WalletGateway walletGateway;
    @Mock
    LedgerGateway ledgerGateway;
    @Mock
    PixGateway pixGateway;
    @Mock
    IdempotencyKeyRepository idempotencyRepository;
    @Mock ObjectMapper objectMapper;
    @Mock TransactionTemplate transactionTemplate;
    @Mock PixKeyGateway pixKeyGateway;

    TransferPixUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new TransferPixUseCase(walletGateway, ledgerGateway, pixGateway, idempotencyRepository, objectMapper, pixKeyGateway, transactionTemplate);
    }

    @Test
    void shouldExecuteTransferSuccessfully() {
        // Mock TransactionTemplate para executar o callback imediatamente
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(mock(TransactionStatus.class));
        });

        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, new BigDecimal("100"));
        when(walletGateway.findByIdForUpdate(walletId)).thenReturn(Optional.of(wallet));

        var input = new TransferPixInput(walletId, "key", new BigDecimal("50"), "idem-key");

        TransferPixOutput output = useCase.execute(input);

        assertThat(output.status()).isEqualTo(TransferStatus.PENDING);
        assertThat(wallet.getBalance()).isEqualByComparingTo(new BigDecimal("50")); // Debitou

        verify(pixGateway).save(any());
        verify(ledgerGateway).save(any());
        verify(idempotencyRepository).save(any()); // Salvou idempotência
    }

    @Test
    void shouldUseIdempotencyCache() throws Exception {
        String key = "idem-key";
        IdempotencyKeyEntity cachedEntity = new IdempotencyKeyEntity();
        cachedEntity.setBody("{}");

        when(idempotencyRepository.findById(key)).thenReturn(Optional.of(cachedEntity));
        when(objectMapper.readValue("{}", TransferPixOutput.class)).thenReturn(new TransferPixOutput("e2e", TransferStatus.PENDING));

        var input = new TransferPixInput(UUID.randomUUID(), "key", BigDecimal.TEN, key);
        useCase.execute(input);

        verify(transactionTemplate, never()).execute(any()); // Não deve processar regra de negócio
    }
}
