package org.example.webtuthien.controller.admin;

import org.example.webtuthien.model.Admin;
import org.example.webtuthien.service.AdminAuthService;
import org.example.webtuthien.service.AdminService;
import org.example.webtuthien.service.CampaignService;
import org.example.webtuthien.service.DonationService;
import org.example.webtuthien.service.PartnerService;
import org.example.webtuthien.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class AdminViewController {

    private final AdminService adminService;
    private final AdminAuthService adminAuthService;
    private final UserService userService;
    private final CampaignService campaignService;
    private final DonationService donationService;
    private final PartnerService partnerService;

    public AdminViewController(AdminService adminService,
                               AdminAuthService adminAuthService,
                               UserService userService,
                               CampaignService campaignService,
                               DonationService donationService,
                               PartnerService partnerService) {
        this.adminService = adminService;
        this.adminAuthService = adminAuthService;
        this.userService = userService;
        this.campaignService = campaignService;
        this.donationService = donationService;
        this.partnerService = partnerService;
    }

    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin/login";
    }

    @PostMapping("/admin/login")
    public String handleAdminLogin(@RequestParam String usernameOrEmail, 
                                   @RequestParam String password, 
                                   Model model, 
                                   HttpSession session) {
        Optional<Admin> adminOpt = adminAuthService.authenticate(usernameOrEmail, password);
        if (adminOpt.isEmpty()) {
            model.addAttribute("error", "Tên đăng nhập/email hoặc mật khẩu không đúng");
            return "admin/login";
        }
        
        Admin admin = adminOpt.get();
        session.setAttribute("admin", admin);
        session.setAttribute("adminId", admin.getId());
        session.setAttribute("adminUsername", admin.getUsername());
        
        return "redirect:/admin";
    }

    @GetMapping("/admin/logout")
    public String adminLogout(HttpSession session) {
        session.removeAttribute("admin");
        session.removeAttribute("adminId");
        session.removeAttribute("adminUsername");
        return "redirect:/admin/login";
    }

    @GetMapping("/admin")
    public String adminHome(Model model, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/login";
        }
        
        model.addAttribute("admin", admin);
        model.addAttribute("stats", adminService.getDashboardStats());
        return "admin/index";
    }

    @GetMapping("/admin/users")
    public String users(Model model, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/login";
        }
        
        model.addAttribute("admin", admin);
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @GetMapping("/admin/campaigns")
    public String campaigns(Model model, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/login";
        }
        
        model.addAttribute("admin", admin);
        model.addAttribute("campaigns", campaignService.list());
        return "admin/campaigns";
    }

    @GetMapping("/admin/donations")
    public String donations(Model model, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/login";
        }
        
        model.addAttribute("admin", admin);
        model.addAttribute("donations", donationService.list());
        return "admin/donations";
    }

    @GetMapping("/admin/partners")
    public String partners(Model model, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/login";
        }
        
        model.addAttribute("admin", admin);
        model.addAttribute("partners", partnerService.list());
        return "admin/partners";
    }
}


