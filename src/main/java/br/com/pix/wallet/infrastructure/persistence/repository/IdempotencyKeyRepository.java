package br.com.pix.wallet.infrastructure.persistence.repository;

import br.com.pix.wallet.infrastructure.persistence.entity.IdempotencyKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, String> {}
