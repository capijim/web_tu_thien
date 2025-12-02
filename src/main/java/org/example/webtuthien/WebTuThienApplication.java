package org.example.webtuthien;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "org.example.webtuthien") // Ensure all packages are scanned
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


