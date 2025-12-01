package org.example.webtuthien.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaigns")
public class Campaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "partner_id", nullable = false)
    private Long partnerId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "target_amount", nullable = false)
    private BigDecimal targetAmount;

    @Column(name = "current_amount", nullable = false)
    private BigDecimal currentAmount;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // NEW: Binary image storage
    @Lob
    @Column(name = "image_data")
    private byte[] imageData;

    @Column(name = "image_mime_type", length = 50)
    private String imageMimeType;

    @Column(name = "image_size")
    private Long imageSize;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // NEW: Getters and Setters
    public byte[] getImageData() { return imageData; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }

    public String getImageMimeType() { return imageMimeType; }
    public void setImageMimeType(String imageMimeType) { this.imageMimeType = imageMimeType; }

    public Long getImageSize() { return imageSize; }
    public void setImageSize(Long imageSize) { this.imageSize = imageSize; }

    // Helper method: Check if campaign has binary image
    @Transient
    public boolean hasBinaryImage() {
        return imageData != null && imageData.length > 0;
    }

    // Helper method: Get image source (URL or binary)
    @Transient
    public String getImageSource() {
        if (hasBinaryImage()) {
            return "/api/campaigns/" + id + "/image";
        }
        return imageUrl != null ? imageUrl : "/images/default-campaign.jpg";
    }

    // Getters and setters for other fields...

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Long partnerId) {
        this.partnerId = partnerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}