package br.com.pix.wallet.domain.gateway;

import br.com.pix.wallet.domain.model.Wallet;

import java.util.Optional;
import java.util.UUID;

public interface WalletGateway {

    Wallet save(Wallet wallet);
    Optional<Wallet> findById(UUID id);
    Optional<Wallet> findByIdForUpdate(UUID id);
    boolean existsById(UUID id);
}
