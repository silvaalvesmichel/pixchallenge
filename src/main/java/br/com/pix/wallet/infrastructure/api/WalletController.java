package br.com.pix.wallet.infrastructure.api;

import br.com.pix.wallet.application.dto.CreateWalletOutput;
import br.com.pix.wallet.application.dto.TransactionInput;
import br.com.pix.wallet.application.usecase.CreateWalletUseCase;
import br.com.pix.wallet.application.usecase.DepositUseCase;
import br.com.pix.wallet.application.usecase.GetBalanceUseCase;
import br.com.pix.wallet.application.usecase.WithdrawUseCase;
import br.com.pix.wallet.infrastructure.api.dto.TransactionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final CreateWalletUseCase createWalletUseCase;
    private final DepositUseCase depositUseCase;
    private final WithdrawUseCase withdrawUseCase;
    private final GetBalanceUseCase getBalanceUseCase;

    @PostMapping
    public ResponseEntity<CreateWalletOutput> create() {
        return ResponseEntity.status(HttpStatus.CREATED).body(createWalletUseCase.execute());
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<Void> deposit(
            @PathVariable UUID id,
            @RequestBody TransactionRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        depositUseCase.execute(new TransactionInput(id, request.amount(), idempotencyKey));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Void> withdraw(
            @PathVariable UUID id,
            @RequestBody TransactionRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        withdrawUseCase.execute(new TransactionInput(id, request.amount(), idempotencyKey));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getBalance(
            @PathVariable UUID id,
            @RequestParam(required = false) LocalDateTime at) {

        var balance = getBalanceUseCase.execute(id, at);
        return ResponseEntity.ok(balance);
    }
}
