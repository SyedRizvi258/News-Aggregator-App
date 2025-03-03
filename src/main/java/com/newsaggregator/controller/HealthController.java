package com.newsaggregator.controller;

import org.springframework.http.ResponseEntity;
import com.newsaggregator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.Date;

@RestController
public class HealthController {
    
    @Autowired
    private UserRepository userRepository;
    
    // Use this endpoint to ping every 10 mins to keep Render service awake to avoid cold starts
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            // Check if we can access the user repository
            long userCount = userRepository.count(); // Simple DB operation
                
            // Return a more detailed response
            return ResponseEntity.ok().body(Map.of(
                "status", "OK",
                "database", "Connected",
                "timestamp", new Date().toString(),
                "users", userCount
            ));
        } catch (Exception e) {
            // Log the error but still return a 200 response to avoid false alarms
            return ResponseEntity.ok().body(Map.of(
                "status", "Partially available",
                "database", "Reconnecting",
                "timestamp", new Date().toString(),
                "error", e.getMessage()
            ));
        }
    }
}
