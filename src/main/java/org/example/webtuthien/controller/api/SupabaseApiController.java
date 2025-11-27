package org.example.webtuthien.controller.api;

import org.example.webtuthien.config.SupabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/supabase")
public class SupabaseApiController {
    
    @Autowired
    private SupabaseConfig supabaseConfig;
    
    /**
     * Get Supabase configuration for frontend
     * Only exposes public anon key, never service role key
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("url", supabaseConfig.getUrl());
        config.put("anonKey", supabaseConfig.getAnonKey()); // Only public key
        config.put("storageBucket", supabaseConfig.getStorage().getBucket());
        
        return ResponseEntity.ok(config);
    }
    
    /**
     * Health check for Supabase connection
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate configuration
            boolean isConfigured = supabaseConfig.getUrl() != null 
                && !supabaseConfig.getUrl().isEmpty()
                && supabaseConfig.getAnonKey() != null
                && !supabaseConfig.getAnonKey().isEmpty();
            
            response.put("status", isConfigured ? "healthy" : "not_configured");
            response.put("supabaseUrl", supabaseConfig.getUrl());
            response.put("configLoaded", isConfigured);
            response.put("storageBucket", supabaseConfig.getStorage().getBucket());
            
            return isConfigured ? ResponseEntity.ok(response) 
                : ResponseEntity.status(503).body(response);
        } catch (Exception e) {
            response.put("status", "unhealthy");
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
