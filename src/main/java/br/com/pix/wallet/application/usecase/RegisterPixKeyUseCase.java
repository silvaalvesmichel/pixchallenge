package br.com.pix.wallet.application.usecase;

import br.com.pix.wallet.application.dto.RegisterPixKeyInput;
import br.com.pix.wallet.application.dto.RegisterPixKeyOutput;
import br.com.pix.wallet.domain.exception.WalletNotFoundException;
import br.com.pix.wallet.domain.gateway.PixKeyGateway;
import br.com.pix.wallet.domain.gateway.WalletGateway;
import br.com.pix.wallet.domain.model.PixKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterPixKeyUseCase {

    private final WalletGateway walletGateway;
    private final PixKeyGateway pixKeyGateway;

    @Transactional
    public RegisterPixKeyOutput execute(RegisterPixKeyInput input) {
        // 1. Valida Carteira
        if (!walletGateway.existsById(input.walletId())) {
            throw new WalletNotFoundException("Wallet not found");
        }

        // 2. Valida Unicidade Global da Chave
        if (pixKeyGateway.existsByValue(input.key())) {
            throw new IllegalArgumentException("Chave Pix ja registrada no sistema.");
        }

        // 3. Cria e Salva
        var pixKey = PixKey.create(input.walletId(), input.type(), input.key());
        var savedKey = pixKeyGateway.save(pixKey);

        return new RegisterPixKeyOutput(savedKey.getId(), savedKey.getType(), savedKey.getValue(), savedKey.getCreatedAt());
    }
}
