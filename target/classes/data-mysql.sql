-- Insert default admin user
INSERT IGNORE INTO admins (username, email, password, full_name, is_active, created_at, updated_at) 
VALUES ('admin', 'admin@webtuthien.com', 'admin123', 'Administrator', true, NOW(), NOW());
