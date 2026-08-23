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

# Balance Perfecto: 300MB Heap + 112MB Metaspace + 32MB CodeCache = ~444MB Total en Render
ENTRYPOINT ["java", \
  "-Xms128m", \
  "-Xmx300m", \
  "-XX:MaxMetaspaceSize=112m", \
  "-XX:ReservedCodeCacheSize=32m", \
  "-Xss256k", \
  "-XX:+UseSerialGC", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-jar", "/app/app.jar"]