SELECT
    c1.nombre || '-' || aprt1.nombre_aeropuerto || ' ' || aprt1.codigo_iata AS origen,
    c2.nombre || '-' || aprt2.nombre_aeropuerto || ' ' || aprt2.codigo_iata AS destino,
    it.hora_salida,
	it.hora_llegada
FROM itinerario it
JOIN aeropuerto aprt1 ON aprt1.id_aeropuerto = it.origen_aeropuerto
JOIN aeropuerto aprt2 ON aprt2.id_aeropuerto = it.destino_aeropuerto
JOIN ciudad c1 ON c1.id_ciudad = aprt1.id_ciudad
JOIN ciudad c2 ON c2.id_ciudad = aprt2.id_ciudad
WHERE it.hora_salida >= NOW()
  AND it.hora_salida < NOW() + INTERVAL '7 days'
ORDER BY it.hora_salida ASC;

SELECT * FROM fnBuscarVuelo('IAH', 'YVR', '2025-08-31');

SELECT * FROM fn_getItinerariosRutYfechas('12345678-9', 10, 0, '2025-09-01', '2025-09-30');

SELECT * FROM fn_getItinerariosRutYfechas('12345678-9', 10, 0, NULL, NULL);

SELECT * FROM fn_getItinerariosRutYfechas('12345678-9', 10, 0, '2025-09-01', NULL);


SELECT
    it1.id_itinerario AS id_itinerario_ida,
    it2.id_itinerario AS id_itinerario_vuelta,
	    -- Horarios
    it1.hora_salida AS salida_ida,
    --it1.hora_llegada AS llegada_ida,
    it2.hora_salida AS salida_vuelta,
    --it2.hora_llegada AS llegada_vuelta,

    -- Origen y destino del itinerario de ida
    c1.nombre || ' - ' || ap1.nombre_aeropuerto || ' (' || ap1.codigo_iata || ')' AS origen_ida,
    c2.nombre || ' - ' || ap2.nombre_aeropuerto || ' (' || ap2.codigo_iata || ')' AS destino_ida,

    -- Origen y destino del itinerario de vuelta
    c2.nombre || ' - ' || ap2.nombre_aeropuerto || ' (' || ap2.codigo_iata || ')' AS origen_vuelta,
    c1.nombre || ' - ' || ap1.nombre_aeropuerto || ' (' || ap1.codigo_iata || ')' AS destino_vuelta



FROM itinerario it1
JOIN aeropuerto ap1 ON ap1.id_aeropuerto = it1.origen_aeropuerto
JOIN aeropuerto ap2 ON ap2.id_aeropuerto = it1.destino_aeropuerto
JOIN ciudad c1 ON c1.id_ciudad = ap1.id_ciudad
JOIN ciudad c2 ON c2.id_ciudad = ap2.id_ciudad

-- Itinerario de vuelta: origen y destino invertidos
JOIN itinerario it2
    ON it1.origen_aeropuerto = it2.destino_aeropuerto
    AND it1.destino_aeropuerto = it2.origen_aeropuerto
    AND it1.id_itinerario <> it2.id_itinerario
	AND it2.hora_salida >= it1.hora_llegada + INTERVAL '12 days'
	AND it2.hora_salida <= it1.hora_llegada + INTERVAL '30 days'



-- Filtros de fecha
WHERE it1.hora_salida >= NOW()
  AND it1.hora_salida < NOW() + INTERVAL '7 days'
  AND it2.hora_salida >= NOW()
  AND it2.hora_salida < NOW() + INTERVAL '120 days'



ORDER BY it1.hora_salida ASC, it2.hora_salida ASC;




SELECT
  TO_CHAR((SELECT COUNT(*) FROM Reserva),             '9G999G999G999') AS total_reservas,
  TO_CHAR((SELECT COUNT(*) FROM Reserva_Asiento),     '9G999G999G999') AS total_reserva_asiento,
  TO_CHAR((SELECT COUNT(*) FROM Reserva_Itinerario),  '9G999G999G999') AS total_reserva_itinerario,
  TO_CHAR((SELECT COUNT(*) FROM Itinerario),          '9G999G999G999') AS total_itinerarios,
  TO_CHAR((SELECT COUNT(*) FROM Vuelo),               '9G999G999G999') AS total_vuelos,
  TO_CHAR((SELECT COUNT(*) FROM Segmento_Vuelo),      '9G999G999G999') AS total_segmentos_vuelo,
  TO_CHAR((SELECT COUNT(*) FROM turno),      '9G999G999G999') AS total_turnos,
  TO_CHAR((SELECT COUNT(*) FROM turno_tripulacion),      '9G999G999G999') AS total_tur,
  TO_CHAR((SELECT COUNT(*) FROM asignacion_puerta),    '9G999G999G999') AS total_asginacionPuerta,
  TO_CHAR((SELECT COUNT(*) FROM Precio_Asiento),      '9G999G999G999') AS total_precio_asiento,
  TO_CHAR((SELECT COUNT(*) FROM Asiento),             '9G999G999G999') AS total_asientos,
  TO_CHAR((SELECT COUNT(*) FROM Itinerario_Vuelo),    '9G999G999G999') AS total_itinerario_vuelo;

select * from aeropuerto;

select * from itinerario it
left join itinerario_vuelo itv
on itv.id_itinerario = it.id_itinerario
where itv.id_itinerario is null;



SELECT
    v1.ID_VUELO AS ID_VUELO_IDA,
    v2.ID_VUELO AS ID_VUELO_VUELTA,
    ao.Nombre_Aeropuerto AS ORIGEN_IDA,
    ad.Nombre_Aeropuerto AS DESTINO_IDA,
    ad2.Nombre_Aeropuerto AS ORIGEN_VUELTA,
    ao2.Nombre_Aeropuerto AS DESTINO_VUELTA
FROM
    Segmento_Vuelo s1
JOIN Vuelo v1 ON s1.ID_VUELO = v1.ID_VUELO
JOIN Aeropuerto ao ON s1.ID_AEROPUERTO_ORIGEN = ao.ID_AEROPUERTO
JOIN Aeropuerto ad ON s1.ID_AEROPUERTO_DESTINO = ad.ID_AEROPUERTO

-- Buscamos otro vuelo (el de vuelta)
JOIN Segmento_Vuelo s2 ON s1.ID_AEROPUERTO_ORIGEN = s2.ID_AEROPUERTO_DESTINO
                      AND s1.ID_AEROPUERTO_DESTINO = s2.ID_AEROPUERTO_ORIGEN
                      AND s1.ID_VUELO <> s2.ID_VUELO -- debe ser un vuelo diferente
JOIN Vuelo v2 ON s2.ID_VUELO = v2.ID_VUELO
JOIN Aeropuerto ao2 ON s2.ID_AEROPUERTO_ORIGEN = ao2.ID_AEROPUERTO
JOIN Aeropuerto ad2 ON s2.ID_AEROPUERTO_DESTINO = ad2.ID_AEROPUERTO

-- Opcional: filtrar por fechas
-- WHERE v1.Fecha_Hora_Salida < v2.Fecha_Hora_Salida

ORDER BY v1.ID_VUELO, v2.ID_VUELO;

