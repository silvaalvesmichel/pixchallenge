package br.com.pix.wallet.infrastructure.gateway;

import br.com.pix.wallet.domain.gateway.PixGateway;
import br.com.pix.wallet.domain.model.PixTransfer;
import br.com.pix.wallet.infrastructure.persistence.entity.PixTransferEntity;
import br.com.pix.wallet.infrastructure.persistence.repository.PixTransferJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PixDatabaseGateway implements PixGateway {

    private final PixTransferJpaRepository repository;

    @Override
    public PixTransfer save(PixTransfer transfer) {
        return repository.save(PixTransferEntity.fromDomain(transfer)).toDomain();
    }

    @Override
    public Optional<PixTransfer> findByEndToEndId(String endToEndId) {
        return repository.findByEndToEndId(endToEndId).map(PixTransferEntity::toDomain);
    }
}
