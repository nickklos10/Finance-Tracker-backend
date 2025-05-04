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
