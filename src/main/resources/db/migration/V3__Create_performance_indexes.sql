-- Create performance indexes for better query performance

-- Indexes for users table
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_enabled ON users(is_enabled);
CREATE INDEX idx_users_email_verified ON users(is_email_verified);
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_users_last_login_at ON users(last_login_at);

-- Indexes for roles table
CREATE INDEX idx_roles_name ON roles(name);

-- Indexes for permissions table
CREATE INDEX idx_permissions_name ON permissions(name);

-- Indexes for refresh_tokens table
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expiry_date ON refresh_tokens(expiry_date);
CREATE INDEX idx_refresh_tokens_user_expiry ON refresh_tokens(user_id, expiry_date);

-- Indexes for user_roles junction table
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
CREATE INDEX idx_user_roles_user_role ON user_roles(user_id, role_id);

-- Indexes for role_permissions junction table
CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);
CREATE INDEX idx_role_permissions_role_permission ON role_permissions(role_id, permission_id); 