package org.example.webtuthien.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.webtuthien.model.Campaign;
import org.example.webtuthien.model.Donation;
import org.example.webtuthien.service.CampaignService;
import org.example.webtuthien.service.DonationService;
import org.example.webtuthien.service.UserService;
import org.example.webtuthien.service.FileStorageService;

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
    
    @Autowired
    private FileStorageService fileStorageService;

    public CampaignController(CampaignService service) {
        this.service = service;
    }

    @GetMapping
    public List<Map<String, Object>> list(HttpServletRequest request) {
        List<Campaign> campaigns = service.list();
        return campaigns.stream().map(c -> enrichCampaignWithFullImageUrl(c, request)).toList();
    }

    @GetMapping("/with-stats")
    public ResponseEntity<List<Map<String, Object>>> listWithStats(HttpServletRequest request) {
        try {
            List<Map<String, Object>> campaignsWithStats = service.listWithStats();
            // Enrich with full image URLs
            campaignsWithStats.forEach(c -> {
                String imageUrl = (String) c.get("imageUrl");
                if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
                    c.put("imageUrl", getBaseUrl(request) + imageUrl);
                }
            });
            return ResponseEntity.ok(campaignsWithStats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/active")
    public List<Map<String, Object>> listActive(HttpServletRequest request) {
        List<Campaign> campaigns = service.listActive();
        return campaigns.stream().map(c -> enrichCampaignWithFullImageUrl(c, request)).toList();
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = fileStorageService.storeFile(file);
            Map<String, String> result = new HashMap<>();
            result.put("url", imageUrl);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
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
    public ResponseEntity<?> getById(@PathVariable Long id, HttpServletRequest request) {
        try {
            var campaignOpt = service.findById(id);
            if (campaignOpt.isPresent()) {
                return ResponseEntity.ok(enrichCampaignWithFullImageUrl(campaignOpt.get(), request));
            }
            return ResponseEntity.status(404).body(Map.of("error", "Không tìm thấy chiến dịch"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi lấy thông tin khuyến góp"));
        }
    }

    @GetMapping("/{id}/with-stats")
    public ResponseEntity<?> getByIdWithStats(@PathVariable Long id, HttpServletRequest request) {
        try {
            Map<String, Object> campaign = service.findByIdWithStats(id);
            String imageUrl = (String) campaign.get("imageUrl");
            if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
                campaign.put("imageUrl", getBaseUrl(request) + imageUrl);
            }
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
            
            Long partnerId = (Long) userIdObj;
            campaign.setPartnerId(partnerId);
            
            // Validate image URL if provided
            if (campaign.getImageUrl() != null && !campaign.getImageUrl().isBlank()) {
                if (!campaign.getImageUrl().startsWith("/uploads/") && 
                    !campaign.getImageUrl().startsWith("http://") && 
                    !campaign.getImageUrl().startsWith("https://")) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "URL ảnh không hợp lệ");
                    return ResponseEntity.status(400).body(error);
                }
            }
            
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
                error.put("error", "Bạn cần đăng nhập để quyên góp");
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
    
    private Map<String, Object> enrichCampaignWithFullImageUrl(Campaign campaign, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", campaign.getId());
        result.put("partnerId", campaign.getPartnerId());
        result.put("title", campaign.getTitle());
        result.put("description", campaign.getDescription());
        result.put("targetAmount", campaign.getTargetAmount());
        result.put("currentAmount", campaign.getCurrentAmount());
        result.put("category", campaign.getCategory());
        result.put("status", campaign.getStatus());
        result.put("endDate", campaign.getEndDate());
        result.put("createdAt", campaign.getCreatedAt());
        
        String imageUrl = campaign.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                imageUrl = getBaseUrl(request) + imageUrl;
            }
        }
        result.put("imageUrl", imageUrl);
        
        return result;
    }
    
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        
        String baseUrl = scheme + "://" + serverName;
        if ((scheme.equals("http") && serverPort != 80) || 
            (scheme.equals("https") && serverPort != 443)) {
            baseUrl += ":" + serverPort;
        }
        
        return baseUrl;
    }
}
