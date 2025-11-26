# Quick Start Guide

## 🚀 Deploy trong 5 phút

### Bước 1: Verify Schema (1 phút)

1. Vào [Supabase Dashboard](https://app.supabase.com)
2. Mở project `gbzwqsyoihqtpcionaze`
3. **SQL Editor** > Run:

```sql
SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';
```

**Nếu không có tables:**
- Copy `src/main/resources/schema-postgresql.sql` > Run
- Copy `src/main/resources/data-postgresql.sql` > Run

### Bước 2: Deploy Railway (2 phút)

```bash
git add .
git commit -m "Deploy"
git push origin main
```

1. [Railway Dashboard](https://railway.app) > New Project
2. Deploy from GitHub > chọn `web_tu_thien`
3. Đợi build xong

### Bước 3: Set Variables (1 phút)

Railway Dashboard > Variables:

```bash
SPRING_PROFILES_ACTIVE=railway
DATABASE_PASSWORD=zvBSwzV/@S8D?uvn
FILE_UPLOAD_DIR=/app/uploads
```

### Bước 4: Verify Connection (1 phút)

**Test 1: Database Connection Detail**
```bash
curl https://your-app.railway.app/api/health/db-info
```

Expected:
```json
{
  "status": "SUCCESS",
  "connected": true,
  "tablesCount": 5
}
```

**Test 2: Quick Test**
```bash
curl https://your-app.railway.app/api/health/db-test
```

**Test 3: Spring Health**
```bash
curl https://your-app.railway.app/actuator/health
```

### Nếu thấy lỗi:

**Error: "Connection refused"**
```bash
# Railway Variables > Add:
DATABASE_URL=jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:6543/postgres?sslmode=require&ssl=true
```

**Error: "tablesCount": 0**
```bash
# Vào Supabase SQL Editor, run:
# 1. schema-postgresql.sql
# 2. data-postgresql.sql
```

## ✅ Success Indicators

- Health check: `{"status":"UP","components":{"db":{"status":"UP"}}}`
- Logs: No "connection refused" errors
- Can access admin dashboard

## 🆘 Still Failing?

1. Railway Shell > Test connection:
```bash
apt install postgresql-client -y
PGPASSWORD='zvBSwzV/@S8D?uvn' psql -h db.gbzwqsyoihqtpcionaze.supabase.co -U postgres.gbzwqsyoihqtpcionaze -d postgres
```

2. Check Railway logs:
```bash
railway logs --tail 200 | grep -i "connection\|error\|exception"
```

3. Verify Supabase project not paused

