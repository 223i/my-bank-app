FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY cash-service/pom.xml ./cash-service/
COPY common-kafka/pom.xml ./common-kafka/
COPY cash-service/src ./cash-service/src
COPY common-kafka/src ./common-kafka/src

WORKDIR /app
RUN mvn install -N -B -Dmaven.repo.local=/app/.m2/repository

WORKDIR /app/common-kafka
RUN mvn clean install -DskipTests -B -Dmaven.repo.local=/app/.m2/repository

WORKDIR /app/cash-service
RUN mvn clean package -DskipTests -B -Dmaven.repo.local=/app/.m2/repository

# runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup && \
    chown -R appuser:appgroup /app

COPY --from=builder /app/cash-service/target/*.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser:appgroup

ENTRYPOINT ["java", "-jar", "app.jar"]
