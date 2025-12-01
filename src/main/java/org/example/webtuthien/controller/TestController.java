package org.example.webtuthien.controller;

import org.example.webtuthien.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired
    private EmailService emailService;
    
    @GetMapping("/send-test-email")
    public ResponseEntity<?> sendTestEmail(@RequestParam(defaultValue = "test@example.com") String toEmail) {
        try {
            System.out.println("\n═══════════════════════════════════════════════════");
            System.out.println("🧪 TEST EMAIL ENDPOINT CALLED");
            System.out.println("Target Email: " + toEmail);
            System.out.println("═══════════════════════════════════════════════════\n");
            
            emailService.sendDonationSuccessEmail(
                toEmail,
                "Nguyễn Văn Test",
                new BigDecimal("250000"),
                "Test Campaign - Giúp đỡ trẻ em nghèo",
                "Giáo dục",
                1L,
                999L,
                "Đây là email test từ hệ thống. Chúc các em học giỏi!",
                OffsetDateTime.now()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "✅ Test email sent successfully!");
            response.put("to", toEmail);
            response.put("timestamp", OffsetDateTime.now().toString());
            response.put("instructions", Map.of(
                "step1", "Check inbox: " + toEmail,
                "step2", "Check SPAM/JUNK folder",
                "step3", "Check Promotions tab (Gmail)",
                "step4", "Search for 'Test Campaign'",
                "step5", "Wait 1-2 minutes for delivery"
            ));
            
            System.out.println("\n✅ API Response: Email sent successfully");
            System.out.println("Please check: " + toEmail);
            System.out.println("═══════════════════════════════════════════════════\n");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("\n❌ ERROR in test email endpoint:");
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("error", e.getMessage());
            error.put("type", e.getClass().getSimpleName());
            error.put("timestamp", OffsetDateTime.now().toString());
            
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/verify-email-config")
    public ResponseEntity<?> verifyEmailConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("mailHost", "smtp.gmail.com");
        config.put("mailPort", 587);
        config.put("fromEmail", emailService.fromEmail);
        config.put("fromName", emailService.fromName);
        config.put("baseUrl", emailService.baseUrl);
        config.put("status", "Email configuration loaded");
        
        return ResponseEntity.ok(config);
    }
}
