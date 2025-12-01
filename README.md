# Web Từ Thiện

Hệ thống quản lý hoạt động từ thiện - Spring Boot + Railway PostgreSQL

## 🎯 Chức Năng

- ✅ Quản lý chiến dịch quyên góp
- ✅ Theo dõi donations
- ✅ Thanh toán VNPay
- ✅ Admin dashboard
- ✅ Email notifications
- ✅ Upload ảnh local storage

## 🚀 Chạy Local (Docker)

```bash
# Start Docker Desktop, sau đó:
docker-compose up
```

**Access:** http://localhost:8080

## 🌐 Deploy Production (Railway)

### Quick Deploy

```bash
# 1. Commit code
git add .
git commit -m "Deploy to Railway"
git push origin main

# 2. Railway Dashboard > New Project > Deploy from GitHub

# 3. Set environment variables:
SPRING_PROFILES_ACTIVE=railway
SPRING_DATASOURCE_URL=jdbc:postgresql://hopper.proxy.rlwy.net:14179/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=ADfVtAfzyPNskyYwUcGblgbUaiChaimL

# 4. Deploy - App runs in 5-10 minutes
```

### Railway Environment Variables

#### ✅ Required:
```bash
SPRING_PROFILES_ACTIVE=railway
SPRING_DATASOURCE_URL=jdbc:postgresql://hopper.proxy.rlwy.net:14179/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=ADfVtAfzyPNskyYwUcGblgbUaiChaimL
```

#### 📧 Optional (Enable email notifications):
```bash
SPRING_MAIL_USERNAME=222x3.666@gmail.com
SPRING_MAIL_PASSWORD=<your-app-password>
```

#### 💳 Optional (Enable VNPay payment):
```bash
VNPAY_TMN_CODE=<your-vnpay-code>
VNPAY_HASH_SECRET=<your-vnpay-secret>
VNPAY_RETURN_URL=https://your-app.railway.app/vnpay/return
```

## 🔧 Troubleshooting

```bash
# Test database connection
curl https://your-app.railway.app/api/health/db-info

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
  "tablesCount": 6,
  "tables": {
    "users": true,
    "campaigns": true,
    "donations": true,
    "admins": true,
    "payments": true,
    "partners": true
  }
}
```

## 🔐 Default Credentials

- Admin: `admin` / `admin123`

**⚠️ ĐỔI PASSWORD SAU KHI DEPLOY**

## 📊 Tech Stack

- Spring Boot 3.x + Spring Security
- PostgreSQL (Railway)
- Thymeleaf + Bootstrap 5
- VNPay Payment Gateway
- Local File Storage

## 💰 Cost

- **Local:** FREE
- **Production:** $5/month (Railway PostgreSQL)

## 📦 File Upload

Files are stored in local filesystem:
- **Local:** `./uploads`
- **Docker:** `/app/uploads` (mounted volume)
- **Railway:** `/app/uploads` (ephemeral storage)

**Note:** Railway's filesystem is ephemeral - files will be lost on restart. For production, consider using:
- Cloudinary (free tier)
- imgbb (free unlimited)
- AWS S3
- Any CDN service


