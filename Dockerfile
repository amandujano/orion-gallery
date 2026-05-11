# Multi-stage build for Spring Boot + Angular application
# Builds both frontend and backend, serving static files from Spring Boot

# Stage 1: Build the Spring Boot application with Angular frontend
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and frontend
COPY src ./src
COPY frontend ./frontend

# Build the application including frontend
# The frontend-maven-plugin will build Angular and copy dist to static folder
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre

# Install curl for health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port
# Render uses the PORT environment variable, default to 8080 for local development
EXPOSE 8080

# Set environment variables
# BLOB_READ_WRITE_TOKEN can be overridden at runtime
ENV BLOB_READ_WRITE_TOKEN=""
# PORT for Render deployment (Spring Boot will use SERVER_PORT if set)
ENV PORT=8080
ENV SERVER_PORT=${PORT}

# Health check for Render deployment
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:${PORT}/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
