package org.example.webtuthien.controller;

import jakarta.servlet.http.HttpSession;
import org.example.webtuthien.model.Campaign;
import org.example.webtuthien.model.Donation;
import org.example.webtuthien.service.CampaignService;
import org.example.webtuthien.service.DonationService;
import org.example.webtuthien.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Map;

@Controller
public class CampaignViewController {

    private final CampaignService campaignService;
    private final DonationService donationService;
    private final UserService userService;

    public CampaignViewController(CampaignService campaignService,
                                  DonationService donationService,
                                  UserService userService) {
        this.campaignService = campaignService;
        this.donationService = donationService;
        this.userService = userService;
    }

    @GetMapping("/campaign/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var campaignOpt = campaignService.findById(id);
        if (campaignOpt.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy chiến dịch");
            return "campaign_detail";
        }
        Campaign c = campaignOpt.get();
        Map<String, Object> withStats = campaignService.findByIdWithStats(id);
        model.addAttribute("campaign", c);
        model.addAttribute("stats", withStats);
        return "campaign_detail";
    }

    @PostMapping("/campaign/{id}/donate")
    public String donate(@PathVariable Long id,
                         @RequestParam("amount") BigDecimal amount,
                         @RequestParam(value = "message", required = false) String message,
                         HttpSession session,
                         Model model) {
        var campaignOpt = campaignService.findById(id);
        if (campaignOpt.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy chiến dịch");
            return "campaign_detail";
        }
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            model.addAttribute("error", "Vui lòng đăng nhập để ủng hộ");
            model.addAttribute("campaign", campaignOpt.get());
            model.addAttribute("stats", campaignService.findByIdWithStats(id));
            return "campaign_detail";
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            model.addAttribute("error", "Số tiền không hợp lệ");
            model.addAttribute("campaign", campaignOpt.get());
            model.addAttribute("stats", campaignService.findByIdWithStats(id));
            return "campaign_detail";
        }

        var userOpt = userService.getUserById(Long.valueOf(userIdObj.toString()));
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy người dùng");
            model.addAttribute("campaign", campaignOpt.get());
            model.addAttribute("stats", campaignService.findByIdWithStats(id));
            return "campaign_detail";
        }

        Donation donation = new Donation();
        donation.setCampaignId(id);
        donation.setDonorName(userOpt.get().getName());
        donation.setAmount(amount);
        donation.setMessage(message);
        donationService.create(donation);
        campaignService.updateCurrentAmount(id, amount);

        model.addAttribute("success", "Cảm ơn bạn đã ủng hộ!");
        model.addAttribute("campaign", campaignOpt.get());
        model.addAttribute("stats", campaignService.findByIdWithStats(id));
        return "campaign_detail";
    }
}


