package br.com.pix.wallet.infrastructure.persistence.repository;

import br.com.pix.wallet.infrastructure.persistence.entity.LedgerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface LedgerJpaRepository extends JpaRepository<LedgerEntity, UUID> {

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN l.type = 'CREDIT' THEN l.amount ELSE -l.amount END), 0)
        FROM LedgerEntity l
        WHERE l.walletId = :walletId AND l.createdAt <= :date
    """)
    BigDecimal calculateBalanceUntil(UUID walletId, LocalDateTime date);
}
