FROM eclipse-temurin:21-jre-alpine

# Create a non‑root user and group
RUN addgroup -S akibahub && adduser -S akibahub -G akibahub

WORKDIR /app
COPY target/*.jar app.jar

# Change ownership to the new user
RUN chown -R akibahub:akibahub /app

# Switch to the non‑root user
USER akibahub

ENTRYPOINT ["java", "-jar", "app.jar"]