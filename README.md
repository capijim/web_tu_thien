# Web Từ Thiện

Hệ thống quản lý hoạt động từ thiện - Spring Boot + Railway PostgreSQL

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

## 🌐 Deploy Production (Railway)

### Railway Environment Variables

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

## 🔧 Troubleshooting

```bash
# Test database connection
curl https://your-app.railway.app/api/health/db-test

# Spring Boot health check
curl https://your-app.railway.app/actuator/health
```

## 🔐 Default Credentials

- Admin: `admin` / `admin123`

**⚠️ ĐỔI PASSWORD SAU KHI DEPLOY**

## 📊 Tech Stack

- Spring Boot 3.x + Spring Security
- PostgreSQL (Railway)
- Thymeleaf + Bootstrap 5
- VNPay Payment Gateway

## 💰 Cost

- **Local:** FREE
- **Production:** $5/month (Railway PostgreSQL)


