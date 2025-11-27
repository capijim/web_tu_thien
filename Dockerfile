FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy JAR file
COPY target/*.jar app.jar

# Create uploads directory
RUN mkdir -p /app/uploads && \
    chmod 755 /app/uploads

# Railway provides PORT environment variable
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=production

# Expose port (Railway will override this)
EXPOSE ${PORT}

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:${PORT}/health || exit 1

# Run application with Railway-compatible settings
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar /app/app.jar"]
