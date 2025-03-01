package com.newsaggregator.controller;

import java.util.HashMap;
import java.util.Map;

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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3001", allowCredentials = "true")
public class AuthController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    public AuthController(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // Collect error messages from binding result and return them in a structured response
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
            response.put("message", "You have been registered successfully! Check your email for the verification link.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Return a structured JSON error response
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        // Verify the email using the token
        boolean isVerified = userService.verifyEmail(token);
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
            
            // Set the username as a separate cookie
            ResponseCookie usernameCookie = ResponseCookie.from("username", user.getUsername())
            .httpOnly(false) // Set to false so frontend can access it
            .secure(true) // Set to true in production (HTTPS)
            .path("/") // Available throughout the entire app
            .maxAge(24 * 60 * 60) // 1 day
            .sameSite("None")
            .build();
            
            LoginResponse loginResponse = new LoginResponse("Login successful", token, user.getUsername(), user.getId());

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.SET_COOKIE, jwtCookie.toString());
            headers.add(HttpHeaders.SET_COOKIE, usernameCookie.toString());

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

    @GetMapping("/verify")
    public ResponseEntity<?> verifyUser(HttpServletRequest request) {
        String token = jwtTokenUtil.getTokenFromCookies(request); // Extract token from cookies

        if (token != null && jwtTokenUtil.validateToken(token, jwtTokenUtil.getSubjectFromToken(token))) {
            String userId = jwtTokenUtil.getSubjectFromToken(token); // Get userId from token
            return ResponseEntity.ok().body(Map.of(
                "message", "User is authenticated",
                "userId", userId
            ));
        } else {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
    }

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

        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(request.getEmail(), request.getNewPassword());
            // Return structured response for success
            ChangePasswordResponse changePasswordResponse = new ChangePasswordResponse("Your password has been updated successfully! Check your email for the verification link.");
            return ResponseEntity.ok().body(changePasswordResponse);
        } catch (RuntimeException e) {
            // Return structured response for error
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // Add global exception handling for validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }
}
