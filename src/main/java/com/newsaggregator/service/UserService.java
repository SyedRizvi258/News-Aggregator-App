package com.newsaggregator.service;

import java.util.UUID;
import java.util.regex.Pattern;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.newsaggregator.model.User;
import com.newsaggregator.repository.UserRepository;


/*
 * UserService.java
 *
 * This service class handles core user-related operations.
    - Validates usernames and passwords according to defined rules.
    - Registers new users with email verification.
    - Authenticates users with password checks.
    - Handles password changes with re-verification.
    - Sends email notifications for account verification and password updates.
    - Verifies user emails through unique tokens.
 */
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository; // To interact with the database for user operations


    @Autowired
    private JavaMailSender mailSender; // To send verification and notification emails

    @Autowired
    private PasswordEncoder passwordEncoder; // To securely hash user passwords

    @Value("${app.base-url}")
    private String baseUrl;

    // Password requirements for user registration
    private static final String PASSWORD_REQUIREMENTS
        = "Password must be at least 8 characters long.\n" +
          "Contain at least:\n" + 
          "1) One digit\n" +
          "2) One lowercase letter\n" +
          "3) One uppercase letter\n" +
          "4) One special character (@#$%^&+=)";
    
    // Regular expression for password validation
    private static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$";


    /**
     * Validates the format and length of a username.
     * 
     * @param username the username to validate
     * @throws RuntimeException if the username is invalid
     */
    public void validateUsername(String username) {
        if (username == null || username.length() < 3 || username.length() > 20) {
            throw new RuntimeException("Username must be between 3 and 20 characters.");
        }
        if (!username.matches("^[a-zA-Z0-9_.]*$")) {
            throw new RuntimeException("Username can only contain letters, numbers, underscores, and dots.");
        }
    }

    /**
     * Validates the strength of a password based on defined rules.
     * 
     * @param password the password to validate
     * @throws RuntimeException if the password does not meet the requirements
     */
    public void validatePassword(String password) {
        if (password == null || !Pattern.matches(PASSWORD_REGEX, password)) {
            throw new RuntimeException(PASSWORD_REQUIREMENTS);
        }
    }


    /**
     * Registers a new user after validating their information,
     * hashes the password, generates an email verification token, 
     * and sends a verification email.
     * 
     * @param user the user to register
     * @return the registered user
     * @throws RuntimeException if the email or username already exists or if email sending fails
     */
    public User registerUser(User user) {
        // Check if an account with that email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("An account with this email exists already. Please login instead.");
        }
        // Check if the username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username is already taken. Please choose a different one.");
        }

        validateUsername(user.getUsername());

        validatePassword(user.getPassword());

        // Encode the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Generate email verification token
        String token = UUID.randomUUID().toString();
        user.setEmailVerificationToken(token);

        // Save the user with the token
        userRepository.save(user);

        // Send verification email
        try {
            sendVerificationEmail(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email notification: " + e.getMessage());
        }
        return user;
    }

    /**
     * Sends a verification email to the user with a verification link.
     * 
     * @param user the user to send the verification email to
     * @throws MessagingException if email sending fails
     */
    private void sendVerificationEmail(User user) throws MessagingException {
        String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + user.getEmailVerificationToken();
        
        //  Create a new MimeMessage instance
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true); // true = multipart message

        helper.setTo(user.getEmail());
        helper.setSubject("QuickByte Email Verification");

        String htmlContent = "<p>Hi " + user.getUsername() + ",</p>"
            + "<p>Thank you for registering with QuickByte. To verify your email address, please click the link below:</p>"
            + "<p><a href=\"" + verificationUrl + "\">Verify Email Address</a></p>"
            + "<p>If you did not register for this account, please ignore this email.</p>"
            + "<p>Best regards,<br>The QuickByte Team</p>";

        helper.setText(htmlContent, true); // true = HTML content

        mailSender.send(mimeMessage);
    }


    /**
     * Authenticates a user by verifying their email and password.
     * 
     * @param email the user's email
     * @param password the user's password
     * @return the authenticated user
     * @throws RuntimeException if the user does not exist, the password is invalid, or the email is not verified
     */
    public User authenticateUser(String email, String password) {
        // Find the user by email
        User user = userRepository.findByEmail(email);
    
        // Check if an account with that email exists
        if (user == null) {
            throw new RuntimeException("An account with this email does not exist");
        }
        
        // Check if the password is correct
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // Check if the user's email is verified
        if (!user.isEmailVerified()) {
            throw new RuntimeException("Please verify your email before logging in.");
        }

        return user;
    }


    /**
     * Changes the password for a user after validating the new password,
     * hashing it, generating a new email verification token, and sending a notification email.
     * 
     * @param email the user's email
     * @param newPassword the new password
     * @throws RuntimeException if the user does not exist, the new password is invalid, or email sending fails
     */
    public void changePassword(String email, String newPassword) {
        // Find the user by email
        User user = userRepository.findByEmail(email);

        // Check if an account with that email exists
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

    /**
     * Sends an email to the user confirming their password change.
     * 
     * @param user the user to send the email to
     * @throws MessagingException if email sending fails
     */
    private void sendPasswordChangedEmail(User user) throws MessagingException {
        String passwordChangeUrl = baseUrl + "/api/auth/verify-email?token=" + user.getEmailVerificationToken();

        // Create a new MimeMessage instance
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true); // true = multipart message

        helper.setTo(user.getEmail());
        helper.setSubject("QuickByte Password Update Verification");

        String htmlContent = "<p>Hi " + user.getUsername() + ",</p>"
            + "<p>Your password has been updated successfully. To verify your password update, please click the link below:</p>"
            + "<p><a href=\"" + passwordChangeUrl + "\">Verify Password Update</a></p>"
            + "<p>If you did not make this change, please update your password.</p>"
            + "<p>Best regards,<br>The QuickByte Team</p>";

        helper.setText(htmlContent, true); // true = HTML content

        mailSender.send(mimeMessage);
    }


    /**
     * Verifies a user's email by checking the verification token.
     * 
     * @param token the verification token
     * @return true if the email is verified, false otherwise
     */
    public boolean verifyEmail(String token) {
        // Find the user by the verification token
        User user = userRepository.findByEmailVerificationToken(token);
        // If the user exists, verify the email
        if (user != null) {
            user.setEmailVerified(true);
            user.setEmailVerificationToken(null);  // clear the token after verification
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
