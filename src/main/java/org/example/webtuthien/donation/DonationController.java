package org.example.webtuthien.donation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<Donation> create(@RequestBody Donation donation) {
        Donation created = service.create(donation);
        return ResponseEntity.ok(created);
    }
}


