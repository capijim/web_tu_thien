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
        if (url == null || url.isEmpty() || anonKey == null || anonKey.isEmpty()) {
            logger.warn("Supabase is not fully configured. Real-time features and storage will be disabled.");
            logger.info("To enable Supabase features, set SUPABASE_URL and SUPABASE_ANON_KEY environment variables.");
        } else {
            logger.info("Supabase configured successfully: {}", url);
        }
    }
    
    public boolean isConfigured() {
        return url != null && !url.isEmpty() && anonKey != null && !anonKey.isEmpty();
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
