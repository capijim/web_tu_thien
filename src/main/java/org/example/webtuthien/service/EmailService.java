package org.example.webtuthien.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private BrevoEmailService brevoEmailService;

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
        
        logger.info("📧 Delegating email sending to Brevo API service...");
        brevoEmailService.sendDonationSuccessEmail(
            toEmail, donorName, amount, campaignTitle, campaignCategory,
            campaignId, donationId, message, donationDate
        );
    }
}
