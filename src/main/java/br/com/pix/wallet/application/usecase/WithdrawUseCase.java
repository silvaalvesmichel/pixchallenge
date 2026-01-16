package br.com.pix.wallet.application.usecase;

import br.com.pix.wallet.application.dto.TransactionInput;
import br.com.pix.wallet.domain.exception.WalletNotFoundException;
import br.com.pix.wallet.domain.gateway.LedgerGateway;
import br.com.pix.wallet.domain.gateway.WalletGateway;
import br.com.pix.wallet.domain.model.LedgerEntry;
import br.com.pix.wallet.domain.model.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WithdrawUseCase {

    private final WalletGateway walletGateway;
    private final LedgerGateway ledgerGateway;

    @Transactional
    public void execute(TransactionInput input) {

        var wallet = walletGateway.findByIdForUpdate(input.walletId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + input.walletId()));

        wallet.debit(input.amount());
        walletGateway.save(wallet);

        var ledger = LedgerEntry.create(
                wallet.getId(),
                input.amount(),
                TransactionType.DEBIT,
                "WITHDRAW",
                input.idempotencyKey()
        );
        ledgerGateway.save(ledger);
    }
}

