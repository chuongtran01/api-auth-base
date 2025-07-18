-- Migration: Create security_events table
-- Description: Creates table for logging security-related events

CREATE TABLE security_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type ENUM('LOGIN_ATTEMPT','LOGIN_SUCCESS','LOGIN_FAILURE','LOGOUT','PASSWORD_CHANGE','PASSWORD_RESET_REQUEST','PASSWORD_RESET_SUCCESS','ACCOUNT_LOCKED','ACCOUNT_UNLOCKED','EMAIL_VERIFICATION','REGISTRATION','ACCOUNT_DISABLED','ACCOUNT_ENABLED','ROLE_ASSIGNED','ROLE_REMOVED','SUSPICIOUS_ACTIVITY','TOKEN_REFRESH','TOKEN_INVALID','SESSION_EXPIRED') NOT NULL,
    description VARCHAR(500) NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    success BOOLEAN NOT NULL,
    details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Add indexes for performance
CREATE INDEX idx_security_events_event_type ON security_events(event_type);
CREATE INDEX idx_security_events_created_at ON security_events(created_at);
CREATE INDEX idx_security_events_user_id ON security_events(user_id);
CREATE INDEX idx_security_events_success ON security_events(success);
CREATE INDEX idx_security_events_ip_address ON security_events(ip_address); 