package br.com.pix.wallet.infrastructure.persistence.repository;

import br.com.pix.wallet.infrastructure.persistence.entity.PixTransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PixTransferJpaRepository extends JpaRepository<PixTransferEntity, UUID> {
    Optional<PixTransferEntity> findByEndToEndId(String endToEndId);
}
