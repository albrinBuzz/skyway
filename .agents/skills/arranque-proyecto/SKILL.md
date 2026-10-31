---
name: arranque-proyecto
description: >-
  Procedimiento para levantar y verificar el entorno completo de desarrollo de SkyWay: base de datos PostgreSQL/PostGIS y backend monolito en Docker, y aplicación móvil Ionic/Angular en local. Usar cuando el usuario pida levantar, iniciar, correr o encender el proyecto o verificar el estado de sus servicios.
---

# Arranque del Proyecto SkyWay

Procedimiento estándar para iniciar los servicios del sistema SkyWay respetando las reglas de ejecución del proyecto.

## 1. Backend y Base de Datos (Docker)

> [!WARNING]
> NUNCA levantar el servicio `mobile` desde Docker. Solo levantar `postgres_db` y `backend`.

1. Ejecutar Docker Compose desde la raíz del proyecto (`C:\Users\diego\skyway`):
   ```powershell
   docker compose -p skyway --profile docker-db up -d postgres_db backend
   ```
2. Verificar que los contenedores estén activos:
   ```powershell
   docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
   ```
   * **`skyway_postgres`**: Puerto `5432`
   * **`skyway_backend`**: Puerto `8080` (Spring Boot + JSF + API REST `/api/v1/mobile`)

## 2. Aplicación Móvil (Local)

La app móvil se ejecuta siempre en entorno local, nunca en contenedor.

1. Desde el directorio `mobile` (`C:\Users\diego\skyway\mobile`), ejecutar como proceso en segundo plano / daemon:
   ```cmd
   npx.cmd ng serve --host 127.0.0.1 --port 8101 --proxy-config proxy.conf.json --prebundle=false
   ```
2. Validar que el puerto `8101` esté escuchando:
   ```powershell
   Get-NetTCPConnection -LocalPort 8101 -State Listen -ErrorAction SilentlyContinue
   ```
3. Acceso:
   * **App Móvil**: [http://127.0.0.1:8101](http://127.0.0.1:8101)
   * **Proxy API**: Redirige automáticamente `/api/**` hacia `http://localhost:8080`.
