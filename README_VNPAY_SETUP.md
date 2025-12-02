# VNPay Setup Guide

## Option 1: Test with ngrok (Recommended for development)

1. Install ngrok: https://ngrok.com/download
2. Run your app on localhost:8080
3. Start ngrok:
   ```bash
   ngrok http 8080
   ```
4. Copy the https URL (e.g., https://abc123.ngrok.io)
5. Set environment variable:
   ```bash
   export APP_BASE_URL=https://abc123.ngrok.io
   ```
6. Restart your app

## Option 2: Railway with VNPay Production

1. Register for VNPay merchant account
2. Add your Railway domain to VNPay whitelist
3. Set environment variables on Railway:
   ```
   APP_BASE_URL=https://your-app.up.railway.app
   VNPAY_TMN_CODE=your_real_code
   VNPAY_HASH_SECRET=your_real_secret
   ```

## Option 3: Use Cloudflare Tunnel

1. Install cloudflared
2. Run:
   ```bash
   cloudflared tunnel --url http://localhost:8080
   ```
3. Use the provided URL

## Test Configuration

Visit: http://localhost:8080/vnpay/config

This will show you the actual URLs being used.

## Common Error Codes

- **72**: Website không tồn tại - Domain chưa được VNPay whitelist
- **75**: Ngân hàng bảo trì
- **79**: Giao dịch vượt hạn mức
- **00**: Thành công
