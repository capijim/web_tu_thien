package org.example.webtuthien.service;

import org.example.webtuthien.model.Partner;
import org.example.webtuthien.repository.PartnerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartnerService {
    private final PartnerRepository repository;

    public PartnerService(PartnerRepository repository) {
        this.repository = repository;
    }

    public List<Partner> list() {
        return repository.findAll();
    }

    public Partner create(Partner partner) {
        if (partner.getName() == null || partner.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        return repository.insert(partner);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}


