# Use .dockerignore to avoid copying unnecessary files (e.g., target/, .git, etc.)
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
USER 1000
ENTRYPOINT ["java", "-jar", "app.jar"]
