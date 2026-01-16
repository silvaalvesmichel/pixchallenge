package br.com.pix.wallet.application;

import br.com.pix.wallet.application.dto.WebhookInput;
import br.com.pix.wallet.application.usecase.ProcessWebhookUseCase;
import br.com.pix.wallet.domain.gateway.LedgerGateway;
import br.com.pix.wallet.domain.gateway.PixEventGateway;
import br.com.pix.wallet.domain.gateway.PixGateway;
import br.com.pix.wallet.domain.gateway.WalletGateway;
import br.com.pix.wallet.domain.model.enums.PixEventType;
import br.com.pix.wallet.domain.model.PixTransfer;
import br.com.pix.wallet.domain.model.enums.TransferStatus;
import br.com.pix.wallet.domain.model.Wallet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessWebhookUseCaseTest {

    @Mock
    PixGateway pixGateway;
    @Mock
    WalletGateway walletGateway;
    @Mock
    LedgerGateway ledgerGateway;
    @Mock
    PixEventGateway pixEventGateway;

    @InjectMocks
    ProcessWebhookUseCase useCase;

    @Test
    void shouldProcessConfirmedEvent() {
        String e2e = "e2e";
        PixTransfer transfer = PixTransfer.create(UUID.randomUUID(), BigDecimal.TEN, "key");
        when(pixGateway.findByEndToEndId(e2e)).thenReturn(Optional.of(transfer));
        when(pixEventGateway.existsByEventId("evt1")).thenReturn(false);

        useCase.execute(new WebhookInput(e2e, "evt1", PixEventType.CONFIRMED, LocalDateTime.now()));

        verify(pixGateway).save(argThat(t -> t.getStatus() == TransferStatus.CONFIRMED));
        verify(walletGateway, never()).save(any()); // Confirmed não mexe em saldo
    }

    @Test
    void shouldProcessRejectedEventAndRefund() {
        String e2e = "e2e";
        UUID walletId = UUID.randomUUID();
        PixTransfer transfer = new PixTransfer(UUID.randomUUID(), e2e, walletId, BigDecimal.TEN, "key", TransferStatus.PENDING, LocalDateTime.now());
        Wallet wallet = new Wallet(walletId, BigDecimal.ZERO);

        when(pixGateway.findByEndToEndId(e2e)).thenReturn(Optional.of(transfer));
        when(walletGateway.findByIdForUpdate(walletId)).thenReturn(Optional.of(wallet));

        useCase.execute(new WebhookInput(e2e, "evt1", PixEventType.REJECTED, LocalDateTime.now()));

        verify(pixGateway).save(argThat(t -> t.getStatus() == TransferStatus.REJECTED));
        verify(walletGateway).save(argThat(w -> w.getBalance().compareTo(BigDecimal.TEN) == 0)); // Estornado
        verify(ledgerGateway).save(any());
    }

    @Test
    void shouldIgnoreDuplicateEvent() {
        when(pixEventGateway.existsByEventId("evt1")).thenReturn(true);
        useCase.execute(new WebhookInput("e2e", "evt1", PixEventType.CONFIRMED, LocalDateTime.now()));
        verify(pixGateway, never()).findByEndToEndId(any());
    }
}
