// Dockerfile para compilación multi-stage y despliegue en producción

# Stage 1: Build
FROM maven:3.9.0-eclipse-temurin-17 AS builder
WORKDIR /build

# Copiar archivos del proyecto
COPY pom.xml .
COPY src ./src

# Compilar la aplicación
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiar el JAR compilado desde el stage anterior
COPY --from=builder /build/target/auth-api-1.0.0.jar app.jar

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/api/health || exit 1

# Variables de entorno
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8081
