package org.example.webtuthien.campaign;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Campaign {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private String category;
    private String imageUrl;
    private String status;
    private OffsetDateTime endDate;
    private OffsetDateTime createdAt;

    public Campaign() {}

    public Campaign(Long userId, String title, String description, BigDecimal targetAmount, 
                   String category, String imageUrl, OffsetDateTime endDate) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.targetAmount = targetAmount;
        this.currentAmount = BigDecimal.ZERO;
        this.category = category;
        this.imageUrl = imageUrl;
        this.status = "active";
        this.endDate = endDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getTargetAmount() { return targetAmount; }
    public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }

    public BigDecimal getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(BigDecimal currentAmount) { this.currentAmount = currentAmount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OffsetDateTime getEndDate() { return endDate; }
    public void setEndDate(OffsetDateTime endDate) { this.endDate = endDate; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    // Helper methods
    public BigDecimal getProgressPercentage() {
        if (targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentAmount.divide(targetAmount, 4, BigDecimal.ROUND_HALF_UP)
                          .multiply(new BigDecimal("100"));
    }

    public boolean isCompleted() {
        return currentAmount.compareTo(targetAmount) >= 0;
    }

    public boolean isExpired() {
        return endDate != null && OffsetDateTime.now().isAfter(endDate);
    }

    @Override
    public String toString() {
        return "Campaign{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", targetAmount=" + targetAmount +
                ", currentAmount=" + currentAmount +
                ", category='" + category + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", status='" + status + '\'' +
                ", endDate=" + endDate +
                ", createdAt=" + createdAt +
                '}';
    }
}
