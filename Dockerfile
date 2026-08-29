# Etapa 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
# Limitar la memoria de Maven en el build para no romper el contenedor de Render
ENV MAVEN_OPTS="-Xms128m -Xmx384m"
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

# Parámetros optimizados para consumo total <= 450MB (Margen seguro antes de los 500MB)
ENTRYPOINT ["java", \
  "-Xms160m", \
  "-Xmx250m", \
  "-XX:MaxMetaspaceSize=160m", \
  "-XX:ReservedCodeCacheSize=32m", \
  "-Xss256k", \
  "-XX:+UseSerialGC", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-jar", "app.jar"]