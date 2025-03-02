FROM maven:3-openjdk-21 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:21-slim
COPY --from=build /target/newsaggregator-0.0.1-SNAPSHOT.jar newsaggregator.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "newsaggregator.jar"]
