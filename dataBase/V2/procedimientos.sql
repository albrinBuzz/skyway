
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
SELECT * FROM fnBuscarVuelo('SCL', 'LAX', '2025-08-22');

SELECT * FROM fnBuscarVuelo('LAX', 'SCL', '2025-08-23');

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
WHERE
    vl.id_vuelo=p_idVuelo
ORDER BY
    a.Numero_Asiento;

END;
$$ LANGUAGE plpgsql;


SELECT * FROM fn_getAsientosAvion(27)
where numero_asiento='15C';



CREATE OR REPLACE PROCEDURE spConfirmar_reserva(
    IN p_idVuelo INT,
	IN p_idReserva INT,
    IN p_asientos INT[],
    IN p_rutPasajero TEXT,
    OUT p_resultado TEXT
)
LANGUAGE plpgsql
AS $$
DECLARE
    reserva_id INT;
    estado_reserva_id INT := 1;  -- Suponemos 1 = pendiente o confirmada
    i INT;
    id_avion INT;
    id_asientoP INT;
    asiento_en_reserva INT;
    numero_asiento TEXT;
    asientos_reservados TEXT := '';
BEGIN
    -- Obtener el avión asignado al vuelo
    SELECT vl.id_avion INTO id_avion
    FROM vuelo vl
    WHERE id_vuelo = p_idVuelo;

    -- Iniciar transacción (implícita en SP)
    -- Crear la reserva
    /*INSERT INTO reserva (rut_pasajero, fecha_reserva, estado_reserva, total)
    VALUES (p_rutPasajero, CURRENT_TIMESTAMP, estado_reserva_id, 0)
    RETURNING id_reserva INTO reserva_id;*/

    FOR i IN 1..array_length(p_asientos, 1)
    LOOP
        id_asientoP := p_asientos[i];

        -- Verificar si el asiento ya está reservado en este vuelo
        SELECT 1 INTO asiento_en_reserva
		FROM reserva_asiento ra
		where ra.ID_VUELO=p_idVuelo
		and ra.ID_ASIENTO=id_asientoP
		FOR UPDATE;



        IF asiento_en_reserva > 0 THEN
            SELECT numero_asiento INTO numero_asiento
            FROM asiento ast
            WHERE ast.id_asiento = id_asientoP;

            asientos_reservados := asientos_reservados || numero_asiento || ', ';
        ELSE
            -- Insertar en reserva_asiento
            INSERT INTO reserva_asiento (id_reserva, id_asiento,ID_VUELO)
            VALUES (p_idReserva, id_asientoP,p_idVuelo);
        END IF;
    END LOOP;

    IF asientos_reservados <> '' THEN
        p_resultado := 'ERROR: Asientos ya reservados: ' || LEFT(asientos_reservados, LENGTH(asientos_reservados) - 2);
        -- Puedes eliminar la reserva si quedó sin asientos
        DELETE FROM reserva WHERE id_reserva = p_idReserva;
    ELSE
        -- Asociar la reserva con el vuelo
        --INSERT INTO id_asientoP (id_reserva, id_vuelo)
        --VALUES (reserva_id, p_idVuelo);

        p_resultado := 'OK: Reserva realizada correctamente.';
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        -- Rollback seguro en caso de error
        RAISE NOTICE 'Ocurrió un error: %', SQLERRM;
        DELETE FROM reserva WHERE id_reserva = p_idReserva;
        p_resultado := 'ERROR: No se pudo completar la reserva.'||SQLERRM;
END;
$$;




CREATE OR REPLACE PROCEDURE spPreReserva(
    IN p_idVuelo INT,
    IN p_idReserva INT,
    IN p_asientos INT[],
    IN p_rutPasajero TEXT,
    IN p_tiempo_pre_reserva INTERVAL DEFAULT '10 minutes',  -- Tiempo de pre-reserva
    OUT p_resultado TEXT
)
LANGUAGE plpgsql
AS $$
DECLARE
    id_asientoP INT;
    numero_asiento TEXT;
    asientos_reservados TEXT := '';
    tiempo_actual TIMESTAMP := CURRENT_TIMESTAMP;
    tiempo_expiracion TIMESTAMP;
BEGIN
    -- Establecer el tiempo de expiración para la pre-reserva
    tiempo_expiracion := tiempo_actual + p_tiempo_pre_reserva;

    -- Iniciar la transacción para realizar la pre-reserva
    BEGIN
        -- Reservar los asientos seleccionados
        FOR i IN 1..array_length(p_asientos, 1)
        LOOP
            id_asientoP := p_asientos[i];

            -- Verificar si el asiento ya está reservado para este vuelo
            IF EXISTS (
                SELECT 1
                FROM reserva_asiento ra
                WHERE ra.id_vuelo = p_idVuelo
                AND ra.id_asiento = id_asientoP
                AND ra.id_reserva != p_idReserva  -- No permitir que se reserven si ya está asociado a otra reserva
                ) THEN
                -- Si el asiento ya está reservado, agregar a la lista de asientos no disponibles
                SELECT numero_asiento INTO numero_asiento
                FROM asiento ast
                WHERE ast.id_asiento = id_asientoP;

                asientos_reservados := asientos_reservados || numero_asiento || ', ';
            ELSE
                -- Insertar el asiento como pre-reservado (sin completar)
                INSERT INTO reserva_asiento (id_reserva, id_asiento, id_vuelo)
                VALUES (p_idReserva, id_asientoP, p_idVuelo);
            END IF;
        END LOOP;

        -- Si hay asientos que no se pudieron reservar, lanzar un error
        IF asientos_reservados <> '' THEN
            p_resultado := 'ERROR: Los siguientes asientos ya están reservados: ' || LEFT(asientos_reservados, LENGTH(asientos_reservados) - 2);
            RAISE EXCEPTION 'Pre-reserva fallida';
        ELSE
            p_resultado := 'Pre-reserva exitosa';
        END IF;

        -- Aquí, puedes dejar la pre-reserva activa durante el tiempo definido por p_tiempo_pre_reserva.
        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            p_resultado := 'Error en la pre-reserva: ' || SQLERRM;
    END;
END;
$$;


