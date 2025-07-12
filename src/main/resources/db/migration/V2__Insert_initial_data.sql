-- Insert default permissions
INSERT INTO permissions (name, description) VALUES
('USER_READ', 'Read user information'),
('USER_WRITE', 'Create and update user information'),
('USER_DELETE', 'Delete user accounts'),
('ROLE_READ', 'Read role information'),
('ROLE_WRITE', 'Create and update roles'),
('ROLE_DELETE', 'Delete roles'),
('ADMIN_ACCESS', 'Full administrative access');

-- Insert default roles
INSERT INTO roles (name, description) VALUES
('USER', 'Standard user role'),
('ADMIN', 'Administrator role with full access'),
('MODERATOR', 'Moderator role with limited administrative access');

-- Assign permissions to USER role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'USER' AND p.name = 'USER_READ';

-- Assign all permissions to ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ADMIN' AND p.name IN (
    'USER_READ', 'USER_WRITE', 'USER_DELETE',
    'ROLE_READ', 'ROLE_WRITE', 'ROLE_DELETE',
    'ADMIN_ACCESS'
);

-- Assign permissions to MODERATOR role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'MODERATOR' AND p.name IN (
    'USER_READ', 'USER_WRITE', 'ROLE_READ'
); 