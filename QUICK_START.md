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

