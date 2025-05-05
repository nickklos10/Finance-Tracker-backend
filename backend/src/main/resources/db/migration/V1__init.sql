-- V1__init.sql  (initial schema)
CREATE TABLE IF NOT EXISTS categories (
        id          BIGSERIAL PRIMARY KEY,
        name        TEXT UNIQUE NOT NULL,
        description TEXT
);

CREATE TABLE IF NOT EXISTS transactions (
    id          BIGSERIAL PRIMARY KEY,
    description TEXT           NOT NULL,
    amount      NUMERIC(12,2)  NOT NULL,
    date        TIMESTAMP      NOT NULL,
    type        TEXT           NOT NULL,
    notes       TEXT,
    category_id BIGINT         NOT NULL REFERENCES categories(id)
    );

-- Helpful indexes
CREATE INDEX IF NOT EXISTS idx_transactions_date
    ON transactions(date);
CREATE INDEX IF NOT EXISTS idx_transactions_type
    ON transactions(type);
CREATE INDEX IF NOT EXISTS idx_transactions_category
    ON transactions(category_id);
