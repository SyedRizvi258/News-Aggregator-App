package com.newsaggregator.dto;

public class ChangePasswordResponse {

    private String message;

    // Constructor
    public ChangePasswordResponse(String message) {
        this.message = message;
    }

    // Getter and Setter
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
