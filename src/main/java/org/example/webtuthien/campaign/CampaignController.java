package org.example.webtuthien.campaign;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

import org.example.webtuthien.donation.Donation;
import org.example.webtuthien.donation.DonationService;
import org.example.webtuthien.user.UserService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {
    private final CampaignService service;
    
    @Autowired
    private DonationService donationService;
    
    @Autowired
    private UserService userService;

    public CampaignController(CampaignService service) {
        this.service = service;
    }

    @GetMapping
    public List<Campaign> list() {
        return service.list();
    }

    @GetMapping("/with-stats")
    public ResponseEntity<List<Map<String, Object>>> listWithStats() {
        try {
            List<Map<String, Object>> campaignsWithStats = service.listWithStats();
            return ResponseEntity.ok(campaignsWithStats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/active")
    public List<Campaign> listActive() {
        return service.listActive();
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "File trống");
                return ResponseEntity.badRequest().body(error);
            }

            // Create uploads directory if not exists
            Path uploadDir = Paths.get("uploads").toAbsolutePath();
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
            }
            String storedName = UUID.randomUUID().toString().replace("-", "") + ext;
            Path destination = uploadDir.resolve(storedName);

            Files.copy(file.getInputStream(), destination);

            Map<String, String> result = new HashMap<>();
            result.put("url", "/uploads/" + storedName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi upload: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/categories")
    public List<String> getCategories() {
        return service.getCategories();
    }

    @GetMapping("/category/{category}")
    public List<Campaign> listByCategory(@PathVariable String category) {
        return service.findByCategory(category);
    }

    @GetMapping("/my")
    public ResponseEntity<?> listMyCampaigns(HttpSession session) {
        try {
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Chưa đăng nhập");
                return ResponseEntity.status(401).body(error);
            }
            
            Long partnerId = (Long) userIdObj; // Tạm dùng session userId như partnerId nếu chưa có module partners
            List<Campaign> campaigns = service.findByPartnerId(partnerId);
            return ResponseEntity.ok(campaigns);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi lấy danh sách khuyến góp: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            var campaignOpt = service.findById(id);
            if (campaignOpt.isPresent()) {
                return ResponseEntity.ok(campaignOpt.get());
            }
            return ResponseEntity.status(404).body(Map.of("error", "Không tìm thấy chiến dịch"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi lấy thông tin khuyến góp"));
        }
    }

    @GetMapping("/{id}/with-stats")
    public ResponseEntity<?> getByIdWithStats(@PathVariable Long id) {
        try {
            Map<String, Object> campaign = service.findByIdWithStats(id);
            return ResponseEntity.ok(campaign);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(404).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi lấy thông tin khuyến góp: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Campaign campaign, HttpSession session) {
        try {
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Chưa đăng nhập");
                return ResponseEntity.status(401).body(error);
            }
            
            Long partnerId = (Long) userIdObj; // tạm map userId -> partnerId
            campaign.setPartnerId(partnerId);
            
            Campaign created = service.create(campaign);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(400).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi tạo khuyến góp: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/{id}/donate")
    public ResponseEntity<?> donate(@PathVariable Long id, @RequestBody Map<String, Object> request, HttpSession session) {
        try {
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Chưa đăng nhập");
                return ResponseEntity.status(401).body(error);
            }

            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Số tiền phải lớn hơn 0");
                return ResponseEntity.status(400).body(error);
            }

            // Get user info for donation record
            Long userId = Long.valueOf(userIdObj.toString());
            var userOpt = userService.getUserById(userId);
            if (userOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Không tìm thấy thông tin người dùng");
                return ResponseEntity.status(400).body(error);
            }

            // Create donation record
            Donation donation = new Donation();
            donation.setCampaignId(id);
            donation.setDonorName(userOpt.get().getName());
            donation.setAmount(amount);
            donation.setMessage(request.get("message") != null ? request.get("message").toString() : null);
            
            donationService.create(donation);

            // Update campaign amount
            Campaign updatedCampaign = service.updateCurrentAmount(id, amount);
            return ResponseEntity.ok(updatedCampaign);
        } catch (NumberFormatException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Số tiền không hợp lệ");
            return ResponseEntity.status(400).body(error);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(404).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi ủng hộ: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> request, HttpSession session) {
        try {
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Chưa đăng nhập");
                return ResponseEntity.status(401).body(error);
            }

            String status = request.get("status");
            if (status == null || status.isBlank()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Status là bắt buộc");
                return ResponseEntity.status(400).body(error);
            }

            Campaign updatedCampaign = service.updateStatus(id, status);
            return ResponseEntity.ok(updatedCampaign);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(400).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi cập nhật trạng thái: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
