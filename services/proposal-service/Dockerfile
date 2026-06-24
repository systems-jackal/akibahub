FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S akiba && adduser -S akiba -G akiba
COPY --from=builder /app/target/proposal-service-1.0.0.jar app.jar
RUN chown akiba:akiba app.jar
USER akiba
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
