package org.example.webtuthien.service;

import org.example.webtuthien.model.Campaign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private UserService userService;

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private DonationService donationService;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Thống kê users
            List<org.example.webtuthien.model.User> users = userService.getAllUsers();
            stats.put("totalUsers", users.size());
            
            // Thống kê campaigns
            List<Campaign> campaigns = campaignService.list();
            stats.put("totalCampaigns", campaigns.size());
            
            long activeCampaigns = campaigns.stream()
                .filter(c -> "active".equals(c.getStatus()))
                .count();
            stats.put("activeCampaigns", activeCampaigns);
            
            // Tính tổng tiền mục tiêu và đã quyên góp
            BigDecimal totalTargetAmount = campaigns.stream()
                .map(Campaign::getTargetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("totalTargetAmount", totalTargetAmount);
            
            BigDecimal totalRaisedAmount = campaigns.stream()
                .map(Campaign::getCurrentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("totalRaisedAmount", totalRaisedAmount);
            
            // Thống kê donations
            List<org.example.webtuthien.model.Donation> donations = donationService.list();
            stats.put("totalDonations", donations.size());
            
            BigDecimal totalDonationAmount = donations.stream()
                .map(org.example.webtuthien.model.Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("totalDonationAmount", totalDonationAmount);
            
        } catch (Exception e) {
            // Nếu có lỗi, trả về stats mặc định
            stats.put("totalUsers", 0);
            stats.put("totalCampaigns", 0);
            stats.put("activeCampaigns", 0);
            stats.put("totalTargetAmount", BigDecimal.ZERO);
            stats.put("totalRaisedAmount", BigDecimal.ZERO);
            stats.put("totalDonations", 0);
            stats.put("totalDonationAmount", BigDecimal.ZERO);
        }
        
        return stats;
    }

    public void updateCampaignStatus(Long campaignId, String status) {
        campaignService.updateStatus(campaignId, status);
    }
}
