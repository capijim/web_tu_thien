package org.example.webtuthien.campaign;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CampaignService {
    private final CampaignRepository repository;

    public CampaignService(CampaignRepository repository) {
        this.repository = repository;
    }

    public List<Campaign> list() {
        return repository.findAll();
    }

    public List<Campaign> listActive() {
        return repository.findActive();
    }

    public List<Campaign> findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    public List<Campaign> findByCategory(String category) {
        return repository.findByCategory(category);
    }

    public Optional<Campaign> findById(Long id) {
        return repository.findById(id);
    }

    public Campaign create(Campaign campaign) {
        // Validate required fields
        if (campaign.getUserId() == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (campaign.getTitle() == null || campaign.getTitle().isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        if (campaign.getDescription() == null || campaign.getDescription().isBlank()) {
            throw new IllegalArgumentException("description is required");
        }
        if (campaign.getTargetAmount() == null || campaign.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("targetAmount must be greater than 0");
        }
        if (campaign.getCategory() == null || campaign.getCategory().isBlank()) {
            throw new IllegalArgumentException("category is required");
        }
        if (campaign.getEndDate() != null && campaign.getEndDate().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("endDate must be in the future");
        }

        // Set default values
        if (campaign.getCurrentAmount() == null) {
            campaign.setCurrentAmount(BigDecimal.ZERO);
        }
        if (campaign.getStatus() == null || campaign.getStatus().isBlank()) {
            campaign.setStatus("active");
        }

        return repository.insert(campaign);
    }

    public Campaign updateCurrentAmount(Long campaignId, BigDecimal amount) {
        Optional<Campaign> campaignOpt = repository.findById(campaignId);
        if (campaignOpt.isEmpty()) {
            throw new IllegalArgumentException("Campaign not found");
        }

        Campaign campaign = campaignOpt.get();
        BigDecimal newAmount = campaign.getCurrentAmount().add(amount);
        repository.updateCurrentAmount(campaignId, newAmount);

        // Update campaign status if target is reached
        if (newAmount.compareTo(campaign.getTargetAmount()) >= 0) {
            repository.updateStatus(campaignId, "completed");
        }

        return repository.findById(campaignId).orElse(campaign);
    }

    public Campaign updateStatus(Long campaignId, String status) {
        Optional<Campaign> campaignOpt = repository.findById(campaignId);
        if (campaignOpt.isEmpty()) {
            throw new IllegalArgumentException("Campaign not found");
        }

        if (!List.of("active", "completed", "cancelled", "expired").contains(status)) {
            throw new IllegalArgumentException("Invalid status");
        }

        repository.updateStatus(campaignId, status);
        return repository.findById(campaignId).orElse(campaignOpt.get());
    }

    public List<String> getCategories() {
        return List.of(
            "Y tế", "Giáo dục", "Thiên tai", "Trẻ em", "Người già", 
            "Môi trường", "Động vật", "Văn hóa", "Thể thao", "Khác"
        );
    }

    public void deleteCampaign(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Campaign ID is required");
        }
        repository.deleteById(id);
    }
}
