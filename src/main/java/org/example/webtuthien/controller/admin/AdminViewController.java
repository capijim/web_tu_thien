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
import java.util.HashMap;
import java.util.Map;

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

    @GetMapping("/admin/test")
    public String testDatabase(Model model) {
        try {
            System.out.println("Testing database connection...");
            var admins = adminAuthService.getAllAdmins();
            System.out.println("Found " + admins.size() + " admins in database");
            model.addAttribute("message", "Database connection OK. Found " + admins.size() + " admins.");
            model.addAttribute("admins", admins);
            return "admin/test";
        } catch (Exception e) {
            System.err.println("Database test failed: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Database test failed: " + e.getMessage());
            return "admin/test";
        }
    }

    @PostMapping("/admin/login")
    public String handleAdminLogin(@RequestParam String usernameOrEmail, 
                                   @RequestParam String password, 
                                   Model model, 
                                   HttpSession session) {
        try {
            System.out.println("Admin login attempt for: " + usernameOrEmail);
            
            // Kiểm tra input
            if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
                model.addAttribute("error", "Tên đăng nhập/email không được để trống");
                return "admin/login";
            }
            
            if (password == null || password.trim().isEmpty()) {
                model.addAttribute("error", "Mật khẩu không được để trống");
                return "admin/login";
            }
            
            Optional<Admin> adminOpt = adminAuthService.authenticate(usernameOrEmail.trim(), password);
            if (adminOpt.isEmpty()) {
                System.out.println("Authentication failed for: " + usernameOrEmail);
                model.addAttribute("error", "Tên đăng nhập/email hoặc mật khẩu không đúng");
                return "admin/login";
            }
            
            Admin admin = adminOpt.get();
            System.out.println("Authentication successful for admin: " + admin.getUsername());
            
            session.setAttribute("admin", admin);
            session.setAttribute("adminId", admin.getId());
            session.setAttribute("adminUsername", admin.getUsername());
            
            return "redirect:/admin";
        } catch (Exception e) {
            System.err.println("Error in admin login: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Lỗi đăng nhập: " + e.getMessage());
            return "admin/login";
        }
    }

    @PostMapping("/admin/logout")
    public String handleAdminLogout(HttpSession session) {
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
        
        try {
            model.addAttribute("admin", admin);
            model.addAttribute("stats", adminService.getDashboardStats());
            return "admin/index";
        } catch (Exception e) {
            System.err.println("Error loading admin dashboard: " + e.getMessage());
            e.printStackTrace();
            // Trả về stats mặc định nếu có lỗi
            Map<String, Object> defaultStats = new HashMap<>();
            defaultStats.put("totalUsers", 0);
            defaultStats.put("totalCampaigns", 0);
            defaultStats.put("activeCampaigns", 0);
            defaultStats.put("totalDonationAmount", 0);
            model.addAttribute("admin", admin);
            model.addAttribute("stats", defaultStats);
            return "admin/index";
        }
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


