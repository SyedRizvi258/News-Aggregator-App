package com.newsaggregator.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;


/*
 * JwtTokenUtil.java
 * 
 * This utility class provides methods to generate, validate, and extract claims from JWT tokens.
 */
@Component
public class JwtTokenUtil {
    
    @Value("${app.jwt-secret}")
    private String secretKey; // Secret key used to sign the JWT token

    @Value("${jwt.expiration}")
    private long jwtExpiration; // Expiration time of the JWT token in milliseconds
    
    private Key signingKey; // Key used for signing the JWT token

    
    // This method will be called after the bean is initialized and dependencies are injected
    @PostConstruct
    private void init() {
        if (secretKey == null) {
            throw new IllegalArgumentException("JWT secret key is not set");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes());
    }


    /**
     * Generate a JWT token for a user.
     * 
     * @param userId The ID of the user
     * @return A JWT token
     */
    public String generateToken(String userId) {
        return Jwts.builder()
                   .setSubject(userId) // Token subject
                   .setIssuedAt(new Date()) // Token issue date
                   .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // Token expiry date
                   .signWith(signingKey) // Sign with the secret key
                   .compact();
    }

    
    /**
     * Extract claims from a JWT token.
     * 
     * @param token The JWT token
     * @return The claims extracted from the token
     */
    public Claims getClaimsFromToken(String token) {
        try {
            JwtParser parser = Jwts.parser()
                                   .setSigningKey(signingKey)
                                   .build();
            return parser.parseClaimsJws(token).getBody(); // Parse the token and extract the claims
        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("Token has expired", e);
        } catch (JwtException e) {
            throw new IllegalArgumentException("Invalid token", e);
        }
    }

    
    /**
     * Validate a JWT token.
     * 
     * @param token The JWT token
     * @param subject The subject to validate against
     * @return True if the token is valid, false otherwise
     */
    public boolean validateToken(String token, String subject) {
        try {
            Claims claims = getClaimsFromToken(token);
            String tokenSubject = claims.getSubject();
            Date expiration = claims.getExpiration();

            return tokenSubject.equals(subject) && expiration.after(new Date()); // Check subject and expiry
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    
    /**
     * Extract the subject from a JWT token.
     * 
     * @param token The JWT token
     * @return The subject extracted from the token
     */
    public String getSubjectFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    
    /**
     * Extract the JWT token from the cookies in an HTTP request.
     * 
     * @param request The HTTP request
     * @return The JWT token extracted from the cookies
     */
    public String getTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        // Get the token cookie from the request
        return WebUtils.getCookie(request, "token") != null 
            ? WebUtils.getCookie(request, "token").getValue() 
            : null;
    }
}
