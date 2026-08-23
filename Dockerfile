# Etapa 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
# Forzar a la JVM a auto-destruirse inmediatamente si ocurre un OutOfMemoryError
ENV JAVA_OPTS="-Xmx256m -Xms128m -XX:MaxMetaspaceSize=180m -XX:+ExitOnOutOfMemoryError -XX:+CrashOnOutOfMemoryError"

ENV PORT=8080
EXPOSE 8080

# Balance Perfecto: 300MB Heap + 112MB Metaspace + 32MB CodeCache = ~444MB Total en Render
# Reemplaza la línea ENTRYPOINT / CMD de tu Dockerfile con esto:
ENTRYPOINT ["java", \
  "-Xms256m", \
  "-Xmx350m", \
  "-XX:MaxMetaspaceSize=200m", \
  "-XX:ReservedCodeCacheSize=45m", \
  "-Xss256k", \
  "-XX:+UseSerialGC", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-XX:+CrashOnOutOfMemoryError", \
  "-jar", "app.jar"]

