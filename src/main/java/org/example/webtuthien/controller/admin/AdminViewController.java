package org.example.webtuthien.controller.admin;

import org.example.webtuthien.service.AdminService;
import org.example.webtuthien.service.CampaignService;
import org.example.webtuthien.service.DonationService;
import org.example.webtuthien.service.PartnerService;
import org.example.webtuthien.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminViewController {

    private final AdminService adminService;
    private final UserService userService;
    private final CampaignService campaignService;
    private final DonationService donationService;
    private final PartnerService partnerService;

    public AdminViewController(AdminService adminService,
                               UserService userService,
                               CampaignService campaignService,
                               DonationService donationService,
                               PartnerService partnerService) {
        this.adminService = adminService;
        this.userService = userService;
        this.campaignService = campaignService;
        this.donationService = donationService;
        this.partnerService = partnerService;
    }

    @GetMapping("/admin")
    public String adminHome(Model model) {
        model.addAttribute("stats", adminService.getDashboardStats());
        return "admin/index";
    }

    @GetMapping("/admin/users")
    public String users(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @GetMapping("/admin/campaigns")
    public String campaigns(Model model) {
        model.addAttribute("campaigns", campaignService.list());
        return "admin/campaigns";
    }

    @GetMapping("/admin/donations")
    public String donations(Model model) {
        model.addAttribute("donations", donationService.list());
        return "admin/donations";
    }

    @GetMapping("/admin/partners")
    public String partners(Model model) {
        model.addAttribute("partners", partnerService.list());
        return "admin/partners";
    }
}


