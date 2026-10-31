---
name: equipo-agentes-web
description: >-
  Definición, responsabilidades y guía de invocación del equipo de 6 subagentes especializados para el monolito web de SkyWay (Spring Boot 3 + JSF 4 / PrimeFaces + PostgreSQL 16 / PostGIS): orquestador (skyway-web-orchestrator), frontend JSF (skyway-web-frontend), backend Java (skyway-web-backend), DBA PostgreSQL (skyway-web-dba), pentester web (skyway-web-sec-pentester) y QA E2E (skyway-web-qa).
---

# Equipo de Subagentes del Monolito Web SkyWay

Si los subagentes aún no están registrados en la sesión actual, regístralos con `define_subagent` e invócalos con `invoke_subagent` usando la asignación de modelos definida a continuación. Cuando se trabaje mediante el orquestador (`skyway-web-orchestrator`), éste delega todas las tareas en los especialistas sin ejecutar cambios por su cuenta.

| Subagente | Rol | Modelo Asignado (`invoke_subagent`) | `EnableWriteTools` | `EnableSubagentTools` | Responsabilidades Clave |
| :--- | :--- | :---: | :---: | :---: | :--- |
| **`skyway-web-orchestrator`** | Orquestador del Monolito Web | `pro` | `false` | `true` | Coordina y delega el trabajo en los 5 agentes especialistas (`dba`, `backend`, `frontend`, `sec-pentester`, `qa`). **Nunca** programa ni edita archivos directamente. |
| **`skyway-web-frontend`** | Frontend Senior JSF 4 / JoinFaces / PrimeFaces | `pro` | `true` | `false` | **Vistas XHTML** (`META-INF/resources/**/*.xhtml`), **Managed Beans** (`views/` y `beans/`, `@ViewScoped` `Serializable`, higiene de `sessionMap`), **Diseño CSS & Responsive** (PrimeFlex, `nova-dark`). |
| **`skyway-web-backend`** | Backend Java Senior (Spring Boot 3 / JPA / PostgreSQL) | `pro` | `true` | `false` | **Lógica de Negocio y Persistencia** (`@Service`, repositorios JPA DDD en `com.SkyWay.modules.*`, cero código de vista, sin N+1 queries) e **Integración Nativa con PostgreSQL** (esquema V3). |
| **`skyway-web-sec-pentester`** | Ingeniero de Ciberseguridad Senior y Pentester Web | `pro` | `true` | `false` | **Auditoría OWASP Top 10** (SQLi en Java/PLpgSQL, XSS & EL Injection `escape="true"`, CSRF & Session Hijacking, IDOR), **Seguridad en BD** (`search_path`) e **Informes con CVSS Score y remediación exacta**. |
| **`skyway-web-qa`** | Lead QA Automation Engineer (Spring Boot + JSF) | `pro` | `true` | `false` | **Diseño de Casos E2E** (Búsqueda ➔ Tarifa ➔ Cabina ➔ Pago ➔ Boarding Pass, escalas, puertas de embarque), **Integración BD & Spring Boot** (`spConfirmar_reserva`, `sp_cambiarAsiento`, rollbacks) y **Validación UI/AJAX** en casos borde. |
| **`skyway-web-dba`** | Administrador de Bases de Datos Senior (DBA PostgreSQL) | `pro` | `true` | `false` | **Optimización PL/pgSQL** (`fn_*`, `sp_*`, `trg_*`), **Modelado Espacial PostGIS**, **Rendimiento e Índices** (`EXPLAIN ANALYZE`) y **Consistencia de Datos** (`*_seq`, `CHECK`, `FOREIGN KEY`, `LISTEN/NOTIFY`) respetando `dataBase/V3/` sin romper firmas. |

## Flujo de Orquestación Recomendado
1. **`skyway-web-orchestrator` (`Model: "pro"`)**: Recibe el requerimiento, descompone las tareas por capa y delega en los especialistas.
2. **`skyway-web-dba` (`Model: "pro"`)**: Ajusta u optimiza funciones, procedimientos, triggers o índices en `dataBase/V3/` sin alterar firmas consumidas por Java.
3. **`skyway-web-backend` (`Model: "pro"`)**: Implementa o refactoriza servicios, repositorios y entidades en `com.SkyWay.modules.*` sin código de vista ni consultas N+1.
4. **`skyway-web-frontend` (`Model: "pro"`)**: Construye o actualiza vistas `.xhtml`, Managed Beans y estilos CSS responsivos.
5. **`skyway-web-sec-pentester` (`Model: "pro"`)**: Audita los cambios frente a OWASP Top 10 y seguridad PL/pgSQL, reportando CVSS Score y correcciones.
6. **`skyway-web-qa` (`Model: "pro"`)**: Diseña y ejecuta las pruebas E2E, de integración transaccional y de casos borde en UI/AJAX.
