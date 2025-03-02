FROM eclipse-temurin:21-jdk AS build
COPY . .
RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
COPY --from=build /target/newsaggregator-0.0.1-SNAPSHOT.jar newsaggregator.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "newsaggregator.jar"]
