# ------------------------------------------------------------------------------
# ETAPA 1: Construcción (Maven + Java 21)
# ------------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copiar configuración Maven y descargar dependencias
COPY pom.xml ./
RUN mvn dependency:go-offline -B || true

# Copiar código fuente y compilar artefacto JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ------------------------------------------------------------------------------
# ETAPA 2: Runtime Ligero y Seguro
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear usuario sin privilegios para ejecutar la aplicación
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copiar el artefacto JAR construido
COPY --from=builder /app/target/inventario-1.0.0.jar app.jar

# Configuración de variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080 10000

ENTRYPOINT ["sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -Dserver.port=${PORT:-8080} -jar app.jar"]
