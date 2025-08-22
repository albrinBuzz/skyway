
-- Procedimiento almacenado para asignar la capacidad por clase a un avión específico.
-- Inserta la cantidad de asientos disponibles por clase (económica = 1, ejecutiva = 2, primera = 3).
CREATE OR REPLACE PROCEDURE spAsignarCapacidadPorClase(
    IN p_idAvion INT,
    IN p_capacidad_economica INT,
    IN p_capacidad_ejecutiva INT,
    IN p_capacidad_primera INT
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO Capacidad_Clase (ID_AVION, ID_CLASE, Cantidad)
    VALUES
        (p_idAvion, 1, p_capacidad_economica),
        (p_idAvion, 2, p_capacidad_ejecutiva),
        (p_idAvion, 3, p_capacidad_primera);
END;
$$;



CREATE OR REPLACE FUNCTION fnBuscarVuelo(
    p_codigo_origen VARCHAR,
    p_codigo_destino VARCHAR,
    p_fecha_inicio DATE
)
RETURNS TABLE (
    itinerario INT,
    origen VARCHAR,
    destino VARCHAR,
    ciudad_salida TEXT,
    ciudad_llegada TEXT,
    cant_paradas TEXT,
    precio INT,
    duracion TEXT,
    hora_salida_24h TEXT,
    hora_llegada_24h TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        it.id_itinerario,
        arp1.codigo_iata,
        arp2.codigo_iata,
        ci1.nombre || ' - ' || arp1.nombre_aeropuerto || ' (' || arp1.codigo_iata || ')' AS ciudad_salida,
        ci2.nombre || ' - ' || arp2.nombre_aeropuerto || ' (' || arp2.codigo_iata || ')' AS ciudad_llegada,
        CASE
            WHEN it.numero_escalas = 0 THEN 'sin paradas'
            WHEN it.numero_escalas = 1 THEN '1 parada'
            ELSE it.numero_escalas::TEXT || ' paradas'
        END AS cant_paradas,
        it.precio_base,
        (EXTRACT(epoch FROM (it.hora_llegada - it.hora_salida)) / 3600)::INT || ' h ' ||
        ((EXTRACT(epoch FROM (it.hora_llegada - it.hora_salida)) % 3600) / 60)::INT || ' min' AS duracion,
        TO_CHAR(it.hora_salida, 'HH24:MI'),
        TO_CHAR(it.hora_llegada, 'HH24:MI')
    FROM itinerario it
    JOIN aeropuerto arp1 ON arp1.id_aeropuerto = it.origen_aeropuerto
    JOIN aeropuerto arp2 ON arp2.id_aeropuerto = it.destino_aeropuerto
    JOIN ciudad ci1 ON ci1.id_ciudad = arp1.id_ciudad
    JOIN ciudad ci2 ON ci2.id_ciudad = arp2.id_ciudad
    WHERE arp1.codigo_iata = p_codigo_origen
      AND arp2.codigo_iata = p_codigo_destino
      AND it.hora_salida >= p_fecha_inicio
      AND it.hora_salida < p_fecha_inicio + INTERVAL '1 day';
END;
$$ LANGUAGE plpgsql;


-- Buscar vuelo desde Santiago (SCL) a Nueva York (JFK, por ejemplo)
SELECT * FROM fnBuscarVuelo('SCL', 'JFK', '2025-08-01');

-- Buscar vuelo desde Santiago (SCL) a Los Ángeles (LAX)
SELECT * FROM fnBuscarVuelo('SCL', 'LAX', '2025-08-01');


-- 1. SCL -> JFK el 1 de agosto (3 resultados esperados: con escalas, directo, por Bogotá)
SELECT * FROM fnBuscarVuelo('Santiago', 'Nueva York', '2025-08-01');

-- 2. SCL -> Lima el 1 de agosto (vuelo directo)
SELECT * FROM fnBuscarVuelo('Santiago', 'Lima', '2025-08-01');

-- 3. SCL -> Atlanta el 1 de agosto (vía Lima)
SELECT * FROM fnBuscarVuelo('Santiago', 'Atlanta', '2025-08-01');

-- 4. LAX -> SCL el 8 de agosto (vía ATL y LIM)
SELECT * FROM fnBuscarVuelo('Los Angeles', 'Santiago', '2025-08-08');

-- 5. GRU -> JFK el 3 de agosto (una directa y otra con escalas en BOG y MIA)
SELECT * FROM fnBuscarVuelo('São Paulo', 'Nueva York', '2025-08-03');

-- 6. Miami -> Santiago el 3 de agosto (vuelo directo)
SELECT * FROM fnBuscarVuelo('Miami', 'Santiago', '2025-08-03');

-- 7. SCL -> MVD el 3 de agosto (parte de itinerario 4)
SELECT * FROM fnBuscarVuelo('Santiago', 'Montevideo', '2025-08-03');

-- 8. BOG -> Miami el 4 de agosto (vuelo directo)
SELECT * FROM fnBuscarVuelo('Bogotá', 'Miami', '2025-08-04');

-- 9. SCL -> JFK el 2 de agosto (solo debe salir el directo si aplica)
SELECT * FROM fnBuscarVuelo('Santiago', 'Nueva York', '2025-08-02');

-- 10. MVD -> GRU el 3 de agosto (vuelo del itinerario 4)
SELECT * FROM fnBuscarVuelo('Montevideo', 'São Paulo', '2025-08-03');






CREATE OR REPLACE FUNCTION fnDTinitinerario(p_id_itinerario INT)
RETURNS TABLE(
  ID_ITINERARIO INT,
  Aeropuerto_Origen VARCHAR(100),
  Aeropuerto_Destino VARCHAR(100),
  DURACION_TOTAL TEXT,              -- Convertir INTERVAL a TEXT
  NUMERO_ESCALAS INT,
  ORDEN INT,
  Salida TEXT,
  Aeropuerto_Salida VARCHAR(100),
  Ciudad_Salida VARCHAR(100),
  Llegada TEXT,
  Aeropuerto_Llegada VARCHAR(100),
  Ciudad_Llegada VARCHAR(100),
  Duracion TEXT,
  Tiempo_Espera TEXT,              -- Convertir INTERVAL a TEXT
  Modelo_Avion VARCHAR(100),
  Aerolinea VARCHAR(100),
  Vuelo VARCHAR(50),
  Descripcion_Vuelo TEXT
)
AS $$
BEGIN
  RETURN QUERY
  SELECT
    it.ID_ITINERARIO,
    a3.Nombre_Aeropuerto AS Aeropuerto_Origen,
    a4.Nombre_Aeropuerto AS Aeropuerto_Destino,
    -- Convertir INTERVAL a texto
    TO_CHAR(it.DURACION_TOTAL, 'HH24 "h" MI "min"') AS DURACION_TOTAL,  -- Aquí se formatea el INTERVAL
    it.NUMERO_ESCALAS,
    iv.ORDEN,
    a1.Codigo_IATA || ' ' || TO_CHAR(sv.HORA_SALIDA, 'HH24:MI') AS Salida,
    a1.Nombre_Aeropuerto AS Aeropuerto_Salida,
    c1.nombre AS Ciudad_Salida,
    a2.Codigo_IATA || ' ' || TO_CHAR(sv.HORA_LLEGADA, 'HH24:MI') AS Llegada,
    a2.Nombre_Aeropuerto AS Aeropuerto_Llegada,
    c2.nombre AS Ciudad_Llegada,

    -- Convertir la duración a formato de texto
    EXTRACT(HOUR FROM (sv.HORA_LLEGADA - sv.HORA_SALIDA)) || ' h ' ||
    EXTRACT(MINUTE FROM (sv.HORA_LLEGADA - sv.HORA_SALIDA)) || ' min' AS Duracion,

    -- Convertir el tiempo de espera a texto
    CASE
      WHEN LAG(sv.HORA_LLEGADA) OVER (PARTITION BY it.ID_ITINERARIO ORDER BY iv.ORDEN) IS NOT NULL THEN
        EXTRACT(HOUR FROM (sv.HORA_SALIDA - LAG(sv.HORA_LLEGADA) OVER (PARTITION BY it.ID_ITINERARIO ORDER BY iv.ORDEN))) || ' h ' ||
        EXTRACT(MINUTE FROM (sv.HORA_SALIDA - LAG(sv.HORA_LLEGADA) OVER (PARTITION BY it.ID_ITINERARIO ORDER BY iv.ORDEN))) || ' min'
      ELSE
        '0 h 0 min'
    END AS Tiempo_Espera,

    mdv.Nombre AS Modelo_Avion,
    al.Nombre AS Aerolinea,
    v.Numero_Vuelo AS Vuelo,
    'Vuelo ' || v.Numero_Vuelo || ', ' || mdv.Nombre || ', Operado por ' || al.Nombre AS Descripcion_Vuelo

  FROM Itinerario it
  JOIN Itinerario_Vuelo iv ON iv.ID_ITINERARIO = it.ID_ITINERARIO
  JOIN Vuelo v ON v.ID_VUELO = iv.ID_VUELO
  JOIN Avion av ON av.ID_AVION = v.ID_AVION
  JOIN modelo_avion mdv on mdv.id_modelo = av.id_modelo
  JOIN Aerolinea al ON al.ID_AEROLINEA = v.ID_AEROLINEA
  JOIN Piloto p ON p.RUT = v.RUT_PILOTO
  JOIN Segmento_Vuelo sv ON sv.ID_VUELO = v.ID_VUELO
  JOIN Aeropuerto a1 ON a1.ID_AEROPUERTO = sv.ID_AEROPUERTO_ORIGEN
  JOIN Aeropuerto a2 ON a2.ID_AEROPUERTO = sv.ID_AEROPUERTO_DESTINO
  JOIN ciudad c1 on c1.ID_CIUDAD = a1.ID_CIUDAD
  JOIN ciudad c2 on c2.ID_CIUDAD = a2.ID_CIUDAD
  JOIN Aeropuerto a3 ON a3.ID_AEROPUERTO = it.ORIGEN_AEROPUERTO
  JOIN Aeropuerto a4 ON a4.ID_AEROPUERTO = it.DESTINO_AEROPUERTO

  WHERE it.ID_ITINERARIO = p_id_itinerario
  ORDER BY iv.ORDEN;
END;
$$ LANGUAGE plpgsql;



SELECT * FROM fnDTinitinerario(1);






CREATE OR REPLACE FUNCTION fn_getAsientosAvion(in p_idVuelo int)
RETURNS TABLE(
	id_asiento int,
    numero_asiento varchar,
    estado text,
	precio int,
	clase varchar
) AS $$
BEGIN

    RETURN QUERY

		SELECT
    a.ID_ASIENTO,
    a.Numero_Asiento,
    (CASE
        WHEN ra.ID_RESERVA IS NOT NULL THEN 'ocupado'
        ELSE 'libre'
    END) AS Estado,
	 (SELECT psa.precio FROM precio_asiento psa WHERE id_vuelo=p_idVuelo AND id_clase = a.id_clase LIMIT 1) AS precio_asiento,
	cls.descripcion
FROM
    Asiento a
JOIN
    Avion av ON a.ID_AVION = av.ID_AVION
LEFT JOIN
    Reserva_Asiento ra ON a.ID_ASIENTO = ra.ID_ASIENTO
LEFT JOIN
    Reserva r ON ra.ID_RESERVA = r.ID_RESERVA
left join clase_asiento cls
	on cls.id_clase = a.id_clase
WHERE
    av.ID_AVION = (SELECT ID_AVION FROM Vuelo WHERE id_vuelo = p_idVuelo)
ORDER BY
    a.Numero_Asiento;


END;
$$ LANGUAGE plpgsql;


SELECT * FROM fn_getAsientosAvion(25);


