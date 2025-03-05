package com.newsaggregator.dto;


/*
 * ChangePasswordResponse.java
 * 
 * This class represents the response object for changing a user's password.
 * It contains a message field to indicate the status of the password change operation.
 */
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
