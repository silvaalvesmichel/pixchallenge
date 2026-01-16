package br.com.pix.wallet.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_keys")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class IdempotencyKeyEntity {
    @Id
    @Column(name = "key_id")
    private String key;

    @Column(name = "response_status")
    private Integer status;

    @Column(name = "response_body")
    private String body; // Armazenamos o JSON de resposta

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
