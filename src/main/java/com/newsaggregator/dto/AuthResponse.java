package com.newsaggregator.dto;

public class AuthResponse {
    
    private String token;

    // Constructor to initialize the token
    public AuthResponse(String token) {
        this.token = token;
    }

    // Getter for token
    public String getToken() {
        return token;
    }
}
