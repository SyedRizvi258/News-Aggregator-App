package com.newsaggregator.service;

import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.newsaggregator.model.User;
import com.newsaggregator.repository.UserRepository;


@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void validateUsername(String username) {
        if (username == null || username.length() < 3 || username.length() > 20) {
            throw new RuntimeException("Username must be between 3 and 20 characters.");
        }
        if (!username.matches("^[a-zA-Z0-9_.]*$")) {
            throw new RuntimeException("Username can only contain letters, numbers, underscores, and dots.");
        }
    }

    private static final String PASSWORD_REQUIREMENTS
        = "Password must be at least 8 characters long.\n" +
          "Contain at least:\n" + 
          "1) One digit\n" +
          "2) One lowercase letter\n" +
          "3) One uppercase letter\n" +
          "4) One special character (@#$%^&+=)";
    
    private static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$";

    public void validatePassword(String password) {
        if (password == null || !Pattern.matches(PASSWORD_REGEX, password)) {
            throw new RuntimeException(PASSWORD_REQUIREMENTS);
        }
    }

    public User registerUser(User user) {
        // Check if a user with the email or username already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("An account with this email exists already. Please login instead.");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username is already taken. Please choose a different one.");
        }

        // Validate the username
        validateUsername(user.getUsername());  // Validate username length and pattern

        // Validate the password
        validatePassword(user.getPassword());

        // Encode the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Generate verification token
        String token = UUID.randomUUID().toString();
        user.setEmailVerificationToken(token);

        // Save the user with the token
        userRepository.save(user);

        // Send verification email
        sendVerificationEmail(user);

        // Return the saved user
        return user;
    }
    
    @Value("${app.base-url}")
    private String baseUrl;

    private void sendVerificationEmail(User user) {
        String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + user.getEmailVerificationToken();
        
        // Create a simple email message
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("QuickByte Email Verification");
        message.setText("Hi " + user.getUsername() + ",\n\n" +
                        "Thank you for registering with QuickByte. To verify your email address, please click on the link below:\n\n" +
                        verificationUrl + "\n\n" +
                        "If you did not register for this account, please ignore this email.\n\n" +
                        "Best regards,\nThe QuickByte Team");

        mailSender.send(message);
    }

    private void sendPasswordChangedEmail(User user) {
        String passwordChangeUrl = baseUrl + "/api/auth/verify-email?token=" + user.getEmailVerificationToken();

        // Create a simple email message
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("QuickByte Password Update Verification");
        message.setText("Hi " + user.getUsername() + ",\n\n" +
                        "Your password has been updated successfully. To verify your password update, please click on the link below:\n\n" +
                        passwordChangeUrl + "\n\n" +
                        "If you did not make this change, please update your password.\n\n" +
                        "Best regards,\nThe QuickByte Team");
        
        mailSender.send(message);
    }

    public boolean verifyEmail(String token) {
        // Find the user by the verification token
        User user = userRepository.findByEmailVerificationToken(token);
        // If the user exists, verify the email
        if (user != null) {
            user.setEmailVerified(true);
            user.setEmailVerificationToken(null);  // Optional: clear the token after verification
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public User authenticateUser(String email, String password) {
        // Find the user by username
        User user = userRepository.findByEmail(email);
    
        if (user == null) {
            throw new RuntimeException("An account with this email does not exist");
        }
        
        // Check if the password matches
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // Check if the user's email is verified
        if (!user.isEmailVerified()) {
            throw new RuntimeException("Please verify your email before logging in.");
        }

        return user;
    }

    public void changePassword(String email, String newPassword) {
        // Find the user by email
        User user = userRepository.findByEmail(email);

        // Check if the user exists
        if (user == null) {
            throw new RuntimeException("An account with this email does not exist.");
        }

        // Validate the new password
        validatePassword(newPassword);

        // Encode the new password and update the user password
        String hashedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedNewPassword);

        // Generate a new email verification token
        String token = UUID.randomUUID().toString();
        user.setEmailVerificationToken(token);
        user.setEmailVerified(false); // Mark email as unverified after password change

        // Save the updated user
        userRepository.save(user);

        // Send email notification
        try {
            sendPasswordChangedEmail(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email notification: " + e.getMessage());
        }
    }
}
