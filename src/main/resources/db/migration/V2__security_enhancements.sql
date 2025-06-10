-- =============================================================================
-- Migration V2: Security Enhancements
-- Description: Adds password history tracking and email confirmation system
-- Author: WebPet Team
-- Date: 2025-06-04
-- =============================================================================

-- Create password history table for preventing password reuse
CREATE TABLE password_history (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    user_id VARCHAR(36) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create email confirmations table for email verification
CREATE TABLE email_confirmations (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    user_id VARCHAR(36) NOT NULL,
    email VARCHAR(255) NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    confirmed BOOLEAN DEFAULT FALSE,
    confirmed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create login attempts table for brute force protection
CREATE TABLE login_attempts (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    email VARCHAR(255) NOT NULL,
    ip_address INET,
    success BOOLEAN NOT NULL DEFAULT FALSE,
    user_agent TEXT,
    attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    blocked_until TIMESTAMP
);

-- Create audit log table for security tracking
CREATE TABLE audit_logs (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    user_id VARCHAR(36),
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(100),
    ip_address INET,
    user_agent TEXT,
    correlation_id VARCHAR(36),
    details JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- INDEXES FOR PERFORMANCE
-- =============================================================================

-- Password history indexes
CREATE INDEX idx_password_history_user_id ON password_history(user_id);
CREATE INDEX idx_password_history_created_at ON password_history(created_at DESC);
CREATE INDEX idx_password_history_user_created ON password_history(user_id, created_at DESC);

-- Email confirmations indexes
CREATE INDEX idx_email_confirmations_token ON email_confirmations(token);
CREATE INDEX idx_email_confirmations_user_id ON email_confirmations(user_id);
CREATE INDEX idx_email_confirmations_email ON email_confirmations(email);
CREATE INDEX idx_email_confirmations_expires_at ON email_confirmations(expires_at);
CREATE INDEX idx_email_confirmations_confirmed ON email_confirmations(confirmed) WHERE confirmed = FALSE;

-- Login attempts indexes
CREATE INDEX idx_login_attempts_email ON login_attempts(email);
CREATE INDEX idx_login_attempts_ip ON login_attempts(ip_address);
CREATE INDEX idx_login_attempts_attempted_at ON login_attempts(attempted_at DESC);
CREATE INDEX idx_login_attempts_email_attempted ON login_attempts(email, attempted_at DESC);
CREATE INDEX idx_login_attempts_blocked_until ON login_attempts(blocked_until) WHERE blocked_until IS NOT NULL;

-- Audit logs indexes
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_logs_correlation_id ON audit_logs(correlation_id);
CREATE INDEX idx_audit_logs_ip_address ON audit_logs(ip_address);

-- =============================================================================
-- CONSTRAINTS AND VALIDATIONS
-- =============================================================================

-- Password history constraints
ALTER TABLE password_history 
    ADD CONSTRAINT chk_password_history_hash_length 
    CHECK (LENGTH(password_hash) >= 60); -- BCrypt minimum length

-- Email confirmations constraints
ALTER TABLE email_confirmations 
    ADD CONSTRAINT chk_email_confirmations_email_format 
    CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$');

ALTER TABLE email_confirmations 
    ADD CONSTRAINT chk_email_confirmations_token_length 
    CHECK (LENGTH(token) = 64); -- SHA-256 hex length

ALTER TABLE email_confirmations 
    ADD CONSTRAINT chk_email_confirmations_expires_future 
    CHECK (expires_at > created_at);

ALTER TABLE email_confirmations 
    ADD CONSTRAINT chk_email_confirmations_confirmed_logic 
    CHECK ((confirmed = TRUE AND confirmed_at IS NOT NULL) OR 
           (confirmed = FALSE AND confirmed_at IS NULL));

-- Login attempts constraints
ALTER TABLE login_attempts 
    ADD CONSTRAINT chk_login_attempts_email_format 
    CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$');

-- Audit logs constraints
ALTER TABLE audit_logs 
    ADD CONSTRAINT chk_audit_logs_action_not_empty 
    CHECK (LENGTH(TRIM(action)) > 0);

-- =============================================================================
-- TRIGGERS FOR AUTOMATIC TIMESTAMP UPDATES
-- =============================================================================

-- Function to update timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers for updated_at columns
CREATE TRIGGER trigger_password_history_updated_at
    BEFORE UPDATE ON password_history
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_email_confirmations_updated_at
    BEFORE UPDATE ON email_confirmations
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- CLEANUP FUNCTIONS
-- =============================================================================

-- Function to cleanup old password history (keep last 5 per user)
CREATE OR REPLACE FUNCTION cleanup_password_history()
RETURNS void AS $$
BEGIN
    DELETE FROM password_history 
    WHERE id NOT IN (
        SELECT id FROM (
            SELECT id, 
                   ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY created_at DESC) as rn
            FROM password_history
        ) ranked 
        WHERE rn <= 5
    );
END;
$$ LANGUAGE plpgsql;

-- Function to cleanup expired email confirmations
CREATE OR REPLACE FUNCTION cleanup_expired_confirmations()
RETURNS void AS $$
BEGIN
    DELETE FROM email_confirmations 
    WHERE expires_at < CURRENT_TIMESTAMP 
      AND confirmed = FALSE;
END;
$$ LANGUAGE plpgsql;

-- Function to cleanup old login attempts (keep last 30 days)
CREATE OR REPLACE FUNCTION cleanup_old_login_attempts()
RETURNS void AS $$
BEGIN
    DELETE FROM login_attempts 
    WHERE attempted_at < CURRENT_TIMESTAMP - INTERVAL '30 days';
END;
$$ LANGUAGE plpgsql;

-- Function to cleanup old audit logs (keep last 90 days)
CREATE OR REPLACE FUNCTION cleanup_old_audit_logs()
RETURNS void AS $$
BEGIN
    DELETE FROM audit_logs 
    WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '90 days';
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- TABLE COMMENTS FOR DOCUMENTATION
-- =============================================================================

COMMENT ON TABLE password_history IS 'Stores password history to prevent reuse of recent passwords';
COMMENT ON TABLE email_confirmations IS 'Stores email confirmation tokens for account verification';
COMMENT ON TABLE login_attempts IS 'Tracks login attempts for brute force protection';
COMMENT ON TABLE audit_logs IS 'Security audit trail for all user actions';

-- Column comments
COMMENT ON COLUMN password_history.user_id IS 'Reference to user ID from any user table (users, ongs, protetores)';
COMMENT ON COLUMN password_history.password_hash IS 'BCrypt hashed password (minimum 60 characters)';

COMMENT ON COLUMN email_confirmations.user_id IS 'Reference to user ID from any user table';
COMMENT ON COLUMN email_confirmations.token IS 'SHA-256 hex token for email confirmation (64 characters)';
COMMENT ON COLUMN email_confirmations.expires_at IS 'Token expiration timestamp (typically 24 hours from creation)';
COMMENT ON COLUMN email_confirmations.confirmed IS 'Whether email has been successfully confirmed';

COMMENT ON COLUMN login_attempts.email IS 'Email address used in login attempt';
COMMENT ON COLUMN login_attempts.ip_address IS 'Client IP address (supports IPv4 and IPv6)';
COMMENT ON COLUMN login_attempts.success IS 'Whether login attempt was successful';
COMMENT ON COLUMN login_attempts.blocked_until IS 'Timestamp until which account is blocked (null if not blocked)';

COMMENT ON COLUMN audit_logs.action IS 'Action performed (LOGIN, REGISTER, UPDATE_PROFILE, etc.)';
COMMENT ON COLUMN audit_logs.resource IS 'Resource affected by the action';
COMMENT ON COLUMN audit_logs.correlation_id IS 'Request correlation ID for tracing';
COMMENT ON COLUMN audit_logs.details IS 'Additional action details in JSON format';

-- =============================================================================
-- INITIAL DATA AND CONFIGURATION
-- =============================================================================

-- Insert cleanup job configuration (if you have a job scheduler)
-- This would be handled by application scheduling, not database
-- But documenting the cleanup intervals here:

-- Schedule recommendations:
-- - cleanup_password_history(): Daily at 2:00 AM
-- - cleanup_expired_confirmations(): Every 6 hours  
-- - cleanup_old_login_attempts(): Daily at 3:00 AM
-- - cleanup_old_audit_logs(): Weekly on Sunday at 4:00 AM

-- =============================================================================
-- SECURITY NOTES
-- =============================================================================

-- IMPORTANT SECURITY CONSIDERATIONS:
-- 1. password_history: Never expose password hashes in API responses
-- 2. email_confirmations: Tokens should be cryptographically secure (use SecureRandom)
-- 3. login_attempts: Implement rate limiting based on IP and email
-- 4. audit_logs: Ensure PII is not logged in details column
-- 5. All tables: Regular cleanup is essential for GDPR compliance
-- 6. Indexes: Monitor query performance and adjust as needed
-- 7. Constraints: Validate data integrity at application level as well

-- Grant appropriate permissions (adjust schema name as needed)
-- GRANT SELECT, INSERT ON password_history TO webpet_app;
-- GRANT SELECT, INSERT, UPDATE ON email_confirmations TO webpet_app;
-- GRANT SELECT, INSERT ON login_attempts TO webpet_app;
-- GRANT SELECT, INSERT ON audit_logs TO webpet_app;