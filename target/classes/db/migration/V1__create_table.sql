CREATE TABLE wallets (
    id UUID PRIMARY KEY,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00 CHECK (balance >= 0),
    version BIGINT -- Para optimistic lock
);

CREATE TABLE ledger (
    id UUID PRIMARY KEY,
    wallet_id UUID NOT NULL REFERENCES wallets(id),
    amount DECIMAL(19, 2) NOT NULL,
    type VARCHAR(20) NOT NULL, -- CREDIT, DEBIT
    description VARCHAR(255),
    correlation_id VARCHAR(100), -- endToEndId ou IdempotencyKey
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ledger_wallet_date ON ledger(wallet_id, created_at);