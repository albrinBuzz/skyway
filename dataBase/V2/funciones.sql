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
    a1.Codigo_IATA || ' ' || TO_CHAR(sv.HORA_SALIDA, 'DD/MM/YYYY') || ' ' || TO_CHAR(sv.HORA_SALIDA, 'HH:MI AM') AS Salida,
    a1.Nombre_Aeropuerto AS Aeropuerto_Salida,
    c1.nombre AS Ciudad_Salida,
    a2.Codigo_IATA || ' ' || TO_CHAR(sv.HORA_LLEGADA, 'DD/MM/YYYY') || ' ' || TO_CHAR(sv.HORA_LLEGADA, 'HH:MI AM') AS Llegada,
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
	psa.precio AS precio_asiento,
	 --(SELECT psa.precio FROM precio_asiento psa WHERE id_vuelo=p_idVuelo AND id_clase = a.id_clase LIMIT 1) AS precio_asiento,
	cls.descripcion
FROM
    vuelo vl
join avion av
on av.id_avion = vl.id_avion
join asiento a
	on a.id_avion = av.id_avion and a.id_avion = vl.id_avion
LEFT JOIN
    Reserva_Asiento ra ON a.ID_ASIENTO = ra.ID_ASIENTO
	and ra.id_vuelo = vl.id_vuelo
left join clase_asiento cls
	on cls.id_clase = a.id_clase
join precio_asiento psa
	on psa.id_clase = a.id_clase and psa.id_vuelo = vl.id_vuelo
WHERE
    vl.id_vuelo=p_idVuelo
ORDER BY
    a.Numero_Asiento;

END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_getItinerariosRut(
    prut_pasajero TEXT,
    limite INT,
    desplazamiento INT
)
RETURNS TABLE (
    id_itinerario int,
    codigo_iata_origen varchar(255),
    codigo_iata_destino varchar(255),
    hora_salida TIMESTAMP
)
AS
$$
BEGIN
    RETURN QUERY
    SELECT
        i.id_itinerario,
        arp2.codigo_iata,
        arp1.codigo_iata,
        i.hora_salida
    FROM reserva r
    JOIN reserva_itinerario ri ON ri.id_reserva = r.id_reserva
    JOIN itinerario i ON i.id_itinerario = ri.id_itinerario
    JOIN aeropuerto arp1 ON arp1.id_aeropuerto = i.destino_aeropuerto
    JOIN aeropuerto arp2 ON arp2.id_aeropuerto = i.origen_aeropuerto
    WHERE r.rut_pasajero = prut_pasajero
    ORDER BY r.fecha_reserva ASC
    LIMIT limite OFFSET desplazamiento;
END;
$$ LANGUAGE plpgsql STABLE;




CREATE OR REPLACE FUNCTION fn_getItinerariosPorRutYFechas(
    prut_pasajero TEXT,
    limite INT,
    desplazamiento INT,
    pfechaIni TIMESTAMP DEFAULT NULL,
    pfechaFin TIMESTAMP DEFAULT NULL
)
RETURNS TABLE (
    id_itinerario INT,
    codigo_iata_origen VARCHAR(255),
    codigo_iata_destino VARCHAR(255),
    hora_salida TIMESTAMP,
	id_reserva INT
)
AS
$$
BEGIN
    RETURN QUERY
    SELECT
        i.id_itinerario,
        arp2.codigo_iata,
        arp1.codigo_iata,
        i.hora_salida,
		r.id_reserva
    FROM reserva r
    JOIN reserva_itinerario ri ON ri.id_reserva = r.id_reserva
    JOIN itinerario i ON i.id_itinerario = ri.id_itinerario
    JOIN aeropuerto arp1 ON arp1.id_aeropuerto = i.destino_aeropuerto
    JOIN aeropuerto arp2 ON arp2.id_aeropuerto = i.origen_aeropuerto
    WHERE r.rut_pasajero = prut_pasajero
      AND (pfechaIni IS NULL OR i.hora_salida >= pfechaIni)
      AND (pfechaFin IS NULL OR i.hora_salida <= pfechaFin)
    ORDER BY r.fecha_reserva ASC
    LIMIT limite OFFSET desplazamiento;
END;
$$ LANGUAGE plpgsql STABLE;



CREATE OR REPLACE FUNCTION fn_getAsientosPorItinerarioYReserva(
    pid_itinerario INT,
    pid_reserva INT
)
RETURNS TABLE (
    id_asiento INT,
    numero_asiento VARCHAR,
    clase_asiento VARCHAR,
    id_vuelo INT
)
AS
$$
BEGIN
    RETURN QUERY
    SELECT
        a.id_asiento,
        a.numero_asiento,
        ca.descripcion,
        ra.id_vuelo
    FROM reserva_itinerario ri
    JOIN itinerario_vuelo iv ON iv.id_itinerario = ri.id_itinerario
    JOIN reserva_asiento ra ON ra.id_reserva = ri.id_reserva AND ra.id_vuelo = iv.id_vuelo
    JOIN asiento a ON a.id_asiento = ra.id_asiento
    JOIN clase_asiento ca ON ca.id_clase = a.id_clase
    WHERE ri.id_itinerario = pid_itinerario
      AND ri.id_reserva = pid_reserva;
END;
$$ LANGUAGE plpgsql STABLE;


CREATE OR REPLACE FUNCTION fn_getTicket(p_rut_pasajero VARCHAR,  p_id_reserva integer)
RETURNS TABLE (
    numero_vuelo VARCHAR,
    hora_salida TIMESTAMP,
    hora_llegada TIMESTAMP,
    codigo_puerta VARCHAR,
    terminal VARCHAR,
    numero_asiento VARCHAR,
    clase_asiento VARCHAR
)
AS $$
BEGIN
    RETURN QUERY
    SELECT
        vl.numero_vuelo,
        sgm.hora_salida,
        sgm.hora_llegada,
        prta.codigo_puerta,
        prta.terminal,
        ast.numero_asiento,
        clas.descripcion
    FROM reserva_asiento rsva
    JOIN reserva rsv ON rsv.id_reserva = rsva.id_reserva
    JOIN vuelo vl ON vl.id_vuelo = rsva.id_vuelo
    JOIN segmento_vuelo sgm ON sgm.id_vuelo = rsva.id_vuelo
    JOIN asignacion_puerta asgp ON asgp.id_segmento = sgm.id_segmento
    JOIN puerta_embarque prta ON prta.id_puerta = asgp.id_puerta
    JOIN asiento ast ON ast.id_asiento = rsva.id_asiento
    JOIN clase_asiento clas ON clas.id_clase = ast.id_clase
    WHERE rsv.rut_pasajero = p_rut_pasajero and rsv.id_reserva=p_id_reserva
	order by  vl.fecha_hora_salida;
END;
$$ LANGUAGE plpgsql;

-- Ejemplo:
SELECT * FROM fn_getAsientosPorItinerarioYReserva(320, 10);

SELECT * FROM fn_getTicket('12345678-9',4);

SELECT * FROM fn_getItinerariosRut('12345678-9', 100, 0);

SELECT * FROM fn_getItinerariosPorRutYFechas('12345678-9', 1000, 0, '2025-09-01', '2025-09-2');

SELECT * FROM fn_getItinerariosPorRutYFechas('12345678-9', 10, 0, NULL, NULL);

SELECT * FROM fn_getItinerariosPorRutYFechas('12345678-9', 10, 0, '2025-09-01', NULL);

SELECT * FROM fnBuscarVuelo('BOG', 'PEK', '2025-09-02');

SELECT * FROM fn_getItinerariosPorRutYFechas('12345678-9', 10, 0, '2025-09-01', '2025-09-30');

SELECT * FROM fn_getItinerariosPorRutYFechas('12345678-9', 10, 0, NULL, NULL);

SELECT * FROM fn_getItinerariosPorRutYFechas('12345678-9', 10, 0, '2025-09-01', NULL);