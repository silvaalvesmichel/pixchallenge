package br.com.pix.wallet.application;

import br.com.pix.wallet.application.dto.RegisterPixKeyInput;
import br.com.pix.wallet.application.usecase.RegisterPixKeyUseCase;
import br.com.pix.wallet.domain.exception.WalletNotFoundException;
import br.com.pix.wallet.domain.gateway.PixKeyGateway;
import br.com.pix.wallet.domain.gateway.WalletGateway;
import br.com.pix.wallet.domain.model.enums.PixKeyType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterPixKeyUseCaseTest {

    @Mock
    WalletGateway walletGateway;
    @Mock
    PixKeyGateway pixKeyGateway;
    @InjectMocks
    RegisterPixKeyUseCase useCase;

    @Test
    void shouldRegisterKeySuccessfully() {
        UUID walletId = UUID.randomUUID();
        when(walletGateway.existsById(walletId)).thenReturn(true);
        when(pixKeyGateway.existsByValue("user@email.com")).thenReturn(false);
        when(pixKeyGateway.save(any())).thenAnswer(i -> i.getArguments()[0]);

        var input = new RegisterPixKeyInput(walletId, PixKeyType.EMAIL, "user@email.com");
        var output = useCase.execute(input);

        assertThat(output.key()).isEqualTo("user@email.com");
        verify(pixKeyGateway).save(any());
    }

    @Test
    void shouldThrowIfWalletNotFound() {
        UUID walletId = UUID.randomUUID();
        when(walletGateway.existsById(walletId)).thenReturn(false);

        var input = new RegisterPixKeyInput(walletId, PixKeyType.EMAIL, "a@a.com");

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(WalletNotFoundException.class);
    }

    @Test
    void shouldThrowIfKeyDuplicate() {
        UUID walletId = UUID.randomUUID();
        when(walletGateway.existsById(walletId)).thenReturn(true);
        when(pixKeyGateway.existsByValue("dup@key.com")).thenReturn(true);

        var input = new RegisterPixKeyInput(walletId, PixKeyType.EMAIL, "dup@key.com");

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Chave Pix ja registrada");
    }
}
