package org.example.webtuthien.controller;

import org.example.webtuthien.model.Campaign;
import org.example.webtuthien.model.Donation;
import org.example.webtuthien.model.Partner;
import org.example.webtuthien.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import org.example.webtuthien.service.AdminService;
import org.example.webtuthien.service.CampaignService;
import org.example.webtuthien.service.DonationService;
import org.example.webtuthien.service.PartnerService;
import org.example.webtuthien.service.UserService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(originPatterns = "*")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private DonationService donationService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private PartnerService partnerService;

    // Dashboard statistics
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = adminService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // User management
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Campaign management
    @GetMapping("/campaigns")
    public ResponseEntity<List<Campaign>> getAllCampaigns() {
        try {
            List<Campaign> campaigns = campaignService.list();
            return ResponseEntity.ok(campaigns);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/campaigns")
    public ResponseEntity<?> createCampaign(@RequestBody Campaign campaign) {
        try {
            // Cho phép admin chỉ định partnerId từ payload
            if (campaign.getPartnerId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "partnerId là bắt buộc"));
            }
            Campaign created = campaignService.create(campaign);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi tạo campaign"));
        }
    }

    @PutMapping("/campaigns/{id}/status")
    public ResponseEntity<Void> updateCampaignStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            adminService.updateCampaignStatus(id, status);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/campaigns/{id}")
    public ResponseEntity<Void> deleteCampaign(@PathVariable Long id) {
        try {
            campaignService.deleteCampaign(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Donation management
    @GetMapping("/donations")
    public ResponseEntity<List<Donation>> getAllDonations() {
        try {
            List<Donation> donations = donationService.list();
            return ResponseEntity.ok(donations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/donations/{id}")
    public ResponseEntity<Void> deleteDonation(@PathVariable Long id) {
        try {
            donationService.deleteDonation(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Partners management
    @GetMapping("/partners")
    public ResponseEntity<List<Partner>> getAllPartners() {
        try {
            List<Partner> partners = partnerService.list();
            return ResponseEntity.ok(partners);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/partners")
    public ResponseEntity<?> createPartner(@RequestBody Partner partner) {
        try {
            if (partner.getName() == null || partner.getName().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tên đối tác là bắt buộc"));
            }
            Partner created = partnerService.create(partner);
            return ResponseEntity.ok(created);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email đã tồn tại hoặc dữ liệu không hợp lệ"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi tạo đối tác"));
        }
    }

    @DeleteMapping("/partners/{id}")
    public ResponseEntity<?> deletePartner(@PathVariable Long id) {
        try {
            partnerService.delete(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không thể xóa đối tác"));
        }
    }

}
