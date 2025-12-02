package org.example.webtuthien.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class BrevoEmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(BrevoEmailService.class);
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";
    
    @Value("${brevo.api-key:}")
    private String brevoApiKey;
    
    @Value("${app.email.from}")
    private String fromEmail;
    
    @Value("${app.email.name}")
    private String fromName;
    
    @Value("${app.base-url}")
    private String baseUrl;
    
    @Autowired
    private TemplateEngine templateEngine;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public void sendDonationSuccessEmail(
            String toEmail,
            String donorName,
            BigDecimal amount,
            String campaignTitle,
            String campaignCategory,
            Long campaignId,
            Long donationId,
            String message,
            OffsetDateTime donationDate) {
        
        if (brevoApiKey == null || brevoApiKey.isEmpty()) {
            logger.warn("⚠️  Brevo API key not configured. Email simulation mode.");
            logEmailSimulation(toEmail, donorName, amount, campaignTitle);
            return;
        }
        
        try {
            logger.info("\n╔════════════════════════════════════════════════════════════╗");
            logger.info("║              📧 SENDING EMAIL VIA BREVO API                ║");
            logger.info("╠════════════════════════════════════════════════════════════╣");
            logger.info("║ To:       {}", toEmail);
            logger.info("║ Donor:    {}", donorName);
            logger.info("║ Amount:   {} VNĐ", String.format("%,d", amount.longValue()));
            logger.info("║ Campaign: {}", campaignTitle);
            logger.info("╚════════════════════════════════════════════════════════════╝");
            
            // Prepare template variables
            Context context = new Context();
            context.setVariable("donorName", donorName);
            context.setVariable("amount", amount);
            context.setVariable("campaignTitle", campaignTitle);
            context.setVariable("campaignCategory", campaignCategory);
            context.setVariable("donationId", donationId);
            context.setVariable("message", message);
            context.setVariable("donationDate", donationDate);
            context.setVariable("campaignUrl", baseUrl + "/campaign/" + campaignId);
            
            String htmlContent = templateEngine.process("email/donation-success", context);
            
            // Prepare Brevo API request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);
            
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("sender", Map.of("name", fromName, "email", fromEmail));
            emailData.put("to", List.of(Map.of("email", toEmail, "name", donorName)));
            emailData.put("subject", "✅ Xác nhận quyên góp thành công - " + campaignTitle);
            emailData.put("htmlContent", htmlContent);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(emailData, headers);
            
            // Send via Brevo API
            ResponseEntity<String> response = restTemplate.exchange(
                BREVO_API_URL,
                HttpMethod.POST,
                request,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("\n╔════════════════════════════════════════════════════════════╗");
                logger.info("║              ✅ EMAIL SENT SUCCESSFULLY VIA BREVO!         ║");
                logger.info("╠════════════════════════════════════════════════════════════╣");
                logger.info("║ ✉️  Email delivered to: {}", toEmail);
                logger.info("║ 📧 Donation ID: {}", donationId);
                logger.info("║ 📅 Sent at: {}", donationDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                logger.info("║");
                logger.info("║ 💡 Check email inbox or spam folder");
                logger.info("║ 🔍 Verify in Brevo: https://app.brevo.com/statistics/email");
                logger.info("╚════════════════════════════════════════════════════════════╝\n");
            } else {
                logger.error("❌ Brevo API error: {} - {}", response.getStatusCode(), response.getBody());
            }
            
        } catch (Exception e) {
            logger.error("\n╔════════════════════════════════════════════════════════════╗");
            logger.error("║              ❌ BREVO EMAIL SEND FAILED!                   ║");
            logger.error("╠════════════════════════════════════════════════════════════╣");
            logger.error("║ Error: {}", e.getMessage());
            logger.error("╚════════════════════════════════════════════════════════════╝\n");
            logger.error("Full stack trace:", e);
            throw new RuntimeException("Failed to send email via Brevo: " + e.getMessage(), e);
        }
    }
    
    private void logEmailSimulation(String toEmail, String donorName, BigDecimal amount, String campaignTitle) {
        logger.info("\n╔════════════════════════════════════════════════════════════╗");
        logger.info("║              📧 EMAIL SIMULATION MODE                      ║");
        logger.info("╠════════════════════════════════════════════════════════════╣");
        logger.info("║ To:       {}", toEmail);
        logger.info("║ Donor:    {}", donorName);
        logger.info("║ Amount:   {} VNĐ", String.format("%,d", amount.longValue()));
        logger.info("║ Campaign: {}", campaignTitle);
        logger.info("╠════════════════════════════════════════════════════════════╣");
        logger.info("║ ⚠️  Set BREVO_API_KEY environment variable               ║");
        logger.info("║ Get API key: https://app.brevo.com > SMTP & API           ║");
        logger.info("╚════════════════════════════════════════════════════════════╝\n");
    }
}
