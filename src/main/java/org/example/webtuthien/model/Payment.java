package org.example.webtuthien.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Payment {
    private Long id;
    private Long donationId;
    private String vnpayTransactionId;
    private String vnpayResponseCode;
    private String vnpayTxnRef;
    private BigDecimal amount;
    private String bankCode;
    private String paymentStatus; // PENDING, SUCCESS, FAILED
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    
    // Additional field for tracking
    private Long campaignId;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getDonationId() { return donationId; }
    public void setDonationId(Long donationId) { this.donationId = donationId; }
    
    public String getVnpayTransactionId() { return vnpayTransactionId; }
    public void setVnpayTransactionId(String vnpayTransactionId) { 
        this.vnpayTransactionId = vnpayTransactionId; 
    }
    
    public String getVnpayResponseCode() { return vnpayResponseCode; }
    public void setVnpayResponseCode(String vnpayResponseCode) { 
        this.vnpayResponseCode = vnpayResponseCode; 
    }
    
    public String getVnpayTxnRef() { return vnpayTxnRef; }
    public void setVnpayTxnRef(String vnpayTxnRef) { this.vnpayTxnRef = vnpayTxnRef; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }
    
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Long getCampaignId() { return campaignId; }
    public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }
}
