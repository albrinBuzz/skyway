# Etapa 1: Build ligero en Alpine
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY . .

RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Etapa 2: Runtime ultraligero
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# Parámetros optimizados para no exceder 512 MB de RAM
ENTRYPOINT ["java", \
  "-Xms192m", \
  "-Xmx350m", \
  "-XX:MaxMetaspaceSize=112m", \
  "-XX:ReservedCodeCacheSize=48m", \
  "-Xss256k", \
  "-XX:+UseSerialGC", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-jar", "/app/app.jar"]