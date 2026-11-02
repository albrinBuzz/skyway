-- =============================================================================
-- 06_poblado_operacional_simulado.sql
-- SIMULADOR OPERACIONAL DE VUELOS E ITINERARIOS - SKYWAY AIRLINES
-- =============================================================================
-- Requisitos previos: 01_esquema/tablas.sql, 02_logica/*.sql y 03_datos/poblado.sql.
--
-- Genera de forma DINÁMICA (relativa a LOCALTIMESTAMP) vuelos, segmentos,
-- precios por clase, puertas, turnos e itinerarios (directos y con conexión)
-- usando EXCLUSIVAMENTE los registros existentes del poblado:
--   * Aeropuertos y coordenadas PostGIS  -> distancias y tiempos de bloque reales.
--   * Puerta_Embarque                    -> asignación sin solapamiento (+/- 1 h).
--   * Avion / Modelo_Avion / Capacidad_Clase -> alcance, velocidad, rotación y clases.
--   * Piloto                             -> sin vuelos solapados + descanso mínimo.
--   * Aerolinea 'SW', Estado_Vuelo 'Vuelo Programado', Tipo_Turno 'Vuelo', Tarifa.
--
-- Reglas de realismo:
--   1. Continuidad física: cada avión sale desde donde aterrizó su vuelo anterior.
--   2. Rotación en tierra por tipo de avión (40-120 min) y toque de queda 00:00-05:59.
--   3. Destinos dentro del alcance del modelo; fuselaje ancho solo a aeropuertos con
--      >= 8 puertas (A380 >= 10) y trayectos >= 1.500 km.
--   4. Cobertura global: se priorizan los aeropuertos menos visitados, con retorno
--      probabilístico al hub (SCL) para mantener el modelo hub-and-spoke.
--   5. Conexiones: 1 h a 6 h de espera, sin volver al origen y con desvío
--      geográfico <= 60 % respecto de la ruta directa.
--
-- La función es INCREMENTAL e IDEMPOTENTE por horizonte: continúa desde el último
-- vuelo de cada avión/piloto, por lo que puede ejecutarse a diario (pg_cron o
-- @Scheduled en Spring) para mantener siempre N días de operación futura.
-- Los triggers de V3 completan Numero_Vuelo, fechas de Vuelo/Itinerario y duración.
-- =============================================================================

CREATE OR REPLACE FUNCTION fn_generar_operaciones_simuladas(
    p_dias_horizonte   INT      DEFAULT 90,
    p_hub_iata         VARCHAR  DEFAULT 'SCL',
    p_descanso_piloto  INTERVAL DEFAULT INTERVAL '1 hour 30 minutes',
    p_max_conexiones   INT      DEFAULT 2,
    p_prob_retorno_hub NUMERIC  DEFAULT 0.30
)
    RETURNS TABLE (
        vuelos_generados      INT,
        itinerarios_directos  INT,
        itinerarios_conexion  INT,
        aeropuertos_cubiertos INT,
        ventana_desde         TIMESTAMP,
        ventana_hasta         TIMESTAMP
    )
    LANGUAGE plpgsql
AS
$fn$
DECLARE
v_inicio        TIMESTAMP := date_trunc('hour', LOCALTIMESTAMP) + INTERVAL '3 hours';
    v_fin           TIMESTAMP := date_trunc('day', LOCALTIMESTAMP) + make_interval(days => p_dias_horizonte);
    v_hub           INT;
    v_aerolinea     INT;
    v_estado        INT;
    v_tipo_turno    INT;
    v_av            RECORD;
    v_pil           RECORD;
    v_par           RECORD;
    v_dest          INT;
    v_km            NUMERIC;
    v_bloque        INTERVAL;
    v_salida        TIMESTAMP;
    v_llegada       TIMESTAMP;
    v_internacional BOOLEAN;
    v_iata_o        VARCHAR;
    v_iata_d        VARCHAR;
    v_id_vuelo      INT;
    v_id_seg        INT;
    v_id_it         INT;
    v_puerta        INT;
    v_precio_eco    INT;
    v_cnt_vuelos    INT := 0;
    v_cnt_dir       INT := 0;
    v_cnt_con       INT := 0;
    v_cobertura     INT := 0;
BEGIN
    IF p_dias_horizonte IS NULL OR p_dias_horizonte < 1 THEN
        RAISE EXCEPTION 'p_dias_horizonte debe ser >= 1';
END IF;

    -- Evita ejecuciones concurrentes (pg_cron + ejecución manual, varias instancias, etc.)
    PERFORM pg_advisory_xact_lock(hashtext('skyway_generador_operaciones'));

    -- -------------------------------------------------------------------------
    -- 1. Catálogos existentes (sin claves inventadas)
    -- -------------------------------------------------------------------------
SELECT id_aeropuerto INTO v_hub FROM aeropuerto WHERE codigo_iata = p_hub_iata;
SELECT id_aerolinea INTO v_aerolinea FROM aerolinea WHERE codigo = 'SW' ORDER BY id_aerolinea LIMIT 1;
SELECT id_estado_vuelo INTO v_estado FROM estado_vuelo WHERE descripcion = 'Vuelo Programado' ORDER BY id_estado_vuelo LIMIT 1;
SELECT id_tipo INTO v_tipo_turno FROM tipo_turno WHERE LOWER(nombre) = 'vuelo' ORDER BY id_tipo LIMIT 1;

IF v_hub IS NULL THEN RAISE EXCEPTION 'Hub % no existe en Aeropuerto', p_hub_iata; END IF;
    IF v_aerolinea IS NULL THEN RAISE EXCEPTION 'No existe la aerolínea SW'; END IF;
    IF v_estado IS NULL THEN RAISE EXCEPTION 'No existe Estado_Vuelo "Vuelo Programado"'; END IF;
    IF NOT EXISTS (SELECT 1 FROM piloto) THEN RAISE EXCEPTION 'No hay pilotos registrados'; END IF;

DROP TABLE IF EXISTS tmp_dist, tmp_aerop, tmp_avion, tmp_piloto, tmp_nuevos, tmp_futuros, tmp_pares;

-- -------------------------------------------------------------------------
-- 2. Matriz de distancias geodésicas (PostGIS) y aeropuertos operables
-- -------------------------------------------------------------------------
CREATE TEMP TABLE tmp_dist ON COMMIT DROP AS
SELECT a.id_aeropuerto AS o,
       b.id_aeropuerto AS d,
       ROUND((ST_Distance(a.posicion, b.posicion) / 1000.0)::numeric, 1) AS km
FROM aeropuerto a
         CROSS JOIN aeropuerto b
WHERE a.id_aeropuerto <> b.id_aeropuerto;
CREATE INDEX ON tmp_dist (o, d);

CREATE TEMP TABLE tmp_aerop ON COMMIT DROP AS
SELECT a.id_aeropuerto,
       a.codigo_iata,
       c.id_pais,
       COUNT(p.id_puerta)::INT AS puertas,
    (SELECT COUNT(*) FROM segmento_vuelo s
     WHERE s.id_aeropuerto_destino = a.id_aeropuerto
       AND s.hora_llegada BETWEEN v_inicio AND v_fin)::INT AS visitas
FROM aeropuerto a
         LEFT JOIN ciudad c ON c.id_ciudad = a.id_ciudad
         LEFT JOIN puerta_embarque p ON p.id_aeropuerto = a.id_aeropuerto
GROUP BY a.id_aeropuerto, a.codigo_iata, c.id_pais;
CREATE INDEX ON tmp_aerop (id_aeropuerto);

-- -------------------------------------------------------------------------
-- 3. Flota operativa: parámetros por modelo y posición actual de cada avión
-- -------------------------------------------------------------------------
CREATE TEMP TABLE tmp_avion ON COMMIT DROP AS
SELECT av.id_avion,
       prm.alcance,
       prm.velocidad,
       prm.rotacion,
       prm.min_puertas,
       CASE WHEN prm.min_puertas > 1 THEN 1500 ELSE 0 END AS km_min,
       COALESCE(ult.destino, v_hub) AS ubicacion,
       GREATEST(v_inicio, COALESCE(ult.llegada + prm.rotacion, v_inicio)) AS disponible,
       TRUE AS activo
FROM avion av
         JOIN modelo_avion m ON m.id_modelo = av.id_modelo
         CROSS JOIN LATERAL (
    SELECT CASE
               WHEN m.nombre ILIKE '%E195%' THEN 4200
               WHEN m.nombre ILIKE '%737%'  THEN 5700
               WHEN m.nombre ILIKE '%A320%' THEN 6300
               WHEN m.nombre ILIKE '%A321%' THEN 6800
               WHEN m.nombre ILIKE '%757%'  THEN 7200
               WHEN m.nombre ILIKE '%A330%' THEN 11500
               WHEN m.nombre ILIKE '%747%'  THEN 13400
               WHEN m.nombre ILIKE '%777%'  THEN 13600
               WHEN m.nombre ILIKE '%787%'  THEN 14000
               WHEN m.nombre ILIKE '%A380%' THEN 14800
               WHEN m.nombre ILIKE '%A350%' THEN 15000
               ELSE 5000 END AS alcance,
           CASE
               WHEN m.nombre ILIKE '%E195%' THEN 780
               WHEN m.nombre ILIKE ANY (ARRAY['%A320%', '%A321%', '%737%', '%757%']) THEN 830
               ELSE 900 END AS velocidad,
           CASE
               WHEN m.nombre ILIKE '%E195%' THEN INTERVAL '40 minutes'
               WHEN m.nombre ILIKE ANY (ARRAY['%A320%', '%A321%', '%737%']) THEN INTERVAL '45 minutes'
               WHEN m.nombre ILIKE '%757%'  THEN INTERVAL '50 minutes'
               WHEN m.nombre ILIKE '%A380%' THEN INTERVAL '120 minutes'
               WHEN m.nombre ILIKE '%747%'  THEN INTERVAL '110 minutes'
               WHEN m.nombre ILIKE '%777%'  THEN INTERVAL '100 minutes'
               ELSE INTERVAL '90 minutes' END AS rotacion,
               CASE
                   WHEN m.nombre ILIKE '%A380%' THEN 10
                   WHEN m.nombre ILIKE ANY (ARRAY['%A330%', '%747%', '%777%', '%787%', '%A350%']) THEN 8
                   ELSE 1 END AS min_puertas
        ) prm
             LEFT JOIN LATERAL (
        SELECT s.id_aeropuerto_destino AS destino, s.hora_llegada AS llegada
        FROM vuelo v
                 JOIN segmento_vuelo s ON s.id_vuelo = v.id_vuelo
        WHERE v.id_avion = av.id_avion
          AND s.hora_llegada IS NOT NULL
        ORDER BY s.hora_llegada DESC
        LIMIT 1
        ) ult ON TRUE
    WHERE av.estado_de_mantenimiento ILIKE 'operativo'
      AND EXISTS (SELECT 1 FROM capacidad_clase cc WHERE cc.id_avion = av.id_avion);

    -- Pilotos: disponibles tras su último vuelo + descanso mínimo
    CREATE TEMP TABLE tmp_piloto ON COMMIT DROP AS
SELECT p.rut,
       GREATEST(v_inicio, COALESCE(MAX(v.fecha_hora_llegada) + p_descanso_piloto, v_inicio)) AS disponible,
       0::BIGINT AS minutos
FROM piloto p
         LEFT JOIN vuelo v ON v.rut_piloto = p.rut
GROUP BY p.rut;

CREATE TEMP TABLE tmp_nuevos (id_vuelo INT PRIMARY KEY, origen INT, destino INT) ON COMMIT DROP;

    -- -------------------------------------------------------------------------
    -- 4. Simulación por eventos: siempre se programa el avión que queda libre antes
    -- -------------------------------------------------------------------------
    LOOP
SELECT * INTO v_av FROM tmp_avion WHERE activo ORDER BY disponible, id_avion LIMIT 1;
EXIT WHEN NOT FOUND;

        IF v_av.disponible >= v_fin THEN
UPDATE tmp_avion SET activo = FALSE WHERE id_avion = v_av.id_avion;
CONTINUE;
END IF;

        -- 4.1 Destino: retorno al hub o aeropuerto menos visitado dentro del alcance
        v_dest := NULL;
        v_km := NULL;
        IF v_av.ubicacion <> v_hub AND random() < p_prob_retorno_hub THEN
SELECT d.d, d.km INTO v_dest, v_km
FROM tmp_dist d
         JOIN tmp_aerop ta ON ta.id_aeropuerto = d.d
WHERE d.o = v_av.ubicacion AND d.d = v_hub
  AND d.km <= v_av.alcance AND ta.puertas >= v_av.min_puertas;
END IF;

        IF v_dest IS NULL THEN
SELECT d.d, d.km INTO v_dest, v_km
FROM tmp_dist d
         JOIN tmp_aerop ta ON ta.id_aeropuerto = d.d
WHERE d.o = v_av.ubicacion
  AND d.km BETWEEN GREATEST(v_av.km_min, 150) AND v_av.alcance
  AND ta.puertas >= v_av.min_puertas
ORDER BY ta.visitas * 1500 + d.km * 0.15 + random() * 900
    LIMIT 1;
END IF;

        IF v_dest IS NULL THEN -- Respaldo: cualquier aeropuerto con puertas dentro del alcance
SELECT d.d, d.km INTO v_dest, v_km
FROM tmp_dist d
         JOIN tmp_aerop ta ON ta.id_aeropuerto = d.d
WHERE d.o = v_av.ubicacion AND d.km <= v_av.alcance AND ta.puertas > 0
ORDER BY ta.visitas, d.km
    LIMIT 1;
END IF;

        IF v_dest IS NULL THEN
UPDATE tmp_avion SET activo = FALSE WHERE id_avion = v_av.id_avion;
CONTINUE;
END IF;

        -- 4.2 Piloto libre (sin solapamiento) y horario de salida
SELECT * INTO v_pil
FROM tmp_piloto
ORDER BY GREATEST(disponible, v_av.disponible), minutos, rut
    LIMIT 1;

v_salida := GREATEST(v_av.disponible, v_pil.disponible);
        v_salida := date_trunc('hour', v_salida)
                        + make_interval(mins => (CEIL(EXTRACT(MINUTE FROM v_salida) / 5.0) * 5)::INT);
        IF EXTRACT(HOUR FROM v_salida) < 6 THEN -- Toque de queda nocturno
            v_salida := date_trunc('day', v_salida) + INTERVAL '6 hours';
END IF;

        IF v_salida >= v_fin THEN
UPDATE tmp_avion SET activo = FALSE WHERE id_avion = v_av.id_avion;
CONTINUE;
END IF;

        -- Tiempo de bloque: 30 min de rodaje/ascenso + crucero según velocidad del modelo
        v_bloque := make_interval(mins => (CEIL((30 + v_km / v_av.velocidad * 60) / 5.0) * 5)::INT);
        v_llegada := v_salida + v_bloque;

SELECT o.codigo_iata, d.codigo_iata, (o.id_pais IS DISTINCT FROM d.id_pais)
INTO v_iata_o, v_iata_d, v_internacional
FROM tmp_aerop o, tmp_aerop d
WHERE o.id_aeropuerto = v_av.ubicacion AND d.id_aeropuerto = v_dest;

-- 4.3 Vuelo + segmento (triggers: Numero_Vuelo, fechas del vuelo y turno)
INSERT INTO vuelo (fecha_hora_salida, fecha_hora_llegada, id_estado_vuelo, id_avion, rut_piloto, id_aerolinea)
VALUES (v_salida, v_llegada, v_estado, v_av.id_avion, v_pil.rut, v_aerolinea)
    RETURNING id_vuelo INTO v_id_vuelo;

INSERT INTO segmento_vuelo (id_vuelo, orden_segmento, id_aeropuerto_origen, id_aeropuerto_destino,
                            hora_salida, hora_llegada, duracion_estimada)
VALUES (v_id_vuelo, 1, v_av.ubicacion, v_dest, v_salida, v_llegada, v_bloque)
    RETURNING id_segmento INTO v_id_seg;

-- 4.4 Puerta libre en el origen (prioriza terminal internacional/doméstica)
SELECT p.id_puerta INTO v_puerta
FROM puerta_embarque p
WHERE p.id_aeropuerto = v_av.ubicacion
  AND NOT EXISTS (SELECT 1
                  FROM asignacion_puerta ap
                           JOIN segmento_vuelo sv ON sv.id_segmento = ap.id_segmento
                  WHERE ap.id_puerta = p.id_puerta
                    AND sv.hora_salida BETWEEN v_salida - INTERVAL '1 hour' AND v_salida + INTERVAL '1 hour')
ORDER BY CASE WHEN (p.terminal ILIKE '%internacional%') = v_internacional THEN 0 ELSE 1 END, p.id_puerta
    LIMIT 1;

IF v_puerta IS NOT NULL THEN
            INSERT INTO asignacion_puerta (id_segmento, id_puerta, hora_asignacion)
            VALUES (v_id_seg, v_puerta, LOCALTIMESTAMP);
END IF;

        -- 4.5 Precio por clase (solo clases configuradas en el avión)
        v_precio_eco := (ROUND((15000 + v_km * 4) / 100) * 100)::INT;
INSERT INTO precio_asiento (precio, id_vuelo, id_clase)
SELECT DISTINCT
    CASE
        WHEN ca.descripcion ILIKE 'Primera%'   THEN (ROUND(v_precio_eco * 3.8 / 100) * 100)::INT
                WHEN ca.descripcion ILIKE 'Ejecutiva%' THEN (ROUND(v_precio_eco * 2.4 / 100) * 100)::INT
                ELSE v_precio_eco END,
            v_id_vuelo,
            cc.id_clase
        FROM capacidad_clase cc
                 JOIN clase_asiento ca ON ca.id_clase = cc.id_clase
        WHERE cc.id_avion = v_av.id_avion
        ON CONFLICT ON CONSTRAINT unique_precio_vuelo_clase DO NOTHING;

        -- 4.6 Turno de vuelo (la dotación se asigna desde el módulo de turnos)
        IF v_tipo_turno IS NOT NULL THEN
            INSERT INTO turno (id_vuelo, fecha, hora_inicio, hora_fin, id_tipo_turno)
            VALUES (v_id_vuelo, v_salida::DATE, v_salida, v_llegada, v_tipo_turno);
END IF;

        -- 4.7 Itinerario comercial directo + tarifas
INSERT INTO itinerario (fecha_creacion, origen_aeropuerto, destino_aeropuerto, hora_salida, hora_llegada,
                        duracion_total, numero_escalas, precio_base)
VALUES (LOCALTIMESTAMP, v_av.ubicacion, v_dest, v_salida, v_llegada, v_bloque, 0,
        (ROUND((45000 + v_km * 65) / 100) * 100)::INT)
    RETURNING id_itinerario INTO v_id_it;

INSERT INTO itinerario_vuelo (id_itinerario, id_vuelo, orden, tiempo_espera, tipo_conexion)
VALUES (v_id_it, v_id_vuelo, 1, INTERVAL '0', 'Vuelo directo ' || v_iata_o || ' - ' || v_iata_d);

INSERT INTO itinerario_tarifa (id_itinerario, id_tarifa, precio)
SELECT v_id_it, t.id_tarifa,
       ROUND((12000 + v_km * 2.2) * CASE
                                        WHEN t.nombre ILIKE 'Flex%' THEN 1.3
                                        WHEN t.nombre ILIKE 'Prem%' THEN 1.8
                                        ELSE 1.0 END / 100) * 100
FROM tarifa t
    ON CONFLICT ON CONSTRAINT unique_itinerario_tarifa DO NOTHING;

-- 4.8 Actualizar estado de la simulación
UPDATE tmp_avion
SET ubicacion = v_dest, disponible = v_llegada + rotacion
WHERE id_avion = v_av.id_avion;

UPDATE tmp_piloto
SET disponible = v_llegada + p_descanso_piloto,
    minutos    = minutos + EXTRACT(EPOCH FROM v_bloque)::BIGINT / 60
WHERE rut = v_pil.rut;

UPDATE tmp_aerop SET visitas = visitas + 1 WHERE id_aeropuerto = v_dest;
INSERT INTO tmp_nuevos VALUES (v_id_vuelo, v_av.ubicacion, v_dest);

v_cnt_vuelos := v_cnt_vuelos + 1;
        v_cnt_dir := v_cnt_dir + 1;
END LOOP;

    -- -------------------------------------------------------------------------
    -- 5. Itinerarios con conexión (1 escala) sobre vuelos futuros no cancelados
    -- -------------------------------------------------------------------------
    CREATE TEMP TABLE tmp_futuros ON COMMIT DROP AS
SELECT v.id_vuelo, v.id_avion,
       so.id_aeropuerto_origen  AS origen,
       sd.id_aeropuerto_destino AS destino,
       so.hora_salida           AS salida,
       sd.hora_llegada          AS llegada,
       EXISTS (SELECT 1 FROM tmp_nuevos n WHERE n.id_vuelo = v.id_vuelo) AS nuevo
FROM vuelo v
         JOIN estado_vuelo ev ON ev.id_estado_vuelo = v.id_estado_vuelo
         JOIN LATERAL (SELECT * FROM segmento_vuelo s WHERE s.id_vuelo = v.id_vuelo
                       ORDER BY s.orden_segmento LIMIT 1) so ON TRUE
    JOIN LATERAL (SELECT * FROM segmento_vuelo s WHERE s.id_vuelo = v.id_vuelo
    ORDER BY s.orden_segmento DESC LIMIT 1) sd ON TRUE
WHERE so.hora_salida > LOCALTIMESTAMP
  AND ev.descripcion NOT ILIKE '%cancel%';
CREATE INDEX ON tmp_futuros (origen, salida);

CREATE TEMP TABLE tmp_pares ON COMMIT DROP AS
SELECT a.id_vuelo AS v1, b.id_vuelo AS v2
FROM itinerario_vuelo a
         JOIN itinerario_vuelo b ON b.id_itinerario = a.id_itinerario AND b.orden = 2
WHERE a.orden = 1;
CREATE INDEX ON tmp_pares (v1, v2);

FOR v_par IN
SELECT *
FROM (SELECT f1.id_vuelo AS v1, f2.id_vuelo AS v2,
             f1.origen, f1.destino AS escala, f2.destino,
             f1.salida, f2.llegada,
             (f2.salida - f1.llegada)    AS espera,
             d1.km AS km1, d2.km AS km2,
             (f1.id_avion = f2.id_avion) AS mismo_avion,
             ae.codigo_iata AS iata_escala, ce.nombre AS ciudad_escala, ad.codigo_iata AS iata_destino,
             ROW_NUMBER() OVER (PARTITION BY f1.id_vuelo ORDER BY f2.salida) AS rn
      FROM tmp_futuros f1
               JOIN tmp_futuros f2 ON f2.origen = f1.destino
          AND f2.salida BETWEEN f1.llegada + INTERVAL '1 hour' AND f1.llegada + INTERVAL '6 hours'
          AND f2.destino <> f1.origen
          JOIN tmp_dist d1 ON d1.o = f1.origen AND d1.d = f1.destino
          JOIN tmp_dist d2 ON d2.o = f2.origen AND d2.d = f2.destino
          JOIN tmp_dist dt ON dt.o = f1.origen AND dt.d = f2.destino
          JOIN aeropuerto ae ON ae.id_aeropuerto = f1.destino
          JOIN ciudad ce ON ce.id_ciudad = ae.id_ciudad
          JOIN aeropuerto ad ON ad.id_aeropuerto = f2.destino
      WHERE (f1.nuevo OR f2.nuevo)
        AND (d1.km + d2.km) <= dt.km * 1.6
        AND (f2.llegada - f1.salida) < INTERVAL '23 hours'
        AND NOT EXISTS (SELECT 1 FROM tmp_pares tp WHERE tp.v1 = f1.id_vuelo AND tp.v2 = f2.id_vuelo)) x
WHERE x.rn <= p_max_conexiones
    LOOP
INSERT INTO itinerario (fecha_creacion, origen_aeropuerto, destino_aeropuerto, hora_salida, hora_llegada,
                        duracion_total, numero_escalas, precio_base)
VALUES (LOCALTIMESTAMP, v_par.origen, v_par.destino, v_par.salida, v_par.llegada,
    v_par.llegada - v_par.salida, 1,
    (ROUND((90000 + (v_par.km1 + v_par.km2) * 65) * 0.85 / 100) * 100)::INT)
    RETURNING id_itinerario INTO v_id_it;

INSERT INTO itinerario_vuelo (id_itinerario, id_vuelo, orden, tiempo_espera, tipo_conexion)
VALUES (v_id_it, v_par.v1, 1, v_par.espera,
        'Conexión en ' || v_par.ciudad_escala || ' (' || v_par.iata_escala || ')'
            || CASE WHEN v_par.mismo_avion THEN ' - mismo avión' ELSE ' - cambio de avión' END),
       (v_id_it, v_par.v2, 2, INTERVAL '0', 'Vuelo final hacia ' || v_par.iata_destino);

INSERT INTO itinerario_tarifa (id_itinerario, id_tarifa, precio)
SELECT v_id_it, t.id_tarifa,
       ROUND((12000 + (v_par.km1 + v_par.km2) * 2.0) * CASE
                                                           WHEN t.nombre ILIKE 'Flex%' THEN 1.3
                                                           WHEN t.nombre ILIKE 'Prem%' THEN 1.8
                                                           ELSE 1.0 END / 100) * 100
FROM tarifa t
    ON CONFLICT ON CONSTRAINT unique_itinerario_tarifa DO NOTHING;

v_cnt_con := v_cnt_con + 1;
END LOOP;

SELECT COUNT(DISTINCT x) INTO v_cobertura
FROM (SELECT origen AS x FROM tmp_nuevos UNION SELECT destino FROM tmp_nuevos) u;

RETURN QUERY SELECT v_cnt_vuelos, v_cnt_dir, v_cnt_con, v_cobertura, v_inicio, v_fin;
END;
$fn$;


-- =============================================================================
-- EJECUCIÓN INICIAL: 90 días de operación desde el momento de ejecución
-- =============================================================================
SELECT * FROM fn_generar_operaciones_simuladas(90);

select * from itinerario

-- =============================================================================
-- PROCESO AUTOMÁTICO (opcional): extender el horizonte cada madrugada
-- Requiere la extensión pg_cron (disponible en Azure Flexible Server; no incluida
-- en la imagen postgis/postgis:16-3.4-alpine). Alternativa: @Scheduled en Spring.
-- =============================================================================
-- CREATE EXTENSION IF NOT EXISTS pg_cron;
-- SELECT cron.schedule('skyway_generar_operaciones', '15 3 * * *',
--                      $$SELECT * FROM fn_generar_operaciones_simuladas(90)$$);

-- =============================================================================
-- VERIFICACIONES
-- =============================================================================
-- Aeropuertos sin vuelos futuros:
-- SELECT a.codigo_iata FROM aeropuerto a
-- WHERE NOT EXISTS (SELECT 1 FROM segmento_vuelo s
--                   WHERE (s.id_aeropuerto_origen = a.id_aeropuerto OR s.id_aeropuerto_destino = a.id_aeropuerto)
--                     AND s.hora_salida > LOCALTIMESTAMP);
--
-- Solapamientos de avión (debe devolver 0 filas):
-- SELECT v1.id_vuelo, v2.id_vuelo FROM vuelo v1 JOIN vuelo v2
--   ON v1.id_avion = v2.id_avion AND v1.id_vuelo < v2.id_vuelo
--  AND v1.fecha_hora_salida < v2.fecha_hora_llegada AND v2.fecha_hora_salida < v1.fecha_hora_llegada;
