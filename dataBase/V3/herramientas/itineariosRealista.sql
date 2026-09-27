-- ============================================================
-- RED DE ITINERARIOS REALISTAS - CONO SUR & ATLÁNTICO
-- Solo usando pilotos y tripulación ya existentes en la BD
--
-- PILOTOS DISPONIBLES:
--   12345678-9  Juan Pérez        (9 años, comercial)
--   87654321-0  María Gómez       (8 años, comercial)
--   11223344-1  Diego Fernández   (7 años, comercial)
--   55667788-2  Sofía Romero      (12 años, comercial)
--   98765432-1  Andrés Martínez   (10 años, comercial)
--   15678342-K  Carlos Villalobos (15 años, intl.)
--   16345892-1  Patricia Alarcón  (11 años, nacional)
--   12890453-7  Mauricio Ortega   (20 años, transoceánico)
--   17556128-4  Elena Barros      (6 años, regional)
--   14223987-2  Ricardo Sanhueza  (14 años, intl.)
--
-- AEROLÍNEA ID = 1
-- ESTADO VUELO ID = 1 (Programado)
--
-- FILOSOFÍA LOAD FACTOR:
--   Cada vuelo físico transporta pasajeros de varios itinerarios.
--   SCL→GRU lleva simultáneamente: pasajeros SCL→GRU (se quedan),
--   pasajeros SCL→JFK (siguen a Nueva York), pasajeros SCL→MAD
--   (siguen a Madrid), pasajeros SCL→EZE (siguen a Buenos Aires).
-- ============================================================


-- ============================================================
-- 1. AEROLINEA (si no existe aún)
-- ============================================================
/*INSERT INTO Aerolinea (ID_AEROLINEA, Nombre, Codigo)
VALUES (1, 'LAN Airlines', 'LA')
    ON CONFLICT (ID_AEROLINEA) DO NOTHING;


-- ============================================================
-- 2. ESTADO DE VUELO (si no existe aún)
-- ============================================================
INSERT INTO Estado_Vuelo (ID_ESTADO_VUELO, Descripcion, Estado)
VALUES (1, 'Vuelo programado sin novedades', 'Programado')
    ON CONFLICT (ID_ESTADO_VUELO) DO NOTHING;


-- ============================================================
-- 3. CLASE DE ASIENTOS (si no existen aún)
-- ============================================================
INSERT INTO Clase_asiento (ID_CLASE, Descripcion) VALUES
                                                      (1, 'Económica'),
                                                      (2, 'Ejecutiva'),
                                                      (3, 'Primera Clase')
    ON CONFLICT (ID_CLASE) DO NOTHING;*/


-- ============================================================
-- 4. VUELOS FÍSICOS
--
-- SEMANA OPERATIVA: Día D = NOW() + N días
--
-- CORREDOR A — CONO SUR (Hub GRU)
--   A1: SCL → GRU   (alimentador de todo)      Piloto: 12345678-9
--   A2: GRU → EZE   (regional SurAmérica)       Piloto: 17556128-4
--   A3: GRU → LIM   (regional andino)           Piloto: 17556128-4
--   A4: GRU → BOG   (regional norte)            Piloto: 16345892-1
--   A5: GRU → JFK   (long-haul transoceánico)   Piloto: 12890453-7
--   A6: GRU → MAD   (transoceánico Europa)      Piloto: 15678342-K
--
-- RETORNOS
--   R1: JFK → GRU   (retorno USA)               Piloto: 55667788-2
--   R2: MAD → GRU   (retorno Europa)            Piloto: 14223987-2
--   R3: EZE → GRU   (retorno Argentina)         Piloto: 87654321-0
--   R4: LIM → GRU   (retorno Perú)              Piloto: 11223344-1
--   R5: BOG → GRU   (retorno Colombia)          Piloto: 98765432-1
--   R6: GRU → SCL   (cierre de ciclo hub)       Piloto: 12345678-9
-- ============================================================

-- Referencia de aviones (los del INSERT original):
--   ID_AVION 1 = DEF456 A320    (185 pax)
--   ID_AVION 2 = GHI789 B747    (380 pax)
--   ID_AVION 3 = JKL012 A350    (310 pax)
--   ID_AVION 4 = MNO345 B787    (220 pax)
--   ID_AVION 5 = PQR678 E195    (120 pax)
--   ID_AVION 6 = XYZ123 B777    (450 pax)
--   ID_AVION 7 = LMN987 A380    (650 pax)
--   ID_AVION 8 = STU456 A330    (250 pax)
--   ID_AVION 9 = WXY543 B757    (190 pax)

INSERT INTO Vuelo (Numero_Vuelo, Fecha_Hora_Salida, Fecha_Hora_Llegada,
                   ID_ESTADO_VUELO, ID_AVION, RUT_PILOTO, ID_AEROLINEA)
VALUES

-- CORREDOR A — VUELOS DE IDA
-- A1: SCL → GRU  (A350, tronco de toda la operación, carga 4 tipos de pasajeros)
('LA501',
 DATE_TRUNC('day', NOW()) + INTERVAL '3 days 07:00',
 DATE_TRUNC('day', NOW()) + INTERVAL '3 days 11:30',
 1, 3, '12345678-9', 1),

-- A2: GRU → EZE  (B757 regional)
('LA502',
 DATE_TRUNC('day', NOW()) + INTERVAL '3 days 14:00',
 DATE_TRUNC('day', NOW()) + INTERVAL '3 days 17:15',
 1, 9, '17556128-4', 1),

-- A3: GRU → LIM  (E195 regional andino)
('LA503',
 DATE_TRUNC('day', NOW()) + INTERVAL '3 days 13:30',
 DATE_TRUNC('day', NOW()) + INTERVAL '3 days 17:00',
 1, 5, '17556128-4', 1),

-- A4: GRU → BOG  (A320 regional norte, sale después de EZE)
('LA504',
 DATE_TRUNC('day', NOW()) + INTERVAL '3 days 14:30',
 DATE_TRUNC('day', NOW()) + INTERVAL '3 days 18:00',
 1, 1, '16345892-1', 1),

-- A5: GRU → JFK  (B777 long-haul, sale nocturno)
('LA505',
 DATE_TRUNC('day', NOW()) + INTERVAL '3 days 21:00',
 DATE_TRUNC('day', NOW()) + INTERVAL '4 days 08:30',
 1, 6, '12890453-7', 1),

-- A6: GRU → MAD  (A380 transoceánico, sale a medianoche)
('LA506',
 DATE_TRUNC('day', NOW()) + INTERVAL '3 days 23:45',
 DATE_TRUNC('day', NOW()) + INTERVAL '4 days 15:00',
 1, 7, '15678342-K', 1),

-- A7: LIM → BOG  (E195, el mismo avión que llega de GRU→LIM sigue a BOG)
('LA507',
 DATE_TRUNC('day', NOW()) + INTERVAL '3 days 18:30',
 DATE_TRUNC('day', NOW()) + INTERVAL '3 days 20:45',
 1, 5, '17556128-4', 1),

-- A8: BOG → MIA  (A320, el mismo avión GRU→BOG continúa a Miami)
('LA508',
 DATE_TRUNC('day', NOW()) + INTERVAL '3 days 20:00',
 DATE_TRUNC('day', NOW()) + INTERVAL '3 days 23:30',
 1, 1, '16345892-1', 1),

-- A9: MAD → FCO  (A320 corto europeo, pasajeros que siguen a Roma)
('LA509',
 DATE_TRUNC('day', NOW()) + INTERVAL '4 days 17:00',
 DATE_TRUNC('day', NOW()) + INTERVAL '4 days 19:30',
 1, 1, '16345892-1', 1),

-- A10: MAD → LHR  (B757 corto europeo)
('LA510',
 DATE_TRUNC('day', NOW()) + INTERVAL '4 days 16:30',
 DATE_TRUNC('day', NOW()) + INTERVAL '4 days 18:15',
 1, 9, '87654321-0', 1),

-- CORREDOR A — VUELOS DE RETORNO
-- R1: JFK → GRU  (B777, retorno directo USA→Brasil)
('LA601',
 DATE_TRUNC('day', NOW()) + INTERVAL '7 days 22:00',
 DATE_TRUNC('day', NOW()) + INTERVAL '8 days 10:00',
 1, 6, '55667788-2', 1),

-- R2: MAD → GRU  (A380 retorno Europa→Brasil)
('LA602',
 DATE_TRUNC('day', NOW()) + INTERVAL '7 days 13:00',
 DATE_TRUNC('day', NOW()) + INTERVAL '8 days 04:30',
 1, 7, '14223987-2', 1),

-- R3: EZE → GRU  (B757 regional retorno)
('LA603',
 DATE_TRUNC('day', NOW()) + INTERVAL '7 days 10:00',
 DATE_TRUNC('day', NOW()) + INTERVAL '7 days 13:15',
 1, 9, '87654321-0', 1),

-- R4: LIM → GRU  (E195 retorno andino)
('LA604',
 DATE_TRUNC('day', NOW()) + INTERVAL '7 days 09:00',
 DATE_TRUNC('day', NOW()) + INTERVAL '7 days 12:30',
 1, 5, '11223344-1', 1),

-- R5: BOG → GRU  (A320 retorno norte)
('LA605',
 DATE_TRUNC('day', NOW()) + INTERVAL '7 days 11:00',
 DATE_TRUNC('day', NOW()) + INTERVAL '7 days 14:30',
 1, 1, '98765432-1', 1),

-- R6: GRU → SCL  (A350 cierre de ciclo, misma aeronave que abrió el corredor)
('LA606',
 DATE_TRUNC('day', NOW()) + INTERVAL '8 days 06:00',
 DATE_TRUNC('day', NOW()) + INTERVAL '8 days 10:30',
 1, 3, '12345678-9', 1),

-- R7: FCO → MAD  (retorno Roma-Madrid)
('LA607',
 DATE_TRUNC('day', NOW()) + INTERVAL '7 days 09:00',
 DATE_TRUNC('day', NOW()) + INTERVAL '7 days 11:30',
 1, 1, '16345892-1', 1),

-- R8: LHR → MAD  (retorno Londres-Madrid)
('LA608',
 DATE_TRUNC('day', NOW()) + INTERVAL '7 days 08:00',
 DATE_TRUNC('day', NOW()) + INTERVAL '7 days 09:45',
 1, 9, '87654321-0', 1),

-- R9: MIA → BOG  (retorno Miami-Bogotá)
('LA609',
 DATE_TRUNC('day', NOW()) + INTERVAL '7 days 10:00',
 DATE_TRUNC('day', NOW()) + INTERVAL '7 days 13:30',
 1, 1, '98765432-1', 1),

-- R10: BOG → LIM  (retorno Colombia-Perú, el mismo E195)
('LA610',
 DATE_TRUNC('day', NOW()) + INTERVAL '7 days 15:00',
 DATE_TRUNC('day', NOW()) + INTERVAL '7 days 17:15',
 1, 5, '11223344-1', 1);


-- ============================================================
-- 5. SEGMENTOS DE VUELO (tramos físicos)
-- ============================================================
-- IDs de aeropuertos usados:
--   1  = SCL (Santiago)        3  = GRU (São Paulo)
--   2  = MVD (Montevideo)      4  = JFK (Nueva York)
--   6  = LIM (Lima)            8  = BOG (Bogotá)
--   9  = MIA (Miami)          10  = MAD (Madrid)
--  13  = LHR (Londres)        15  = FCO (Roma)
--  23  = EZE (Buenos Aires)
-- ============================================================

INSERT INTO Segmento_Vuelo (ID_VUELO, ORDEN_SEGMENTO,
                            ID_AEROPUERTO_ORIGEN, ID_AEROPUERTO_DESTINO,
                            HORA_SALIDA, HORA_LLEGADA, DURACION_ESTIMADA)
SELECT v.ID_VUELO, s.orden, s.orig, s.dest, s.sal, s.lleg, s.dur
FROM (VALUES

          -- CORREDOR A IDA
          ('LA501', 1,  1,  3,
           DATE_TRUNC('day',NOW())+INTERVAL '3 days 07:00',
           DATE_TRUNC('day',NOW())+INTERVAL '3 days 11:30',
           INTERVAL '4h 30m'),

          ('LA502', 1,  3,  23,
           DATE_TRUNC('day',NOW())+INTERVAL '3 days 14:00',
           DATE_TRUNC('day',NOW())+INTERVAL '3 days 17:15',
           INTERVAL '3h 15m'),

          ('LA503', 1,  3,  6,
           DATE_TRUNC('day',NOW())+INTERVAL '3 days 13:30',
           DATE_TRUNC('day',NOW())+INTERVAL '3 days 17:00',
           INTERVAL '3h 30m'),

          ('LA504', 1,  3,  8,
           DATE_TRUNC('day',NOW())+INTERVAL '3 days 14:30',
           DATE_TRUNC('day',NOW())+INTERVAL '3 days 18:00',
           INTERVAL '3h 30m'),

          ('LA505', 1,  3,  4,
           DATE_TRUNC('day',NOW())+INTERVAL '3 days 21:00',
           DATE_TRUNC('day',NOW())+INTERVAL '4 days 08:30',
           INTERVAL '11h 30m'),

          ('LA506', 1,  3,  10,
           DATE_TRUNC('day',NOW())+INTERVAL '3 days 23:45',
           DATE_TRUNC('day',NOW())+INTERVAL '4 days 15:00',
           INTERVAL '15h 15m'),

          ('LA507', 1,  6,  8,
           DATE_TRUNC('day',NOW())+INTERVAL '3 days 18:30',
           DATE_TRUNC('day',NOW())+INTERVAL '3 days 20:45',
           INTERVAL '2h 15m'),

          ('LA508', 1,  8,  9,
           DATE_TRUNC('day',NOW())+INTERVAL '3 days 20:00',
           DATE_TRUNC('day',NOW())+INTERVAL '3 days 23:30',
           INTERVAL '3h 30m'),

          ('LA509', 1, 10, 15,
           DATE_TRUNC('day',NOW())+INTERVAL '4 days 17:00',
           DATE_TRUNC('day',NOW())+INTERVAL '4 days 19:30',
           INTERVAL '2h 30m'),

          ('LA510', 1, 10, 13,
           DATE_TRUNC('day',NOW())+INTERVAL '4 days 16:30',
           DATE_TRUNC('day',NOW())+INTERVAL '4 days 18:15',
           INTERVAL '1h 45m'),

          -- RETORNOS
          ('LA601', 1,  4,  3,
           DATE_TRUNC('day',NOW())+INTERVAL '7 days 22:00',
           DATE_TRUNC('day',NOW())+INTERVAL '8 days 10:00',
           INTERVAL '12h 00m'),

          ('LA602', 1, 10,  3,
           DATE_TRUNC('day',NOW())+INTERVAL '7 days 13:00',
           DATE_TRUNC('day',NOW())+INTERVAL '8 days 04:30',
           INTERVAL '15h 30m'),

          ('LA603', 1, 23,  3,
           DATE_TRUNC('day',NOW())+INTERVAL '7 days 10:00',
           DATE_TRUNC('day',NOW())+INTERVAL '7 days 13:15',
           INTERVAL '3h 15m'),

          ('LA604', 1,  6,  3,
           DATE_TRUNC('day',NOW())+INTERVAL '7 days 09:00',
           DATE_TRUNC('day',NOW())+INTERVAL '7 days 12:30',
           INTERVAL '3h 30m'),

          ('LA605', 1,  8,  3,
           DATE_TRUNC('day',NOW())+INTERVAL '7 days 11:00',
           DATE_TRUNC('day',NOW())+INTERVAL '7 days 14:30',
           INTERVAL '3h 30m'),

          ('LA606', 1,  3,  1,
           DATE_TRUNC('day',NOW())+INTERVAL '8 days 06:00',
           DATE_TRUNC('day',NOW())+INTERVAL '8 days 10:30',
           INTERVAL '4h 30m'),

          ('LA607', 1, 15, 10,
           DATE_TRUNC('day',NOW())+INTERVAL '7 days 09:00',
           DATE_TRUNC('day',NOW())+INTERVAL '7 days 11:30',
           INTERVAL '2h 30m'),

          ('LA608', 1, 13, 10,
           DATE_TRUNC('day',NOW())+INTERVAL '7 days 08:00',
           DATE_TRUNC('day',NOW())+INTERVAL '7 days 09:45',
           INTERVAL '1h 45m'),

          ('LA609', 1,  9,  8,
           DATE_TRUNC('day',NOW())+INTERVAL '7 days 10:00',
           DATE_TRUNC('day',NOW())+INTERVAL '7 days 13:30',
           INTERVAL '3h 30m'),

          ('LA610', 1,  8,  6,
           DATE_TRUNC('day',NOW())+INTERVAL '7 days 15:00',
           DATE_TRUNC('day',NOW())+INTERVAL '7 days 17:15',
           INTERVAL '2h 15m')

     ) AS s(vuelo, orden, orig, dest, sal, lleg, dur)
         JOIN Vuelo v ON v.Numero_Vuelo = s.vuelo;


-- ============================================================
-- 6. ITINERARIOS COMERCIALES
-- 20 productos de venta sobre la misma red de vuelos
-- ============================================================
INSERT INTO Itinerario (ORIGEN_AEROPUERTO, DESTINO_AEROPUERTO,
                        HORA_SALIDA, HORA_LLEGADA,
                        DURACION_TOTAL, NUMERO_ESCALAS, Precio_Base)
VALUES

-- ── IDA: DESDE SANTIAGO ─────────────────────────────────────────────────────

-- P01: SCL → GRU  directo (pasajero termina en Brasil)
(1,  3,
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 07:00',
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 11:30',
 INTERVAL '4h 30m', 0, 310000),

-- P02: SCL → EZE  (1 escala GRU, comparte LA501 + LA502)
(1, 23,
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 07:00',
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 17:15',
 INTERVAL '10h 15m', 1, 450000),

-- P03: SCL → LIM  (1 escala GRU, comparte LA501 + LA503)
(1,  6,
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 07:00',
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 17:00',
 INTERVAL '10h 00m', 1, 380000),

-- P04: SCL → BOG  (1 escala GRU, comparte LA501 + LA504)
(1,  8,
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 07:00',
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 18:00',
 INTERVAL '11h 00m', 1, 420000),

-- P05: SCL → JFK  (1 escala GRU, comparte LA501 + LA505)
(1,  4,
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 07:00',
 DATE_TRUNC('day',NOW())+INTERVAL '4 days 08:30',
 INTERVAL '25h 30m', 1, 890000),

-- P06: SCL → MAD  (1 escala GRU, comparte LA501 + LA506)
(1, 10,
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 07:00',
 DATE_TRUNC('day',NOW())+INTERVAL '4 days 15:00',
 INTERVAL '32h 00m', 1, 1050000),

-- P07: SCL → MIA  (2 escalas GRU+BOG, comparte LA501+LA504+LA508)
(1,  9,
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 07:00',
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 23:30',
 INTERVAL '16h 30m', 2, 680000),

-- P08: SCL → FCO  (2 escalas GRU+MAD, comparte LA501+LA506+LA509)
(1, 15,
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 07:00',
 DATE_TRUNC('day',NOW())+INTERVAL '4 days 19:30',
 INTERVAL '36h 30m', 2, 1180000),

-- P09: SCL → LHR  (2 escalas GRU+MAD, comparte LA501+LA506+LA510)
(1, 13,
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 07:00',
 DATE_TRUNC('day',NOW())+INTERVAL '4 days 18:15',
 INTERVAL '35h 15m', 2, 1150000),

-- P10: SCL → BOG vía LIM  (2 escalas GRU+LIM, comparte LA501+LA503+LA507)
(1,  8,
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 07:00',
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 20:45',
 INTERVAL '13h 45m', 2, 460000),

-- ── DESDE PUNTOS INTERMEDIOS ────────────────────────────────────────────────

-- P11: GRU → JFK  directo nocturno (LA505)
(3,  4,
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 21:00',
 DATE_TRUNC('day',NOW())+INTERVAL '4 days 08:30',
 INTERVAL '11h 30m', 0, 620000),

-- P12: GRU → MAD  directo nocturno (LA506)
(3, 10,
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 23:45',
 DATE_TRUNC('day',NOW())+INTERVAL '4 days 15:00',
 INTERVAL '15h 15m', 0, 750000),

-- P13: GRU → EZE  regional (LA502)
(3, 23,
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 14:00',
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 17:15',
 INTERVAL '3h 15m', 0, 180000),

-- P14: GRU → LIM  regional (LA503)
(3,  6,
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 13:30',
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 17:00',
 INTERVAL '3h 30m', 0, 190000),

-- P15: GRU → BOG  regional (LA504)
(3,  8,
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 14:30',
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 18:00',
 INTERVAL '3h 30m', 0, 200000),

-- P16: LIM → BOG  (LA507, corto andino)
(6,  8,
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 18:30',
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 20:45',
 INTERVAL '2h 15m', 0, 150000),

-- P17: BOG → MIA  (LA508)
(8,  9,
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 20:00',
 DATE_TRUNC('day',NOW())+INTERVAL '3 days 23:30',
 INTERVAL '3h 30m', 0, 310000),

-- ── RETORNOS ────────────────────────────────────────────────────────────────

-- P18: JFK → SCL  (1 escala GRU, comparte LA601 + LA606)
(4,  1,
 DATE_TRUNC('day',NOW())+INTERVAL '7 days 22:00',
 DATE_TRUNC('day',NOW())+INTERVAL '8 days 10:30',
 INTERVAL '36h 30m', 1, 890000),

-- P19: MAD → SCL  (1 escala GRU, comparte LA602 + LA606)
(10, 1,
 DATE_TRUNC('day',NOW())+INTERVAL '7 days 13:00',
 DATE_TRUNC('day',NOW())+INTERVAL '8 days 10:30',
 INTERVAL '45h 30m', 1, 1050000),

-- P20: EZE → SCL  (1 escala GRU, comparte LA603 + LA606)
(23, 1,
 DATE_TRUNC('day',NOW())+INTERVAL '7 days 10:00',
 DATE_TRUNC('day',NOW())+INTERVAL '8 days 10:30',
 INTERVAL '48h 30m', 1, 450000),

-- P21: LIM → SCL  (1 escala GRU, comparte LA604 + LA606)
(6,  1,
 DATE_TRUNC('day',NOW())+INTERVAL '7 days 09:00',
 DATE_TRUNC('day',NOW())+INTERVAL '8 days 10:30',
 INTERVAL '49h 30m', 1, 380000),

-- P22: BOG → SCL  (1 escala GRU, comparte LA605 + LA606)
(8,  1,
 DATE_TRUNC('day',NOW())+INTERVAL '7 days 11:00',
 DATE_TRUNC('day',NOW())+INTERVAL '8 days 10:30',
 INTERVAL '47h 30m', 1, 420000),

-- P23: LHR → SCL  (2 escalas MAD+GRU, comparte LA608+LA602+LA606)
(13, 1,
 DATE_TRUNC('day',NOW())+INTERVAL '7 days 08:00',
 DATE_TRUNC('day',NOW())+INTERVAL '8 days 10:30',
 INTERVAL '50h 30m', 2, 1150000),

-- P24: FCO → SCL  (2 escalas MAD+GRU, comparte LA607+LA602+LA606)
(15, 1,
 DATE_TRUNC('day',NOW())+INTERVAL '7 days 09:00',
 DATE_TRUNC('day',NOW())+INTERVAL '8 days 10:30',
 INTERVAL '49h 30m', 2, 1180000),

-- P25: MIA → SCL  (2 escalas BOG+GRU, comparte LA609+LA605+LA606)
(9,  1,
 DATE_TRUNC('day',NOW())+INTERVAL '7 days 10:00',
 DATE_TRUNC('day',NOW())+INTERVAL '8 days 10:30',
 INTERVAL '48h 30m', 2, 680000),

-- P26: GRU → SCL  retorno directo (LA606)
(3,  1,
 DATE_TRUNC('day',NOW())+INTERVAL '8 days 06:00',
 DATE_TRUNC('day',NOW())+INTERVAL '8 days 10:30',
 INTERVAL '4h 30m', 0, 310000);


-- ============================================================
-- 7. ITINERARIO_VUELO
-- Mapeo exacto: qué vuelos físicos componen cada itinerario
-- Aquí está el núcleo del load factor compartido
-- ============================================================

INSERT INTO Itinerario_Vuelo (ID_ITINERARIO, ID_VUELO, ORDEN, TIEMPO_ESPERA, TIPO_CONEXION)

-- P01: SCL → GRU  (solo LA501)
SELECT i.ID_ITINERARIO, v.ID_VUELO, 1, INTERVAL '0', 'Vuelo directo'
FROM (SELECT ID_ITINERARIO FROM Itinerario
    WHERE ORIGEN_AEROPUERTO=1 AND DESTINO_AEROPUERTO=3 AND NUMERO_ESCALAS=0
    ORDER BY ID_ITINERARIO DESC LIMIT 1) i
    CROSS JOIN (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo='LA501') v

UNION ALL

-- P02: SCL → EZE  (LA501 → LA502)
SELECT i.ID_ITINERARIO, v.ID_VUELO, r.orden, r.espera::interval, r.tipo
FROM (SELECT ID_ITINERARIO FROM Itinerario
      WHERE ORIGEN_AEROPUERTO=1 AND DESTINO_AEROPUERTO=23
      ORDER BY ID_ITINERARIO DESC LIMIT 1) i
         CROSS JOIN (VALUES ('LA501',1,'0','SCL→GRU tramo alimentador'),
                            ('LA502',2,'2h 30m','Conexión en São Paulo → Buenos Aires')) r(vn,orden,espera,tipo)
         JOIN Vuelo v ON v.Numero_Vuelo = r.vn

UNION ALL

-- P03: SCL → LIM  (LA501 → LA503)
SELECT i.ID_ITINERARIO, v.ID_VUELO, r.orden, r.espera::interval, r.tipo
FROM (SELECT ID_ITINERARIO FROM Itinerario
      WHERE ORIGEN_AEROPUERTO=1 AND DESTINO_AEROPUERTO=6 AND NUMERO_ESCALAS=1
      ORDER BY ID_ITINERARIO DESC LIMIT 1) i
         CROSS JOIN (VALUES ('LA501',1,'0','SCL→GRU tramo alimentador'),
                            ('LA503',2,'2h 00m','Conexión en São Paulo → Lima')) r(vn,orden,espera,tipo)
         JOIN Vuelo v ON v.Numero_Vuelo = r.vn

UNION ALL

-- P04: SCL → BOG  (LA501 → LA504)
SELECT i.ID_ITINERARIO, v.ID_VUELO, r.orden, r.espera::interval, r.tipo
FROM (SELECT ID_ITINERARIO FROM Itinerario
      WHERE ORIGEN_AEROPUERTO=1 AND DESTINO_AEROPUERTO=8 AND NUMERO_ESCALAS=1
      ORDER BY ID_ITINERARIO DESC LIMIT 1) i
         CROSS JOIN (VALUES ('LA501',1,'0','SCL→GRU tramo alimentador'),
                            ('LA504',2,'3h 00m','Conexión en São Paulo → Bogotá')) r(vn,orden,espera,tipo)
         JOIN Vuelo v ON v.Numero_Vuelo = r.vn

UNION ALL

-- P05: SCL → JFK  (LA501 → LA505)  ← el itinerario estrella
SELECT i.ID_ITINERARIO, v.ID_VUELO, r.orden, r.espera::interval, r.tipo
FROM (SELECT ID_ITINERARIO FROM Itinerario
      WHERE ORIGEN_AEROPUERTO=1 AND DESTINO_AEROPUERTO=4 AND NUMERO_ESCALAS=1
      ORDER BY ID_ITINERARIO DESC LIMIT 1) i
         CROSS JOIN (VALUES ('LA501',1,'0','SCL→GRU tramo alimentador matutino'),
                            ('LA505',2,'9h 30m','Conexión nocturna en São Paulo → Nueva York')) r(vn,orden,espera,tipo)
         JOIN Vuelo v ON v.Numero_Vuelo = r.vn

UNION ALL

-- P06: SCL → MAD  (LA501 → LA506)
SELECT i.ID_ITINERARIO, v.ID_VUELO, r.orden, r.espera::interval, r.tipo
FROM (SELECT ID_ITINERARIO FROM Itinerario
      WHERE ORIGEN_AEROPUERTO=1 AND DESTINO_AEROPUERTO=10 AND NUMERO_ESCALAS=1
      ORDER BY ID_ITINERARIO DESC LIMIT 1) i
         CROSS JOIN (VALUES ('LA501',1,'0','SCL→GRU tramo alimentador matutino'),
                            ('LA506',2,'12h 15m','Conexión nocturna en São Paulo → Madrid')) r(vn,orden,espera,tipo)
         JOIN Vuelo v ON v.Numero_Vuelo = r.vn

UNION ALL

-- P07: SCL → MIA  (LA501 → LA504 → LA508)
SELECT i.ID_ITINERARIO, v.ID_VUELO, r.orden, r.espera::interval, r.tipo
FROM (SELECT ID_ITINERARIO FROM Itinerario
      WHERE ORIGEN_AEROPUERTO=1 AND DESTINO_AEROPUERTO=9
      ORDER BY ID_ITINERARIO DESC LIMIT 1) i
         CROSS JOIN (VALUES ('LA501',1,'0','SCL→GRU tramo alimentador'),
                            ('LA504',2,'3h 00m','Conexión en São Paulo → Bogotá'),
                            ('LA508',3,'2h 00m','Conexión en Bogotá → Miami')) r(vn,orden,espera,tipo)
         JOIN Vuelo v ON v.Numero_Vuelo = r.vn

UNION ALL

-- P08: SCL → FCO  (LA501 → LA506 → LA509)
SELECT i.ID_ITINERARIO, v.ID_VUELO, r.orden, r.espera::interval, r.tipo
FROM (SELECT ID_ITINERARIO FROM Itinerario
      WHERE ORIGEN_AEROPUERTO=1 AND DESTINO_AEROPUERTO=15
      ORDER BY ID_ITINERARIO DESC LIMIT 1) i
         CROSS JOIN (VALUES ('LA501',1,'0','SCL→GRU alimentador'),
                            ('LA506',2,'12h 15m','Conexión nocturna GRU→MAD'),
                            ('LA509',3,'2h 00m','Conexión en Madrid → Roma')) r(vn,orden,espera,tipo)
         JOIN Vuelo v ON v.Numero_Vuelo = r.vn

UNION ALL

-- P09: SCL → LHR  (LA501 → LA506 → LA510)
SELECT i.ID_ITINERARIO, v.ID_VUELO, r.orden, r.espera::interval, r.tipo
FROM (SELECT ID_ITINERARIO FROM Itinerario
      WHERE ORIGEN_AEROPUERTO=1 AND DESTINO_AEROPUERTO=13
      ORDER BY ID_ITINERARIO DESC LIMIT 1) i
         CROSS JOIN (VALUES ('LA501',1,'0','SCL→GRU alimentador'),
                            ('LA506',2,'12h 15m','Conexión nocturna GRU→MAD'),
                            ('LA510',3,'1h 30m','Conexión en Madrid → Londres')) r(vn,orden,espera,tipo)
         JOIN Vuelo v ON v.Numero_Vuelo = r.vn

UNION ALL

-- P10: SCL → BOG vía LIM  (LA501 → LA503 → LA507)
SELECT i.ID_ITINERARIO, v.ID_VUELO, r.orden, r.espera::interval, r.tipo
FROM (SELECT ID_ITINERARIO FROM Itinerario
      WHERE ORIGEN_AEROPUERTO=1 AND DESTINO_AEROPUERTO=8 AND NUMERO_ESCALAS=2
      ORDER BY ID_ITINERARIO DESC LIMIT 1) i
         CROSS JOIN (VALUES ('LA501',1,'0','SCL→GRU alimentador'),
                            ('LA503',2,'2h 00m','Conexión GRU → Lima'),
                            ('LA507',3,'1h 30m','Conexión Lima → Bogotá')) r(vn,orden,espera,tipo)
         JOIN Vuelo v ON v.Numero_Vuelo = r.vn

UNION ALL

-- P11: GRU → JFK  (solo LA505)
SELECT i.ID_ITINERARIO, v.ID_VUELO, 1, INTERVAL '0', 'Vuelo directo nocturno'
FROM (SELECT ID_ITINERARIO FROM Itinerario
    WHERE ORIGEN_AEROPUERTO=3 AND DESTINO_AEROPUERTO=4
    ORDER BY ID_ITINERARIO DESC LIMIT 1) i
    CROSS JOIN (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo='LA505') v

UNION ALL

-- P12: GRU → MAD  (solo LA506)
SELECT i.ID_ITINERARIO, v.ID_VUELO, 1, INTERVAL '0', 'Vuelo directo nocturno'
FROM (SELECT ID_ITINERARIO FROM Itinerario
    WHERE ORIGEN_AEROPUERTO=3 AND DESTINO_AEROPUERTO=10
    ORDER BY ID_ITINERARIO DESC LIMIT 1) i
    CROSS JOIN (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo='LA506') v

UNION ALL

-- P13: GRU → EZE  (solo LA502)
SELECT i.ID_ITINERARIO, v.ID_VUELO, 1, INTERVAL '0', 'Vuelo regional directo'
FROM (SELECT ID_ITINERARIO FROM Itinerario
    WHERE ORIGEN_AEROPUERTO=3 AND DESTINO_AEROPUERTO=23
    ORDER BY ID_ITINERARIO DESC LIMIT 1) i
    CROSS JOIN (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo='LA502') v

UNION ALL

-- P14: GRU → LIM  (solo LA503)
SELECT i.ID_ITINERARIO, v.ID_VUELO, 1, INTERVAL '0', 'Vuelo regional directo'
FROM (SELECT ID_ITINERARIO FROM Itinerario
    WHERE ORIGEN_AEROPUERTO=3 AND DESTINO_AEROPUERTO=6
    ORDER BY ID_ITINERARIO DESC LIMIT 1) i
    CROSS JOIN (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo='LA503') v

UNION ALL

-- P15: GRU → BOG  (solo LA504)
SELECT i.ID_ITINERARIO, v.ID_VUELO, 1, INTERVAL '0', 'Vuelo regional directo'
FROM (SELECT ID_ITINERARIO FROM Itinerario
    WHERE ORIGEN_AEROPUERTO=3 AND DESTINO_AEROPUERTO=8
    ORDER BY ID_ITINERARIO DESC LIMIT 1) i
    CROSS JOIN (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo='LA504') v

UNION ALL

-- P16: LIM → BOG  (solo LA507)
SELECT i.ID_ITINERARIO, v.ID_VUELO, 1, INTERVAL '0', 'Vuelo regional directo'
FROM (SELECT ID_ITINERARIO FROM Itinerario
    WHERE ORIGEN_AEROPUERTO=6 AND DESTINO_AEROPUERTO=8
    ORDER BY ID_ITINERARIO DESC LIMIT 1) i
    CROSS JOIN (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo='LA507') v

UNION ALL

-- P17: BOG → MIA  (solo LA508)
SELECT i.ID_ITINERARIO, v.ID_VUELO, 1, INTERVAL '0', 'Vuelo regional directo'
FROM (SELECT ID_ITINERARIO FROM Itinerario
    WHERE ORIGEN_AEROPUERTO=8 AND DESTINO_AEROPUERTO=9
    ORDER BY ID_ITINERARIO DESC LIMIT 1) i
    CROSS JOIN (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo='LA508') v

UNION ALL

-- P18: JFK → SCL  (LA601 → LA606)
SELECT i.ID_ITINERARIO, v.ID_VUELO, r.orden, r.espera::interval, r.tipo
FROM (SELECT ID_ITINERARIO FROM Itinerario
      WHERE ORIGEN_AEROPUERTO=4 AND DESTINO_AEROPUERTO=1
      ORDER BY ID_ITINERARIO DESC LIMIT 1) i
         CROSS JOIN (VALUES ('LA601',1,'0','JFK→GRU vuelo nocturno'),
                            ('LA606',2,'20h 00m','Conexión en São Paulo → Santiago')) r(vn,orden,espera,tipo)
         JOIN Vuelo v ON v.Numero_Vuelo = r.vn

UNION ALL

-- P19: MAD → SCL  (LA602 → LA606)
SELECT i.ID_ITINERARIO, v.ID_VUELO, r.orden, r.espera::interval, r.tipo
FROM (SELECT ID_ITINERARIO FROM Itinerario
      WHERE ORIGEN_AEROPUERTO=10 AND DESTINO_AEROPUERTO=1
      ORDER BY ID_ITINERARIO DESC LIMIT 1) i
         CROSS JOIN (VALUES ('LA602',1,'0','MAD→GRU vuelo largo'),
                            ('LA606',2,'1h 30m','Conexión en São Paulo → Santiago')) r(vn,orden,espera,tipo)
         JOIN Vuelo v ON v.Numero_Vuelo = r.vn

UNION ALL

-- P20: EZE → SCL  (LA603 → LA606)
SELECT i.ID_ITINERARIO, v.ID_VUELO, r.orden, r.espera::interval, r.tipo
FROM (SELECT ID_ITINERARIO FROM Itinerario
      WHERE ORIGEN_AEROPUERTO=23 AND DESTINO_AEROPUERTO=1
      ORDER BY ID_ITINERARIO DESC LIMIT 1) i
         CROSS JOIN (VALUES ('LA603',1,'0','EZE→GRU regional'),
                            ('LA606',2,'16h 45m','Conexión en São Paulo → Santiago')) r(vn,orden,espera,tipo)
         JOIN Vuelo v ON v.Numero_Vuelo = r.vn

UNION ALL

-- P21: LIM → SCL  (LA604 → LA606)
SELECT i.ID_ITINERARIO, v.ID_VUELO, r.orden, r.espera::interval, r.tipo
FROM (SELECT ID_ITINERARIO FROM Itinerario
      WHERE ORIGEN_AEROPUERTO=6 AND DESTINO_AEROPUERTO=1
      ORDER BY ID_ITINERARIO DESC LIMIT 1) i
         CROSS JOIN (VALUES ('LA604',1,'0','LIM→GRU regional'),
                            ('LA606',2,'17h 30m','Conexión en São Paulo → Santiago')) r(vn,orden,espera,tipo)
         JOIN Vuelo v ON v.Numero_Vuelo = r.vn

UNION ALL

-- P22: BOG → SCL  (LA605 → LA606)
SELECT i.ID_ITINERARIO, v.ID_VUELO, r.orden, r.espera::interval, r.tipo
FROM (SELECT ID_ITINERARIO FROM Itinerario
      WHERE ORIGEN_AEROPUERTO=8 AND DESTINO_AEROPUERTO=1
      ORDER BY ID_ITINERARIO DESC LIMIT 1) i
         CROSS JOIN (VALUES ('LA605',1,'0','BOG→GRU regional'),
                            ('LA606',2,'15h 30m','Conexión en São Paulo → Santiago')) r(vn,orden,espera,tipo)
         JOIN Vuelo v ON v.Numero_Vuelo = r.vn

UNION ALL

-- P23: LHR → SCL  (LA608 → LA602 → LA606)
SELECT i.ID_ITINERARIO, v.ID_VUELO, r.orden, r.espera::interval, r.tipo
FROM (SELECT ID_ITINERARIO FROM Itinerario
      WHERE ORIGEN_AEROPUERTO=13 AND DESTINO_AEROPUERTO=1
      ORDER BY ID_ITINERARIO DESC LIMIT 1) i
         CROSS JOIN (VALUES ('LA608',1,'0','LHR→MAD corto europeo'),
                            ('LA602',2,'3h 15m','Conexión en Madrid → São Paulo'),
                            ('LA606',3,'1h 30m','Conexión en São Paulo → Santiago')) r(vn,orden,espera,tipo)
         JOIN Vuelo v ON v.Numero_Vuelo = r.vn

UNION ALL

-- P24: FCO → SCL  (LA607 → LA602 → LA606)
SELECT i.ID_ITINERARIO, v.ID_VUELO, r.orden, r.espera::interval, r.tipo
FROM (SELECT ID_ITINERARIO FROM Itinerario
      WHERE ORIGEN_AEROPUERTO=15 AND DESTINO_AEROPUERTO=1
      ORDER BY ID_ITINERARIO DESC LIMIT 1) i
         CROSS JOIN (VALUES ('LA607',1,'0','FCO→MAD corto europeo'),
                            ('LA602',2,'1h 30m','Conexión en Madrid → São Paulo'),
                            ('LA606',3,'1h 30m','Conexión en São Paulo → Santiago')) r(vn,orden,espera,tipo)
         JOIN Vuelo v ON v.Numero_Vuelo = r.vn

UNION ALL

-- P25: MIA → SCL  (LA609 → LA605 → LA606)
SELECT i.ID_ITINERARIO, v.ID_VUELO, r.orden, r.espera::interval, r.tipo
FROM (SELECT ID_ITINERARIO FROM Itinerario
      WHERE ORIGEN_AEROPUERTO=9 AND DESTINO_AEROPUERTO=1
      ORDER BY ID_ITINERARIO DESC LIMIT 1) i
         CROSS JOIN (VALUES ('LA609',1,'0','MIA→BOG'),
                            ('LA605',2,'1h 30m','Conexión en Bogotá → São Paulo'),
                            ('LA606',3,'15h 30m','Conexión en São Paulo → Santiago')) r(vn,orden,espera,tipo)
         JOIN Vuelo v ON v.Numero_Vuelo = r.vn

UNION ALL

-- P26: GRU → SCL  retorno directo (LA606)
SELECT i.ID_ITINERARIO, v.ID_VUELO, 1, INTERVAL '0', 'Vuelo directo de retorno'
FROM (SELECT ID_ITINERARIO FROM Itinerario
    WHERE ORIGEN_AEROPUERTO=3 AND DESTINO_AEROPUERTO=1
    ORDER BY ID_ITINERARIO DESC LIMIT 1) i
    CROSS JOIN (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo='LA606') v;


-- ============================================================
-- VERIFICACIÓN: load factor por vuelo
-- Cada vuelo debería aparecer en múltiples itinerarios
-- ============================================================
 SELECT
     v.Numero_Vuelo,
     COUNT(DISTINCT iv.ID_ITINERARIO) AS itinerarios_compartidos,
     sv_orig.Codigo_IATA             AS origen,
     sv_dest.Codigo_IATA             AS destino
 FROM Vuelo v
 JOIN Itinerario_Vuelo iv ON iv.ID_VUELO = v.ID_VUELO
 JOIN Segmento_Vuelo sg ON sg.ID_VUELO = v.ID_VUELO
 JOIN Aeropuerto sv_orig ON sg.ID_AEROPUERTO_ORIGEN  = sv_orig.ID_AEROPUERTO
 JOIN Aeropuerto sv_dest ON sg.ID_AEROPUERTO_DESTINO = sv_dest.ID_AEROPUERTO
 GROUP BY v.Numero_Vuelo, sv_orig.Codigo_IATA, sv_dest.Codigo_IATA
 ORDER BY itinerarios_compartidos DESC;
--
-- Resultado esperado:
--   LA501 (SCL→GRU)  → 10 itinerarios  ← el vuelo más cargado
--   LA506 (GRU→MAD)  → 4  itinerarios
--   LA602 (MAD→GRU)  → 4  itinerarios
--   LA606 (GRU→SCL)  → 9  itinerarios
--   LA505 (GRU→JFK)  → 2  itinerarios