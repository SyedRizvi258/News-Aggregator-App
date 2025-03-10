package com.newsaggregator.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newsaggregator.dto.ChangePasswordRequest;
import com.newsaggregator.dto.ChangePasswordResponse;
import com.newsaggregator.dto.LoginRequest;
import com.newsaggregator.dto.LoginResponse;
import com.newsaggregator.model.User;
import com.newsaggregator.security.JwtTokenUtil;
import com.newsaggregator.service.UserService;
import com.newsaggregator.repository.UserRepository;


/*
 * AuthController.java
 * 
 * This controller class defines the REST API endpoints for user authentication and authorization.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3001", "https://quickbyte-t50m.onrender.com"}, allowCredentials = "true") // Allow cross-origin requests from the specified URLs
public class AuthController {
    
    @Autowired
    private UserService userService; // Service class for user operations

    @Autowired
    private UserRepository userRepository; // To interact with the database for user operations

    @Autowired
    private JwtTokenUtil jwtTokenUtil; // Utility class for JWT token operations

    public AuthController(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }
    

    /**
     * Register a new user.
     * 
     * @param user The user object containing the registration details
     * @param bindingResult The validation result of the user object
     * @return A response entity with a success message or error message
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // Collect error messages and return them in a structured response
            Map<String, String> errorResponse = new HashMap<>();
            
            for (FieldError error : bindingResult.getFieldErrors()) {
                errorResponse.put(error.getField(), error.getDefaultMessage());
            }
            
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            // Register the user
            userService.registerUser(user);
            
            // Return success message as JSON
            Map<String, String> response = new HashMap<>();
            response.put("message", "You have been registered successfully! A verification link has been sent to your email. Be sure to check your inbox and spam folder.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Return a structured JSON error response
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }


    /**
     * Verify the user's email using the verification token.
     * 
     * @param token The verification token sent to the user's email
     * @return A response entity with a success message or error message
     */
    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        // Verify the email using the token
        boolean isVerified = userService.verifyEmail(token);

        // Return a success message or error message in HTML format
        if (isVerified) {
            return ResponseEntity.ok(
            "<html>" +
            "<head><title>Email Verification</title></head>" +
            "<body style='text-align: center; margin-top: 50px; font-family: Arial, sans-serif;'>" +
            "<h1 style='color: green;'>Email Verified Successfully!</h1>" +
            "<p>You can now return to the application and login.</p>" +
            "</body>" +
            "</html>"
            );
        } else {
            return ResponseEntity.badRequest().body(
            "<html>" +
            "<head><title>Email Verification</title></head>" +
            "<body style='text-align: center; margin-top: 50px; font-family: Arial, sans-serif;'>" +
            "<h1 style='color: red;'>Invalid or Expired Token</h1>" +
            "</body>" +
            "</html>"
            );
        }
    }


    /**
     * Login a user and generate a JWT token.
     * 
     * @param loginRequest The login request object containing the user's email and password
     * @return A response entity with a success message or error message
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            // Validate the user credentials
            User user = userService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
            
            // Generate a JWT token
            String token = jwtTokenUtil.generateToken(user.getId());

            // Set the JWT as an HttpOnly cookie
            ResponseCookie jwtCookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(true) // Ensure this is true in production (HTTPS)
                .path("/")
                .maxAge(24 * 60 * 60) // 1 day
                .sameSite("None")
                .build();
            
            // Return a structured response with the JWT token and user details
            LoginResponse loginResponse = new LoginResponse("Login successful", token, user.getUsername(), user.getId());

            // Set the JWT cookie in the response header
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.SET_COOKIE, jwtCookie.toString());

            // Return a response with a header containing the JWT cookie and the response body
            return ResponseEntity.ok()
                .headers(headers)
                .body(loginResponse);
        } catch (RuntimeException e) {
            // Return an error response in JSON format
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }


    /**
     * Verify if a user is authenticated using the JWT token.
     * 
     * @param request The HTTP request object
     * @return A response entity with a success message or error message
     */
    @GetMapping("/verify")
    public ResponseEntity<?> verifyUser(HttpServletRequest request) {
        String token = jwtTokenUtil.getTokenFromCookies(request); // Extract token from cookies

        // Check if the token is valid and the user exists
        if (token != null && jwtTokenUtil.validateToken(token, jwtTokenUtil.getSubjectFromToken(token))) {
            String userId = jwtTokenUtil.getSubjectFromToken(token); // Get userId from token

            // Fetch username from db
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }
            String username = userOptional.get().getUsername();
            
            // Return a structured response with user details
            return ResponseEntity.ok().body(Map.of(
                "message", "User is authenticated",
                "userId", userId,
                "username", username
            ));
        } else {
            // Return an error response if the user is not authenticated
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
    }


    /**
     * Logout a user by expiring the JWT token.
     * 
     * @param response The HTTP response object
     * @return A response entity with a success message
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        // Create an expired cookie to remove the existing token
        ResponseCookie expiredCookie = ResponseCookie.from("token", "")
            .httpOnly(true)
            .secure(true) // Set to true in production with HTTPS
            .path("/")
            .maxAge(0) // Expire immediately
            .sameSite("None")
            .build();

        // Set the expired cookie in the response header
        response.addHeader("Set-Cookie", expiredCookie.toString());

        // Return a success message
        return ResponseEntity.ok("Logged out successfully");
    }


    /**
     * Change the user's password.
     * 
     * @param request The change password request object containing the user's email and new password
     * @return A response entity with a success message or error message
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(request.getEmail(), request.getNewPassword());
            // Return structured response for success
            ChangePasswordResponse changePasswordResponse = new ChangePasswordResponse("Your password has been updated successfully! A verification link has been sent to your email. Be sure to check your inbox and spam folder.");
            return ResponseEntity.ok().body(changePasswordResponse);
        } catch (RuntimeException e) {
            // Return structured response for error
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }


    /**
     * Handle validation exceptions for request body.
     * 
     * @param ex The exception object
     * @return A response entity with a map of field errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }
}
