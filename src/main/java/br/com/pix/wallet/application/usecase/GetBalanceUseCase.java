package br.com.pix.wallet.application.usecase;

import br.com.pix.wallet.domain.exception.WalletNotFoundException;
import br.com.pix.wallet.domain.gateway.LedgerGateway;
import br.com.pix.wallet.domain.gateway.WalletGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetBalanceUseCase {

    private final WalletGateway walletGateway;
    private final LedgerGateway ledgerGateway;

    @Transactional(readOnly = true)
    public BigDecimal execute(UUID walletId, LocalDateTime atDate) {
        if (!walletGateway.existsById(walletId)) {
            throw new WalletNotFoundException("Wallet not found");
        }

        if (atDate == null) {
            // Saldo atual
            return walletGateway.findById(walletId)
                    .orElseThrow()
                    .getBalance();
        }

        // Saldo histórico
        return ledgerGateway.getBalanceAt(walletId, atDate);
    }
}
