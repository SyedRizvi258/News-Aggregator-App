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

@Component
public class JwtTokenUtil {
    
    @Value("${app.jwt-secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    private Key signingKey; // Key used to sign the JWT token

     // This method will be called after the bean is initialized and dependencies are injected
    @PostConstruct
    private void init() {
        if (secretKey == null) {
            throw new IllegalArgumentException("JWT secret key is not set");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes());
    }


    // Generate JWT token
    public String generateToken(String userId) {
        return Jwts.builder()
                   .setSubject(userId)
                   .setIssuedAt(new Date()) // Token issue date
                   .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // Token expiry date
                   .signWith(signingKey) // Sign with the secret key
                   .compact();
    }

    // Extract claims from JWT token
    public Claims getClaimsFromToken(String token) {
        try {
            JwtParser parser = Jwts.parser()
                                   .setSigningKey(signingKey) // Use the signing key
                                   .build();
            return parser.parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("Token has expired", e);
        } catch (JwtException e) {
            throw new IllegalArgumentException("Invalid token", e);
        }
    }

    // Validate JWT token
    public boolean validateToken(String token, String subject) {
        try {
            Claims claims = getClaimsFromToken(token);
            String tokenSubject = claims.getSubject();
            Date expiration = claims.getExpiration();

            return tokenSubject.equals(subject) && expiration.after(new Date());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // Extract username from JWT token
    public String getSubjectFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    // Extract token from cookies
    public String getTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        // Look for the "token" cookie
        return WebUtils.getCookie(request, "token") != null 
            ? WebUtils.getCookie(request, "token").getValue() 
            : null;
    }

}
