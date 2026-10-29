# syntax=docker/dockerfile:1

# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy POMs first to cache dependency resolution between builds
COPY pom.xml .
COPY backend/api-base/pom.xml backend/api-base/
COPY backend/api-core/pom.xml backend/api-core/
COPY backend/api-test/pom.xml backend/api-test/
COPY backend/api-rest/pom.xml backend/api-rest/
COPY backend/api-auth/pom.xml backend/api-auth/
COPY backend/s3-storage/pom.xml backend/s3-storage/
COPY backend/mail-module/pom.xml backend/mail-module/
COPY backend/api-wompi/pom.xml backend/api-wompi/
COPY backend/check-builder/pom.xml backend/check-builder/
COPY platform/application/pom.xml platform/application/
COPY platform/security/pom.xml platform/security/
RUN mvn -B -q dependency:go-offline -DexcludeReactor=true || true

# Copy sources and build the executable jar
COPY backend backend
COPY platform platform
RUN mvn -B -DskipTests -Djacoco.skip=true package \
 && cp platform/application/target/application-*.jar /workspace/app.jar

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build --chown=spring:spring /workspace/app.jar app.jar
USER spring

# Render injects PORT at runtime; 8080 is the local fallback
ENV PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${PORT} -jar /app/app.jar"]
