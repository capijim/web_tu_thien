package org.example.webtuthien.campaign;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {
    private final CampaignService service;

    public CampaignController(CampaignService service) {
        this.service = service;
    }

    @GetMapping
    public List<Campaign> list() {
        return service.list();
    }

    @GetMapping("/active")
    public List<Campaign> listActive() {
        return service.listActive();
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
            
            Long userId = (Long) userIdObj;
            List<Campaign> campaigns = service.findByUserId(userId);
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
            return service.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
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
            
            Long userId = (Long) userIdObj;
            campaign.setUserId(userId);
            
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
