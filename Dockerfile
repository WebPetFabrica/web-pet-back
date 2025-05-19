FROM maven:3.9-amazoncorretto-21 as build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM oraclelinux
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
