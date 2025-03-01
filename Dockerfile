# Use an official Java runtime as the base image
FROM openjdk:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the jar file into the container
COPY target/newsaggregator-0.0.1-SNAPSHOT.jar /app/newsaggregator.jar

# Expose the port the app runs on
EXPOSE 8082

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "newsaggregator.jar"]
