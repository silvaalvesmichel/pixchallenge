package br.com.pix.wallet.infrastructure.gateway;

import br.com.pix.wallet.domain.gateway.WalletGateway;
import br.com.pix.wallet.domain.model.Wallet;
import br.com.pix.wallet.infrastructure.persistence.entity.WalletEntity;
import br.com.pix.wallet.infrastructure.persistence.repository.WalletJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WalletDatabaseGateway implements WalletGateway {

    private final WalletJpaRepository repository;

    @Override
    public Wallet save(Wallet wallet) {
        // Nota: Precisamos lidar com o versionamento se a entidade ja existir para não sobrescrever null
        // Simplificação: recuperamos a entity se existir para manter a version
        WalletEntity entityToSave;
        Optional<WalletEntity> existing = repository.findById(wallet.getId());

        if (existing.isPresent()) {
            entityToSave = existing.get();
            entityToSave.setBalance(wallet.getBalance());
        } else {
            entityToSave = WalletEntity.fromDomain(wallet);
        }

        return repository.save(entityToSave).toDomain();
    }

    @Override
    public Optional<Wallet> findById(UUID id) {
        return repository.findById(id).map(WalletEntity::toDomain);
    }

    @Override
    public Optional<Wallet> findByIdForUpdate(UUID id) {
        return repository.findByIdWithLock(id).map(WalletEntity::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
}
