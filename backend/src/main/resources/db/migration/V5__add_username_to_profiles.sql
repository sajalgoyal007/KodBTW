-- KodBTW Phase 10: Add unique username to profiles table

ALTER TABLE profiles ADD COLUMN username VARCHAR(50) NULL;

-- Safely and deterministically backfill existing profiles using their unique user_id
UPDATE profiles 
SET username = CONCAT('user-', user_id) 
WHERE username IS NULL OR username = '';

-- Enforce NOT NULL and UNIQUE constraint
-- Note: MySQL automatically builds a unique index for the unique constraint,
-- so an explicit secondary index on username is redundant and omitted.
ALTER TABLE profiles MODIFY COLUMN username VARCHAR(50) NOT NULL;
ALTER TABLE profiles ADD CONSTRAINT uk_profiles_username UNIQUE (username);
