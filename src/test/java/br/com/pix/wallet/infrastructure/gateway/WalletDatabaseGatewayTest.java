package br.com.pix.wallet.infrastructure.gateway;

import br.com.pix.wallet.domain.model.Wallet;
import br.com.pix.wallet.infrastructure.persistence.entity.WalletEntity;
import br.com.pix.wallet.infrastructure.persistence.repository.WalletJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletDatabaseGatewayTest {

    @Mock
    WalletJpaRepository repository;
    @InjectMocks WalletDatabaseGateway gateway;

    @Test
    void shouldSaveWallet() {
        Wallet wallet = new Wallet(UUID.randomUUID(), BigDecimal.TEN);
        when(repository.save(any())).thenReturn(WalletEntity.fromDomain(wallet));

        // Simula inexistência prévia
        when(repository.findById(wallet.getId())).thenReturn(Optional.empty());

        Wallet saved = gateway.save(wallet);
        assertThat(saved.getBalance()).isEqualByComparingTo(BigDecimal.TEN);
    }

    @Test
    void shouldFindByIdForUpdate() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdWithLock(id)).thenReturn(Optional.of(new WalletEntity(id, BigDecimal.ZERO, 1L)));

        Optional<Wallet> wallet = gateway.findByIdForUpdate(id);
        assertThat(wallet).isPresent();
    }
}
