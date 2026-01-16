-- Tabela para logar eventos recebidos e garantir que não processamos o mesmo eventId duas vezes
CREATE TABLE pix_events (
    event_id VARCHAR(255) PRIMARY KEY, -- Id único do evento enviado pelo PSP
    end_to_end_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(50) NOT NULL, -- CONFIRMED, REJECTED
    occurred_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pix_events_e2e ON pix_events(end_to_end_id);