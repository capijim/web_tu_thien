package org.example.webtuthien.service;

import org.example.webtuthien.model.Donation;
import org.example.webtuthien.repository.DonationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DonationService {
    private final DonationRepository repository;

    public DonationService(DonationRepository repository) {
        this.repository = repository;
    }

    public List<Donation> list() {
        return repository.findAll();
    }

    public Donation create(Donation donation) {
        if (donation.getCampaignId() == null) {
            throw new IllegalArgumentException("campaignId is required");
        }
        if (donation.getDonorName() == null || donation.getDonorName().isBlank()) {
            throw new IllegalArgumentException("donorName is required");
        }
        if (donation.getAmount() == null) {
            throw new IllegalArgumentException("amount is required");
        }
        return repository.insert(donation);
    }

    public void deleteDonation(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Donation ID is required");
        }
        repository.deleteById(id);
    }
}


