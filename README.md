# Web Từ Thiện

Hệ thống quản lý hoạt động từ thiện - Spring Boot + Railway PostgreSQL

## 🎯 Chức Năng

- ✅ Quản lý chiến dịch quyên góp
- ✅ Theo dõi donations
- ✅ Thanh toán VNPay
- ✅ Admin dashboard
- ✅ **Email notifications via Brevo SMTP**
- ✅ **Auto database migration on first deploy**

## 🚀 Chạy Local (Docker)

```bash
# Start Docker Desktop, sau đó:
docker-compose up
```

**Access:** http://localhost:8080

## 📧 Email Configuration (Brevo SMTP)

### Setup Brevo Account:

1. Đăng ký tài khoản tại: https://www.brevo.com
2. Verify email và hoàn tất đăng ký
3. Vào **Settings > SMTP & API**
4. Copy **SMTP credentials**:
   - SMTP Server: `smtp-relay.brevo.com`
   - Port: `587`
   - Login: Your Brevo login email
   - SMTP Key: Generate new key

### Configure Local Environment:

```properties
# src/main/resources/application.properties
spring.mail.host=smtp-relay.brevo.com
spring.mail.port=587
spring.mail.username=your-brevo-email@domain.com
spring.mail.password=your-brevo-smtp-key
app.email.from=your-verified-sender@domain.com
```

### Configure Railway:

```bash
# Railway Environment Variables
SPRING_MAIL_HOST=smtp-relay.brevo.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-brevo-email@domain.com
SPRING_MAIL_PASSWORD=your-brevo-smtp-key
APP_EMAIL_FROM=your-verified-sender@domain.com
APP_EMAIL_NAME=Web Từ Thiện
```

**⚠️ Important:**
- Sender email (`APP_EMAIL_FROM`) must be verified in Brevo dashboard
- Free plan: 300 emails/day
- Paid plans available for higher volume

## 🌐 Deploy Production (Railway)

### 🆕 Auto Database Setup

App sẽ **TỰ ĐỘNG tạo tables** khi deploy lần đầu! Không cần chạy SQL thủ công.

### Quick Deploy

```bash
# 1. Commit code
git add .
git commit -m "Deploy to Railway with auto-migration"
git push origin main

# 2. Railway Dashboard > New Project > Deploy from GitHub

# 3. Set environment variables:
SPRING_PROFILES_ACTIVE=railway
SPRING_DATASOURCE_URL=jdbc:postgresql://hopper.proxy.rlwy.net:14179/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=ADfVtAfzyPNskyYwUcGblgbUaiChaimL

# 4. Deploy - App sẽ tự động:
#    ✅ Tạo tất cả tables
#    ✅ Thêm indexes và constraints
#    ✅ Insert admin account mặc định
#    ✅ Insert dữ liệu mẫu
```

### Verify Database Setup

```bash
# Check database initialization status
curl https://your-app.railway.app/api/health/db-info

# Expected response:
{
  "status": "SUCCESS",
  "connected": true,
  "tablesCount": 6,
  "tables": {
    "users": true,
    "partners": true,
    "campaigns": true,
    "donations": true,
    "admins": true,
    "payments": true
  }
}
```

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

## 📊 Database Migration System

### How it works:

1. **First Deploy:**
   - App detects empty database
   - Runs `V1__init_schema.sql` - Creates all tables
   - Runs `V2__seed_data.sql` - Inserts default data
   - Logs: "🎉 Database initialization completed!"

2. **Subsequent Deploys:**
   - App detects existing tables
   - Skips migration
   - Logs: "✅ Database already initialized"

### Migration Files:

```
src/main/resources/db/migration/
├── V1__init_schema.sql    # Create tables, indexes, triggers
└── V2__seed_data.sql      # Insert admin & sample data
```

### Add New Migration:

```bash
# Create new migration file
touch src/main/resources/db/migration/V3__add_new_feature.sql

### Common Issues:

**1. Migration failed**
```bash
# Check Railway logs for SQL errors
railway logs --tail 200

# Manual fix: Connect to Railway PostgreSQL and run SQL manually
railway connect postgres
\i src/main/resources/db/migration/V1__init_schema.sql
```

**2. Tables exist but migration runs again**
```bash
# Check table count
curl https://your-app.railway.app/api/health/db-info

# If tableCount = 0 but tables exist, check schema:
# Tables might be in wrong schema (not 'public')
```

## 🔐 Default Credentials

- Admin: `admin` / `admin123`

**⚠️ ĐỔI PASSWORD SAU KHI DEPLOY**

## 📊 Tech Stack

- Spring Boot 3.x + Spring Security
- PostgreSQL (Railway)
- Thymeleaf + Bootstrap 5
- VNPay Payment Gateway
- **Auto database migration system**

## 💰 Cost

- **Local:** FREE
- **Production:** $5/month (Railway PostgreSQL)


