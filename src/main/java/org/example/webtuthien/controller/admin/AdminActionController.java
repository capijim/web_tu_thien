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
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

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
    public String createCampaign(Campaign campaign, 
                                @RequestParam("image") MultipartFile image, 
                                HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/login";
        }
        
        try {
            // Xử lý upload ảnh
            if (image != null && !image.isEmpty()) {
                String uploadDir = "uploads/";
                Path uploadPath = Paths.get(uploadDir);
                
                // Tạo thư mục nếu chưa tồn tại
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                // Tạo tên file unique
                String originalFilename = image.getOriginalFilename();
                if (originalFilename == null || originalFilename.isEmpty()) {
                    throw new IllegalArgumentException("Original filename is null or empty");
                }
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
                
                // Lưu file
                Path filePath = uploadPath.resolve(uniqueFilename);
                Files.copy(image.getInputStream(), filePath);
                
                // Set imageUrl cho campaign
                campaign.setImageUrl("/" + uploadDir + uniqueFilename);
            }
            
            campaignService.create(campaign);
            return "redirect:/admin/campaigns";
            
        } catch (IOException e) {
            System.err.println("Error uploading image: " + e.getMessage());
            e.printStackTrace();
            // Vẫn tạo campaign nhưng không có ảnh
            campaignService.create(campaign);
            return "redirect:/admin/campaigns";
        }
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


