# Etapa 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# 1. Copiar primero el wrapper y los archivos de configuración
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Dar permisos de ejecución al ejecutable de Maven
RUN chmod +x mvnw

# 2. Pre-descargar las dependencias del proyecto a la caché de Docker.
# Se usa 'dependency:resolve' y 'dependency:resolve-plugins' para asegurar que baje todo.
RUN ./mvnw dependency:resolve dependency:resolve-plugins -B

# 3. Copiar el código fuente
COPY src ./src

# 4. Compilar el proyecto en modo offline (utilizará solo lo descargado previamente)
RUN ./mvnw clean package -DskipTests -o

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

# Ejecución limpia y directa para tu PC local
ENTRYPOINT ["java", "-jar", "app.jar"]