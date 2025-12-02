package org.example.webtuthien.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import reactor.core.publisher.Mono;

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
            
            // Create plain text version
            String textContent = String.format(
                "Xin chào %s,\n\n" +
                "Cảm ơn bạn đã quyên góp %s VNĐ cho chiến dịch: %s\n\n" +
                "Thông tin chi tiết:\n" +
                "- Danh mục: %s\n" +
                "- Ngày quyên góp: %s\n" +
                "- Mã giao dịch: %s\n" +
                "%s\n" +
                "Xem chi tiết chiến dịch: %s\n\n" +
                "Trân trọng,\n" +
                "Web Từ Thiện",
                donorName,
                String.format("%,d", amount.longValue()),
                campaignTitle,
                campaignCategory,
                donationDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                donationId,
                (message != null && !message.isEmpty() ? "\nLời nhắn của bạn: " + message + "\n" : ""),
                baseUrl + "/campaign/" + campaignId
            );
            
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
            
            // Content - ADD BOTH HTML AND TEXT
            emailRequest.put("subject", "✅ Xác nhận quyên góp thành công - " + campaignTitle);
            emailRequest.put("htmlContent", htmlContent);
            emailRequest.put("textContent", textContent); // ← ADD THIS
            
            // Add headers to improve deliverability
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Mailer", "Brevo");
            headers.put("charset", "UTF-8");
            emailRequest.put("headers", headers);
            
            logger.info("╔════════════════════════════════════════════════════════════╗");
            logger.info("║ 📤 Brevo API Request Details:                             ║");
            logger.info("║ Sender: {} <{}>", fromName, fromEmail);
            logger.info("║ To: {} <{}>", donorName, toEmail);
            logger.info("║ Subject: {}", emailRequest.get("subject"));
            logger.info("║ API Key: {}", brevoApiKey.substring(0, 20) + "...");
            logger.info("╚════════════════════════════════════════════════════════════╝");
            
            // Send via Brevo API
            Map<String, Object> response = webClient.post()
                    .uri("/smtp/email")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("api-key", brevoApiKey)
                    .bodyValue(emailRequest)
                    .retrieve()
                    .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                logger.error("Brevo API Error Response: {}", errorBody);
                                return Mono.error(new RuntimeException("Brevo API Error: " + errorBody));
                            })
                    )
                    .bodyToMono(Map.class)
                    .block();
            
            logger.info("\n╔════════════════════════════════════════════════════════════╗");
            logger.info("║              ✅ EMAIL SENT VIA BREVO API!                  ║");
            logger.info("╠════════════════════════════════════════════════════════════╣");
            logger.info("║ ✉️  Delivered to: {}", toEmail);
            logger.info("║ 📧 Donation ID: {}", donationId);
            logger.info("║ 🆔 Message ID: {}", response != null ? response.get("messageId") : "N/A");
            logger.info("║ 📅 Sent at: {}", donationDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            logger.info("╠════════════════════════════════════════════════════════════╣");
            logger.info("║ 🔍 CHECK EMAIL:                                           ║");
            logger.info("║    1. Inbox: {}", toEmail);
            logger.info("║    2. Spam/Junk folder                                    ║");
            logger.info("║    3. Brevo Dashboard > Email > Transactional             ║");
            logger.info("║    4. Check sender verification status                    ║");
            logger.info("╠════════════════════════════════════════════════════════════╣");
            logger.info("║ ⚠️  IMPORTANT:                                             ║");
            logger.info("║    - Sender email MUST be verified in Brevo               ║");
            logger.info("║    - Check Brevo > Senders & IP > Senders                ║");
            logger.info("║    - Email: {} must have ✅ mark", fromEmail);
            logger.info("╚════════════════════════════════════════════════════════════╝\n");
            
        } catch (WebClientResponseException e) {
            logger.error("\n╔════════════════════════════════════════════════════════════╗");
            logger.error("║              ❌ BREVO API ERROR!                           ║");
            logger.error("╠════════════════════════════════════════════════════════════╣");
            logger.error("║ Status Code: {}", e.getStatusCode());
            logger.error("║ Response: {}", e.getResponseBodyAsString());
            logger.error("╠════════════════════════════════════════════════════════════╣");
            logger.error("║ Common Issues:                                            ║");
            logger.error("║ 1. Sender email not verified                              ║");
            logger.error("║ 2. Invalid API key                                        ║");
            logger.error("║ 3. API key missing email permission                       ║");
            logger.error("║ 4. Recipient email invalid/blocked                        ║");
            logger.error("╚════════════════════════════════════════════════════════════╝\n");
            throw new RuntimeException("Brevo API error: " + e.getResponseBodyAsString(), e);
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
