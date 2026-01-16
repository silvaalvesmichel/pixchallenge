package br.com.pix.wallet.infrastructure.api;

import br.com.pix.wallet.application.dto.CreateWalletOutput;
import br.com.pix.wallet.application.usecase.CreateWalletUseCase;
import br.com.pix.wallet.application.usecase.DepositUseCase;
import br.com.pix.wallet.application.usecase.GetBalanceUseCase;
import br.com.pix.wallet.application.usecase.WithdrawUseCase;
import br.com.pix.wallet.domain.exception.InsufficientBalanceException;
import br.com.pix.wallet.domain.exception.WalletNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
class WalletControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean
    CreateWalletUseCase createWalletUseCase;
    @MockitoBean
    DepositUseCase depositUseCase;
    @MockitoBean
    WithdrawUseCase withdrawUseCase;
    @MockitoBean
    GetBalanceUseCase getBalanceUseCase; // Mock necessário se usado no controller

    @Test
    void shouldCreateWallet() throws Exception {
        UUID id = UUID.randomUUID();
        when(createWalletUseCase.execute()).thenReturn(new CreateWalletOutput(id, BigDecimal.ZERO));

        mockMvc.perform(post("/wallets"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void shouldDeposit() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(post("/wallets/" + id + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 100}"))
                .andExpect(status().isOk());

        verify(depositUseCase).execute(any());
    }

    @Test
    void shouldHandleWalletNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new WalletNotFoundException("Not found")).when(depositUseCase).execute(any());

        mockMvc.perform(post("/wallets/" + id + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 100}"))
                .andExpect(status().isNotFound()); // 404
    }

    @Test
    void shouldHandleInsufficientBalance() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new InsufficientBalanceException("No money")).when(withdrawUseCase).execute(any());

        mockMvc.perform(post("/wallets/" + id + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 100}"))
                .andExpect(status().isUnprocessableEntity()); // 422
    }
}
