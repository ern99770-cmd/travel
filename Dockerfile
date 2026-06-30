# 多阶段构建 Dockerfile
FROM maven:3.8.6-openjdk-8-slim AS builder

WORKDIR /app
COPY travel-server/pom.xml ./pom.xml
COPY travel-server/src ./src

RUN mvn clean package -DskipTests

FROM openjdk:8-jre-slim

WORKDIR /app

COPY --from=builder /app/target/travel-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
