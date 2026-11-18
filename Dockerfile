# Step 1: Use Maven to build the project
FROM maven:3.10.0-amazoncorretto-21-al2023@sha256:c287cc32273f94ac4ab07ba68c92809374db696633307ae06887d902f4bfb93e AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Use Amazon Corretto JDK to run the application
FROM amazoncorretto:21-alpine@sha256:600e99e39da14b6a909027a924136c423e9334cb7347a8a77d5908bdaa3011b7
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Expose the port your Spring Boot application runs on
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
