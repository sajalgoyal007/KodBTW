ALTER TABLE google_auth_challenges
    ADD COLUMN operation VARCHAR(16) NOT NULL DEFAULT 'LOGIN' AFTER nonce_hash,
    ADD COLUMN user_id BIGINT NULL AFTER operation,
    ADD INDEX idx_google_auth_challenges_operation_user_expiry (operation, user_id, expires_at),
    ADD CONSTRAINT fk_google_auth_challenge_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
