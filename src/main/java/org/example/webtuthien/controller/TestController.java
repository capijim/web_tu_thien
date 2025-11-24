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
            response.put("message", "✅ Test email sent successfully!");
            response.put("to", toEmail);
            response.put("note", "Check your inbox (and spam folder)");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "❌ " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
