package br.com.pix.wallet.domain.model;

import br.com.pix.wallet.domain.exception.InsufficientBalanceException;

import java.math.BigDecimal;
import java.util.UUID;

public class Wallet {
    private final UUID id;
    private BigDecimal balance;

    public Wallet(UUID id, BigDecimal balance) {
        this.id = id;
        this.balance = balance != null ? balance : BigDecimal.ZERO;
    }

    public static Wallet create() {
        return new Wallet(UUID.randomUUID(), BigDecimal.ZERO);
    }

    public void debit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor de débito deve ser positivo");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente");
        }
        this.balance = this.balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor de crédito deve ser positivo");
        }
        this.balance = this.balance.add(amount);
    }

    public UUID getId() {
        return id;
    }
    public BigDecimal getBalance() {
        return balance;
    }
}