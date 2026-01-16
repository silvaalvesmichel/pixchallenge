package br.com.pix.wallet.infrastructure.gateway;

import br.com.pix.wallet.domain.gateway.PixEventGateway;
import br.com.pix.wallet.domain.model.PixEvent;
import br.com.pix.wallet.infrastructure.persistence.entity.PixEventEntity;
import br.com.pix.wallet.infrastructure.persistence.repository.PixEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PixEventDatabaseGateway implements PixEventGateway {

    private final PixEventJpaRepository repository;

    @Override
    public boolean existsByEventId(String eventId) {
        return repository.existsById(eventId);
    }

    @Override
    public void save(PixEvent event) {
        repository.save(PixEventEntity.fromDomain(event));
    }
}

