package org.example.webtuthien.controller;

import org.example.webtuthien.model.Donation;
import org.example.webtuthien.service.DonationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/donations")
public class DonationController {
    private final DonationService service;

    public DonationController(DonationService service) {
        this.service = service;
    }

    @GetMapping
    public List<Donation> list() {
        return service.list();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Donation donation, HttpSession session) {
        try {
            // Kiểm tra đăng nhập
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Bạn cần đăng nhập để quyên góp");
                return ResponseEntity.status(401).body(error);
            }

            Donation created = service.create(donation);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi tạo donation: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}


