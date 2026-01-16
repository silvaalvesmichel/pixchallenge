package br.com.pix.wallet.infrastructure.persistence.repository;

import br.com.pix.wallet.infrastructure.persistence.entity.PixEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PixEventJpaRepository extends JpaRepository<PixEventEntity, String> {}
