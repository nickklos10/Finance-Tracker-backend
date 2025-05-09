-- ---------- USERS ------------------------------------------------
CREATE TABLE users (
                       id         BIGSERIAL PRIMARY KEY,
                       auth0_sub  VARCHAR(60) UNIQUE NOT NULL,
                       name       VARCHAR(100)       NOT NULL,
                       email      VARCHAR(100) UNIQUE NOT NULL
);

-- ---------- CATEGORIES ------------------------------------------
CREATE TABLE categories (
                            id          BIGSERIAL PRIMARY KEY,
                            name        TEXT UNIQUE NOT NULL,
                            description TEXT
);

-- ---------- TRANSACTIONS ----------------------------------------
CREATE TABLE transactions (
                              id           BIGSERIAL PRIMARY KEY,
                              description  TEXT          NOT NULL,
                              amount       NUMERIC(12,2) NOT NULL,
                              date         TIMESTAMP     NOT NULL,
                              type         TEXT          NOT NULL,        -- matches TransactionType enum
                              notes        TEXT,
                              category_id  BIGINT        NOT NULL REFERENCES categories(id)
);

-- Helpful indexes for queries & reports
CREATE INDEX idx_transactions_date     ON transactions(date);
CREATE INDEX idx_transactions_category ON transactions(category_id);
