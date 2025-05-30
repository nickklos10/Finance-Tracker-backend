-- Fix the user_id constraint to match JPA entity
-- In production, you would first ensure all transactions have valid user_ids

-- For development/fresh databases, we can safely make user_id NOT NULL
-- If there are existing transactions without user_id, they would need to be handled first

-- Make user_id NOT NULL to match the JPA entity
ALTER TABLE transactions ALTER COLUMN user_id SET NOT NULL; 