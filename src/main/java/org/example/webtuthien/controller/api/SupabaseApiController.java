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
        if (!supabaseConfig.isConfigured()) {
            return ResponseEntity.status(503).body(Map.of(
                "error", "Supabase not configured",
                "message", "Real-time features are disabled"
            ));
        }
        
        Map<String, String> config = new HashMap<>();
        config.put("url", supabaseConfig.getUrl());
        config.put("anonKey", supabaseConfig.getAnonKey());
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
            boolean isConfigured = supabaseConfig.isConfigured();
            
            response.put("status", isConfigured ? "healthy" : "not_configured");
            response.put("supabaseUrl", supabaseConfig.getUrl() != null ? supabaseConfig.getUrl() : "not set");
            response.put("configLoaded", isConfigured);
            response.put("storageBucket", supabaseConfig.getStorage().getBucket());
            response.put("message", isConfigured 
                ? "Supabase is configured and ready" 
                : "Supabase is not configured. Set SUPABASE_URL and SUPABASE_ANON_KEY to enable real-time features.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
