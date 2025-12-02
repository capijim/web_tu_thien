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
    
    @Value("${app.base-url}")
    private String baseUrl;
    
    @Value("${vnpay.version}")
    private String version;
    
    @Value("${vnpay.command}")
    private String command;
    
    @Value("${vnpay.mock-mode:false}")
    private boolean mockMode;
    
    public String getTmnCode() { return tmnCode; }
    public String getHashSecret() { return hashSecret; }
    public String getVnpayUrl() { return vnpayUrl; }
    public String getVersion() { return version; }
    public String getCommand() { return command; }
    public String getBaseUrl() { return baseUrl; }
    public boolean isMockMode() { return mockMode; }
    
    public String getReturnUrl() {
        return baseUrl + "/vnpay/return";
    }
}
