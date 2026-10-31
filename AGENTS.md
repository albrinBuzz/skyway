# AGENTS.md — Contexto y Reglas del Proyecto SkyWay

## 1. Visión General del Proyecto
**SkyWay (`PRJ-SKYWAY-2026`)** es una plataforma integral de gestión operacional y comercial para una aerolínea, compuesta por:
1. **Backend & Web Monolito (`/` - Java / Spring Boot 3.3.4 / JSF PrimeFaces):**
   - Gestiona tanto el *Front-Office* web (búsqueda de vuelos, selección de tarifas y asientos, equipaje, pasarelas de pago WebPay/MercadoPago/PayPal, check-in web, emisión de E-Ticket PDF) como el *Back-Office* administrativo y operativo (gestión de flota, cabinas, aeropuertos con PostGIS, rutas/segmentos, itinerarios, tripulación, turnos y monitoreo).
   - Expone además la **API REST Móvil** bajo el prefijo `/api/v1/mobile/**` (`com.SkyWay.mobile.*`), protegida con JWT stateless (`OAuth2 Resource Server`, HmacSHA256, scope `mobile`, audiencia `skyway-passengers`, emisor `skyway-mobile`).
2. **Base de Datos (`/dataBase/V3` - PostgreSQL 16 + PostGIS 3.4):**
   - Base de datos relacional y espacial (`aerolinea_db`) con lógica avanzada en PL/pgSQL: funciones (`fnBuscarVuelo`, `fn_getAsientosAvion`, `fn_getticket`, etc.), procedimientos almacenados (`spConfirmar_reserva`, `sp_cambiarAsiento`, `sp_upsertPasajero`, `sp_cancelarReservaPasajero`) y triggers de sincronización de horarios, generación automática de asientos según `Configuracion_Cabina`, y notificaciones operacionales (`04_mobile_historial.sql` + `pg_notify`).
3. **Aplicación Móvil de Pasajeros (`/mobile` - Ionic 8 + Angular 20 + Capacitor 7):**
   - App enfocada en la experiencia del pasajero (*companion app*): autenticación JWT en memoria, buscador de itinerarios con traspaso a compra en la web, consulta de próximo vuelo y reservas activas, detalle de reserva con soporte multi-pasajero (titular y acompañantes), mapa interactivo de cabina para selección/cambio de asiento por pasajero, registro de check-in, tarjeta de embarque (*Boarding Pass*) individual con código QR, historial de viajes/cambios y bandeja de alertas/notificaciones, y gestión de perfil (correo y contraseña).

---

## 2. Cómo Levantar el Proyecto (Regla Crítica de Ejecución)

### A. Backend (Web + API) y Base de Datos (PostgreSQL + PostGIS) -> Vía Docker
El entorno de backend y base de datos se levanta con **Docker Compose** bajo el proyecto de nombre **`skyway`**, levantando **únicamente** los servicios de base de datos (`postgres_db`) y el monolito web/backend (`backend`), **NUNCA el contenedor `mobile`**:

```powershell
# Desde la raíz del repositorio (C:\Users\diego\skyway)
docker compose -p skyway --profile docker-db up -d postgres_db backend
```
* **Contenedores activos esperados en el proyecto Docker `skyway`:**
  * `skyway_postgres` (imagen `postgis/postgis:16-3.4-alpine`, puerto `5432`)
  * `skyway_backend` (Spring Boot + JSF + API `/api/v1/mobile`, puerto `8080`)
* **IMPORTANTE:** **No** levantar el servicio `mobile` desde Docker.

### B. Aplicación Móvil (`/mobile`) -> Vía CMD / Terminal Local
La aplicación móvil (`C:\Users\diego\skyway\mobile`) se levanta **siempre en local** mediante `cmd` ejecutando exactamente este comando dentro de la carpeta `mobile`:

```cmd
npx.cmd ng serve --host 127.0.0.1 --port 8101 --proxy-config proxy.conf.json --prebundle=false
```
* **URL de desarrollo móvil:** `http://127.0.0.1:8101` (o `http://localhost:8101`)
* **Proxy de desarrollo (`mobile/proxy.conf.json`):** Redirige todas las peticiones `/api/**` hacia `http://localhost:8080`.

---

## 3. Arquitectura delRepositorio

### Raíz y Backend (`/src/main/java/com/SkyWay`)
* `config/`:
  * `security/SecurityConfig.java`: Cadena de seguridad web (`@Order(2)` o por defecto) con sesión HTTP para vistas JSF (`.xhtml`).
  * `pago/` y `webPay/`: Estrategias de pasarelas de pago (`WebPayStrategy`, `MercadoPagoPaymentStrategy`, `PayPalPaymentStrategy`, `PagoFactoryService`).
  * `dataBase/PgNotifyConfig.java`: Listener de PostgreSQL `LISTEN/NOTIFY` para eventos en tiempo real.
* `mobile/` (**Módulo Backend de la App Móvil**):
  * `security/MobileSecurityConfig.java`: `@Order(1)` para `/api/v1/mobile/**`, stateless, CORS configurable (`MOBILE_ALLOWED_ORIGINS`), validación JWT HS256.
  * `controller/MobileAuthController.java`: Endpoints `/api/v1/mobile/auth/login`, `/me`, `/perfil/correo`, `/perfil/contrasena`.
  * `controller/MobilePassengerController.java`: Endpoints de `/inicio`, `/catalogo/aeropuertos`, `/busqueda/itinerarios`, `/reservas`, `/reservas/{id}/vuelos/{idVuelo}/asientos`, `/checkin`, `/pase-abordar`, `/tickets/**` (gestión por pasajero individual) y `/notificaciones`.
  * `service/`:
    * `MobileAuthService.java`: Login exclusivo para cuentas con registro en tabla `Pasajero`, emisión de JWT y actualización de credenciales.
    * `MobileBookingService.java`: Catálogo de aeropuertos con coordenadas, búsqueda de itinerarios y creación de reservas.
    * `MobileReadService.java`: Lectura optimizada de inicio, reservas activas/historial, detalle de reserva, vuelos, segmentos, puertas, tarifas y notificaciones.
    * `MobileOperationsService.java`: Reglas de negocio para mapa y cambio de asientos (valida estado de reserva, check-in previo, estado del vuelo, clase contratada y reglas de tarifa como `"Permite Cambios Asiento"` y `"Horas Minimas Cambio Asiento"`), check-in con bloqueo pesimista (`FOR UPDATE`) y lectura de notificaciones.
    * `MobileGroupTicketsService.java`: Soporte multi-pasajero por reserva y vuelo (`listar`, `vista`, `mapa`, `cambiarAsiento`). Valida que solo el titular pueda gestionar tickets/asientos de acompañantes, mientras que un acompañante solo ve su propio ticket.
  * `dto/MobileDtos.java`: Records inmutables de contrato JSON en `camelCase`.
* `modules/`: Arquitectura modular DDD (`domain/model`, `domain/repository`, `domain/service`, `application/serviceImpl`, `presentation/dto`) para cada entidad del dominio (`aerolinea`, `aeropuerto`, `asiento`, `asignacionpuerta`, `avion`, `capacidadclase`, `CaracteristicaTarifa`, `checkin`, `ciudad`, `claseasiento`, `continente`, `equipaje`, `estadoreserva`, `estadovuelo`, `fabricante`, `itinerario`, `itinerariovuelo`, `metodopago`, `modeloavion`, `notificacion`, `pago`, `pai`, `pasajero`, `PasajeroReserva`, `personaladministrativo`, `piloto`, `precioasiento`, `puertaembarque`, `reserva`, `reservaasiento`, `reservaitinerario`, `rolusuario`, `segmentovuelo`, `tarifa`, `TarifaCaracteristica`, `tarifaItinerario`, `tipoequipaje`, `tipoturno`, `tripulacion`, `turno`, `turnotripulacion`, `usuario`, `vuelo`).
  * `modules/reserva/domain/repository/MobileReservaQueriesImpl.java`: Consultas SQL nativas parametrizadas y bloqueos transaccionales para la API móvil.
* `views/` y `beans/`: Managed Beans de JSF/PrimeFaces (`@Named`, `@ViewScoped`) y vistas en `src/main/resources/META-INF/resources/**/*.xhtml`.

### Frontend Móvil (`/mobile/src/app`)
* `core/`:
  * `api.service.ts`: Cliente HTTP hacia `/api/v1/mobile`.
  * `auth.store.ts`: Almacenamiento del JWT **exclusivamente en memoria** (sin `localStorage` por seguridad).
  * `auth.interceptor.ts` y `auth.guard.ts`: Inyección de header `Authorization: Bearer <token>` y redirección a `/login` ante `401`.
  * `models.ts` y `ui.ts`: Interfaces TypeScript alineadas con `MobileDtos.java` y helpers de presentación.
* `shell.page.ts`: Barra de navegación inferior (`ion-tabs`) con 5 secciones principales: `Inicio`, `Vuelos`, `Historial`, `Alertas`, `Perfil`.
* `pages/`:
  * `login/`: Inicio de sesión de pasajeros.
  * `inicio/`: Buscador de vuelos con modal de aeropuertos (filtro por ciudad, nombre o IATA) y enlaces de compra hacia la web (`http://localhost:8080`).
  * `vuelos/`: Vista del próximo vuelo (`hero` card) y listado paginado de reservas activas.
  * `reserva-detalle/`: Detalle de itinerarios, tarifas, segmentos, equipaje, listado de tickets individuales por pasajero y acción de check-in (solo titular).
  * `asientos/`: Renderizado visual del fuselaje/cabina por clases (`Primera`, `Ejecutiva`, `Económica`), leyenda de estados y confirmación de asignación o cambio de asiento (propio o por RUT de pasajero).
  * `boarding-pass/`: Vista de ticket individual por pasajero (`/tickets/:rut`) o pase general, con generación de código QR (`qrcode`) tras el check-in.
  * `historial/`: Segmentos para reservas `Activas`, `Historial` (finalizadas/canceladas) y línea de tiempo de `Cambios` operacionales.
  * `alertas/`: Bandeja de notificaciones con filtro (`Todas` / `Nuevas`) y marcado como leída.
  * `perfil/`: Datos del pasajero, cambio de correo electrónico y cambio de contraseña.

### Monolito Web (Spring Boot 3.3.4 + JoinFaces 5.3.0 / JSF 4 / PrimeFaces + OmniFaces 4.3)
* **Configuración e Infraestructura (`JsfConfig.java`, `application.properties`, `config/`, `exception/`):**
  * `JsfConfig.java` y `application.properties`: Registra `FacesServlet` mapeado a `*.xhtml` (`loadOnStartup = 1`), tema `nova-dark`, `spring.jpa.open-in-view=true` y `spring.jpa.hibernate.ddl-auto=none`.
  * `config/security/SecurityConfig.java` y `AuthCustom.java`: Autenticación stateful con `HttpSessionSecurityContextRepository`, `BCryptPasswordEncoder` y `DaoAuthenticationProvider` (`UsuarioService`). Tras autenticar en `/home/login.xhtml` (`LoginBean`), `AuthCustom` guarda la entidad `Usuario` en `HttpSession` bajo el atributo `"usuario"`.
  * `config/WebSocketConfig.java`: Broker STOMP (`/topic`, `/app`) con endpoints SockJS `/ws-asientos` (eventos de bloqueo/liberación de asientos en tiempo real en `/topic/vuelo/{idVuelo}`) y `/ws-metrics` (telemetría en `/topic/metrics`).
  * `config/UnifiedSystemMonitorTask.java` y `config/health/*`: Monitoreo de rendimiento de BD (`DatabasePerformanceTracker`), ciclo de vida JSF (`JSFMetricsTracker`), sesiones HTTP (`HttpSessionMetricsListener`) y entorno host/cloud (`AzureCloudEnvironmentTracker`).
  * `config/CustomJSFExceptionHandler.java` y `exception/CustomViewExpiredExceptionResolver.java`: Captura de `ViewExpiredException` (redirigiendo a login) y excepciones de ciclo de vida JSF verificando `!fc.getResponseComplete()`.
* **Controladores MVC y Callbacks de Pasarelas (`controller/`):**
  * Navegación e integración: `HomeController.java` (`/` redirige a `home/index.xhtml`), `AdminController.java`, `PerfilController.java`, `VueloController.java`, `AvionController.java`, `PilotoController.java`, `HealthCheckController.java`.
  * Retornos de pasarelas de pago (`@RestController` con `RedirectView`): `WebPayController.java` (`/webpay/commit`), `MercadoPagoController.java` (`/mercadopago/commit`, `/mercadopago/cancel`) y `PayPalController.java` (`/paypal/commit`, `/paypal/cancel`). Validan el token con `PagoFactoryService` / `WebPayService`, recuperan `SOLICITUD_RESERVA_PENDIENTE` desde `HttpSession`, ejecutan la persistencia transaccional en `ReservaProcesadorService`, guardan `ULTIMO_ID_RESERVA` en sesión y redirigen a `/home/pago-exitoso.xhtml` o `/home/errorPago.xhtml`.
* **Managed Beans de JSF (`views/` y `beans/`):**
  * **Front-Office y Flujo de Compra:**
    * `ItinerarioBusquedaBean` (`@RequestScoped`): Buscador de vuelos (`OW` solo ida y `RT` ida y vuelta) con autocompletado de aeropuertos y redirección por query params a `/home/vuelos.xhtml`.
    * `ItinerarioDetalleBean` (`@ViewScoped`): Selección de itinerarios de ida/regreso, visualización de escalas, tarifas por itinerario (`ItinerarioTarifa`) y trazado de ruta JSON para el mapa.
    * `ReservaAsientoBean` (`@Named("seleccionAsientosBean")`, `@ViewScoped`): Renderizado de cabina (`FilaCabinaDTO`, `BloqueAsientosDTO`), retención temporal de asientos en memoria con TTL de 10 minutos mediante `AsientoCacheService` + WebSockets, y persistencia de estado en `sessionMap` (`asientosSeleccionados`, `itinerarios`, `tarifasItinerios`, `pasajeros`, `reservaSessionId`).
    * `EquipajeBean` (`@ViewScoped`): Selección de equipaje adicional por pasajero (`MAX_CABINA = 1`, `MAX_BODEGA = 3` entre 15kg y 23kg) en `sessionMap` (`equipajePorPasajero`).
    * `ReservaBean` (`@ViewScoped`): Registro y validación de pasajeros (RUT/Cédula, Pasaporte, Email), cálculo de totales, armado de `SolicitudReservaDTO` en sesión (`SOLICITUD_RESERVA_PENDIENTE`) e inicio de pago con `PagoFactoryService`.
    * `PagoExitosoController` (`@ViewScoped`): Consume `ULTIMO_ID_RESERVA` de sesión, genera PNR (`PnrGenerator`), envía comprobante por correo asíncrono (`EmailReservaService`), libera asientos en `AsientoCacheService`, purga las claves temporales de compra en `sessionMap` y genera descargas PDF (`StreamedContent` vía `ETicketPdfGeneratorService` con OpenPDF).
    * `CheckInBean` (`@Named("checkInView")`, `@ViewScoped`): Check-in web por parámetros o búsqueda manual (PNR + Apellido/RUT con límite de 5 intentos fallidos), validación de ventana horaria (`ValidadorCheckIn`: habilitado entre 48h y 60 min antes del vuelo) y emisión de tarjetas de embarque.
  * **Perfil y Autogestión Web (`views/perfil/`, `beans/viajesPasajero/`):**
    * `LoginBean`, `NavigationBean`, `PerfilBean` (pasajero y piloto), `MisTurnosBean` (tripulación), `ReservaView` (`viajePsView`), `CambioAsientoBean` (`cambioBean`) y `CambiarAsientoView` (`cambioView`).
  * **Back-Office y Administración (`views/admin/`, `views/avion/`, `views/aeropuerto/`, `views/Vuelo/`, `views/Itinerario/`, `views/turno/`):**
    * `GestionFlotaIntegradaBean` (`@ViewScoped`): CRUD unificado de fabricantes, modelos, aviones y capacidades por clase (`CapacidadClase`).
    * `GestionAeropuertosBean` (`@ViewScoped`): ABM de países, ciudades y aeropuertos con geometría PostGIS e interacción con mapa Leaflet.
    * `VueloBean`, `SegmentoVueloBean`, `EstadoVueloBean`: Programación de vuelos, segmentos, asignación de puertas (`AsignacionPuerta`), precios por clase (`PrecioAsiento`) y turnos.
    * `ItinerarioBean` (`@ViewScoped`): Armado comercial de itinerarios conectando vuelos, escalas y precios por tarifa (`ItinerarioTarifa`).
    * `TurnoBean` (`@ViewScoped`): Asignación de tripulación a turnos de vuelo (`TurnoTripulacion`).
    * `TarifaBean`, `GestionParametricasBean`, `GestionUsuariosAdminBean` (`@ViewScoped`): Gestión de tarifas y características dinámicas (`boolean`, `int`, `text`), catálogos paramétricos y administración multi-rol de usuarios (`Pasajero`, `Piloto`, `Tripulacion`, `Admin`).
* **Vistas XHTML y Recursos Estáticos (`src/main/resources/META-INF/resources/`):**
  * `home/*.xhtml`: Portal de pasajeros y flujo de reserva (`index.xhtml`, `vuelos.xhtml`, `seleccionAsientos.xhtml`, `equipaje.xhtml`, `reserva.xhtml`, `pago-exitoso.xhtml`, `errorPago.xhtml`, `checkin.xhtml`, `estado-vuelo.xhtml`, `login.xhtml`, `registro.xhtml`, `mapa.xhtml`) sobre `home/template/main.xhtml`.
  * `admin/**/*.xhtml`: Consola administrativa (`index.xhtml`, `gestionFlota.xhtml`, `gestionAeropuertos.xhtml`, `gestionarTarifas.xhtml`, `gestionParametricas.xhtml`, `usuarios.xhtml`, `metrics-ws.xhtml`, `vuelo/vuelos.xhtml`, `itinerario/itinerarios.xhtml`, `turno/turno.xhtml`) sobre `admin/template.xhtml`.
  * `perfil/**/*.xhtml`: Vistas protegidas por rol (`pasajero/index.xhtml`, `pasajero/detalleReserva.xhtml`, `pasajero/cambioAsiento.xhtml`, `piloto/index.xhtml`, `tripulacion/index.xhtml`).
  * `resources/`: Estilos (`css/`), mapas interactivos (`js/mapa.js`, `js/vuelos/globo-vuelo.js`) y cliente WebSocket de telemetría (`js/metrics-dashboard.js`).

### Base de Datos PostgreSQL 16 + PostGIS 3.4 (`/dataBase/V3`)
* `01_esquema/tablas.sql`: Secuencias explícitas (`*_seq`) y tablas principales: identidad (`Usuario`, `Roles`, `RolUsuario`, `Pasajero`, `Piloto`, `Tripulacion`, `Personal_Administrativo`), geografía con índice GiST (`Continente`, `Pais`, `Ciudad`, `Aeropuerto` con columna `posicion GEOGRAPHY(Point, 4326)`, `Puerta_Embarque`), flota y cabina (`Fabricante`, `Modelo_Avion`, `Avion`, `Clase_asiento`, `Capacidad_Clase`, `Configuracion_Cabina`, `Asiento`), operaciones (`Vuelo`, `Estado_Vuelo`, `Segmento_Vuelo`, `Asignacion_Puerta`, `Precio_Asiento`, `Turno`, `Tipo_Turno`, `Turno_Tripulacion`), comercial (`Itinerario`, `Itinerario_Vuelo`, `Tarifa`, `Caracteristica_Tarifa`, `Tarifa_Caracteristica`, `Itinerario_Tarifa`), y reservas/auditoría (`Reserva`, `Estado_reserva`, `Reserva_Itinerario`, `pasajero_reserva`, `Reserva_Asiento`, `Equipaje`, `Tipo_Equipaje`, `Pago`, `Metodo_Pago`, `Checkin`, `Checkin_Pasajero`, `Cancelacion`, `Cancelacion_Asiento`, `Notificacion`, `Log_Auditoria`).
* `02_logica/01_triggers.sql`:
  * `trigger_insertar_asientos` (`fn_insertarAsientos` en `Capacidad_Clase`): Genera automáticamente los asientos del avión en `Asiento` según los rangos de filas y columnas de `Configuracion_Cabina`.
  * Sincronización automática de órdenes, códigos y horarios: `trg_set_orden_segmento_vuelo`, `trg_set_numero_vuelo` (asigna número `IATA1-IATA2<id_segmento>`), `trg_set_fecha_vuelo` (calcula salida/llegada de `Vuelo` desde sus segmentos), `trg_actualizar_fechas_turno`, `trg_set_orden_itinerario_vuelo`, `trg_set_fecha_itinerario`, `trg_vuelo_hacia_itinerario` y `trg_actualizar_duracion_itinerario`.
  * Alertas operacionales y `LISTEN/NOTIFY`: `tr_notificacionVueloCabecera` y `tr_notificacionVueloEstado` (`fn_notificacionVueloEstado`) crean notificaciones cuando cambia el horario o estado de un vuelo/segmento; `tr_notify_new_notificacion` ejecuta `pg_notify('nuevo_correo', id_notificacion)`, que `NotificacionListenerService` escucha en vivo para despachar correos vía `JavaMailSender`.
* `02_logica/02_funciones.sql` y `03_procedimientos.sql`:
  * Funciones: `fnBuscarVuelo`, `FnbuscarVuelos`, `fnDTinitinerario`, `fn_getAsientosAvion`, `fn_getItinerariosRut`, `fn_getItinerariosPorRutYFechas`, `fn_getAsientosPorItinerarioYReserva` (con respaldo en `Cancelacion_Asiento`), `fn_getticket` y `fn_getVueloInfo`.
  * Procedimientos: `spConfirmar_reserva`, `spVerificarDisponinibilidadAsientos`, `sp_cambiarAsiento`, `sp_upsertPasajero` y `sp_cancelarReservaPasajero` (registra en `Cancelacion`, respalda asientos en `Cancelacion_Asiento`, audita en `Log_Auditoria`, libera `Reserva_Asiento` y actualiza `Reserva`).

---

## 4. Reglas Básicas de Desarrollo para Agentes
1. **No levantar `mobile` en Docker:** Siempre usar el comando local `npx.cmd ng serve --host 127.0.0.1 --port 8101 --proxy-config proxy.conf.json --prebundle=false` en `C:\Users\diego\skyway\mobile`.
2. **Respetar el contrato Backend ↔ Mobile:** Cualquier cambio en `MobileDtos.java` o en los controladores de `com.SkyWay.mobile.controller` debe reflejarse exactamente en `mobile/src/app/core/models.ts` y `mobile/src/app/core/api.service.ts`.
3. **Seguridad y Autorización:**
   - La identidad del usuario autenticado en la API móvil proviene **siempre** de `jwt.getSubject()` (RUT del pasajero), nunca de parámetros enviados por el cliente.
   - Mantener la distinción entre **titular** (`reserva.rut_pasajero`) y **acompañante** (`pasajero_reserva` / `reserva_asiento`). Solo el titular puede realizar el check-in de la reserva y gestionar asientos/tickets de otros pasajeros de la misma reserva.
4. **Compatibilidad con Base de Datos V3:** No romper las tablas, secuencias, triggers ni funciones existentes en `dataBase/V3`. Las consultas móviles deben seguir usando SQL parametrizado y bloqueos `FOR UPDATE` en operaciones críticas de concurrencia (cambio de asiento y check-in).
5. **Verificación en Mobile:** Antes de dar por finalizada una tarea en el frontend móvil, verificar el tipado con `npm run typecheck` (`tsc --noEmit -p tsconfig.app.json`) dentro de `mobile/`.
6. **Desarrollo en el Monolito JSF / PrimeFaces:**
   - Todo Managed Bean con alcance `@ViewScoped` (`jakarta.faces.view.ViewScoped` u OmniFaces) **debe implementar `Serializable`**. En métodos `@PostConstruct`, validar siempre que `FacesContext.getCurrentInstance()` y `getExternalContext()` no sean nulos antes de acceder a parámetros o sesión, y verificar `!fc.getResponseComplete()` antes de redirigir.
   - Mantener la higiene de `sessionMap` / `HttpSession`: las claves temporales del flujo de compra (`asientosSeleccionados`, `itinerarios`, `tarifasItinerios`, `pasajeros`, `equipajePorPasajero`, `SOLICITUD_RESERVA_PENDIENTE`, `ULTIMO_ID_RESERVA`) deben limpiarse al finalizar o reiniciar la compra, liberando también los asientos retenidos en `AsientoCacheService`.
   - En vistas PrimeFaces (`.xhtml`), actualizar explícitamente los componentes afectados mediante `update="..."` o `PrimeFaces.current().ajax().update(...)` y evitar anidar formularios `<h:form>`.
7. **Integración con Triggers y Secuencias de PostgreSQL V3:**
   - Con `spring.jpa.hibernate.ddl-auto=none`, toda entidad JPA debe respetar las secuencias (`@SequenceGenerator` alineado con `*_seq`) y columnas de `dataBase/V3/01_esquema/tablas.sql`.
   - No insertar manualmente registros en `Asiento` al registrar capacidades de avión (el trigger `trigger_insertar_asientos` en `Capacidad_Clase` los genera según `Configuracion_Cabina`) ni desincronizar horarios de `Vuelo`/`Itinerario`/`Turno` que son mantenidos por los triggers sobre `Segmento_Vuelo` e `Itinerario_Vuelo`.
8. **Verificación en Backend / Monolito Web:** Antes de dar por finalizada una tarea en el monolito Java/JSF, verificar que compile sin errores ejecutando `.\mvnw.cmd test-compile` (o `mvn test-compile`) en la raíz del proyecto.

## 5. reglas de uso de tokens
1. No programar sin contexto
   ANTES de escribir codigo: lee los archivos relevantes, revisa git log, entiende la arquitectura.
   Si no tienes contexto suficiente, pregunta. No asumas.
2. Respuestas cortas
   Responde en 1-3 oraciones. Sin preambulos, sin resumen final.
   Español neutro, sin voseo: "puedes", "avísame", "prefieres", "úsalo" — nunca "podés", "avisame", "preferís", "usalo". Aplica a respuestas, comentarios, docs y la wiki.
   EXCEPCIÓN: Si el usuario pide explícitamente una explicación detallada o si se trata de un debug complejo, ignora el límite de oraciones y da el contexto necesario.
   No repitas lo que el usuario dijo. No expliques lo obvio.
   Codigo habla por si mismo: no narres cada linea que escribes.
3. No reescribir archivos completos
   Usa Edit (reemplazo parcial), NUNCA Write para archivos existentes salvo que el cambio sea >80% del archivo.
   Cambia solo lo necesario. No "limpies" codigo alrededor del cambio.
4. No releer archivos ya leidos
   Si ya leiste un archivo en esta conversacion, no lo vuelvas a leer salvo que haya cambiado.
   Toma notas mentales de lo importante en tu primera lectura.
5. Validar antes de declarar hecho
   Despues de un cambio: compila, corre tests, o verifica que funciona.
   Nunca digas "listo" sin evidencia de que funciona.
6. Cero charla aduladora
   No digas "Excelente pregunta", "Gran idea", "Perfecto", etc.
   No halagues al usuario. Ve directo al trabajo.
7. Soluciones simples
   Implementa lo minimo que resuelve el problema. Nada mas.
   No agregues abstracciones, helpers, tipos, validaciones, ni features que no se pidieron.
   3 lineas repetidas > 1 abstraccion prematura.
8. No pelear con el usuario
   Si el usuario dice "hazlo asi", hazlo asi. No debatas salvo riesgo real de seguridad o perdida de datos.
   EXCEPCIÓN: Si notas una mala práctica grave de arquitectura o rendimiento, menciona tu advertencia (concern) y una mejor alternativa en 1 o 2 oraciones máximas, y procede con lo que pidió.
9. Leer solo lo necesario
   No leas archivos completos si solo necesitas una seccion. Usa offset y limit.
   Si sabes la ruta exacta, usa Read directo. No hagas Glob + Grep + Read cuando Read basta.
10. No narrar el plan antes de ejecutar
    No digas "Voy a leer el archivo, luego modificar la funcion, luego compilar...". Solo hazlo.
    El usuario ve tus tool calls. No necesita un preview en texto.
11. Paralelizar tool calls
    Si necesitas leer 3 archivos independientes, lee los 3 en un solo mensaje, no uno por uno.
    Menos roundtrips = menos tokens de contexto acumulado.
12. No duplicar codigo en la respuesta
    Si ya editaste un archivo, no copies el resultado en tu respuesta. El usuario lo ve en el diff.
    Si creaste un archivo, no lo muestres entero en texto tambien.
13. No usar Agent cuando Grep/Read basta
    Agent duplica todo el contexto en un subproceso. Solo úsalo para busquedas amplias o tareas complejas.
    Para buscar una funcion o archivo especifico, usa Grep o Glob directo.