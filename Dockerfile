# Etapa de construcción usando JDK 23
FROM eclipse-temurin:23-jdk AS builder
WORKDIR /app

# Copiar el proyecto completo al contenedor
COPY . .

# Dar permisos de ejecución a mvnw y empaquetar omitiendo tests
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Etapa de ejecución usando JRE 23 ligero
FROM eclipse-temurin:23-jre
WORKDIR /app

# Copiar el .jar generado desde la etapa builder
COPY --from=builder /app/target/*.jar app.jar

# Puerto donde escucha tu App (Ajusta a 8080 si corresponde)
EXPOSE 8080

# Ejecución optimizando los límites de memoria de la JVM
ENTRYPOINT ["java", "-Xmx512m", "-Xms512m", "-jar", "/app/app.jar"]