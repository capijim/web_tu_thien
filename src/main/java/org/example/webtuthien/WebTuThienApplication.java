package org.example.webtuthien;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class WebTuThienApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebTuThienApplication.class, args);
    }
    
    @RestController
    public static class HealthController {
        @GetMapping("/health")
        public String health() {
            return "OK";
        }
    }
}


