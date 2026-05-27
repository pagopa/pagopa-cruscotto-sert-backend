# Step 1: Use Maven to build the project
FROM maven:3.9.5-amazoncorretto-21-al2023@sha256:2091cf3ebc5ca0ef4348fb89db39ef9c55ae4b71ceaa6243907e3fb337d13d46 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Use Amazon Corretto JDK to run the application
FROM amazoncorretto:21-alpine@sha256:937a7f5c5f7ec41315f1c7238fd9ec0347684d6d99e086db81201ca21d1f5778
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Expose the port your Spring Boot application runs on
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]