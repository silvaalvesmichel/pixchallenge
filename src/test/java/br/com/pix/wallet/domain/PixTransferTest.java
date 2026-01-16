package br.com.pix.wallet.domain;

import br.com.pix.wallet.domain.model.PixTransfer;
import br.com.pix.wallet.domain.model.enums.TransferStatus;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class PixTransferTest {

    @Test
    void shouldCreatePendingTransfer() {
        PixTransfer transfer = PixTransfer.create(UUID.randomUUID(), BigDecimal.TEN, "key");
        assertThat(transfer.getStatus()).isEqualTo(TransferStatus.PENDING);
        assertThat(transfer.getEndToEndId()).startsWith("E");
    }

    @Test
    void shouldConfirmTransfer() {
        PixTransfer transfer = PixTransfer.create(UUID.randomUUID(), BigDecimal.TEN, "key");
        transfer.confirm();
        assertThat(transfer.getStatus()).isEqualTo(TransferStatus.CONFIRMED);
        assertThat(transfer.isTerminalState()).isTrue();
    }

    @Test
    void shouldRejectTransfer() {
        PixTransfer transfer = PixTransfer.create(UUID.randomUUID(), BigDecimal.TEN, "key");
        transfer.reject();
        assertThat(transfer.getStatus()).isEqualTo(TransferStatus.REJECTED);
        assertThat(transfer.isTerminalState()).isTrue();
    }

    @Test
    void shouldIgnoreStateChangeIfTerminal() {
        PixTransfer transfer = PixTransfer.create(UUID.randomUUID(), BigDecimal.TEN, "key");
        transfer.confirm(); // Terminal

        transfer.reject(); // Should be ignored
        assertThat(transfer.getStatus()).isEqualTo(TransferStatus.CONFIRMED);
    }
}
