package br.com.pix.wallet;

import br.com.pix.wallet.application.dto.TransactionInput;
import br.com.pix.wallet.application.usecase.CreateWalletUseCase;
import br.com.pix.wallet.application.usecase.DepositUseCase;
import br.com.pix.wallet.application.usecase.WithdrawUseCase;
import br.com.pix.wallet.domain.gateway.WalletGateway;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
public class ConcurrencyTest {

    @Autowired private CreateWalletUseCase createWalletUseCase;
    @Autowired private DepositUseCase depositUseCase;
    @Autowired private WithdrawUseCase withdrawUseCase;
    @Autowired private WalletGateway walletGateway;

    @Test
    public void testRaceConditionOnWithdraw() throws InterruptedException {
        // 1. Setup: Carteira com 100.00
        var walletOut = createWalletUseCase.execute();
        UUID walletId = walletOut.id();
        depositUseCase.execute(new TransactionInput(walletId, new BigDecimal("100.00"), "INIT"));

        // 2. Cenário: 20 threads tentando sacar 10.00 simultaneamente.
        // Total esperado de saque: 200.00. Saldo disponível: 100.00.
        // O sistema DEVE permitir apenas 10 saques e falhar os outros 10.
        int numberOfThreads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    withdrawUseCase.execute(new TransactionInput(walletId, new BigDecimal("10.00"), UUID.randomUUID().toString()));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet(); // Esperamos exceções de saldo insuficiente
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // Espera todos terminarem

        // 3. Verificação
        var finalWallet = walletGateway.findById(walletId).get();
        System.out.println("Sucessos: " + successCount.get());
        System.out.println("Falhas: " + failCount.get());
        System.out.println("Saldo Final: " + finalWallet.getBalance());

        // O saldo deve ser ZERO, nunca negativo.
        Assertions.assertEquals(0, finalWallet.getBalance().compareTo(BigDecimal.ZERO));
        // Deve ter exatamente 10 sucessos (10 * 10 = 100)
        Assertions.assertEquals(10, successCount.get());
    }
}
