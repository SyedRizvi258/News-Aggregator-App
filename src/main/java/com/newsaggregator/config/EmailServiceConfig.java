package com.newsaggregator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;


/*
 * EmailServiceConfig.java
 * 
 * This configuration class sets up the JavaMailSender bean for sending emails.
 */
@Configuration
public class EmailServiceConfig {

    @Value("${SPRING_MAIL_USERNAME}")
    private String emailUsername;

    @Value("${SPRING_MAIL_PASSWORD}")
    private String emailPassword;

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl(); // Create a new instance of JavaMailSenderImpl
        mailSender.setHost("smtp.gmail.com"); // Set the host to Gmail SMTP server
        mailSender.setPort(587);

        mailSender.setUsername(emailUsername);
        mailSender.setPassword(emailPassword);

        // Set the properties for the mail sender
        java.util.Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); 
        props.put("mail.smtp.timeout", "5000");

        return mailSender;
    }
}
