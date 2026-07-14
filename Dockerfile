# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-alpine AS builder

# Set the working directory
WORKDIR /app

# Copy gradle wrapper and related files
COPY gradlew .
COPY gradle gradle

# Make gradlew executable
RUN chmod +x gradlew

# Copy build.gradle and settings.gradle
COPY build.gradle settings.gradle ./

# Copy the application source code
COPY src src

# Build the application (skip tests for faster deployment)
RUN ./gradlew build -x test

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the compiled JAR file from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the port (Render sets the PORT environment variable)
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
