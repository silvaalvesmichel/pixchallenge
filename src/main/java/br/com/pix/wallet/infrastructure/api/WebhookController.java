package br.com.pix.wallet.infrastructure.api;

import br.com.pix.wallet.application.dto.WebhookInput;
import br.com.pix.wallet.application.usecase.ProcessWebhookUseCase;
import br.com.pix.wallet.domain.model.enums.PixEventType;
import br.com.pix.wallet.infrastructure.api.dto.WebhookRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pix")
@RequiredArgsConstructor
public class WebhookController {

    private final ProcessWebhookUseCase processWebhookUseCase;

    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveWebhook(@RequestBody WebhookRequest request) {

        PixEventType type;
        try {
            type = PixEventType.valueOf(request.eventType());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        var input = new WebhookInput(
                request.endToEndId(),
                request.eventId(),
                type,
                request.occurredAt()
        );

        processWebhookUseCase.execute(input);
        return ResponseEntity.ok().build();
    }
}
