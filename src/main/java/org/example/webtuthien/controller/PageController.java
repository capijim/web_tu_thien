package org.example.webtuthien.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.example.webtuthien.service.CampaignService;
import org.example.webtuthien.service.DonationService;
import org.example.webtuthien.service.UserService;
import org.example.webtuthien.service.EmailService;
import org.example.webtuthien.model.Campaign;
import org.example.webtuthien.model.Donation;
import org.example.webtuthien.model.User;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
public class PageController {
    
    private static final Logger logger = LoggerFactory.getLogger(PageController.class);
    
    @Autowired
    private CampaignService campaignService;
    
    @Autowired
    private DonationService donationService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EmailService emailService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/campaigns")
    public String campaigns(Model model) {
        try {
            List<Campaign> campaigns = campaignService.list();
            model.addAttribute("campaigns", campaigns);
            return "campaigns";
        } catch (Exception e) {
            logger.error("Error loading campaigns", e);
            model.addAttribute("error", "Lỗi tải danh sách chiến dịch");
            return "campaigns";
        }
    }

    @GetMapping("/campaign/{id}")
    public String campaignDetail(@PathVariable Long id, Model model) {
        logger.info("Loading campaign detail for id: {}", id);
        try {
            var campaignOpt = campaignService.findById(id);
            if (campaignOpt.isEmpty()) {
                logger.warn("Campaign not found: {}", id);
                model.addAttribute("error", "Không tìm thấy chiến dịch");
                return "campaign_detail";
            }
            
            Campaign campaign = campaignOpt.get();
            logger.info("Found campaign: {}", campaign.getTitle());
            model.addAttribute("campaign", campaign);
            
            // Get donation stats
            try {
                Map<String, Object> stats = campaignService.getCampaignStats(id);
                logger.info("Campaign stats: {}", stats);
                model.addAttribute("stats", stats);
            } catch (Exception e) {
                logger.error("Error loading stats", e);
            }
            
            // Get donations list
            try {
                List<Donation> donations = donationService.findByCampaignId(id);
                logger.info("Found {} donations", donations.size());
                model.addAttribute("donations", donations);
            } catch (Exception e) {
                logger.error("Error loading donations", e);
            }
            
            return "campaign_detail";
        } catch (Exception e) {
            logger.error("Error loading campaign detail", e);
            model.addAttribute("error", "Lỗi tải thông tin chiến dịch: " + e.getMessage());
            return "campaign_detail";
        }
    }

    @PostMapping("/campaign/{id}/donate")
    public String donateToCampaign(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String message,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            logger.info("Processing donation for campaign {}, amount: {}", id, amount);
            
            // Check authentication
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj == null) {
                logger.warn("User not authenticated");
                redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để quyên góp");
                return "redirect:/login?redirect=/campaign/" + id;
            }

            // Validate amount
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                logger.warn("Invalid amount: {}", amount);
                redirectAttributes.addFlashAttribute("error", "Số tiền phải lớn hơn 0");
                return "redirect:/campaign/" + id;
            }

            // Get user info
            Long userId = Long.valueOf(userIdObj.toString());
            var userOpt = userService.getUserById(userId);
            if (userOpt.isEmpty()) {
                logger.warn("User not found: {}", userId);
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin người dùng");
                return "redirect:/campaign/" + id;
            }
            
            User user = userOpt.get();
            
            // Get campaign info
            var campaignOpt = campaignService.findById(id);
            if (campaignOpt.isEmpty()) {
                logger.warn("Campaign not found: {}", id);
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy chiến dịch");
                return "redirect:/campaign/" + id;
            }
            
            Campaign campaign = campaignOpt.get();

            // Create donation record
            Donation donation = new Donation();
            donation.setCampaignId(id);
            donation.setDonorName(user.getName());
            donation.setAmount(amount);
            donation.setMessage(message);
            
            logger.info("Creating donation record: campaign={}, donor={}, amount={}", 
                id, donation.getDonorName(), amount);
            Donation createdDonation = donationService.create(donation);
            logger.info("Donation created successfully with ID: {}", createdDonation.getId());

            // Update campaign amount
            campaignService.updateCurrentAmount(id, amount);
            logger.info("Campaign amount updated");
            
            // Send email notification
            try {
                emailService.sendDonationSuccessEmail(
                    user.getEmail(),
                    user.getName(),
                    amount,
                    campaign.getTitle(),
                    campaign.getCategory(),
                    id,
                    createdDonation.getId(),
                    message,
                    createdDonation.getCreatedAt()
                );
                logger.info("Donation success email sent to: {}", user.getEmail());
            } catch (Exception e) {
                logger.error("Failed to send email, but donation was successful", e);
                // Continue - don't fail the donation if email fails
            }
            
            redirectAttributes.addFlashAttribute("success", 
                "Quyên góp thành công! Cảm ơn bạn đã đóng góp " + 
                amount.toString() + " VNĐ. Email xác nhận đã được gửi đến " + user.getEmail());
            return "redirect:/campaign/" + id;
        } catch (Exception e) {
            logger.error("Error donating", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi quyên góp: " + e.getMessage());
            return "redirect:/campaign/" + id;
        }
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/change-password")
    public String changePassword() {
        return "change_password";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }
}


