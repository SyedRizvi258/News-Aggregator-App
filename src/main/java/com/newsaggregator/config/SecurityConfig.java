package com.newsaggregator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;


/*
 * SecurityConfig.java
 * 
 * This class configures security settings for the application.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configure a password encoder bean for encoding and verifying passwords.
     * 
     * @return A PasswordEncoder object
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /**
     * Configure security settings for the application.
     * This method defines the security filter chain that determines which requests are secured.
     * 
     * @param http An HttpSecurity object to configure security settings
     * @return A SecurityFilterChain object with configured security settings
     * @throws Exception If an error occurs while configuring security settings
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors().configurationSource(corsConfigurationSource()) 
            .and()
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/verify-email", "/api/auth/logout", "/api/auth/change-password", "/api/auth/verify", "/api/news/top-headlines", "/api/news/search", "/api/news/articles", "/api/favorites/**", "/health").permitAll() // Allow unauthenticated access
                    .anyRequest().authenticated() // Secure all other routes
            )
            .csrf().disable() // Disable CSRF for APIs, if you're not using cookies or sessions
            .httpBasic().disable();
        return http.build();
    }


    /**
     * Configure CORS (Cross-Origin Resource Sharing) settings.
     * This allows the frontend to make requests to the backend from a different origin.
     * 
     * 
     * @return A CorsConfigurationSource object with allowed origins, methods, headers, and credentials.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("https://quickbyte-t50m.onrender.com", "http://localhost:3001"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
