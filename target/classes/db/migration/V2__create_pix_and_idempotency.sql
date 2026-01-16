-- Tabela para garantir processamento único por chave
CREATE TABLE idempotency_keys (
    key_id VARCHAR(255) PRIMARY KEY, -- A chave enviada pelo cliente
    response_status INT NOT NULL,
    response_body TEXT, -- JSON do resultado cacheado
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Tabela para controlar o estado da transferência Pix
CREATE TABLE pix_transfers (
    id UUID PRIMARY KEY,
    end_to_end_id VARCHAR(100) NOT NULL UNIQUE,
    wallet_id UUID NOT NULL REFERENCES wallets(id),
    amount DECIMAL(19, 2) NOT NULL,
    to_pix_key VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL, -- PENDING, CONFIRMED, REJECTED
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE INDEX idx_pix_wallet ON pix_transfers(wallet_id);