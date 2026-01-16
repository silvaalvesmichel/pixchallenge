package br.com.pix.wallet.infrastructure.api;

import br.com.pix.wallet.application.dto.RegisterPixKeyInput;
import br.com.pix.wallet.application.dto.RegisterPixKeyOutput;
import br.com.pix.wallet.application.usecase.RegisterPixKeyUseCase;
import br.com.pix.wallet.domain.model.enums.PixKeyType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class PixKeyController {

    private final RegisterPixKeyUseCase registerPixKeyUseCase;

    public record PixKeyRequest(PixKeyType type, String key) {}

    @PostMapping("/{id}/pix-keys")
    public ResponseEntity<RegisterPixKeyOutput> register(
            @PathVariable UUID id,
            @RequestBody PixKeyRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) { // Header Opcional

        // Se quisermos ser estritos, podemos implementar a lógica de cache aqui também.
        // Para simplificar neste momento (já que não temos tabela de cache para chaves especificamente),
        // vamos confiar na Unique Constraint do banco, mas o contrato da API já nasce pronto.

        var input = new RegisterPixKeyInput(id, request.type(), request.key());
        var output = registerPixKeyUseCase.execute(input);

        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }
}
