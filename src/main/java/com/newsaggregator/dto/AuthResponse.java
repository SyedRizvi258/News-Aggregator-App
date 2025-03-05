package com.newsaggregator.dto;


/*
 * AuthResponse.java
 * 
 * This class represents the response object containing the JWT token.
 * It is used to send the token back to the client after successful authentication.
 */
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
