package org.example.webtuthien.campaign;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.webtuthien.donation.DonationRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
public class CampaignService {
    private final CampaignRepository repository;
    
    @Autowired
    private DonationRepository donationRepository;

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
        BigDecimal base = campaign.getCurrentAmount() != null ? campaign.getCurrentAmount() : BigDecimal.ZERO;
        BigDecimal newAmount = base.add(amount);
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
        // Xóa donations liên quan trước để tránh lỗi FK
        try {
            donationRepository.deleteByCampaignId(id);
        } catch (Exception ignored) {}
        repository.deleteById(id);
    }

    public List<Map<String, Object>> listWithStats() {
        List<Campaign> campaigns = repository.findAll();
        return campaigns.stream().map(campaign -> {
            Map<String, Object> campaignWithStats = new HashMap<>();
            campaignWithStats.put("id", campaign.getId());
            campaignWithStats.put("userId", campaign.getUserId());
            campaignWithStats.put("title", campaign.getTitle());
            campaignWithStats.put("description", campaign.getDescription());
            campaignWithStats.put("targetAmount", campaign.getTargetAmount());
            campaignWithStats.put("currentAmount", campaign.getCurrentAmount());
            campaignWithStats.put("category", campaign.getCategory());
            campaignWithStats.put("imageUrl", campaign.getImageUrl());
            campaignWithStats.put("status", campaign.getStatus());
            campaignWithStats.put("endDate", campaign.getEndDate() != null ? campaign.getEndDate().toString() : null);
            campaignWithStats.put("createdAt", campaign.getCreatedAt() != null ? campaign.getCreatedAt().toString() : null);

            // Add donation count (fail-safe)
            int donationCount = 0;
            try {
                donationCount = donationRepository.countByCampaignId(campaign.getId());
            } catch (Exception ignored) {}
            campaignWithStats.put("donationCount", donationCount);

            return campaignWithStats;
        }).collect(java.util.stream.Collectors.toList());
    }
    
    public Map<String, Object> findByIdWithStats(Long id) {
        Optional<Campaign> campaignOpt = repository.findById(id);
        if (campaignOpt.isEmpty()) {
            throw new IllegalArgumentException("Campaign not found");
        }

        Campaign campaign = campaignOpt.get();
        Map<String, Object> campaignWithStats = new HashMap<>();
        campaignWithStats.put("id", campaign.getId());
        campaignWithStats.put("userId", campaign.getUserId());
        campaignWithStats.put("title", campaign.getTitle());
        campaignWithStats.put("description", campaign.getDescription());
        campaignWithStats.put("targetAmount", campaign.getTargetAmount());
        campaignWithStats.put("currentAmount", campaign.getCurrentAmount());
        campaignWithStats.put("category", campaign.getCategory());
        campaignWithStats.put("imageUrl", campaign.getImageUrl());
        campaignWithStats.put("status", campaign.getStatus());
        campaignWithStats.put("endDate", campaign.getEndDate() != null ? campaign.getEndDate().toString() : null);
        campaignWithStats.put("createdAt", campaign.getCreatedAt() != null ? campaign.getCreatedAt().toString() : null);

        // Add donation count (fail-safe)
        int donationCount = 0;
        try {
            donationCount = donationRepository.countByCampaignId(campaign.getId());
        } catch (Exception ignored) {}
        campaignWithStats.put("donationCount", donationCount);

        return campaignWithStats;
    }
}
