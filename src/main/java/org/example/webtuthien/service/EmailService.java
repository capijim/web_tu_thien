package org.example.webtuthien.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private TemplateEngine templateEngine;
    
    @Value("${app.email.from}")
    private String fromEmail;
    
    @Value("${app.email.name}")
    private String fromName;

    @Value("${app.base-url}")
    private String baseUrl;

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
        logger.info("║              📧 SENDING REAL EMAIL VIA GMAIL               ║");
        logger.info("╠════════════════════════════════════════════════════════════╣");
        logger.info("║ From:    {} <{}>", fromName, fromEmail);
        logger.info("║ To:      {}", toEmail);
        logger.info("║ Donor:   {}", donorName);
        logger.info("║ Amount:  {} VNĐ", String.format("%,d", amount.longValue()));
        logger.info("║ Campaign: {}", campaignTitle);
        logger.info("║ Base URL: {}", baseUrl);
        logger.info("╚════════════════════════════════════════════════════════════╝");
        
        try {
            // Validate email configuration
            if (fromEmail == null || fromEmail.isEmpty()) {
                throw new IllegalStateException("Email sender address is not configured!");
            }
            if (toEmail == null || toEmail.isEmpty()) {
                throw new IllegalArgumentException("Recipient email address is required!");
            }
            
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
            
            logger.info("Processing email template...");
            // Process template
            String htmlContent = templateEngine.process("email/donation-success", context);
            logger.info("✓ Template processed ({} chars)", htmlContent.length());
            
            // Create email
            logger.info("Creating MIME message...");
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("✅ Xác nhận quyên góp thành công - " + campaignTitle);
            helper.setText(htmlContent, true);
            
            logger.info("Attempting to send email via Gmail SMTP...");
            
            // Send email
            mailSender.send(mimeMessage);
            
            logger.info("\n╔════════════════════════════════════════════════════════════╗");
            logger.info("║              ✅ EMAIL SENT SUCCESSFULLY!                   ║");
            logger.info("╠════════════════════════════════════════════════════════════╣");
            logger.info("║ ✉️  Email delivered to: {}", toEmail);
            logger.info("║ 📧 Email ID: {}", donationId);
            logger.info("║ 📅 Sent at: {}", donationDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            logger.info("║ 💡 Check inbox (and spam folder)");
            logger.info("╚════════════════════════════════════════════════════════════╝\n");
            
        } catch (MessagingException e) {
            logger.error("\n╔════════════════════════════════════════════════════════════╗");
            logger.error("║              ❌ EMAIL SEND FAILED!                         ║");
            logger.error("╠════════════════════════════════════════════════════════════╣");
            logger.error("║ Error Type: MessagingException");
            logger.error("║ Message: {}", e.getMessage());
            if (e.getCause() != null) {
                logger.error("║ Cause: {}", e.getCause().getMessage());
            }
            logger.error("╠════════════════════════════════════════════════════════════╣");
            logger.error("║ Troubleshooting:                                          ║");
            logger.error("║ 1. Verify App Password: 16 chars, no spaces              ║");
            logger.error("║ 2. Enable 2-Step Verification in Gmail                   ║");
            logger.error("║ 3. Check Gmail SMTP settings on Railway                  ║");
            logger.error("║ 4. Verify Railway environment variables                  ║");
            logger.error("╚════════════════════════════════════════════════════════════╝\n");
            logger.error("Full stack trace:", e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("\n╔════════════════════════════════════════════════════════════╗");
            logger.error("║              ❌ UNEXPECTED ERROR!                          ║");
            logger.error("╠════════════════════════════════════════════════════════════╣");
            logger.error("║ Error: {}", e.getMessage());
            logger.error("╚════════════════════════════════════════════════════════════╝\n");
            logger.error("Full stack trace:", e);
            throw new RuntimeException("Unexpected error sending email: " + e.getMessage(), e);
        }
    }
}
