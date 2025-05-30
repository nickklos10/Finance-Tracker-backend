-- Add user_id column to transactions table (nullable first)
ALTER TABLE transactions ADD COLUMN user_id BIGINT;

-- Add foreign key constraint
ALTER TABLE transactions ADD CONSTRAINT fk_transactions_user 
    FOREIGN KEY (user_id) REFERENCES users(id);

-- Add index for performance
CREATE INDEX idx_transactions_user ON transactions(user_id);

-- In a production environment, you would:
-- 1. Migrate existing data to assign proper user_ids
-- 2. Then make the column NOT NULL
-- For now, we'll handle orphaned transactions in the application layer 