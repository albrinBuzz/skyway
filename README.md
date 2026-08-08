# ✈️ SkyWay — Plataforma Web de Gestión Operacional y Comercial Aeronáutica

[![Estándar](https://img.shields.io/badge/Estándar-IEEE_830_%7C_PMI-blue.svg)](#)
[![Entorno](https://img.shields.io/badge/Entorno-Linux_Fedora_%7C_Java_JVM-orange.svg)](#)
[![Base de Datos](https://img.shields.io/badge/BD-PostgreSQL_%2F_Oracle_SQL-blue.svg)](#)
[![Revisión](https://img.shields.io/badge/SRS-Rev_01_(23%2F07%2F2026)-green.svg)](#)

**SkyWay (`PRJ-SKYWAY-2026`)** es una solución web integral (*Standalone Web System*) diseñada para digitalizar, centralizar y optimizar de extremo a extremo los flujos comerciales, operativos y logísticos de la aerolínea.

La plataforma une la interacción directa con el cliente (*Front-Office*) con la planificación interna y logística aeronáutica (*Back-Office*), garantizando consistencia transaccional (ACID), prevención activa de *overbooking* y validación estricta de no solapamiento en los turnos de la tripulación.

---

## 👨‍💻 Ficha del Proyecto & Créditos

* **Proyecto:** Sistema Web Aerolínea SkyWay (`PRJ-SKYWAY-2026`)
* **Estándares Aplicados:** IEEE Std 830-1998, Guía PMBOK 7ª Edición (PMI), ISO/IEC 25010.

---

## 📋 Tabla de Contenidos
- [Antecedentes y Propósito](#-antecedentes-y-propósito)
- [Ámbito del Sistema (Alcance)](#-ámbito-del-sistema-alcance)
- [Arquitectura y Stack Tecnológico](#-arquitectura-y-stack-tecnológico)
- [Módulos del Sistema](#-módulos-del-sistema)
- [Matriz de Requisitos Funcionales (RF)](#-matriz-de-requisitos-funcionales-rf)
- [Requisitos No Funcionales (RNF)](#-requisitos-no-funcionales-rnf)
- [Gestión de Riesgos](#-gestión-de-riesgos)
- [Planificación y Estimación (EDT & Gantt)](#-planificación-y-estimación)
- [Caso de Uso Representativo](#-caso-de-uso-representativo)
- [Anexos y Artefactos](#-anexos-y-artefactos)

---

## 📖 Antecedentes y Propósito

### Antecedentes
La industria del transporte aéreo comercial exige un nivel crítico de precisión, coordinación y sincronización en tiempo real. En el escenario operativo coinciden dos dimensiones complejas:
1. **Front-Office (Comercial):** Búsqueda de vuelos, evaluación de tarifas dinámicas, asignación de asientos en mapa físico, procesamiento transaccional de pago y emisión de pases de abordar (*Check-in*).
2. **Back-Office (Operativo y Logístico):** Inventario de aeronaves con sus capacidades, planificación de itinerarios y rutas complejas (tramos y escalas), asignación de personal calificado (pilotos con licencias y tripulantes de cabina) y logística de embarque en terminales.

La falta de una plataforma integrada genera silos de información, riesgo de duplicidad en reservas (*overbooking*), ineficiencias en la programación de turnos y limitaciones en la trazabilidad de los estados de vuelo.

### Propósito y Justificación
Concebir e implementar una plataforma web centralizada para la gestión integrada de las operaciones comerciales, aeronáuticas y logísticas de SkyWay, garantizando alta disponibilidad, usabilidad, integridad de datos e inviolabilidad de reglas de negocio.

---

## 🎯 Ámbito del Sistema (Alcance)

```text
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                 ALCANCE DE SKYWAY                                      │
├────────────────────────────────────────────────────────┬───────────────────────────────┤
│                  🟢 IN-SCOPE                           │        🔴 OUT-OF-SCOPE        │
├────────────────────────────────────────────────────────┼───────────────────────────────┤
│ • Buscador de vuelos y cotización por tarifas.         │ • Procesamiento de dinero     │
│ • Seleccionador interactivo de asientos en cabina.     │   real vía pasarelas bancarias│
│ • Simulación transaccional y generación de PNR.        │   (usa módulo de simulación). │
│ • Check-in online y emisión de Boarding Pass.          │ • Mantenimiento de motores,   │
│ • Control de flota, capacidades y mapa de asientos.    │   inventarios o combustible.  │
│ • Programación de itinerarios, escalas y estados.      │ • Integración con sistemas    │
│ • Asignación de tripulación sin solapamiento horario.  │   externos de radar o ATC.    │
│ • Asignación de puertas de embarque y bultos/equipaje. │ • Hardware físico de balanzas │
│ • Control de Acceso Basado en Roles (RBAC).            │   o escáneres de embarque.    │
└────────────────────────────────────────────────────────┴───────────────────────────────┘