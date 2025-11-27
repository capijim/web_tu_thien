# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /build

# Copy Maven files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build application
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Create uploads directory
RUN mkdir -p /app/uploads && \
    chmod 755 /app/uploads

# Railway provides PORT environment variable
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=production

# Expose port
EXPOSE ${PORT}

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:${PORT}/health || exit 1

# Run application
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar /app/app.jar"]
