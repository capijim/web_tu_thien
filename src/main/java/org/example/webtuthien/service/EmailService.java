package org.example.webtuthien.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private TemplateEngine templateEngine;
    
    @Value("${app.email.from}")
    private String fromEmail;
    
    @Value("${app.email.name}")
    private String fromName;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    private final WebClient webClient;

    public EmailService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .build();
    }

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
        
        logger.info("\n╔════════════════════════════════════════════════════════════╗");
        logger.info("║              📧 SENDING EMAIL VIA BREVO API                ║");
        logger.info("╠════════════════════════════════════════════════════════════╣");
        logger.info("║ From:    {} <{}>", fromName, fromEmail);
        logger.info("║ To:      {}", toEmail);
        logger.info("║ Donor:   {}", donorName);
        logger.info("║ Amount:  {} VNĐ", String.format("%,d", amount.longValue()));
        logger.info("║ Campaign: {}", campaignTitle);
        logger.info("║ Base URL: {}", baseUrl);
        logger.info("╚════════════════════════════════════════════════════════════╝");
        
        try {
            // Validate
            if (brevoApiKey == null || brevoApiKey.isEmpty()) {
                throw new IllegalStateException("Brevo API key is not configured! Set BREVO_API_KEY in Railway.");
            }
            if (toEmail == null || toEmail.isEmpty() || !toEmail.contains("@")) {
                throw new IllegalArgumentException("Invalid recipient email address: " + toEmail);
            }
            
            logger.info("✓ Configuration validated");
            
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
            context.setVariable("appBaseUrl", baseUrl);
            context.setVariable("appEmailFrom", fromEmail);
            context.setVariable("appEmailName", fromName);
            
            logger.info("Processing email template...");
            String htmlContent = templateEngine.process("email/donation-success", context);
            logger.info("✓ Template processed ({} chars)", htmlContent.length());
            
            // Prepare Brevo API request
            Map<String, Object> emailRequest = new HashMap<>();
            
            // Sender
            Map<String, String> sender = new HashMap<>();
            sender.put("name", fromName);
            sender.put("email", fromEmail);
            emailRequest.put("sender", sender);
            
            // Recipient
            Map<String, String> recipient = new HashMap<>();
            recipient.put("email", toEmail);
            recipient.put("name", donorName);
            emailRequest.put("to", new Map[]{recipient});
            
            // Content
            emailRequest.put("subject", "✅ Xác nhận quyên góp thành công - " + campaignTitle);
            emailRequest.put("htmlContent", htmlContent);
            
            logger.info("Sending email via Brevo API...");
            
            // Send via Brevo API
            String response = webClient.post()
                    .uri("/smtp/email")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("api-key", brevoApiKey)
                    .bodyValue(emailRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            logger.info("\n╔════════════════════════════════════════════════════════════╗");
            logger.info("║              ✅ EMAIL SENT SUCCESSFULLY VIA BREVO API!     ║");
            logger.info("╠════════════════════════════════════════════════════════════╣");
            logger.info("║ ✉️  Email delivered to: {}", toEmail);
            logger.info("║ 📧 Donation ID: {}", donationId);
            logger.info("║ 📅 Sent at: {}", donationDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            logger.info("║ 📊 API Response: {}", response != null ? response.substring(0, Math.min(100, response.length())) : "OK");
            logger.info("╚════════════════════════════════════════════════════════════╝\n");
            
        } catch (Exception e) {
            logger.error("\n╔════════════════════════════════════════════════════════════╗");
            logger.error("║              ❌ EMAIL SEND FAILED!                         ║");
            logger.error("╠════════════════════════════════════════════════════════════╣");
            logger.error("║ Error: {}", e.getMessage());
            logger.error("╠════════════════════════════════════════════════════════════╣");
            logger.error("║ Troubleshooting:                                          ║");
            logger.error("║ 1. Check BREVO_API_KEY is set in Railway                 ║");
            logger.error("║ 2. Verify sender email in Brevo dashboard                ║");
            logger.error("║ 3. Check Brevo account is active                         ║");
            logger.error("║ 4. Verify API key has email sending permission           ║");
            logger.error("╚════════════════════════════════════════════════════════════╝\n");
            logger.error("Full stack trace:", e);
            throw new RuntimeException("Failed to send email via Brevo API: " + e.getMessage(), e);
        }
    }
}
