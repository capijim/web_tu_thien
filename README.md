# Web Từ Thiện

Hệ thống quản lý hoạt động từ thiện - Spring Boot + Supabase PostgreSQL

## 🎯 Chức Năng

- ✅ Quản lý chiến dịch quyên góp
- ✅ Theo dõi donations
- ✅ Thanh toán VNPay
- ✅ Admin dashboard
- ✅ Email notifications

## 🚀 Chạy Local (Docker)

```bash
# Start Docker Desktop, sau đó:
docker-compose up
```

**Access:** http://localhost:8080

## 🌐 Deploy Production

**📖 [RAILWAY_DEPLOY.md](RAILWAY_DEPLOY.md) - Hướng dẫn chi tiết**

### Quick Deploy

```bash
# 1. Commit code
git add .
git commit -m "Deploy to Railway"
git push origin main

# 2. Railway Dashboard > New Project > Deploy from GitHub
# 3. Set variables (chỉ cần 2 biến):
SPRING_PROFILES_ACTIVE=railway
DATABASE_PASSWORD=zvBSwzV/@S8D?uvn

# 4. Done! App sẽ chạy sau 5-10 phút
```

### Config Info

- **Database:** Supabase PostgreSQL
- **Host:** db.gbzwqsyoihqtpcionaze.supabase.co
- **Email:** 222x3.666@gmail.com (đã config sẵn)

## 🔧 Cấu hình Supabase

### 1. Lấy Supabase Keys

1. Truy cập https://supabase.com/dashboard
2. Chọn project: `gbzwqsyoihqtpcionaze`
3. Settings > API:
   - **Project URL**: `https://gbzwqsyoihqtpcionaze.supabase.co`
   - **anon/public key** - dùng cho frontend real-time features
   - **service_role key** - dùng cho backend admin operations (GIỮ BÍ MẬT!)

### 2. Cấu hình Environment Variables

#### Local Development (application-local.properties)
```properties
# Supabase Configuration
supabase.url=https://gbzwqsyoihqtpcionaze.supabase.co
supabase.anon-key=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imdiendxc3lvaWhxdHBjaW9uYXplIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQxNTIwODYsImV4cCI6MjA3OTcyODA4Nn0.zQgjlkrV7Q8i8cKrjdJm21qqbruFUPEs0-0lWMHTzlY
supabase.service-role-key=your-service-role-key-here
supabase.storage.bucket=campaign-images
```

#### Railway Production
```bash
# Railway Dashboard > Variables > Add variables:
SUPABASE_URL=https://gbzwqsyoihqtpcionaze.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
SUPABASE_SERVICE_ROLE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 3. Cài đặt Row Level Security

```sql
-- Chạy trong Supabase SQL Editor
-- File: src/main/resources/schema-supabase-rls.sql

-- Enable RLS
ALTER TABLE campaigns ENABLE ROW LEVEL SECURITY;
ALTER TABLE donations ENABLE ROW LEVEL SECURITY;

-- Public read access
CREATE POLICY "Public can view campaigns" ON campaigns FOR SELECT USING (true);
CREATE POLICY "Public can view donations" ON donations FOR SELECT USING (true);

-- Authenticated insert
CREATE POLICY "Authenticated can insert donations" ON donations 
  FOR INSERT WITH CHECK (auth.role() = 'authenticated' OR auth.role() = 'anon');
```

### 4. Cấu hình Storage Bucket

1. Supabase Dashboard > Storage
2. Tạo bucket: `campaign-images`
3. Settings:
   - ✅ Public bucket
   - File size limit: 5MB
   - Allowed MIME types: image/jpeg, image/png, image/webp

4. Policies:
```sql
-- Allow public read
CREATE POLICY "Public Access" ON storage.objects FOR SELECT USING (bucket_id = 'campaign-images');

-- Allow authenticated upload
CREATE POLICY "Authenticated Upload" ON storage.objects FOR INSERT 
  WITH CHECK (bucket_id = 'campaign-images' AND auth.role() = 'authenticated');
```

## 📊 Supabase Features

### ✅ Đã tích hợp:

- PostgreSQL database (JDBC)
- Real-time subscriptions (WebSocket)
- Storage for images
- Row Level Security (RLS)

### 🔄 Real-time Updates:

- Donations list tự động cập nhật
- Campaign progress real-time
- Admin dashboard live data

### 📦 Storage:

- Upload campaign images
- CDN delivery
- Automatic optimization

## 🔧 Troubleshooting

```bash
# Test database connection (detailed info)
curl https://your-app.railway.app/api/health/db-info

# Simple database test
curl https://your-app.railway.app/api/health/db-test

# Spring Boot health check
curl https://your-app.railway.app/actuator/health

# View logs
railway logs --tail 100
```

### Expected Response (Success):
```json
{
  "status": "SUCCESS",
  "connected": true,
  "databaseProductName": "PostgreSQL",
  "databaseProductVersion": "15.x.x",
  "url": "jdbc:postgresql://db.gbzwqsyoihqtpcionaze.supabase.co:5432/postgres",
  "username": "postgres.gbzwqsyoihqtpcionaze",
  "tablesCount": 5,
  "tables": {
    "users": true,
    "campaigns": true,
    "donations": true,
    "admins": true
  }
}
```

## 🔐 Default Credentials

- Admin: `admin` / `admin123`

**⚠️ ĐỔI PASSWORD SAU KHI DEPLOY**

## 📊 Tech Stack

- Spring Boot 3.x + Spring Security
- PostgreSQL (Supabase)
- Thymeleaf + Bootstrap 5
- VNPay Payment Gateway

## 💰 Cost

- **Local:** FREE
- **Production:** $0-5/month (Railway + Supabase free tier)

## 🧪 Testing Supabase Connection

```bash
# 1. Test Supabase config API (should return URL and anon key)
curl http://localhost:8080/api/supabase/config

# 2. Test Supabase health
curl http://localhost:8080/api/supabase/health

# 3. Test database connection
curl http://localhost:8080/api/health/db-test

# 4. Test from browser console
fetch('/api/supabase/config').then(r => r.json()).then(console.log)
```

### Expected Responses:

**Supabase Config:**
```json
{
  "url": "https://gbzwqsyoihqtpcionaze.supabase.co",
  "anonKey": "eyJhbGci...",
  "storageBucket": "campaign-images"
}
```

**Supabase Health:**
```json
{
  "status": "healthy",
  "supabaseUrl": "https://gbzwqsyoihqtpcionaze.supabase.co",
  "configLoaded": true,
  "storageBucket": "campaign-images"
}
```

## ⚠️ Security Notes

1. **KHÔNG commit service role key** vào Git
2. **Anon key** là public key, có thể expose an toàn
3. **Service role key** chỉ dùng cho backend, có full admin access
4. Sử dụng Row Level Security (RLS) để bảo vệ data
5. Configure CORS trong Supabase Dashboard nếu cần


