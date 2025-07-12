-- Migration: Add account lockout fields to users table
-- Description: Adds fields for tracking failed login attempts and account lockout functionality

-- Add failed login attempts counter
ALTER TABLE users ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0;

-- Add account locked until timestamp
ALTER TABLE users ADD COLUMN account_locked_until TIMESTAMP NULL;

-- Add last failed login timestamp
ALTER TABLE users ADD COLUMN last_failed_login_at TIMESTAMP NULL;

-- Add index for performance on account lockout queries
CREATE INDEX idx_users_account_locked_until ON users(account_locked_until);
CREATE INDEX idx_users_failed_login_attempts ON users(failed_login_attempts); 