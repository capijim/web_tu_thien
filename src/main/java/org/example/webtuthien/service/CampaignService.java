package org.example.webtuthien.service;

import org.example.webtuthien.model.Campaign;
import org.example.webtuthien.repository.CampaignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class CampaignService {
    
    @Autowired
    private CampaignRepository repository;

    public List<Campaign> list() {
        return repository.findAll();
    }

    public List<Campaign> listActive() {
        return repository.findByStatus("Active");
    }

    public Optional<Campaign> findById(Long id) {
        return repository.findById(id);
    }

    public List<Campaign> findByCategory(String category) {
        return repository.findByCategory(category);
    }

    public List<Campaign> findByPartnerId(Long partnerId) {
        return repository.findByPartnerId(partnerId);
    }

    public Campaign create(Campaign campaign) {
        if (campaign.getTitle() == null || campaign.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (campaign.getTargetAmount() == null || campaign.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Target amount must be greater than 0");
        }
        if (campaign.getCurrentAmount() == null) {
            campaign.setCurrentAmount(BigDecimal.ZERO);
        }
        if (campaign.getStatus() == null || campaign.getStatus().isEmpty()) {
            campaign.setStatus("Active");
        }
        return repository.save(campaign);
    }

    @Transactional
    public Campaign updateCurrentAmount(Long id, BigDecimal amount) {
        Campaign campaign = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        
        BigDecimal newAmount = campaign.getCurrentAmount().add(amount);
        campaign.setCurrentAmount(newAmount);
        
        return repository.save(campaign);
    }

    @Transactional
    public Campaign updateStatus(Long id, String status) {
        Campaign campaign = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        
        List<String> validStatuses = Arrays.asList("Active", "Completed", "Cancelled");
        if (!validStatuses.contains(status)) {
            throw new IllegalArgumentException("Invalid status. Must be one of: " + String.join(", ", validStatuses));
        }
        
        campaign.setStatus(status);
        return repository.save(campaign);
    }

    public List<String> getCategories() {
        return Arrays.asList(
            "Y tế", "Giáo dục", "Thiên tai", "Trẻ em", 
            "Người già", "Môi trường", "Động vật", 
            "Văn hóa", "Thể thao", "Khác"
        );
    }

    public Map<String, Object> getCampaignStats(Long campaignId) {
        Campaign campaign = repository.findById(campaignId)
            .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("donationCount", 0); // This should be calculated from donations table
        stats.put("totalAmount", campaign.getCurrentAmount());
        stats.put("progressPercentage", campaign.getProgressPercentage());
        
        return stats;
    }

    public List<Map<String, Object>> listWithStats() {
        List<Campaign> campaigns = repository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Campaign campaign : campaigns) {
            Map<String, Object> campaignMap = new HashMap<>();
            campaignMap.put("id", campaign.getId());
            campaignMap.put("partnerId", campaign.getPartnerId());
            campaignMap.put("title", campaign.getTitle());
            campaignMap.put("description", campaign.getDescription());
            campaignMap.put("targetAmount", campaign.getTargetAmount());
            campaignMap.put("currentAmount", campaign.getCurrentAmount());
            campaignMap.put("category", campaign.getCategory());
            campaignMap.put("status", campaign.getStatus());
            campaignMap.put("imageUrl", campaign.getImageUrl());
            campaignMap.put("endDate", campaign.getEndDate());
            campaignMap.put("createdAt", campaign.getCreatedAt());
            campaignMap.put("progressPercentage", campaign.getProgressPercentage());
            campaignMap.put("donationCount", 0); // Should be from donations table
            
            result.add(campaignMap);
        }
        
        return result;
    }

    public Map<String, Object> findByIdWithStats(Long id) {
        Campaign campaign = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Campaign not found with id: " + id));
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", campaign.getId());
        result.put("partnerId", campaign.getPartnerId());
        result.put("title", campaign.getTitle());
        result.put("description", campaign.getDescription());
        result.put("targetAmount", campaign.getTargetAmount());
        result.put("currentAmount", campaign.getCurrentAmount());
        result.put("category", campaign.getCategory());
        result.put("status", campaign.getStatus());
        result.put("imageUrl", campaign.getImageUrl());
        result.put("endDate", campaign.getEndDate());
        result.put("createdAt", campaign.getCreatedAt());
        result.put("progressPercentage", campaign.getProgressPercentage());
        result.put("donationCount", 0); // Should be from donations table
        
        return result;
    }

    @Transactional
    public void deleteCampaign(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Campaign not found with id: " + id);
        }
        repository.deleteById(id);
    }
}
