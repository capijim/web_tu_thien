package org.example.webtuthien.service;

import org.example.webtuthien.entity.Campaign;
import org.example.webtuthien.repository.CampaignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Service
public class CampaignImageService {
    private static final Logger logger = LoggerFactory.getLogger(CampaignImageService.class);
    
    @Autowired
    private CampaignRepository campaignRepository;
    
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/png", "image/webp"};
    
    @Transactional
    public void saveImageToDatabase(Long campaignId, MultipartFile file) throws IOException {
        // Validate
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File rỗng");
        }
        
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("File quá lớn. Tối đa 5MB");
        }
        
        String contentType = file.getContentType();
        if (!isAllowedType(contentType)) {
            throw new IllegalArgumentException("Định dạng không hợp lệ. Chỉ chấp nhận: JPEG, PNG, WEBP");
        }
        
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chiến dịch"));
        
        // Save binary data
        campaign.setImageData(file.getBytes());
        campaign.setImageMimeType(contentType);
        campaign.setImageSize(file.getSize());
        campaign.setImageUrl(null); // Clear URL if using binary
        
        campaignRepository.save(campaign);
        logger.info("Đã lưu ảnh vào database cho campaign ID: {} ({}KB)", 
                    campaignId, file.getSize() / 1024);
    }
    
    public byte[] getImageData(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chiến dịch"));
        
        if (campaign.getImageData() == null) {
            throw new IllegalArgumentException("Chiến dịch không có ảnh");
        }
        
        return campaign.getImageData();
    }
    
    public String getImageMimeType(Long campaignId) {
        return campaignRepository.findById(campaignId)
            .map(Campaign::getImageMimeType)
            .orElse("image/jpeg");
    }
    
    @Transactional
    public void deleteImage(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chiến dịch"));
        
        campaign.setImageData(null);
        campaign.setImageMimeType(null);
        campaign.setImageSize(null);
        
        campaignRepository.save(campaign);
        logger.info("Đã xóa ảnh của campaign ID: {}", campaignId);
    }
    
    private boolean isAllowedType(String contentType) {
        if (contentType == null) return false;
        for (String type : ALLOWED_TYPES) {
            if (type.equals(contentType)) return true;
        }
        return false;
    }
}
