# ------------------------------------------------------------------------------
# ETAPA 1: Construcción (Maven + Java 21)
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copiar configuración Maven y dependencias primero para optimizar caché
COPY pom.xml ./
COPY .mvn .mvn/
COPY mvnw ./

# Descargar dependencias de Maven
RUN ./mvnw dependency:go-offline -B || true

# Copiar código fuente y compilar
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

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
ENV SPRING_PROFILES_ACTIVE=prod \
    PORT=8080

EXPOSE 8080

# Health check del contenedor
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget -q --spider http://localhost:8080/api/v1/health || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
