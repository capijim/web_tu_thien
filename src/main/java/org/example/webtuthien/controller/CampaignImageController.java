package org.example.webtuthien.controller;

import org.example.webtuthien.service.CampaignImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignImageController {
    
    @Autowired
    private CampaignImageService imageService;
    
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getCampaignImage(@PathVariable Long id) {
        try {
            byte[] imageData = imageService.getImageData(id);
            String mimeType = imageService.getImageMimeType(id);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(mimeType));
            headers.setCacheControl("max-age=31536000"); // Cache 1 year
            
            return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
