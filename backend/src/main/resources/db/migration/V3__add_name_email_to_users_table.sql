-- 1) Add name and email columns to the users table
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS name  VARCHAR(100);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS email VARCHAR(100);


UPDATE users
SET name  = 'Demo User',
    email = 'demo@example.com'
WHERE auth0_sub = 'dev|seed';
