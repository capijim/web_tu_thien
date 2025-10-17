package org.example.webtuthien.controller;

import org.example.webtuthien.service.CampaignService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    private final CampaignService campaignService;

    public PageController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @GetMapping({"/index", "/home"})
    public String index(Model model) {
        model.addAttribute("featured", campaignService.listActive());
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/campaigns")
    public String campaigns(Model model) {
        model.addAttribute("campaigns", campaignService.list());
        return "campaigns";
    }

    // Đường dẫn chi tiết chiến dịch đã được xử lý tại CampaignViewController#detail
    // Tránh trùng mapping với "/campaign/{id}" để không gây lỗi Ambiguous mapping

    @GetMapping("/change-password")
    public String changePassword() {
        return "change_password";
    }
}


