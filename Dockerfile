# PLC Recipe Management System - Docker Setup

FROM openjdk:17-slim

LABEL maintainer="PLC Recipe Management Team"
LABEL description="PLC Batching Recipe Management System with Angular UI"

WORKDIR /app

# Copy the built JAR
COPY target/recipe-management-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=10s --timeout=5s --retries=5 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# Optional: Set JVM options for production
# ENV JAVA_OPTS="-Xmx1g -Xms512m -XX:+UseG1GC"
