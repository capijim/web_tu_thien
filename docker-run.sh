#!/bin/bash

# Tạo thư mục uploads trên host nếu chưa có
mkdir -p uploads

# Build image
docker-compose build

# Chạy container
docker-compose up -d

# Xem logs
docker-compose logs -f app
