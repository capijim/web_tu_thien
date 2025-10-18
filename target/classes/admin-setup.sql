-- Script to create default admin user
-- Run this after creating the database schema

INSERT INTO admins (username, email, password, full_name, is_active, created_at, updated_at) 
VALUES ('admin', 'admin@webtuthien.com', 'admin123', 'Administrator', true, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    username = VALUES(username),
    email = VALUES(email),
    password = VALUES(password),
    full_name = VALUES(full_name),
    is_active = VALUES(is_active),
    updated_at = NOW();

-- You can also create additional admin users:
-- INSERT INTO admins (username, email, password, full_name, is_active, created_at, updated_at) 
-- VALUES ('superadmin', 'superadmin@webtuthien.com', 'superadmin123', 'Super Administrator', true, NOW(), NOW())
-- ON DUPLICATE KEY UPDATE 
--     username = VALUES(username),
--     email = VALUES(email),
--     password = VALUES(password),
--     full_name = VALUES(full_name),
--     is_active = VALUES(is_active),
--     updated_at = NOW();
