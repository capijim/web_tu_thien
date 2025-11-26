# Web Từ Thiện

Hệ thống quản lý hoạt động từ thiện - Spring Boot + PostgreSQL

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

## 🔧 Troubleshooting

```bash
# View logs
railway logs --tail 100

# Test health
curl https://your-app.railway.app/actuator/health

# Test database
PGPASSWORD='zvBSwzV/@S8D?uvn' psql -h db.gbzwqsyoihqtpcionaze.supabase.co -p 5432 -U postgres.gbzwqsyoihqtpcionaze -d postgres
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


