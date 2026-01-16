package br.com.pix.wallet.application;

import br.com.pix.wallet.application.dto.TransactionInput;
import br.com.pix.wallet.application.usecase.CreateWalletUseCase;
import br.com.pix.wallet.application.usecase.DepositUseCase;
import br.com.pix.wallet.application.usecase.WithdrawUseCase;
import br.com.pix.wallet.domain.exception.WalletNotFoundException;
import br.com.pix.wallet.domain.gateway.LedgerGateway;
import br.com.pix.wallet.domain.gateway.WalletGateway;
import br.com.pix.wallet.domain.model.Wallet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionUseCasesTest {

    @Mock
    WalletGateway walletGateway;
    @Mock
    LedgerGateway ledgerGateway;

    @InjectMocks
    CreateWalletUseCase createWalletUseCase;
    @InjectMocks
    DepositUseCase depositUseCase;
    @InjectMocks
    WithdrawUseCase withdrawUseCase;

    @Test
    void shouldCreateWallet() {
        when(walletGateway.save(any(Wallet.class))).thenAnswer(i -> i.getArguments()[0]);
        var output = createWalletUseCase.execute();
        assertThat(output.id()).isNotNull();
        assertThat(output.balance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldDeposit() {
        UUID id = UUID.randomUUID();
        Wallet wallet = new Wallet(id, BigDecimal.ZERO);
        when(walletGateway.findByIdForUpdate(id)).thenReturn(Optional.of(wallet));

        depositUseCase.execute(new TransactionInput(id, BigDecimal.TEN, "key"));

        assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.TEN);
        verify(walletGateway).save(wallet);
        verify(ledgerGateway).save(any());
    }

    @Test
    void shouldWithdraw() {
        UUID id = UUID.randomUUID();
        Wallet wallet = new Wallet(id, new BigDecimal("100"));
        when(walletGateway.findByIdForUpdate(id)).thenReturn(Optional.of(wallet));

        withdrawUseCase.execute(new TransactionInput(id, BigDecimal.TEN, "key"));

        assertThat(wallet.getBalance()).isEqualByComparingTo(new BigDecimal("90"));
        verify(walletGateway).save(wallet);
        verify(ledgerGateway).save(any());
    }

    @Test
    void shouldThrowWhenWalletNotFound() {
        UUID id = UUID.randomUUID();
        when(walletGateway.findByIdForUpdate(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> depositUseCase.execute(new TransactionInput(id, BigDecimal.TEN, "key")))
                .isInstanceOf(WalletNotFoundException.class);
    }
}

