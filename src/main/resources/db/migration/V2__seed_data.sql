-- Web Từ Thiện - Seed Data
-- Version: 1.0.0
-- Description: Insert default admin account and sample data

-- Insert default admin (password: admin123 - BCrypt hashed)
INSERT INTO admins (username, email, password, full_name, is_active) 
VALUES (
  'admin',
  'admin@webtuthien.com',
  '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUP1KUOYTa',
  'Administrator',
  TRUE
) ON CONFLICT (username) DO NOTHING;

-- Insert sample partner
INSERT INTO partners (name, email, phone, address)
VALUES (
  'Hội Chữ Thập Đỏ Việt Nam',
  'contact@redcross.vn',
  '1900-1234',
  'Số 82 Nguyễn Du, Hai Bà Trưng, Hà Nội'
) ON CONFLICT DO NOTHING;

-- Insert sample campaign
INSERT INTO campaigns (partner_id, title, description, target_amount, current_amount, category, status, end_date)
SELECT 
  p.id,
  'Hỗ trợ người dân vùng lũ miền Trung',
  'Chiến dịch quyên góp hỗ trợ người dân các tỉnh miền Trung bị ảnh hưởng bởi lũ lụt. Số tiền quyên góp sẽ được sử dụng để mua lương thực, thực phẩm, thuốc men và vật dụng sinh hoạt thiết yếu.',
  500000000.00,
  0.00,
  'Cứu trợ thiên tai',
  'active',
  CURRENT_TIMESTAMP + INTERVAL '30 days'
FROM partners p
WHERE p.email = 'contact@redcross.vn'
ON CONFLICT DO NOTHING;
