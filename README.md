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
   - `anon` key (public) - dùng cho frontend
   - `service_role` key - dùng cho backend admin operations

### 2. Cài đặt Row Level Security

```bash
# Chạy file SQL trong Supabase SQL Editor
cat src/main/resources/schema-supabase-rls.sql
```

### 3. Cấu hình Storage Bucket

1. Supabase Dashboard > Storage
2. Tạo bucket: `campaign-images`
3. Public access: ✅ Enable
4. File size limit: 5MB

### 4. Environment Variables

```bash
# Local (.env)
SUPABASE_ANON_KEY=your-anon-key
SUPABASE_SERVICE_KEY=your-service-key

# Railway
SUPABASE_ANON_KEY=your-anon-key
SUPABASE_SERVICE_KEY=your-service-key
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
# Test database
curl http://localhost:8080/api/health/db-test

# Test Supabase config
curl http://localhost:8080/api/supabase/health

# Test Supabase config API
curl http://localhost:8080/api/supabase/config
```


