CREATE TABLE pix_keys (
    id UUID PRIMARY KEY,
    wallet_id UUID NOT NULL REFERENCES wallets(id),
    type VARCHAR(20) NOT NULL, -- CPF, EMAIL, PHONE, RANDOM
    key_value VARCHAR(255) NOT NULL UNIQUE, -- Garante unicidade no sistema
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pix_keys_wallet ON pix_keys(wallet_id);