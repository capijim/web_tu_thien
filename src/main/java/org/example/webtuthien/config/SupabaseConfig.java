package org.example.webtuthien.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@ConfigurationProperties(prefix = "supabase")
public class SupabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(SupabaseConfig.class);
    
    private String url;
    private String anonKey;
    private String serviceRoleKey;
    private Storage storage = new Storage();
    
    @PostConstruct
    public void validate() {
        if (!isConfigured()) {
            logger.warn("=============================================================");
            logger.warn("Supabase is NOT configured!");
            logger.warn("Real-time features and storage will be DISABLED.");
            logger.warn("To enable Supabase, set these environment variables:");
            logger.warn("  SUPABASE_URL=https://xxx.supabase.co");
            logger.warn("  SUPABASE_ANON_KEY=your-anon-key");
            logger.warn("  SUPABASE_SERVICE_ROLE_KEY=your-service-role-key (optional)");
            logger.warn("=============================================================");
        } else {
            logger.info("✓ Supabase configured successfully");
            logger.info("  URL: {}", maskUrl(url));
            logger.info("  Anon Key: {}...", anonKey != null ? anonKey.substring(0, Math.min(20, anonKey.length())) : "null");
            logger.info("  Storage Bucket: {}", storage.getBucket());
        }
    }
    
    private String maskUrl(String url) {
        if (url == null || url.isEmpty()) return "not set";
        return url.replaceAll("//([^.]+)", "//*****");
    }
    
    public boolean isConfigured() {
        return url != null && !url.isEmpty() && 
               !url.equals("${SUPABASE_URL:}") &&
               anonKey != null && !anonKey.isEmpty() &&
               !anonKey.equals("${SUPABASE_ANON_KEY:}");
    }
    
    public static class Storage {
        private String bucket = "campaign-images";
        private String maxFileSize = "5MB";
        
        // Getters and setters
        public String getBucket() { return bucket; }
        public void setBucket(String bucket) { this.bucket = bucket; }
        public String getMaxFileSize() { return maxFileSize; }
        public void setMaxFileSize(String maxFileSize) { this.maxFileSize = maxFileSize; }
    }
    
    // Getters and setters
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getAnonKey() { return anonKey; }
    public void setAnonKey(String anonKey) { this.anonKey = anonKey; }
    public String getServiceRoleKey() { return serviceRoleKey; }
    public void setServiceRoleKey(String serviceRoleKey) { this.serviceRoleKey = serviceRoleKey; }
    public Storage getStorage() { return storage; }
    public void setStorage(Storage storage) { this.storage = storage; }
}
