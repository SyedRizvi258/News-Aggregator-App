package com.newsaggregator.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


/*
 * ChangePasswordRequest.java
 * 
 * This class represents the request object for changing a user's password.
 * It contains the email and new password fields.
 */
public class ChangePasswordRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "New password is required")
    private String newPassword;

    // Getters and Setters
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getNewPassword() {
        return newPassword;
    }
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
