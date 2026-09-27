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


CREATE OR REPLACE FUNCTION FnbuscarVuelos(
    p_codigo_origen  VARCHAR,
    p_codigo_destino VARCHAR,
    p_fecha_inicio DATE DEFAULT NULL
)
RETURNS SETOF vuelo
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        v.*
    FROM vuelo v

    -- Primer segmento (origen)
    LEFT JOIN segmento_vuelo sgmv1
        ON sgmv1.id_vuelo = v.id_vuelo
       AND sgmv1.orden_segmento = 1

    -- Último segmento (destino final)
    JOIN (
        SELECT DISTINCT ON (id_vuelo)
            id_vuelo,
            id_aeropuerto_destino
        FROM segmento_vuelo
        ORDER BY id_vuelo, orden_segmento DESC
    ) sgmv2
        ON sgmv2.id_vuelo = v.id_vuelo

    JOIN aeropuerto arp1
        ON arp1.id_aeropuerto = sgmv1.id_aeropuerto_origen

    JOIN aeropuerto arp2
        ON arp2.id_aeropuerto = sgmv2.id_aeropuerto_destino

    WHERE arp1.codigo_iata = p_codigo_origen
      AND arp2.codigo_iata = p_codigo_destino
	 AND (
            p_fecha_inicio IS NULL
            OR (
                sgmv1.hora_salida >= p_fecha_inicio
                AND sgmv1.hora_salida < p_fecha_inicio + INTERVAL '1 day'
            )
      );
END;
$$;

SELECT * FROM FnbuscarVuelos('SCL', 'JFK', '2025-12-05');

SELECT * FROM FnbuscarVuelos('SCL', 'JFK', NULL);


select
*
from vuelo v
 join segmento_vuelo sg ON sg.id_vuelo = v.id_vuelo
order by v.id_vuelo;





CREATE OR REPLACE FUNCTION fnDTinitinerario(p_id_itinerario INT)
RETURNS TABLE(
  ID_ITINERARIO INT,
  Aeropuerto_Origen VARCHAR(100),
  Aeropuerto_Destino VARCHAR(100),
  DURACION_TOTAL TEXT,
  NUMERO_ESCALAS INT,
  ORDEN INT,
  Salida TEXT,
  Aeropuerto_Salida VARCHAR(100),
  Ciudad_Salida VARCHAR(100),
  Llegada TEXT,
  Aeropuerto_Llegada VARCHAR(100),
  Ciudad_Llegada VARCHAR(100),
  Duracion TEXT,
  Tiempo_Espera TEXT,
  Modelo_Avion VARCHAR(100),
  Aerolinea VARCHAR(100),
  Vuelo VARCHAR(50),
  Descripcion_Vuelo TEXT
)
AS $$
BEGIN
RETURN QUERY
    WITH tramos_calculados AS (
    SELECT
        it.ID_ITINERARIO AS id_it_temp,
        a3.Nombre_Aeropuerto AS At_Origen,
        a4.Nombre_Aeropuerto AS At_Destino,
        TO_CHAR(it.DURACION_TOTAL, 'HH24 "h" MI "min"') AS Dur_Total,
        it.NUMERO_ESCALAS,
        iv.ORDEN AS orden_vuelo,
        a1.Codigo_IATA AS Iata_Salida,
        a1.Nombre_Aeropuerto AS Ap_Salida,
        c1.nombre AS Cd_Salida,
        a2.Codigo_IATA AS Iata_Llegada,
        a2.Nombre_Aeropuerto AS Ap_Llegada,
        c2.nombre AS Cd_Llegada,

        (sv_dest.HORA_LLEGADA - sv_orig.HORA_SALIDA) AS duracion_vuelo,
        iv.TIEMPO_ESPERA::INTERVAL AS espera_conexion,

        COALESCE(
            LAG(it.HORA_SALIDA) OVER (PARTITION BY it.ID_ITINERARIO ORDER BY iv.ORDEN),
            it.HORA_SALIDA
        ) AS base_salida,

        mdv.Nombre AS Mod_Avion,
        al.Nombre AS Name_Aerolinea,
        v.Numero_Vuelo AS Num_Vuelo
    FROM Itinerario it
    JOIN Itinerario_Vuelo iv ON iv.ID_ITINERARIO = it.ID_ITINERARIO
    JOIN Vuelo v            ON v.ID_VUELO = iv.ID_VUELO
    JOIN Avion av           ON av.ID_AVION = v.ID_AVION
    JOIN modelo_avion mdv   ON mdv.id_modelo = av.id_modelo
    JOIN Aerolinea al       ON al.ID_AEROLINEA = v.ID_AEROLINEA

    JOIN (SELECT id_vuelo, min(orden_segmento) as p, max(orden_segmento) as u FROM segmento_vuelo GROUP BY id_vuelo) ext
         ON ext.id_vuelo = v.id_vuelo
    JOIN Segmento_Vuelo sv_orig ON sv_orig.ID_VUELO = v.ID_VUELO AND sv_orig.orden_segmento = ext.p
    JOIN Segmento_Vuelo sv_dest ON sv_dest.ID_VUELO = v.ID_VUELO AND sv_dest.orden_segmento = ext.u

    JOIN Aeropuerto a1 ON a1.ID_AEROPUERTO = sv_orig.ID_AEROPUERTO_ORIGEN
    JOIN Aeropuerto a2 ON a2.ID_AEROPUERTO = sv_dest.ID_AEROPUERTO_DESTINO
    JOIN ciudad c1     ON c1.ID_CIUDAD = a1.ID_CIUDAD
    JOIN ciudad c2     ON c2.ID_CIUDAD = a2.ID_CIUDAD
    JOIN Aeropuerto a3 ON a3.ID_AEROPUERTO = it.ORIGEN_AEROPUERTO
    JOIN Aeropuerto a4 ON a4.ID_AEROPUERTO = it.DESTINO_AEROPUERTO
    WHERE it.ID_ITINERARIO = p_id_itinerario
),
linea_tiempo_acumulada AS (
    SELECT
        *,
        CASE
            WHEN orden_vuelo = 1 THEN base_salida
            ELSE base_salida + SUM(duracion_vuelo + espera_conexion) OVER (PARTITION BY id_it_temp ORDER BY orden_vuelo ROWS BETWEEN UNBOUNDED PRECEDING AND 1 PRECEDING) + espera_conexion
        END AS hora_salida_real
    FROM tramos_calculados
)
SELECT
    l.id_it_temp AS ID_ITINERARIO,
    l.At_Origen,
    l.At_Destino,
    l.Dur_Total,
    l.NUMERO_ESCALAS,
    l.orden_vuelo AS ORDEN,
    l.Iata_Salida || ' ' || TO_CHAR(l.hora_salida_real, 'DD/MM/YYYY HH:MI AM') AS Salida,
    l.Ap_Salida,
    l.Cd_Salida,
    l.Iata_Llegada || ' ' || TO_CHAR(l.hora_salida_real + l.duracion_vuelo, 'DD/MM/YYYY HH:MI AM') AS Llegada,
    l.Ap_Llegada,
    l.Cd_Llegada,

    EXTRACT(HOUR FROM l.duracion_vuelo) || ' h ' || EXTRACT(MINUTE FROM l.duracion_vuelo) || ' min' AS Duracion,
    EXTRACT(HOUR FROM l.espera_conexion) || ' h ' || EXTRACT(MINUTE FROM l.espera_conexion) || ' min' AS Tiempo_Espera,

    l.Mod_Avion,
    l.Name_Aerolinea,
    l.Num_Vuelo,
    'Vuelo ' || l.Num_Vuelo || ', ' || l.Mod_Avion || ', Operado por ' || l.Name_Aerolinea AS Descripcion_Vuelo
FROM linea_tiempo_acumulada l
ORDER BY l.orden_vuelo;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION fn_getAsientosAvion(
    IN p_idVuelo INT,
    IN p_idReserva INT DEFAULT NULL
)
RETURNS TABLE (
    id_asiento INT,
    numero_asiento VARCHAR,
    estado TEXT,
    precio INT,
    clase VARCHAR,
    fila INT,
    letra VARCHAR,
    es_ventana BOOLEAN,
    es_pasillo BOOLEAN,
    es_emergencia BOOLEAN
) AS $$
BEGIN
RETURN QUERY
SELECT
    a.id_asiento,
    a.numero_asiento,
    CASE
        WHEN p_idReserva IS NOT NULL AND rsv.id_reserva = p_idReserva THEN 'seleccionado'
        WHEN rsv.id_reserva IS NOT NULL THEN 'ocupado'
        ELSE 'libre'
        END AS estado,
    psa.precio,
    cls.descripcion AS clase,
    a.fila,
    a.letra,
    a.es_ventana,
    a.es_pasillo,
    a.es_emergencia
FROM vuelo vl
         JOIN avion av ON av.id_avion = vl.id_avion
         JOIN asiento a ON a.id_avion = av.id_avion
         LEFT JOIN reserva_asiento rsv ON rsv.id_asiento = a.id_asiento AND rsv.id_vuelo = vl.id_vuelo
         JOIN precio_asiento psa ON psa.id_clase = a.id_clase AND psa.id_vuelo = vl.id_vuelo
         LEFT JOIN clase_asiento cls ON cls.id_clase = a.id_clase
WHERE vl.id_vuelo = p_idVuelo
ORDER BY a.fila ASC, a.letra ASC;
END;
$$ LANGUAGE plpgsql;

SELECT * FROM fn_getAsientosAvion(25, 5);

select * from reserva;



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
    id_vuelo INT,
    id_reserva INT
)
AS
$$
BEGIN
    RETURN QUERY
    SELECT
        a.id_asiento,
        a.numero_asiento,
        ca.descripcion,
        ra.id_vuelo,
        ra.id_reserva
    FROM reserva_itinerario ri
    JOIN itinerario_vuelo iv ON iv.id_itinerario = ri.id_itinerario
    JOIN reserva_asiento ra ON ra.id_reserva = ri.id_reserva AND ra.id_vuelo = iv.id_vuelo
    JOIN asiento a ON a.id_asiento = ra.id_asiento
    JOIN clase_asiento ca ON ca.id_clase = a.id_clase
    WHERE ri.id_itinerario = pid_itinerario
      AND ri.id_reserva = pid_reserva;
END;
$$ LANGUAGE plpgsql STABLE;





CREATE OR REPLACE FUNCTION fn_getTicket(p_rut_pasajero VARCHAR,  p_id_reserva integer, p_idItinerario integer)
RETURNS TABLE (
    numero_vuelo VARCHAR,
    hora_salida TIMESTAMP,
    hora_llegada TIMESTAMP,
    codigo_puerta VARCHAR,
    terminal VARCHAR,
    numero_asiento VARCHAR,
    clase_asiento VARCHAR,
	nombre text
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
        clas.descripcion,
		us.nombre || ' ' || us.apellido
    FROM reserva_asiento rsva
    left JOIN reserva rsv ON rsv.id_reserva = rsva.id_reserva
	left join itinerario_vuelo itv on itv.id_vuelo = rsva.id_vuelo
	join pasajero p on p.rut = rsva.rut
	join usuario us on us.rut = p.rut
    left JOIN vuelo vl ON vl.id_vuelo = rsva.id_vuelo
    left JOIN segmento_vuelo sgm ON sgm.id_vuelo = rsva.id_vuelo
    left JOIN asignacion_puerta asgp ON asgp.id_segmento = sgm.id_segmento
    left JOIN puerta_embarque prta ON prta.id_puerta = asgp.id_puerta
    left JOIN asiento ast ON ast.id_asiento = rsva.id_asiento
    left JOIN clase_asiento clas ON clas.id_clase = ast.id_clase
    WHERE rsv.rut_pasajero = p_rut_pasajero and rsv.id_reserva=p_id_reserva
	and itv.id_itinerario=p_idItinerario
	order by  vl.fecha_hora_salida;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION fn_getVueloInfo(p_idVuelo integer)
RETURNS TABLE (
	destino varchar,
    id_vuelo int,
    numero_vuelo text,
    ciudad_salida text,
    ciudad_llegada text,
    fecha_hora_salida TIMESTAMP,
    fecha_hora_llegada TIMESTAMP,
	duracion text

) AS $$
BEGIN
    RETURN QUERY
	    SELECT
		p1.nombre,
        v.ID_VUELO,
        v.Numero_Vuelo::text,
        ci1.nombre||' - '||a1.nombre_aeropuerto||' ('||a1.codigo_iata||')' ,
        ci2.nombre||' - '||a2.nombre_aeropuerto||' ('||a2.codigo_iata||')'  ,
		v.Fecha_Hora_Salida,
		v.Fecha_Hora_Llegada,
		 --(EXTRACT(EPOCH FROM (v.Fecha_Hora_Llegada - v.Fecha_Hora_Salida)) / 3600),
	CAST(EXTRACT(HOUR FROM (v.Fecha_Hora_Llegada - v.Fecha_Hora_Salida)) AS VARCHAR) || 'h ' ||
	CAST(EXTRACT(MINUTE FROM (v.Fecha_Hora_Llegada - v.Fecha_Hora_Salida)) AS VARCHAR) || 'm'
    FROM
        Vuelo v

	JOIN segmento_vuelo sgm on sgm.id_vuelo = v.id_vuelo

    JOIN
        Aeropuerto a1 ON a1.id_aeropuerto = sgm.id_aeropuerto_origen
    JOIN
        Aeropuerto a2 ON a2.id_aeropuerto = sgm.id_aeropuerto_destino
    JOIN
        Ciudad ci1 ON ci1.id_ciudad = a1.id_ciudad
    JOIN
        Ciudad ci2 ON ci2.id_ciudad = a2.id_ciudad
	Join pais p1 on p1.id_pais = ci2.id_pais
		where v.ID_VUELO=p_idVuelo;
		--where extract(day from AGE(v.Fecha_Hora_Salida, CURRENT_TIMESTAMP))<=10;

END;
$$ LANGUAGE plpgsql;

select * from vuelo;

select * from fn_getVueloInfo(23);

-- Ejemplo:
SELECT * FROM fn_getAsientosPorItinerarioYReserva(320, 10);

--SELECT * FROM fn_getTicket('12345678-9',4);

SELECT * FROM fn_getItinerariosRut('12345678-9', 100, 0);

SELECT * FROM fn_getItinerariosPorRutYFechas('12345678-9', 1000, 0, '2025-09-01', '2025-09-2');

SELECT * FROM fn_getItinerariosPorRutYFechas('12345678-9', 10, 0, NULL, NULL);

SELECT * FROM fn_getItinerariosPorRutYFechas('12345678-9', 10, 0, '2025-09-01', NULL);

SELECT * FROM fnBuscarVuelo('BOG', 'PEK', '2025-09-02');

SELECT * FROM fn_getItinerariosPorRutYFechas('12345678-9', 10, 0, '2025-09-01', '2025-09-30');

SELECT * FROM fn_getItinerariosPorRutYFechas('12345678-9', 10, 0, NULL, NULL);

SELECT * FROM fn_getItinerariosPorRutYFechas('12345678-9', 10, 0, '2025-09-01', NULL);