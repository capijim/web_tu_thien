#!/bin/bash

echo "🚀 Deploying to Railway..."

# Build JAR locally first (Railway will use Dockerfile for final build)
echo "📦 Building JAR file..."
mvn clean package -DskipTests

if [ ! -f target/*.jar ]; then
    echo "❌ JAR file not found! Build failed."
    exit 1
fi

echo "✅ JAR file built successfully"

# Deploy to Railway
echo "🚂 Pushing to Railway..."
railway up

echo "✅ Deployment complete!"
echo "🌐 Check status: railway status"
echo "📋 View logs: railway logs"
