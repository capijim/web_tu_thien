# Hướng dẫn Setup Supabase

## 📋 Thông tin Supabase Project

- **Project URL:** https://gbzwqsyoihqtpcionaze.supabase.co
- **Project Ref:** gbzwqsyoihqtpcionaze
- **Region:** Southeast Asia (Singapore)

## 🔑 API Keys (Đã cấu hình)

```bash
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imdiendxc3lvaWhxdHBjaW9uYXplIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQxNTIwODYsImV4cCI6MjA3OTcyODA4Nn0.zQgjlkrV7Q8i8cKrjdJm21qqbruFUPEs0-0lWMHTzlY

SUPABASE_SERVICE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imdiendxc3lvaWhxdHBjaW9uYXplIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc2NDE1MjA4NiwiZXhwIjoyMDc5NzI4MDg2fQ.tv8G5TZfdDwO05cbgPcXbXlNHFPqI5j_eLD9tEHRB4s
```

## ✅ Các bước đã hoàn thành

1. ✅ Database connection qua JDBC
2. ✅ Supabase JavaScript Client integration
3. ✅ API keys đã được cấu hình
4. ✅ Environment variables đã setup

## 🔧 Các bước cần làm tiếp

### 1. Chạy Schema SQL

Truy cập Supabase Dashboard > SQL Editor và chạy:

```sql
-- File: src/main/resources/schema-postgresql.sql
-- Tạo các bảng: users, partners, campaigns, donations, admins, payments
```

### 2. Setup Row Level Security (Optional)

```sql
-- File: src/main/resources/schema-supabase-rls.sql
-- Enable RLS và tạo policies
```

### 3. Tạo Storage Bucket

1. Dashboard > Storage > New Bucket
2. Name: `campaign-images`
3. Public: ✅ Yes
4. File size limit: 5MB

### 4. Test Connection

```bash
# Open in browser
open test-supabase.html

# Hoặc
npm install -g http-server
http-server . -p 8000
# Mở: http://localhost:8000/test-supabase.html
```

## 🧪 Testing

### Test Database Connection (Backend)

```bash
# Local
mvn spring-boot:run

# Test API
curl http://localhost:8080/api/health/db-test
curl http://localhost:8080/api/supabase/health
```

### Test Supabase Client (Frontend)

Mở `test-supabase.html` trong browser và click "Kiểm tra kết nối"

Expected result:
- ✅ Connection successful
- ✅ Can query campaigns table
- ✅ Can query donations table

## 🚀 Deploy to Railway

```bash
# Set environment variables in Railway Dashboard
SPRING_PROFILES_ACTIVE=railway
DATABASE_PASSWORD=zvBSwzV/@S8D?uvn
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
SUPABASE_SERVICE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## 📊 Database Schema

Tables:
- `users` - Người dùng
- `partners` - Đối tác
- `campaigns` - Chiến dịch
- `donations` - Ủng hộ
- `admins` - Quản trị viên
- `payments` - Thanh toán

## 🔐 Security

- ✅ Anon key - dùng cho frontend (public)
- ✅ Service role key - dùng cho backend admin operations
- ⚠️ KHÔNG commit service role key vào Git public repo
- ✅ Sử dụng environment variables

## 🆘 Troubleshooting

### Lỗi: "relation does not exist"

```bash
# Chạy schema SQL trong Supabase Dashboard
```

### Lỗi: "new row violates row-level security policy"

```bash
# Tắt RLS hoặc cấu hình policies đúng
ALTER TABLE campaigns DISABLE ROW LEVEL SECURITY;
```

### Lỗi: Connection timeout

```bash
# Check network/firewall
# Verify Supabase project is active
```
