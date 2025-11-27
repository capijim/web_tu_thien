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
        Map<String, String> response = new HashMap<>();
        
        if (!supabaseConfig.isConfigured()) {
            response.put("error", "true");
            response.put("message", "Supabase not configured. Set SUPABASE_URL and SUPABASE_ANON_KEY environment variables.");
            return ResponseEntity.status(503).body(response);
        }
        
        response.put("url", supabaseConfig.getUrl());
        response.put("anonKey", supabaseConfig.getAnonKey());
        response.put("storageBucket", supabaseConfig.getStorage().getBucket());
        
        return ResponseEntity.ok(response);
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
            response.put("configured", isConfigured);
            
            if (isConfigured) {
                response.put("supabaseUrl", maskUrl(supabaseConfig.getUrl()));
                response.put("storageBucket", supabaseConfig.getStorage().getBucket());
                response.put("message", "Supabase is configured and ready");
            } else {
                response.put("supabaseUrl", "not set");
                response.put("message", "Set SUPABASE_URL and SUPABASE_ANON_KEY to enable features");
                response.put("instructions", Map.of(
                    "windows", "set SUPABASE_URL=https://xxx.supabase.co && set SUPABASE_ANON_KEY=xxx",
                    "linux", "export SUPABASE_URL=https://xxx.supabase.co && export SUPABASE_ANON_KEY=xxx"
                ));
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    private String maskUrl(String url) {
        if (url == null || url.isEmpty()) return "not set";
        try {
            return url.replaceAll("//([^.]+)", "//*****");
        } catch (Exception e) {
            return "***";
        }
    }
}
