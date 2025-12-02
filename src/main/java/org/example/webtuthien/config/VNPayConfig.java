package org.example.webtuthien.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VNPayConfig {
    
    @Value("${vnpay.tmn-code}")
    private String tmnCode;
    
    @Value("${vnpay.hash-secret}")
    private String hashSecret;
    
    @Value("${vnpay.url}")
    private String vnpayUrl;
    
    @Value("${vnpay.return-url}")
    private String returnUrl;
    
    @Value("${app.base-url}")
    private String baseUrl;
    
    @Value("${vnpay.version}")
    private String version;
    
    @Value("${vnpay.command}")
    private String command;
    
    public String getTmnCode() {
        return tmnCode;
    }
    
    public String getHashSecret() {
        return hashSecret;
    }
    
    public String getVnpayUrl() {
        return vnpayUrl;
    }
    
    public String getReturnUrl() {
        // If baseUrl is set and different from localhost, use it
        if (baseUrl != null && !baseUrl.contains("localhost")) {
            return baseUrl + "/vnpay/return";
        }
        return returnUrl;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public String getVersion() {
        return version;
    }
    
    public String getCommand() {
        return command;
    }
}
