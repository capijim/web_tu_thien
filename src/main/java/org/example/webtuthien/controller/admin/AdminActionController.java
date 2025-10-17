package org.example.webtuthien.controller.admin;

import org.example.webtuthien.model.Campaign;
import org.example.webtuthien.model.Partner;
import org.example.webtuthien.service.AdminService;
import org.example.webtuthien.service.CampaignService;
import org.example.webtuthien.service.PartnerService;
import org.example.webtuthien.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
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

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/campaigns/delete/{id}")
    public String deleteCampaign(@PathVariable Long id) {
        campaignService.deleteCampaign(id);
        return "redirect:/admin/campaigns";
    }

    @PostMapping("/campaigns/status/{id}")
    public String updateCampaignStatus(@PathVariable Long id, @RequestParam("status") String status) {
        adminService.updateCampaignStatus(id, status);
        return "redirect:/admin/campaigns";
    }

    @PostMapping("/campaigns/create")
    public String createCampaign(Campaign campaign) {
        campaignService.create(campaign);
        return "redirect:/admin/campaigns";
    }

    @PostMapping("/partners/create")
    public String createPartner(Partner partner) {
        partnerService.create(partner);
        return "redirect:/admin/partners";
    }

    @PostMapping("/partners/delete/{id}")
    public String deletePartner(@PathVariable Long id) {
        partnerService.delete(id);
        return "redirect:/admin/partners";
    }
}


