package br.com.pix.wallet.domain.gateway;

import br.com.pix.wallet.domain.model.PixKey;

import java.util.Optional;

public interface PixKeyGateway {
    PixKey save(PixKey pixKey);
    boolean existsByValue(String value);
    Optional<PixKey> findByKey(String key);
}
