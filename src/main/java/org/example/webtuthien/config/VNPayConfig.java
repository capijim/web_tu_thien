package org.example.webtuthien.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

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
    
    @Value("${vnpay.version}")
    private String version;
    
    @Value("${vnpay.command}")
    private String command;
    
    @PostConstruct
    public void init() {
        System.out.println("=== VNPay Configuration ===");
        System.out.println("TMN Code: " + tmnCode);
        System.out.println("VNPay URL: " + vnpayUrl);
        System.out.println("Return URL: " + returnUrl);
        System.out.println("Version: " + version);
        System.out.println("Command: " + command);
        System.out.println("Hash Secret Length: " + (hashSecret != null ? hashSecret.length() : 0));
    }
    
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
        return returnUrl;
    }
    
    public String getVersion() {
        return version;
    }
    
    public String getCommand() {
        return command;
    }
}
