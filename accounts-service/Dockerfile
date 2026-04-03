FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY accounts-service/pom.xml .
COPY pom.xml ..
RUN mvn dependency:go-offline -B

COPY accounts-service/src ./src
RUN mvn clean package -DskipTests -B

# runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup && \
    chown -R appuser:appgroup /app

COPY --from=builder /app/target/*.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser:appgroup

ENTRYPOINT ["java", "-jar", "app.jar"]
