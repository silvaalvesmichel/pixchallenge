package br.com.pix.wallet.application.usecase;

import br.com.pix.wallet.application.dto.CreateWalletOutput;
import br.com.pix.wallet.domain.gateway.WalletGateway;
import br.com.pix.wallet.domain.model.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateWalletUseCase {

    private final WalletGateway walletGateway;

    @Transactional
    public CreateWalletOutput execute() {
        var wallet = Wallet.create();
        var savedWallet = walletGateway.save(wallet);
        return new CreateWalletOutput(savedWallet.getId(), savedWallet.getBalance());
    }
}
