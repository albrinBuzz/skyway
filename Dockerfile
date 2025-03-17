# Usar una imagen base con JDK (Java 17 o el que estés usando)
FROM eclipse-temurin:23-jdk AS builder

# Establecer el directorio de trabajo dentro del contenedor

RUN  ./mvnw clean package -DskipTest

FROM eclipse-temurin:23-jre

WORKDIR /app

# Copiar el archivo .jar generado al contenedor
#COPY ./target/SkyWay-0.0.1.jar /app/SkyWay-0.0.1.jar

COPY --from=builder /app/target/*.jar app.jar

# Exponer el puerto en el que la aplicación Spring Boot está escuchando (por defecto es 8080)
EXPOSE 8080

# Comando por defecto para ejecutar el .jar con Java
#ENTRYPOINT ["java", "-jar", "/app/SkyWay-0.0.1.jar"]
ENTRYPOINT ["java", "-jar", "/app.jar"]
