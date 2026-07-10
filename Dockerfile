# Build stage: compile and test the Java SDK.
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build
COPY pom.xml .
COPY src ./src

RUN mvn test -q && \
    mvn package -DskipTests -q

# Runtime stage: keep the produced library jar.
FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=builder /build/target/privacy-java-sdk-*.jar ./privacy-java-sdk.jar
