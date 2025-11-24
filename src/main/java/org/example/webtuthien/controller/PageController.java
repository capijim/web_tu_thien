package org.example.webtuthien.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.example.webtuthien.service.CampaignService;
import org.example.webtuthien.model.Campaign;
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
                // Stats are optional, continue without them
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
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj == null) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để quyên góp");
                return "redirect:/login";
            }

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                redirectAttributes.addFlashAttribute("error", "Số tiền phải lớn hơn 0");
                return "redirect:/campaign/" + id;
            }

            campaignService.updateCurrentAmount(id, amount);
            
            redirectAttributes.addFlashAttribute("success", "Quyên góp thành công! Cảm ơn bạn đã đóng góp.");
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


