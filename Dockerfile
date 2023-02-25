FROM maven:3.8.5-openjdk-17-slim as builder
WORKDIR /app
COPY pom.xml ./
COPY src/main ./src/main
RUN mvn package
FROM openjdk:17-slim
WORKDIR /app
COPY --from=builder /app/target/TonCash-0.0.1-SNAPSHOT.jar .
CMD ["java", "-jar", "/app/TonCash-0.0.1-SNAPSHOT.jar"]