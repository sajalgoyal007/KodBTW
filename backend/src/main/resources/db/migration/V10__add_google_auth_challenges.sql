CREATE TABLE google_auth_challenges (
    challenge_id VARCHAR(36) NOT NULL PRIMARY KEY,
    nonce_hash BINARY(32) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_google_auth_challenges_expires_at (expires_at)
);
