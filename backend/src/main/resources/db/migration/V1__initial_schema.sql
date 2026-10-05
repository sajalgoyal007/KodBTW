-- KodBTW Phase 2: Minimal Flyway Schema Verification
CREATE TABLE IF NOT EXISTS schema_initialization_check (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    initialized_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL
);

INSERT INTO schema_initialization_check (status) VALUES ('INITIALIZED');
