# Use an official Java runtime as the base image
FROM openjdk:21-jdk-alpine

# Check Java version inside the container (optional for debugging)
RUN java -version

# Set the working directory inside the container
WORKDIR /app

# Copy the jar file into the container
COPY target/newsaggregator-0.0.1-SNAPSHOT.jar /app/newsaggregator.jar

# Expose the port the app runs on
EXPOSE 8082

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "newsaggregator.jar"]
