/*
 =================================================================================
 DOCUMENTACIÓN OPERATIVA Y COMERCIAL: ESTRATEGIA HUB CONO SUR - SANTIAGO (SCL)
=================================================================================

[1] MAPA OPERATIVO DE VUELOS FÍSICOS Y TRAMOS (Mapeo de Segmentos y Pilotos)
---------------------------------------------------------------------------------

  (1) SANTIAGO [SCL] -- Base Central
          |
          | Vuelo: TEMP_IDA1 | Piloto: 12345678-9 | Horario: 08:00 -> 12:00
          v
  (3) SÃO PAULO [GRU] -- Nodo / Hub de Distribución (Eje de conexiones)
          |
          +---> [Continuación Internacional]
          |       Vuelo: TEMP_IDA2 | Piloto: 87654321-0 | Horario: 14:30 -> 23:59
          |       v
          |   (4) NUEVA YORK [JFK]
          |
          +---> [Ramificación Regional]
          |       Vuelo: TEMP_EZE2 | Piloto: 11223344-1 | Horario: 15:15 -> 18:30
          |       v
          |   (7) BUENOS AIRES [EZE]
          |
          +---> [Vuelo de Retorno Local] (Día 6 de la operación)
                  Vuelo: TEMP_RET_LOCAL | Piloto: 12345678-9 | Horario: 15:00 -> 19:00
                  v
              (1) SANTIAGO [SCL]

---------------------------------------------------------------------------------
  (*) RETORNO LONG-HAUL DIRECTO:
  (4) NUEVA YORK [JFK] --------> (1) SANTIAGO [SCL]
        Vuelo: TEMP_REGR | Piloto: 55667788-2 | Horario: 21:00 -> 06:00 (+1)
=================================================================================


[2] ESTRUCTURA DE ITINERARIOS COMERCIALES (Opciones de Venta al Frontend)
---------------------------------------------------------------------------------

  PRODUCTO CENTRAL: Santiago a Nueva York (1 Escala)
  ===============================================================================
  [SCL (1)] ==( TEMP_IDA1 )==> [GRU (3)] --( Escala: 2h 30m )--==( TEMP_IDA2 )==> [JFK (4)]
  Precio Base: $890.000 | Duración: 15h 59m | Escalas: 1

  PRODUCTO CENTRAL RETORNO: Nueva York a Santiago (Directo)
  ===============================================================================
  [JFK (4)] =========================( TEMP_REGR )=========================> [SCL (1)]
  Precio Base: $750.000 | Duración: 9h 00m  | Escalas: 0

  RUTA SECUNDARIA REGIONAL A: Santiago a São Paulo (Solo Ida)
  ===============================================================================
  [SCL (1)] =========================( TEMP_IDA1 )=========================> [GRU (3)]
  Precio Base: $310.000 | Duración: 4h 00m  | Escalas: 0
  * Nota: Llenado de asientos libres del tramo alimentador matutino.

  RUTA SECUNDARIA REGIONAL B: São Paulo a Santiago (Solo Vuelta)
  ===============================================================================
  [GRU (3)] ======================( TEMP_RET_LOCAL )======================> [SCL (1)]
  Precio Base: $290.000 | Duración: 4h 00m  | Escalas: 0

  RUTA SECUNDARIA INTEGRADA: Santiago a Buenos Aires (1 Escala de Optimización)
  ===============================================================================
  [SCL (1)] ==( TEMP_IDA1 )==> [GRU (3)] --( Escala: 3h 15m )--==( TEMP_EZE2 )==> [EZE (7)]
  Precio Base: $450.000 | Duración: 10h 30m | Escalas: 1
  * Nota: Estrategia de saturación de flota para maximizar el uso de 'TEMP_IDA1'.

=================================================================================
 RESUMEN DE ASIGNACIÓN DE RECURSOS (Consistencia de Reglas de Negocio)
=================================================================================
  1. Pilotos Usados: 4 de tu pool de 5 disponibles (Cero errores de FK / On Conflict).
  2. Multi-Propósito: El avión 'TEMP_IDA1' transporta de manera simultánea a
     pasajeros de 3 Itinerarios distintos (A Nueva York, a Brasil Local y a Argentina).
  3. Automatización: Al ejecutarse, las tablas puente 'Itinerario_Vuelo' mapean de
     forma exacta el orden cronológico de los tramos físicos para el motor de búsqueda.
=================================================================================
 */

-- ======================
-- ===================================================
-- 1. INSERTAR LOS VUELOS (Contenedores operativos - Usando SOLO tus pilotos)
-- =========================================================================
INSERT INTO Vuelo (ID_VUELO, Numero_Vuelo, Fecha_Hora_Salida, Fecha_Hora_Llegada, ID_ESTADO_VUELO, ID_AVION, RUT_PILOTO, ID_AEROLINEA) VALUES
-- Tus 3 vuelos base
(nextval('vuelo_seq'), 'TEMP_IDA1', now(), now(), 1, 1, '12345678-9', 1), -- Piloto 1 (Va a Brasil)
(nextval('vuelo_seq'), 'TEMP_IDA2', now(), now(), 1, 3, '87654321-0', 1), -- Piloto 2 (Va a NY)
(nextval('vuelo_seq'), 'TEMP_REGR', now(), now(), 1, 6, '55667788-2', 1), -- Piloto 4 (Vuelve de NY directo)

-- OPTIMIZACIÓN CON PILOTOS EXISTENTES:
-- El piloto de 'TEMP_IDA1' ('12345678-9') toma el avión de regreso a Chile después de un descanso en el Hub
(nextval('vuelo_seq'), 'TEMP_RET_LOCAL', now(), now(), 1, 1, '12345678-9', 1),

-- Usamos al Piloto 3 ('11223344-1') que estaba libre para abrir la ruta Hub GRU -> Buenos Aires (EZE = 7)
(nextval('vuelo_seq'), 'TEMP_EZE2', now(), now(), 1, 2, '11223344-1', 1);


-- =========================================================================
-- 2. INSERTAR SEGMENTOS (Corregido para fijar las horas exactas AM/PM)
-- =========================================================================
INSERT INTO Segmento_Vuelo (ID_SEGMENTO, ID_VUELO, ID_AEROPUERTO_ORIGEN, ID_AEROPUERTO_DESTINO, HORA_SALIDA, HORA_LLEGADA, ORDEN_SEGMENTO) VALUES
-- Segmentos Base
(nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'TEMP_IDA1'), 1, 3, DATE_TRUNC('day', now()) + interval '3 days 08:00:00', DATE_TRUNC('day', now()) + interval '3 days 12:00:00', NULL),
(nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'TEMP_IDA2'), 3, 4, DATE_TRUNC('day', now()) + interval '3 days 14:30:00', DATE_TRUNC('day', now()) + interval '3 days 23:59:00', NULL),
(nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'TEMP_REGR'), 4, 1, DATE_TRUNC('day', now()) + interval '6 days 21:00:00', DATE_TRUNC('day', now()) + interval '7 days 06:00:00', NULL),

-- NUEVOS TRAMOS FÍSICOS CON LA MISMA TRIPULACIÓN:
-- Retorno local desde Brasil (3) a Chile (1) operado por el piloto de la ida en el día 6
(nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'TEMP_RET_LOCAL'), 3, 1, DATE_TRUNC('day', now()) + interval '6 days 15:00:00', DATE_TRUNC('day', now()) + interval '6 days 19:00:00', NULL),

-- Conexión Regional: Sale de GRU (3) a las 15:15 PM hacia Buenos Aires (EZE = 7) con escala optimizada
(nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'TEMP_EZE2'), 3, 7, DATE_TRUNC('day', now()) + interval '3 days 15:15:00', DATE_TRUNC('day', now()) + interval '3 days 18:30:00', NULL);


-- =========================================================================
-- 4. CREACIÓN DE LOS ITINERARIOS COMERCIALES (Estrategia Multipropósito)
-- =========================================================================
INSERT INTO Itinerario (ORIGEN_AEROPUERTO, DESTINO_AEROPUERTO, HORA_SALIDA, HORA_LLEGADA, DURACION_TOTAL, NUMERO_ESCALAS, Precio_Base) VALUES
-- Tus itinerarios base
(1, 4, now(), now(), '15 hours 59 minutes'::interval, 1, 890000),
(4, 1, now(), now(), '9 hours'::interval, 0, 750000),

-- NUEVAS OFERTAS COMERCIALES COMPARTIENDO EL VUELO DE LAS 08:00 AM:
-- Pasajero que se baja definitivamente en Brasil
(1, 3, now(), now(), '4 hours'::interval, 0, 310000),
-- Pasajero que retorna de Brasil solo
(3, 1, now(), now(), '4 hours'::interval, 0, 290000),

-- NUEVA RUTA INTEGRADA: Chile -> Buenos Aires por São Paulo (Aprovecha el mismo tramo SCL->GRU inicial)
(1, 7, now(), now(), '10 hours 30 minutes'::interval, 1, 450000);


-- =========================================================================
-- 5. RELACIÓN DE ITINERARIOS Y VUELOS (Itinerario_Vuelo)
-- =========================================================================

-- Conexiones Base SCL -> JFK
INSERT INTO Itinerario_Vuelo (ID_ITINERARIO, ID_VUELO, ORDEN, TIEMPO_ESPERA, TIPO_CONEXION) VALUES
                                                                                                ((SELECT ID_ITINERARIO FROM Itinerario WHERE ORIGEN_AEROPUERTO = 1 AND DESTINO_AEROPUERTO = 4 ORDER BY ID_ITINERARIO DESC LIMIT 1),
                                                                                                (SELECT ID_VUELO FROM Segmento_Vuelo WHERE ID_AEROPUERTO_ORIGEN = 1 AND ID_AEROPUERTO_DESTINO = 3 ORDER BY ID_SEGMENTO DESC LIMIT 1), NULL, '2 hours 30 minutes'::interval, 'Escala en São Paulo'),
((SELECT ID_ITINERARIO FROM Itinerario WHERE ORIGEN_AEROPUERTO = 1 AND DESTINO_AEROPUERTO = 4 ORDER BY ID_ITINERARIO DESC LIMIT 1),
 (SELECT ID_VUELO FROM Segmento_Vuelo WHERE ID_AEROPUERTO_ORIGEN = 3 AND ID_AEROPUERTO_DESTINO = 4 ORDER BY ID_SEGMENTO DESC LIMIT 1), NULL, '0'::interval, 'Vuelo Final a New York');

-- Regreso Directo JFK -> SCL
INSERT INTO Itinerario_Vuelo (ID_ITINERARIO, ID_VUELO, ORDEN, TIEMPO_ESPERA, TIPO_CONEXION) VALUES
                                                                                                ((SELECT ID_ITINERARIO FROM Itinerario WHERE ORIGEN_AEROPUERTO = 4 AND DESTINO_AEROPUERTO = 1 ORDER BY ID_ITINERARIO DESC LIMIT 1),
                                                                                                (SELECT ID_VUELO FROM Segmento_Vuelo WHERE ID_AEROPUERTO_ORIGEN = 4 AND ID_AEROPUERTO_DESTINO = 1 ORDER BY ID_SEGMENTO DESC LIMIT 1), NULL, '0'::interval, 'Vuelo directo de retorno');


-- =========================================================================
-- ENLACES DE LA RUTA MAXIMIZADA (Mapeo comercial a las tablas relacionales)
-- =========================================================================

-- A. El pasajero "Solo Brasil" se mete al vuelo de las 08:00 AM
INSERT INTO Itinerario_Vuelo (ID_ITINERARIO, ID_VUELO, ORDEN, TIEMPO_ESPERA, TIPO_CONEXION) VALUES
                                                                                                ((SELECT ID_ITINERARIO FROM Itinerario WHERE ORIGEN_AEROPUERTO = 1 AND DESTINO_AEROPUERTO = 3 ORDER BY ID_ITINERARIO DESC LIMIT 1),
                                                                                                (SELECT ID_VUELO FROM Segmento_Vuelo WHERE ID_AEROPUERTO_ORIGEN = 1 AND ID_AEROPUERTO_DESTINO = 3 ORDER BY ID_SEGMENTO DESC LIMIT 1), NULL, '0'::interval, 'Destino final Sao Paulo');

-- B. Retorno del pasajero "Solo Brasil"
INSERT INTO Itinerario_Vuelo (ID_ITINERARIO, ID_VUELO, ORDEN, TIEMPO_ESPERA, TIPO_CONEXION) VALUES
                                                                                                ((SELECT ID_ITINERARIO FROM Itinerario WHERE ORIGEN_AEROPUERTO = 3 AND DESTINO_AEROPUERTO = 1 ORDER BY ID_ITINERARIO DESC LIMIT 1),
                                                                                                (SELECT ID_VUELO FROM Segmento_Vuelo WHERE ID_AEROPUERTO_ORIGEN = 3 AND ID_AEROPUERTO_DESTINO = 1 ORDER BY ID_SEGMENTO DESC LIMIT 1), NULL, '0'::interval, 'Retorno desde Sao Paulo');

-- C. NUEVA RUTA COMERCIAL: SCL -> GRU -> EZE (Saturando el avión de la mañana)
-- Tramo 1: Comparte el avión de las 08:00 AM con destino al Hub de Brasil
INSERT INTO Itinerario_Vuelo (ID_ITINERARIO, ID_VUELO, ORDEN, TIEMPO_ESPERA, TIPO_CONEXION) VALUES
                                                                                                ((SELECT ID_ITINERARIO FROM Itinerario WHERE ORIGEN_AEROPUERTO = 1 AND DESTINO_AEROPUERTO = 7 ORDER BY ID_ITINERARIO DESC LIMIT 1),
                                                                                                (SELECT ID_VUELO FROM Segmento_Vuelo WHERE ID_AEROPUERTO_ORIGEN = 1 AND ID_AEROPUERTO_DESTINO = 3 ORDER BY ID_SEGMENTO DESC LIMIT 1), NULL, '3 hours 15 minutes'::interval, 'Conexión Cono Sur en Hub GRU'),

-- Tramo 2: Despega desde Brasil con el piloto '11223344-1' hacia Buenos Aires (EZE)
((SELECT ID_ITINERARIO FROM Itinerario WHERE ORIGEN_AEROPUERTO = 1 AND DESTINO_AEROPUERTO = 7 ORDER BY ID_ITINERARIO DESC LIMIT 1),
 (SELECT ID_VUELO FROM Segmento_Vuelo WHERE ID_AEROPUERTO_ORIGEN = 3 AND ID_AEROPUERTO_DESTINO = 7 ORDER BY ID_SEGMENTO DESC LIMIT 1), NULL, '0'::interval, 'Tramo final a Buenos Aires');