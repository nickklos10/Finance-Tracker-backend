-- 1) Ensure the users table exists
CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    auth0_sub VARCHAR(60) NOT NULL UNIQUE
);

-- 2) Add user_id columns
ALTER TABLE transactions
    ADD COLUMN IF NOT EXISTS user_id BIGINT;

ALTER TABLE categories
    ADD COLUMN IF NOT EXISTS user_id BIGINT;

-- 3) Add the foreign‐key constraints
ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_user
        FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE categories
    ADD CONSTRAINT fk_categories_user
        FOREIGN KEY (user_id) REFERENCES users(id);

-- 4) Seed a dev user and backfill existing rows
INSERT INTO users (auth0_sub)
VALUES ('dev|seed')
ON CONFLICT (auth0_sub) DO NOTHING;

UPDATE transactions
SET user_id = (SELECT id FROM users WHERE auth0_sub = 'dev|seed')
WHERE user_id IS NULL;

UPDATE categories
SET user_id = (SELECT id FROM users WHERE auth0_sub = 'dev|seed')
WHERE user_id IS NULL;

-- 5) Add name and email columns to the users table
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS name  VARCHAR(100);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS email VARCHAR(100);

UPDATE users
SET name  = 'Demo User',
    email = 'demo@example.com'
WHERE auth0_sub = 'dev|seed';

-- 6) Add indexes on user_id for performance
CREATE INDEX IF NOT EXISTS idx_transactions_user ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_categories_user ON categories(user_id);
