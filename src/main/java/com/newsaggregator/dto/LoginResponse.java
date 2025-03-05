package com.newsaggregator.dto;


/*
 * LoginResponse.java
 * 
 * This class represents the response object for user login.
 * It contains the message, token, username, and userId fields.
 */
public class LoginResponse {

    private String message;
    private String token;
    private String username;
    private String userId;

    // Constructor
    public LoginResponse(String message, String token, String username, String userId) {
        this.message = message;
        this.token = token;
        this.username = username;
        this.userId = userId;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }
}
