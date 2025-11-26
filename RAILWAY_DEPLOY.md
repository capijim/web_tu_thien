# Hướng dẫn Deploy lên Railway với Supabase

## 📋 Yêu cầu
- Tài khoản [Railway](https://railway.app)
- Tài khoản [Supabase](https://supabase.com) (Free tier đủ dùng)
- Git repository (GitHub/GitLab)

## 🚀 Các bước Deploy

### 1. Tạo Database trên Supabase

1. Đăng nhập vào [Supabase Dashboard](https://app.supabase.com)
2. Tạo project mới (hoặc dùng project có sẵn)
3. Vào **Settings** > **Database**
4. Copy thông tin connection:
   - Host: `db.xxx.supabase.co`
   - Port: `5432`
   - Database name: `postgres`
   - User: `postgres.xxx`
   - Password: (password bạn đã tạo)

### 2. Deploy lên Railway

#### Cách 1: Deploy từ GitHub (Khuyến nghị)

1. Push code lên GitHub repository
2. Đăng nhập [Railway](https://railway.app)
3. Click **New Project** > **Deploy from GitHub repo**
4. Chọn repository `web_tu_thien`
5. Railway sẽ tự động detect Dockerfile và bắt đầu build

#### Cách 2: Deploy từ CLI

```bash
# Cài Railway CLI
npm install -g @railway/cli

# Login
railway login

# Deploy
railway up
```

### 3. Cấu hình Environment Variables

Trong Railway dashboard, vào **Variables** tab và thêm các biến sau:

```
DATABASE_URL=jdbc:postgresql://db.xxx.supabase.co:5432/postgres?sslmode=require
DATABASE_USERNAME=postgres.xxx
DATABASE_PASSWORD=your-supabase-password
FILE_UPLOAD_DIR=/app/uploads
```

#### Optional: VNPay Configuration (nếu dùng thanh toán)
```
VNPAY_TMN_CODE=your-code
VNPAY_HASH_SECRET=your-secret
VNPAY_RETURN_URL=https://your-app.railway.app/vnpay/return
```

#### Optional: Email Configuration (nếu muốn override)
```
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password
```

### 4. Khởi tạo Database Schema

**Quan trọng:** Do Railway có thể có vấn đề network khi khởi động, bạn cần setup schema thủ công:

1. Vào Supabase Dashboard > SQL Editor
2. Copy nội dung file `src/main/resources/schema-postgresql.sql`
3. Paste và chạy trong SQL Editor
4. Copy nội dung file `src/main/resources/data-postgresql.sql`
5. Paste và chạy trong SQL Editor

Sau đó Railway app sẽ có thể kết nối và hoạt động bình thường.

**Lưu ý:** Application được cấu hình với `spring.sql.init.mode=never` để tránh lỗi khi khởi động.

### 5. Kiểm tra Application

1. Railway sẽ cung cấp public URL: `https://your-app.railway.app`
2. Truy cập URL để kiểm tra
3. Login admin mặc định:
   - Username: `admin`
   - Password: `admin123`

## 🔧 Troubleshooting

### Lỗi "Network unreachable" hoặc "Connection refused"
- **Nguyên nhân:** Railway không thể kết nối Supabase khi khởi động để chạy schema
- **Giải pháp:**
  1. Chạy schema thủ công trong Supabase SQL Editor (xem bước 4)
  2. Đảm bảo biến `DATABASE_URL` có `?sslmode=require`
  3. Restart Railway deployment sau khi setup schema

### Lỗi "Authentication failed"
- Kiểm tra `DATABASE_USERNAME` và `DATABASE_PASSWORD`
- Supabase username thường có format: `postgres.project-ref`

### Lỗi "Schema not found"
- Railway tự động chạy schema, kiểm tra logs xem có lỗi gì
- Có thể chạy thủ công SQL trong Supabase SQL Editor

### Application không start
```bash
# Xem logs chi tiết
railway logs --tail 100
```

## 📊 Monitoring

### Health Check
```
GET https://your-app.railway.app/actuator/health
```

### View Logs
```bash
railway logs
```

### Database Management
- Vào Supabase Dashboard > Table Editor
- Hoặc dùng SQL Editor để chạy queries

## 🔐 Bảo mật

**⚠️ QUAN TRỌNG:**

1. **Không commit** file `.env` có chứa credentials
2. **Thay đổi** password admin mặc định sau khi deploy
3. **Sử dụng** strong passwords cho database
4. Cập nhật VNPay return URL cho đúng domain

## 💰 Chi phí

- **Railway**: Free tier 500 hours/month (đủ cho 1 app nhỏ)
- **Supabase**: Free tier 500MB database (đủ cho development)

## 📝 Notes

- Railway tự động detect port từ biến `PORT` environment
- Dockerfile đã được tối ưu cho production
- HikariCP connection pool đã được config sẵn
- Auto-scaling có thể bật trong Railway settings

## 🔄 Update Application

### Automatic Deploy (GitHub)
- Push code mới lên GitHub
- Railway tự động rebuild và redeploy

### Manual Deploy (CLI)
```bash
railway up
```

## 🆘 Support

Nếu gặp vấn đề:
1. Check Railway logs: `railway logs`
2. Check Supabase logs trong Dashboard
3. Verify environment variables
4. Test database connection từ local:
   ```bash
   psql "postgresql://postgres.xxx:password@db.xxx.supabase.co:5432/postgres?sslmode=require"
   ```
