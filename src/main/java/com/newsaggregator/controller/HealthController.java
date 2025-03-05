package com.newsaggregator.controller;

import org.springframework.http.ResponseEntity;
import com.newsaggregator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.Date;


/*
 * HealthController.java
 * 
 * This controller class provides an endpoint to check the health status of the application.
 */
@RestController
public class HealthController {
    
    @Autowired
    private UserRepository userRepository; // To interact with the database for user operations
    
    // Use this endpoint to ping every 10 mins to keep deployed Render service awake to avoid cold starts
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            // Check if we can access the user repository
            long userCount = userRepository.count(); // Simple DB operation (user count)
                
            // If we can access the user repository, return a success response
            return ResponseEntity.ok().body(Map.of(
                "status", "OK",
                "database", "Connected",
                "timestamp", new Date().toString(),
                "users", userCount
            ));
        } catch (Exception e) {
            // Return an error response
            return ResponseEntity.ok().body(Map.of(
                "status", "Partially available",
                "database", "Reconnecting",
                "timestamp", new Date().toString(),
                "error", e.getMessage()
            ));
        }
    }
}
