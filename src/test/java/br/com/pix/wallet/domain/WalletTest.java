package br.com.pix.wallet.domain;

import br.com.pix.wallet.domain.exception.InsufficientBalanceException;
import br.com.pix.wallet.domain.model.Wallet;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WalletTest {

    @Test
    void shouldCreateEmptyWallet() {
        Wallet wallet = Wallet.create();
        assertThat(wallet.getId()).isNotNull();
        assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldCreditAmount() {
        Wallet wallet = Wallet.create();
        wallet.credit(new BigDecimal("100.00"));
        assertThat(wallet.getBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void shouldThrowErrorOnInvalidCredit() {
        Wallet wallet = Wallet.create();
        assertThatThrownBy(() -> wallet.credit(new BigDecimal("-10")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldDebitAmountWhenBalanceIsSufficient() {
        Wallet wallet = Wallet.create();
        wallet.credit(new BigDecimal("100.00"));
        wallet.debit(new BigDecimal("50.00"));
        assertThat(wallet.getBalance()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void shouldThrowErrorOnInsufficientBalance() {
        Wallet wallet = Wallet.create();
        wallet.credit(new BigDecimal("10.00"));

        assertThatThrownBy(() -> wallet.debit(new BigDecimal("20.00")))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    void shouldThrowErrorOnInvalidDebit() {
        Wallet wallet = Wallet.create();
        assertThatThrownBy(() -> wallet.debit(new BigDecimal("0")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

