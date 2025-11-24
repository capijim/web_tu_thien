# Web Từ Thiện

Hệ thống quản lý hoạt động từ thiện được xây dựng với Spring Boot và kiến trúc Microservices.

## 🎯 Chức Năng

### Web Application (Port 8080)
- Quản lý chiến dịch quyên góp
- Theo dõi danh sách nhà hảo tâm
- Báo cáo và thống kê hoạt động từ thiện
- Giao diện web thân thiện cho người dùng

### REST API (Port 8081)
- API endpoints cho mobile/third-party integration
- Xác thực và phân quyền
- Quản lý dữ liệu campaign và donation
- Swagger UI documentation

### Database
- MySQL lưu trữ dữ liệu
- Tự động backup
- Migration với Flyway/Liquibase

## 🚀 Cách Chạy App

### Yêu Cầu
- Docker Desktop ([Download tại đây](https://www.docker.com/products/docker-desktop))

### Chạy App (3 bước)

**Bước 1: Khởi động Docker Desktop**
- Mở Docker Desktop và đợi biểu tượng màu xanh

**Bước 2: Clone project (nếu chưa có)**
```powershell
git clone <repository-url>
cd web_tu_thien
```

**Bước 3: Start tất cả services**
```powershell
docker-compose up
```

### ✅ Truy Cập

| Service | URL | Mô tả |
|---------|-----|-------|
| **Web App** | http://localhost:8080 | Giao diện người dùng |
| **REST API** | http://localhost:8081 | API endpoints |
| **API Docs** | http://localhost:8081/swagger-ui.html | Swagger documentation |
| **MySQL** | localhost:3307 | Database (user: `app_user`, pass: `app_password`) |

### ⏱️ Thời Gian Chờ

- **Lần đầu**: 5-10 phút (download dependencies)
- **Lần sau**: 1-2 phút (sử dụng cache)

### 🛑 Dừng App

```powershell
# Nhấn Ctrl+C trong terminal, hoặc:
docker-compose down
```

## 📋 Lệnh Thường Dùng

```powershell
# Chạy ở background
docker-compose up -d

# Xem logs
docker-compose logs -f

# Restart một service
docker-compose restart webapp

# Xem trạng thái
docker-compose ps

# Xóa tất cả (kể cả data)
docker-compose down -v

# Rebuild sau khi sửa code
docker-compose up --build
```

## 🏗️ Kiến Trúc
```
┌─────────────┐     ┌─────────────┐
│   Web App   │────▶│   REST API  │
│   :8080     │     │   :8081     │
└──────┬──────┘     └──────┬──────┘
       │                   │
       └───────┬───────────┘
               │
        ┌──────▼──────┐
        │    MySQL    │
        │    :3306    │
        └─────────────┘
```

## So sánh Docker Compose vs Kubernetes

| Feature | Docker Compose | Kubernetes |
|---------|----------------|------------|
| **Use Case** | Development local | Production, scaling |
| **Setup** | Đơn giản | Phức tạp hơn |
| **Scaling** | Manual | Auto-scaling |
| **Load Balancing** | Không | Có sẵn |
| **High Availability** | Không | Có |
| **Rolling Updates** | Không | Có |

## Tips

- ✅ Dùng `docker-compose` cho development (nhanh, đơn giản)
- ✅ Dùng `Kubernetes` cho production (scaling, HA, monitoring)
- ✅ Health checks đã được config sẵn
- ⚠️ Thay đổi database credentials trong production
- ✅ Sử dụng ConfigMaps và Secrets cho configuration management

## Workflow Development

### Development Flow (Docker Compose)
```
1. Sửa code
2. Save file
3. Chạy: docker-compose up --build
4. Test tại http://localhost:8080
5. Lặp lại
```

### Production Flow (Kubernetes)
```
1. Sửa code
2. Build image: docker build -t webapp:v2 -f Dockerfile.webapp .
3. Update deployment: kubectl set image deployment/webapp webapp=webapp:v2
4. Monitor: kubectl rollout status deployment/webapp
5. Rollback nếu lỗi: kubectl rollout undo deployment/webapp
```


