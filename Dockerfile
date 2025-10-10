# Usar una imagen base con JDK (Java 17 o el que estés usando)
FROM eclipse-temurin:23-jdk AS builder

# Establecer el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiar el proyecto completo al contenedor
COPY . .

# Dar permisos de ejecución a mvnw
RUN chmod +x mvnw

# Ejecutar mvn para construir el proyecto
RUN ./mvnw clean package -DskipTests

# Usar una imagen base con JRE para la etapa final
FROM eclipse-temurin:23-jre

# Establecer el directorio de trabajo para la ejecución
WORKDIR /app

# Copiar el archivo .jar generado en la etapa de construcción
COPY --from=builder /app/target/*.jar app.jar

# Exponer el puerto en el que la aplicación Spring Boot está escuchando (por defecto es 8080)
EXPOSE 8080

# Comando por defecto para ejecutar el .jar con Java
#ENTRYPOINT ["java", "-jar", "/app/app.jar"]
ENTRYPOINT ["java", "-Xmx512m", "-Xms512m", "-jar", "/app/app.jar"]
