-- KodBTW Phase 4: Email Verification Schema and Existing-User Migration
-- 1. Add email verification columns to users table
ALTER TABLE users
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN email_verified_at TIMESTAMP NULL;

-- 2. Create email_verification_tokens table
CREATE TABLE email_verification_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_email_verification_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_email_verification_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_email_verification_tokens_user ON email_verification_tokens (user_id, created_at);
CREATE INDEX idx_email_verification_tokens_expiry ON email_verification_tokens (expires_at);

-- 3. Migration Policy for Existing Accounts:
-- A. Google OAuth users:
-- Google Identity Services verified email ownership during authentication.
-- Safely mark users with an existing verified Google identity as verified.
UPDATE users u
INNER JOIN user_auth_identities uai ON u.id = uai.user_id AND uai.provider = 'GOOGLE'
SET u.email_verified = TRUE, u.email_verified_at = CURRENT_TIMESTAMP;

-- B. Existing password-only users:
-- Explicit Policy: Strict Security (Do NOT blanket backfill as verified).
-- Existing password users remain email_verified = FALSE.
-- Upon next login, they will receive an EMAIL_NOT_VERIFIED error and can immediately
-- trigger a verification link via POST /api/auth/resend-verification.
