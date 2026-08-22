# Etapa 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Etapa 2: Runtime Ultraseguro
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Render asigna dinámicamente el puerto mediante la variable PORT
ENV PORT=8080
EXPOSE 8080

# Prioridad: Mínimo consumo de RAM
ENTRYPOINT ["java", \
  "-Xms96m", \
  "-Xmx180m", \
  "-XX:MaxMetaspaceSize=96m", \
  "-XX:ReservedCodeCacheSize=24m", \
  "-Xss256k", \
  "-XX:+UseSerialGC", \
  "-XX:+ShrinkHeapInSteps", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-jar", "/app/app.jar"]