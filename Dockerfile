# Etapa 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

# Cambios: Se usa G1GC y se remueven los flags que crashean la JVM
ENTRYPOINT ["java", \
  "-Xms128m", \
  "-Xmx240m", \
  "-XX:MaxMetaspaceSize=180m", \
  "-XX:ReservedCodeCacheSize=30m", \
  "-Xss256k", \
  "-XX:+UseG1GC", \
  "-XX:MaxGCPauseMillis=200", \
  "-XX:InitiatingHeapOccupancyPercent=45", \
  "-jar", "app.jar"]
