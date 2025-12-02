# Railway Environment Variables

**⚠️ IMPORTANT: Never commit API keys to Git!**

Set these in Railway Dashboard > Variables:

```bash
SPRING_PROFILES_ACTIVE=railway
SPRING_DATASOURCE_URL=jdbc:postgresql://hopper.proxy.rlwy.net:14179/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=ADfVtAfzyPNskyYwUcGblgbUaiChaimL

# Email via Brevo API (Get from https://app.brevo.com > SMTP & API)
BREVO_API_KEY=your-brevo-api-key-here
```

## 🔐 Security Best Practices

1. **Never commit secrets** to Git
2. **Rotate API keys** if accidentally exposed
3. **Use Railway Variables** for all sensitive data
4. **Change default admin password** after first deploy