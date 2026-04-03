# Build
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

COPY my-bank-front-app/pom.xml .
COPY pom.xml ..
RUN mvn dependency:go-offline -B

COPY my-bank-front-app/src ./src
RUN mvn clean package -DskipTests -B

# Runtime
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

USER appuser:appgroup

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]