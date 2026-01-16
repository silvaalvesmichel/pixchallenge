package br.com.pix.wallet.infrastructure.gateway;

import br.com.pix.wallet.domain.gateway.PixKeyGateway;
import br.com.pix.wallet.domain.model.PixKey;
import br.com.pix.wallet.infrastructure.persistence.entity.PixKeyEntity;
import br.com.pix.wallet.infrastructure.persistence.repository.PixKeyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PixKeyDatabaseGateway implements PixKeyGateway {

    private final PixKeyJpaRepository repository;

    @Override
    public PixKey save(PixKey pixKey) {
        return repository.save(PixKeyEntity.fromDomain(pixKey)).toDomain();
    }

    @Override
    public boolean existsByValue(String value) {
        return repository.existsByValue(value);
    }

    @Override
    public Optional<PixKey> findByKey(String key) {
        return repository.findByValue(key).map(PixKeyEntity::toDomain);
    }
}
