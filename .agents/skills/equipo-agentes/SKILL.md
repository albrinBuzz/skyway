---
name: equipo-agentes
description: >-
  Definición y guía de invocación del equipo de 3 subagentes de SkyWay: desarrollador (skyway-developer), pentester/QA (skyway-qa-pentester) y auditor de release (skyway-auditor), con sus modelos asignados. Usar cuando el usuario pida trabajar con los agentes de desarrollo, QA/pentesting o auditoría.
---

# Equipo de Subagentes de SkyWay

Si los subagentes aún no están registrados en la sesión actual, regístralos con `define_subagent` e invócalos con `invoke_subagent` usando la asignación de modelos definida a continuación:

| Subagente | Rol | Modelo Asignado (`invoke_subagent`) | Permisos (`EnableWriteTools`) | Responsabilidad Principal |
| :--- | :--- | :--- | :--- | :--- |
| **`skyway-developer`** | Desarrollador Full-Stack Mobile | `pro` | `true` | Desarrollo en Backend (`/api/v1/mobile/**`) y Frontend (`mobile/`), manteniendo el contrato `MobileDtos.java` ↔ `models.ts`/`api.service.ts` y reglas de `AGENTS.md`. |
| **`skyway-qa-pentester`** | Pentester, QA y Rendimiento | `pro` | `true` | Pruebas unitarias, no regresión, pruebas de rendimiento/concurrencia (`FOR UPDATE`) y pentesting (IDOR, autorización titular/acompañante, JWT). |
| **`skyway-auditor`** | Auditor y Documentador de Release | `flash` | `true` | Revisión de `git diff`, verificación de cumplimiento de `AGENTS.md`, validación de resultados de QA y generación del reporte de auditoría y notas de release. |

## Flujo de Trabajo Recomendado
1. **`skyway-developer` (`Model: "pro"`)**: Implementa los cambios y valida compilación/tipado (`npm run typecheck`).
2. **`skyway-qa-pentester` (`Model: "pro"`)**: Ejecuta pruebas unitarias, pruebas de regresión, concurrencia/rendimiento y auditoría ofensiva de seguridad.
3. **`skyway-auditor` (`Model: "flash"`)**: Audita los cambios y resultados de QA y emite el reporte y documentación del release.
