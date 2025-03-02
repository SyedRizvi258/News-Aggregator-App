package com.newsaggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableScheduling
public class CloudBasedNewsAggregatorApplication {

	// Load environment variables from .env file
	static Dotenv dotenv = Dotenv.configure()
                      .ignoreIfMissing()
                      .load();

	static {
		// Set the environment variables for Spring Boot to use
		System.setProperty("SPRING_DATA_MONGODB_URI", dotenv.get("SPRING_DATA_MONGODB_URI"));
		System.setProperty("SERVER_PORT", dotenv.get("SERVER_PORT"));
		System.setProperty("SPRING_MAIL_HOST", dotenv.get("SPRING_MAIL_HOST"));
        System.setProperty("SPRING_MAIL_PORT", dotenv.get("SPRING_MAIL_PORT"));
        System.setProperty("SPRING_MAIL_USERNAME", dotenv.get("SPRING_MAIL_USERNAME"));
        System.setProperty("SPRING_MAIL_PASSWORD", dotenv.get("SPRING_MAIL_PASSWORD"));
        System.setProperty("SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH", dotenv.get("SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH"));
        System.setProperty("SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE", dotenv.get("SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE"));
		System.setProperty("APP_BASE_URL", dotenv.get("APP_BASE_URL"));
		System.setProperty("SECRET_KEY", dotenv.get("SECRET_KEY"));
		System.setProperty("NEWSAPI_KEY", dotenv.get("NEWSAPI_KEY"));
	}

	public static void main(String[] args) {
		SpringApplication.run(CloudBasedNewsAggregatorApplication.class, args);
	}

}
