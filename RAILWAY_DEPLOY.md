# Hướng dẫn Deploy lên Railway với Supabase

## 📋 Thông tin Database của bạn

- **Host:** db.xxxxxxxxxx.supabase.co
- **Port:** 5432
- **Database:** postgres
- **User:** postgres.xxxxxxxxxx
- **Password:** [password bạn vừa tạo]

## 🚀 CÁC BƯỚC DEPLOY (QUAN TRỌNG - LÀM ĐÚNG THỨ TỰ)

### ⚠️ BƯỚC 0: Kiểm tra file cấu hình (BẮT BUỘC)

Đảm bảo file `src/main/resources/application-railway.yml` TỒN TẠI.

```bash
# Kiểm tra file có tồn tại không
ls src/main/resources/application-railway.yml

# Nếu không có, BẮT BUỘC phải tạo file này trước (xem nội dung ở trên)
```

### BƯỚC 1: Tạo Database trên Supabase

1. Đăng nhập [Supabase Dashboard](https://app.supabase.com)
2. Click **New Project**
3. Điền thông tin:
   - Name: `web-tu-thien-db`
   - Database Password: **TẠO PASSWORD MẠNH VÀ LƯU LẠI**
   - Region: `Southeast Asia (Singapore)`
4. Đợi 2-3 phút project khởi tạo
5. Vào **Settings** > **Database**
6. **LƯU LẠI** các thông tin:
   ```
   Host: db.xxxxxxxxxx.supabase.co
   Port: 5432
   Database: postgres
   User: postgres.xxxxxxxxxx
   Password: [password bạn vừa tạo]
   ```

### BƯỚC 2: Khởi tạo Database Schema (LÀM TRƯỚC KHI DEPLOY)

**⚠️ QUAN TRỌNG: Làm bước này TRƯỚC khi deploy application**

1. Trong Supabase Dashboard > **SQL Editor**
2. Click **New Query**
3. Copy toàn bộ file `src/main/resources/schema-postgresql.sql`
4. Paste và click **RUN**
5. Kiểm tra output - phải thấy "Success"
6. Tạo query mới, copy file `src/main/resources/data-postgresql.sql`
7. Paste và **RUN**

**Verify schema:**
```sql
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public';
-- Phải thấy: users, campaigns, donations, partners, admins
```

### BƯỚC 3: Deploy lên Railway

#### Cách 1: GitHub (Khuyến nghị)

```bash
# 1. Commit code (đảm bảo có application-railway.yml)
git add .
git commit -m "Add Railway config for Supabase"
git push origin main

# 2. Trên Railway Dashboard:
# - New Project > Deploy from GitHub repo
# - Chọn repo web_tu_thien
# - Đợi build (5-10 phút)
```

#### Cách 2: Railway CLI

```bash
npm install -g @railway/cli
railway login
railway init
railway up
```

### BƯỚC 4: Cấu hình Environment Variables

**Trong Railway Dashboard > Variables tab:**

#### 🔴 BẮT BUỘC:

```bash
SPRING_PROFILES_ACTIVE=railway
DATABASE_URL=jdbc:postgresql://db.YOUR_REF.supabase.co:5432/postgres?sslmode=require
DATABASE_USERNAME=postgres.YOUR_REF
DATABASE_PASSWORD=your-password
FILE_UPLOAD_DIR=/app/uploads
```

**⚠️ Thay `YOUR_REF` và `your-password` bằng giá trị thực từ Supabase**

#### 🟡 Tùy chọn:

```bash
VNPAY_TMN_CODE=your-code
VNPAY_HASH_SECRET=your-secret
VNPAY_RETURN_URL=https://your-app.railway.app/vnpay/return
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password
```

**Sau khi thêm biến: Click Deploy để restart**

### BƯỚC 5: Kiểm tra Deployment

```bash
# Test health check
curl https://your-app.railway.app/actuator/health

# Expected output:
# {"status":"UP","components":{"db":{"status":"UP"}}}

# View logs
railway logs --tail 100
```

## 🔧 XỬ LÝ LỖI (Troubleshooting)

### ❌ Lỗi: "Network is unreachable"

**Nguyên nhân:** Thiếu `SPRING_PROFILES_ACTIVE` hoặc `DATABASE_URL` sai

**Giải pháp:**
```bash
# 1. Kiểm tra biến môi trường
railway variables

# 2. Set lại nếu thiếu
railway variables set SPRING_PROFILES_ACTIVE=railway

# 3. Verify DATABASE_URL có format đúng:
# jdbc:postgresql://db.xxx.supabase.co:5432/postgres?sslmode=require

# 4. Thử connection pooler nếu vẫn lỗi:
DATABASE_URL=jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:6543/postgres?sslmode=require
```

### ❌ Lỗi: "HikariPool - Exception during pool initialization"

**Nguyên nhân:** Database credentials sai hoặc Supabase bị pause

**Giải pháp:**
```bash
# 1. Test connection từ local
psql "postgresql://postgres.XXX:PASSWORD@db.XXX.supabase.co:5432/postgres?sslmode=require"

# 2. Check Supabase project status (Dashboard)

# 3. Reset password nếu cần (Settings > Database > Reset password)
```

### ❌ Lỗi: "Authentication failed"

**Nguyên nhân:** Username format sai

**Giải pháp:**
```bash
# Username PHẢI có format: postgres.PROJECT_REF
# Lấy từ: Supabase > Settings > Database > Connection string
DATABASE_USERNAME=postgres.abcdefghijklmnop
```

### ❌ Lỗi: "Could not open JPA EntityManager"

**Nguyên nhân:** Schema chưa được tạo

**Giải pháp:**
```sql
-- Vào Supabase SQL Editor, kiểm tra:
SELECT COUNT(*) FROM information_schema.tables 
WHERE table_schema = 'public';

-- Nếu = 0, chạy lại schema-postgresql.sql và data-postgresql.sql
```

### ❌ Lỗi: "application-railway.yml not found"

**Nguyên nhân:** File chưa được tạo hoặc commit

**Giải pháp:**
```bash
# 1. Tạo file (copy nội dung từ đầu guide)
touch src/main/resources/application-railway.yml

# 2. Commit
git add src/main/resources/application-railway.yml
git commit -m "Add Railway config"
git push

# Railway sẽ tự động rebuild
```

## 📊 MONITORING

### View Logs Real-time
```bash
railway logs --tail 100
```

### Check Database Connections
```sql
-- Supabase SQL Editor:
SELECT pid, usename, application_name, client_addr, state
FROM pg_stat_activity
WHERE datname = 'postgres';
```

### Test Endpoints
```bash
curl https://your-app.railway.app/actuator/health
curl https://your-app.railway.app/actuator/info
```

## 🔐 BẢO MẬT PRODUCTION

**⚠️ SAU KHI DEPLOY:**

1. **Đổi admin password:**
   ```sql
   -- Supabase SQL Editor
   UPDATE admins 
   SET password = '$2a$10$NEW_HASHED_PASSWORD' 
   WHERE username = 'admin';
   ```

2. **Rotate database credentials**
3. **Update VNPay return URL**
4. **Enable Supabase RLS**

## ✅ SUCCESS CHECKLIST

- ✅ `application-railway.yml` file exists
- ✅ `SPRING_PROFILES_ACTIVE=railway` set
- ✅ Database schema created
- ✅ Health check returns `{"status":"UP"}`
- ✅ Can login admin dashboard
- ✅ No ERROR in logs
- ✅ Can create campaign

## 💰 CHI PHÍ

- **Railway**: Free tier 500h/month
- **Supabase**: Free tier 500MB
- **Total**: $0-5/month

## 🔄 UPDATE APP

```bash
# Auto deploy (GitHub)
git push origin main

# Manual (CLI)
railway up

# Rollback
# Railway Dashboard > Deployments > Redeploy old version
```

---

**🎉 DONE! Nếu gặp lỗi, xem lại Troubleshooting section từng bước.**
