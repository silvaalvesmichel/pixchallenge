package br.com.pix.wallet.infrastructure.api;

import br.com.pix.wallet.application.dto.TransferPixInput;
import br.com.pix.wallet.application.dto.TransferPixOutput;
import br.com.pix.wallet.application.usecase.TransferPixUseCase;
import br.com.pix.wallet.infrastructure.api.dto.PixTransferRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pix")
@RequiredArgsConstructor
public class PixController {

    private final TransferPixUseCase transferPixUseCase;

    @PostMapping("/transfers")
    public ResponseEntity<TransferPixOutput> transfer(
            @RequestBody PixTransferRequest request,
            @RequestHeader(value = "Idempotency-Key") String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        var input = new TransferPixInput(
                request.fromWalletId(),
                request.toPixKey(),
                request.amount(),
                idempotencyKey
        );

        var output = transferPixUseCase.execute(input);
        return ResponseEntity.ok(output);
    }
}
