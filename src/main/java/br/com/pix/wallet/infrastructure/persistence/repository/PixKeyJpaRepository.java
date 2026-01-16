package br.com.pix.wallet.infrastructure.persistence.repository;

import br.com.pix.wallet.infrastructure.persistence.entity.PixKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PixKeyJpaRepository extends JpaRepository<PixKeyEntity, UUID> {
    boolean existsByValue(String value);
    Optional<PixKeyEntity> findByValue(String value);

}
