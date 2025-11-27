#!/bin/bash

echo "Building JAR file..."
mvn clean package -DskipTests

echo "Building Docker image..."
docker build -t web-tu-thien:latest .

echo "Running container..."
docker-compose up -d

echo "Done! Check logs with: docker logs web_tu_thien_app"
