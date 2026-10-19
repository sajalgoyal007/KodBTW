ALTER TABLE users MODIFY password VARCHAR(255) NULL;

CREATE TABLE user_auth_identities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(30) NOT NULL,
    provider_subject VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_auth_provider_subject UNIQUE (provider, provider_subject),
    CONSTRAINT uk_user_auth_user_provider UNIQUE (user_id, provider),
    CONSTRAINT fk_user_auth_identity_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
