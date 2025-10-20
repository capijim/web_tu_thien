package org.example.webtuthien.controller.admin;

import org.example.webtuthien.model.Admin;
import org.example.webtuthien.model.Campaign;
import org.example.webtuthien.model.Partner;
import org.example.webtuthien.service.AdminService;
import org.example.webtuthien.service.CampaignService;
import org.example.webtuthien.service.PartnerService;
import org.example.webtuthien.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

@Controller
public class AdminActionController {

    private final UserService userService;
    private final CampaignService campaignService;
    private final AdminService adminService;
    private final PartnerService partnerService;

    public AdminActionController(UserService userService,
                                 CampaignService campaignService,
                                 AdminService adminService,
                                 PartnerService partnerService) {
        this.userService = userService;
        this.campaignService = campaignService;
        this.adminService = adminService;
        this.partnerService = partnerService;
    }

    @PostMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/login";
        }
        
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/campaigns/delete/{id}")
    public String deleteCampaign(@PathVariable Long id, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/login";
        }
        
        campaignService.deleteCampaign(id);
        return "redirect:/admin/campaigns";
    }

    @PostMapping("/admin/campaigns/status/{id}")
    public String updateCampaignStatus(@PathVariable Long id, @RequestParam("status") String status, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/login";
        }
        
        adminService.updateCampaignStatus(id, status);
        return "redirect:/admin/campaigns";
    }

    @PostMapping("/admin/campaigns/create")
    public String createCampaign(Campaign campaign, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/login";
        }
        
        campaignService.create(campaign);
        return "redirect:/admin/campaigns";
    }

    @PostMapping("/admin/partners/create")
    public String createPartner(Partner partner, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/login";
        }
        
        partnerService.create(partner);
        return "redirect:/admin/partners";
    }

    @PostMapping("/admin/partners/delete/{id}")
    public String deletePartner(@PathVariable Long id, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/login";
        }
        
        partnerService.delete(id);
        return "redirect:/admin/partners";
    }
}


