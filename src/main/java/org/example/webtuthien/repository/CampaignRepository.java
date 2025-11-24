package org.example.webtuthien.repository;

import org.example.webtuthien.model.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByCategory(String category);
    List<Campaign> findByPartnerId(Long partnerId);
    List<Campaign> findByStatus(String status);
}
