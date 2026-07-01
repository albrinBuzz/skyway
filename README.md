# SkyWay

SkyWay es una aplicación web para la gestión integral de una aerolínea. Permite administrar vuelos, pilotos, pasajeros y reservas de asientos, ofreciendo una experiencia completa tanto para usuarios como para administradores.


## Funciones principales del sistema

1. **Gestión de vuelos**: Permite crear, editar, eliminar y consultar vuelos, incluyendo información de origen, destino, horarios y estado del vuelo.

2. **Administración de pilotos y pasajeros**: Registro y edición de perfiles, asignación de roles (piloto, pasajero, admin) y consulta de historial de vuelos.

3. **Reservas y bloqueo de asientos**: Los usuarios pueden reservar asientos, ver disponibilidad en tiempo real y gestionar cambios o cancelaciones.

4. **Panel de administración**: Acceso exclusivo para administradores para gestionar la operación aérea, monitorear vuelos, usuarios y recursos.

5. **Blog y ayuda para viajeros**: Sección informativa con publicaciones, consejos y novedades relevantes para los usuarios.

6. **Visualización de rutas y detalles**: Muestra mapas de rutas, detalles de vuelos, boletos y perfiles de usuario de forma interactiva.

## Tecnologías utilizadas
- Java 21 (Spring Boot)
- Jakarta Faces (JSF)
- Maven
- HTML, CSS, JavaScript
- JPA/Hibernate para persistencia

## Estructura del proyecto
- `src/main/java`: Lógica de negocio, controladores y modelos.
- `src/main/resources`: Plantillas web y recursos estáticos.
- `dataBase/`: Scripts SQL para la base de datos.
- `addons/`: Utilidades y diagramas.

## Cómo ejecutar
1. Instala JDK 21 y Maven.
2. Ejecuta:
	```zsh
	./mvnw spring-boot:run
	```
3. Accede a la aplicación en tu navegador en `http://localhost:8080`

## Autores
- Equipo SkyWay

---
Este proyecto es educativo y de demostración para gestión de aerolíneas.
