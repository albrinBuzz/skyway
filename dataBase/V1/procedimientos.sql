DO $$
DECLARE

	 cursoFunciones  cursor for
		SELECT routine_name AS function_name,
		routine_type AS function_type
		FROM information_schema.routines
		WHERE   routine_schema = 'public'
		order by routine_type;
BEGIN

	for cols in cursoFunciones
	loop
     RAISE NOTICE 'ID: %, Name: %', cols.function_name, cols.function_type;

	execute ' drop  '|| cols.function_type ||' '||cols.function_name||' cascade';

	end loop;

END $$;




CREATE OR REPLACE PROCEDURE insertar_avion_y_asientos(
    p_numero_registro VARCHAR,
    p_modelo VARCHAR,
    p_fabricante VARCHAR,
    p_ano_fabricacion INT,
    p_capacidad_pasajeros INT,
    p_capacidad_carga INT,
    p_estado_mantenimiento VARCHAR,
	p_cap_economica INT,
	p_cap_ejecutiva INT,
	p_cap_primera INT

)
LANGUAGE plpgsql AS $$
DECLARE
    v_id_avion INT;
    indiceNumero INTEGER := 1;
    letra CHAR;
    asiento VARCHAR;
BEGIN
    -- Insertar el avión y obtener su ID
    INSERT INTO Avion (
        Numero_de_Registro,
        Modelo,
        Fabricante,
        Ano_de_Fabricacion,
        Capacidad_de_Pasajeros,
        Capacidad_de_Carga,
        Estado_de_Mantenimiento,
		cap_economica,
		cap_ejecutiva,
		cap_primera
    ) VALUES (
        p_numero_registro,
        p_modelo,
        p_fabricante,
        p_ano_fabricacion,
        p_capacidad_pasajeros,
        p_capacidad_carga,
        p_estado_mantenimiento,
		p_cap_economica,
		p_cap_ejecutiva,
		p_cap_primera
    );

    SELECT last_value INTO v_id_avion FROM public.avion_seq;

  -- Obtener los IDs de las clases
    --SELECT ID_CLASE INTO v_id_clase_economica FROM Clase_Asiento WHERE Descripcion = 'Económica';
    --SELECT ID_CLASE INTO v_id_clase_ejecutiva FROM Clase_Asiento WHERE Descripcion = 'Ejecutiva';
    --SELECT ID_CLASE INTO v_id_clase_primera FROM Clase_Asiento WHERE Descripcion = 'Primera Clase';

    -- Insertar los asientos para la clase económica
    FOR indice IN 0 .. p_cap_economica - 1 LOOP
        letra := chr(65 + (indice % 6));  -- A-F
        asiento := indice + 1 || letra;   -- Número de asiento (por ejemplo, "1A", "1B", etc.)

        INSERT INTO Asiento (Numero_Asiento, ID_CLASE, ID_AVION)
        VALUES (asiento, 1, v_id_avion);

        -- Incrementar el número de fila cada 6 asientos
        IF (indice + 1) % 6 = 0 THEN
            indice := indice + 1;
        END IF;
    END LOOP;

    -- Insertar los asientos para la clase ejecutiva
    FOR indice IN 0 .. p_cap_ejecutiva - 1 LOOP
        letra := chr(65 + (indice % 6));  -- A-F
        asiento := indice + 1 || letra;   -- Número de asiento (por ejemplo, "1A", "1B", etc.)

        INSERT INTO Asiento (Numero_Asiento, ID_CLASE, ID_AVION)
        VALUES (asiento, 2, v_id_avion);

        -- Incrementar el número de fila cada 6 asientos
        IF (indice + 1) % 6 = 0 THEN
            indice := indice + 1;
        END IF;
    END LOOP;

    -- Insertar los asientos para la clase primera
    FOR indice IN 0 .. p_cap_primera - 1 LOOP
        letra := chr(65 + (indice % 6));  -- A-F
        asiento := indice + 1 || letra;   -- Número de asiento (por ejemplo, "1A", "1B", etc.)

        INSERT INTO Asiento (Numero_Asiento, ID_CLASE, ID_AVION)
        VALUES (asiento, 3, v_id_avion);

        -- Incrementar el número de fila cada 6 asientos
        IF (indice + 1) % 6 = 0 THEN
            indice := indice + 1;
        END IF;
    END LOOP;

END;
$$;


/*
    Pasillo
   -------------------------
   | 1A | 1B | 1C |   | 1D | 1E | 1F |
   -------------------------
   | 2A | 2B | 2C |   | 2D | 2E | 2F |
   -------------------------
   | 3A | 3B | 3C |   | 3D | 3E | 3F |
   -------------------------

    DO $$
DECLARE
    indice INTEGER;
    letra CHAR;
	indiceNumero INTEGER:=1;
	indiceLetra INTEGER:=65;
	contVueltas INTEGER:=0;
	 mensaje TEXT:='';
BEGIN
    FOR indice IN 0 .. 18 LOOP  -- Del 65 al 90, que son las letras A-Z

        letra := chr(indiceLetra);
		indiceLetra:=indiceLetra+1;
		contVueltas:=contVueltas+1;

		mensaje:=mensaje || indiceNumero||'-'||letra|| ' ';

		IF contVueltas =6 THEN
			RAISE NOTICE '%', mensaje;
			indiceLetra:=65;
			indiceNumero:=indiceNumero+1;
			contVueltas:=0;
			mensaje:='';
		else

		END IF;




    END LOOP;
END $$;

*/



-- Crear la función del trigger
CREATE OR REPLACE FUNCTION fn_insertarAsientos()
RETURNS TRIGGER AS $$
DECLARE
    indice INTEGER;
    letra CHAR;
    asiento VARCHAR;
BEGIN
    -- Insertar los asientos para la clase económica



    FOR indice IN 0 .. NEW.cap_economica - 1 LOOP
        letra := chr(65 + (indice % 6));  -- A-F
        asiento := indice + 1 || letra;   -- Número de asiento (por ejemplo, "1A", "1B", etc.)

        INSERT INTO Asiento (Numero_Asiento, ID_CLASE, ID_AVION)
        VALUES (asiento, 1, NEW.id_avion);  -- Clase 1 = Económica
    END LOOP;

    -- Insertar los asientos para la clase ejecutiva
    FOR indice IN 0 .. NEW.cap_ejecutiva - 1 LOOP
        letra := chr(65 + (indice % 6));  -- A-F
        asiento := indice + 1 || letra;   -- Número de asiento (por ejemplo, "1A", "1B", etc.)

        INSERT INTO Asiento (Numero_Asiento, ID_CLASE, ID_AVION)
        VALUES (asiento, 2, NEW.id_avion);  -- Clase 2 = Ejecutiva
    END LOOP;

    -- Insertar los asientos para la clase primera
    FOR indice IN 0 .. NEW.cap_primera - 1 LOOP
        letra := chr(65 + (indice % 6));  -- A-F
        asiento := indice + 1 || letra;   -- Número de asiento (por ejemplo, "1A", "1B", etc.)

        INSERT INTO Asiento (Numero_Asiento, ID_CLASE, ID_AVION)
        VALUES (asiento, 3, NEW.id_avion);  -- Clase 3 = Primera Clase
    END LOOP;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;



-- Crear el trigger que llama a la función cuando se inserta un avión
CREATE TRIGGER trigger_insertar_asientos
AFTER INSERT ON Avion
FOR EACH ROW
EXECUTE FUNCTION fn_insertarAsientos();





--buscar vuelosCREATE OR REPLACE FUNCTION obtener_info_vuelo(
CREATE OR REPLACE FUNCTION obtener_info_vuelo(
    p_ciudad_salida VARCHAR,
    p_ciudad_llegada VARCHAR,
    p_fecha_inicio VARCHAR,
    p_fecha_fin VARCHAR
)RETURNS TABLE (
	destino varchar,
	id_avion int,
    id_vuelo int,
    numero_vuelo text,
    ciudad_salida text,
    ciudad_llegada text,
    fecha_hora_salida TIMESTAMP,
    fecha_hora_llegada TIMESTAMP,
    precio text,
    modelo_avion text,
	duracion text

) AS $$
BEGIN
    RETURN QUERY
	    SELECT
		p1.nombre,
		av.id_avion,
        v.ID_VUELO,
        v.Numero_Vuelo::text,
        ci1.nombre||' - '||a1.nombre_aeropuerto||' ('||a1.codigo_iata||')' ,
        ci2.nombre||' - '||a2.nombre_aeropuerto||' ('||a2.codigo_iata||')'  ,
		v.Fecha_Hora_Salida,
		v.Fecha_Hora_Llegada,
        to_char(v.Precio,'FM$999,999'),
        av.modelo::text,
		 --(EXTRACT(EPOCH FROM (v.Fecha_Hora_Llegada - v.Fecha_Hora_Salida)) / 3600),
	CAST(EXTRACT(HOUR FROM (v.Fecha_Hora_Llegada - v.Fecha_Hora_Salida)) AS VARCHAR) || 'h ' ||
	CAST(EXTRACT(MINUTE FROM (v.Fecha_Hora_Llegada - v.Fecha_Hora_Salida)) AS VARCHAR) || 'm'
    FROM
        Vuelo v
    JOIN
        Aeropuerto a1 ON v.ID_AEROPUERTO_SALIDA = a1.ID_AEROPUERTO
    JOIN
        Aeropuerto a2 ON v.ID_AEROPUERTO_LLEGADA = a2.ID_AEROPUERTO
    JOIN
        Avion av ON v.ID_AVION = av.ID_AVION
    JOIN
        Ciudad ci1 ON a1.Ciudad = ci1.ID_CIUDAD
    JOIN
        Ciudad ci2 ON a2.Ciudad = ci2.ID_CIUDAD
	Join pais p1 on p1.id_pais = ci2.id_pais
    WHERE
        ci1.Nombre = p_ciudad_salida AND
        ci2.Nombre = p_ciudad_llegada AND
       v.Fecha_Hora_Salida BETWEEN to_timestamp(p_fecha_inicio, 'YYYY-MM-DD') AND to_timestamp(p_fecha_fin, 'YYYY-MM-DD');
END;
$$ LANGUAGE plpgsql;

SELECT * FROM obtener_info_vuelo('São Paulo', 'Ciudad de México', '2024-09-22', '2024-12-24');

SELECT * FROM obtener_info_vuelo('Santiago', 'Madrid', '2024-09-22', '2024-12-24');




CREATE OR REPLACE FUNCTION fn_VuelosProximos()
RETURNS TABLE (
	destino varchar,
	id_avion int,
    id_vuelo int,
    numero_vuelo text,
    ciudad_salida text,
    ciudad_llegada text,
    fecha_hora_salida TIMESTAMP,
    fecha_hora_llegada TIMESTAMP,
    precio text,
    modelo_avion text,
	duracion text

) AS $$
BEGIN
    RETURN QUERY
	    SELECT
		p1.nombre,
		COALESCE( av.id_avion,1),
        v.ID_VUELO,
        v.Numero_Vuelo::text,
        ci1.nombre||' - '||a1.nombre_aeropuerto||' ('||a1.codigo_iata||')' ,
        ci2.nombre||' - '||a2.nombre_aeropuerto||' ('||a2.codigo_iata||')'  ,
		v.Fecha_Hora_Salida,
		v.Fecha_Hora_Llegada,
        to_char(v.Precio,'FM$999,999'),
        av.modelo::text,
	 round(EXTRACT(EPOCH FROM (v.Fecha_Hora_Llegada::timestamp - v.Fecha_Hora_Salida::timestamp)) / 3600)||'h'

    FROM
        Vuelo v
    JOIN
        Aeropuerto a1 ON v.ID_AEROPUERTO_SALIDA = a1.ID_AEROPUERTO
    JOIN
        Aeropuerto a2 ON v.ID_AEROPUERTO_LLEGADA = a2.ID_AEROPUERTO
    Left JOIN
        Avion av ON v.ID_AVION = av.ID_AVION
    JOIN
        Ciudad ci1 ON a1.Ciudad = ci1.ID_CIUDAD
    JOIN
        Ciudad ci2 ON a2.Ciudad = ci2.ID_CIUDAD
	Join pais p1 on p1.id_pais = ci2.id_pais
	where 	extract (day from( v.Fecha_Hora_Llegada - v.Fecha_Hora_Salida)) <=20
	and v.Fecha_Hora_Salida>=current_date
	order by v.Fecha_Hora_Salida
		limit 10;
		--where extract(day from AGE(v.Fecha_Hora_Salida, CURRENT_TIMESTAMP))<=10;

END;
$$ LANGUAGE plpgsql;

select * from fn_VuelosProximos();


--buscar Vuelo
CREATE OR REPLACE FUNCTION fn_getVueloInfo(p_idVuelo integer)
RETURNS TABLE (
	destino varchar,
	id_avion int,
    id_vuelo int,
    numero_vuelo text,
    ciudad_salida text,
    ciudad_llegada text,
    fecha_hora_salida TIMESTAMP,
    fecha_hora_llegada TIMESTAMP,
    precio text,
    modelo_avion text,
	duracion text

) AS $$
BEGIN
    RETURN QUERY
	    SELECT
		p1.nombre,
		av.id_avion,
        v.ID_VUELO,
        v.Numero_Vuelo::text,
        ci1.nombre||' - '||a1.nombre_aeropuerto||' ('||a1.codigo_iata||')' ,
        ci2.nombre||' - '||a2.nombre_aeropuerto||' ('||a2.codigo_iata||')'  ,
		v.Fecha_Hora_Salida,
		v.Fecha_Hora_Llegada,
        to_char(v.Precio,'FM$999,999'),
        av.modelo::text,
		 --(EXTRACT(EPOCH FROM (v.Fecha_Hora_Llegada - v.Fecha_Hora_Salida)) / 3600),
	CAST(EXTRACT(HOUR FROM (v.Fecha_Hora_Llegada - v.Fecha_Hora_Salida)) AS VARCHAR) || 'h ' ||
	CAST(EXTRACT(MINUTE FROM (v.Fecha_Hora_Llegada - v.Fecha_Hora_Salida)) AS VARCHAR) || 'm'
    FROM
        Vuelo v
    JOIN
        Aeropuerto a1 ON v.ID_AEROPUERTO_SALIDA = a1.ID_AEROPUERTO
    JOIN
        Aeropuerto a2 ON v.ID_AEROPUERTO_LLEGADA = a2.ID_AEROPUERTO
    JOIN
        Avion av ON v.ID_AVION = av.ID_AVION
    JOIN
        Ciudad ci1 ON a1.Ciudad = ci1.ID_CIUDAD
    JOIN
        Ciudad ci2 ON a2.Ciudad = ci2.ID_CIUDAD
	Join pais p1 on p1.id_pais = ci2.id_pais
		where v.ID_VUELO=p_idVuelo;
		--where extract(day from AGE(v.Fecha_Hora_Salida, CURRENT_TIMESTAMP))<=10;

END;
$$ LANGUAGE plpgsql;


select * from fn_getVueloInfo(23);



--obtener los asinetos de un avion con su estado

CREATE OR REPLACE PROCEDURE sp_getAsientosAvion(IN p_id_avion INT, OUT cursor_asientos REFCURSOR)
LANGUAGE plpgsql AS $$
BEGIN
    -- Abrimos el cursor
    OPEN cursor_asientos FOR
        SELECT
            a.id_asiento,
            a.numero_asiento,
            CASE
                WHEN rvs.id_reserva IS NULL THEN 'libre'
                ELSE 'ocupado'
            END AS estado
        FROM asiento a
        LEFT JOIN reserva_asiento rvs ON rvs.id_asiento = a.id_asiento
        WHERE a.id_avion = p_id_avion;
END;
$$;




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
            a.id_asiento,
            a.numero_asiento,
			(CASE WHEN rsv.id_reserva IS NOT NULL THEN 'ocupado' ELSE 'libre' END) as estado,
			ps.precio,
			cls.descripcion
        FROM vuelo vl
		INNER join avion av
		on av.id_avion = vl.id_avion
		right join asiento a
		on a.id_avion = vl.id_avion
		right join precio_asiento ps
		on ps.id_clase = a.id_clase
		and ps.id_vuelo = vl.id_vuelo
		join clase_asiento cls
		on cls.id_clase = ps.id_clase
		left join reserva rv
		on rv.id_vuelo = vl.id_vuelo
		left join reserva_asiento rsv
		on rsv.id_asiento = a.id_asiento
		and rsv.id_reserva = rv.id_reserva
        WHERE vl.ID_VUELO=p_idVuelo
		order by a.numero_asiento;
END;
$$ LANGUAGE plpgsql;



      		SELECT
            a.id_asiento,
            a.numero_asiento,
			(CASE WHEN rsv.id_reserva IS NOT NULL THEN 'ocupado' ELSE 'libre' END) as estado,
			ps.precio,
			cls.descripcion
        FROM vuelo vl
		INNER join avion av
		on av.id_avion = vl.id_avion
		right join asiento a
		on a.id_avion = vl.id_avion
		right join precio_asiento ps
		on ps.id_clase = a.id_clase
		and ps.id_vuelo = vl.id_vuelo
		join clase_asiento cls
		on cls.id_clase = ps.id_clase
		left join reserva rv
		on rv.id_vuelo = vl.id_vuelo
		left join reserva_asiento rsv
		on rsv.id_asiento = a.id_asiento
		and rsv.id_reserva = rv.id_reserva
        WHERE vl.ID_VUELO=6
		order by a.numero_asiento;


--buscar asientos disponibles del vuelo, junto con los asientos orginimalemte seleccionados para cambio
CREATE OR REPLACE procedure sp_getAsientoSeleccionados(IN p_id_reserva INT,in p_idVuelo int,out cursor_asientos REFCURSOR)
AS $$
BEGIN

   open cursor_asientos for
		   	SELECT
		    a.id_asiento,
		    a.numero_asiento,
		    (CASE
				WHEN rsv.ID_RESERVA=p_id_reserva  THEN 'seleccionado'
		        WHEN rsv.id_reserva IS NOT NULL THEN 'ocupado'
		        ELSE 'libre'
		     END) AS estado,
		    ps.precio,
		    cls.descripcion
		FROM
		    vuelo vl
		INNER JOIN
		    avion av ON av.id_avion = vl.id_avion
		RIGHT JOIN
		    asiento a ON a.id_avion = vl.id_avion
		RIGHT JOIN
		    precio_asiento ps ON ps.id_clase = a.id_clase
		    AND ps.id_vuelo = vl.id_vuelo
		JOIN
		    clase_asiento cls ON cls.id_clase = ps.id_clase
		LEFT JOIN
		    reserva rv ON rv.id_vuelo = vl.id_vuelo
		LEFT JOIN
		    reserva_asiento rsv ON rsv.id_asiento = a.id_asiento
		    AND rsv.id_reserva = rv.id_reserva
		WHERE
		    vl.ID_VUELO =p_idVuelo  and rv.id_reserva=p_id_reserva
		ORDER BY
		    a.numero_asiento;


END;
$$ LANGUAGE plpgsql;





--select * from fn_getAsientosAvion(1,34);



SELECT
    a.ID_ASIENTO,
    a.Numero_Asiento,
    (CASE
        WHEN ra.ID_RESERVA IS NOT NULL THEN 'ocupado'
        ELSE 'libre'
    END) AS Estado
FROM
    Asiento a
JOIN
    Avion av ON a.ID_AVION = av.ID_AVION
LEFT JOIN
    Reserva_Asiento ra ON a.ID_ASIENTO = ra.ID_ASIENTO
LEFT JOIN
    Reserva r ON ra.ID_RESERVA = r.ID_RESERVA
WHERE
    av.ID_AVION = (SELECT ID_AVION FROM Vuelo WHERE ID_VUELO = 6) -- Reemplaza ? con el ID del vuelo
ORDER BY
    a.Numero_Asiento;

--consumir el prodedimiento
DO $$
DECLARE
    r RECORD;  -- Variable para almacenar cada fila
	cursor_asientos REFCURSOR;
BEGIN
    -- Llamar al procedimiento
    CALL sp_getAsientosAvion(1, cursor_asientos);

    -- Bucle para obtener filas una por una
    LOOP
        FETCH cursor_asientos INTO r;  -- Obtener la siguiente fila en 'r'
        EXIT WHEN NOT FOUND;      -- Salir del bucle si no hay más filas

        -- Hacer algo con cada fila, por ejemplo, mostrar los resultados
        RAISE NOTICE 'ID: %, Número: %, Estado: %', r.id_asiento, r.numero_asiento, r.estado;
    END LOOP;

    -- Cerrar el cursor
    CLOSE cursor_asientos;
END $$;


--prodedimineto de vuelos usuario

CREATE OR REPLACE PROCEDURE sp_vuelosPasajero(IN p_rut_pasajero varchar, OUT cursos_reservas REFCURSOR)
LANGUAGE plpgsql AS $$
BEGIN
    -- Abrimos el cursor
    OPEN cursos_reservas FOR
		SELECT
		    v.ID_VUELO,
            r.id_reserva,
		    v.Numero_Vuelo,
		    v.Fecha_Hora_Salida,
		    v.Fecha_Hora_Llegada,
		    a_s.Nombre_Aeropuerto||' - '|| c1.nombre||' - '||p1.nombre AS Aeropuerto_Salida,
		    a_l.Nombre_Aeropuerto||' - '|| c2.nombre||' - '||p2.nombre AS Aeropuerto_Llegada,
		    r.Fecha_Reserva,
		    etv.estado AS Estado_Reserva,
		    v.Precio
		FROM
		   Reserva r
		JOIN
		    Vuelo v ON r.ID_VUELO = v.ID_VUELO
		JOIN
		    Pasajero p ON r.RUT_PASAJERO = p.RUT
		JOIN
		    Aeropuerto a_s ON v.ID_AEROPUERTO_SALIDA = a_s.ID_AEROPUERTO
		JOIN
		    Aeropuerto a_l ON v.ID_AEROPUERTO_LLEGADA = a_l.ID_AEROPUERTO
		JOIN
		    Estado_reserva er ON r.Estado_Reserva = er.ID_ESTADO_RESERVA
		JOIN
			estado_vuelo etv on etv.id_estado_vuelo = v.id_estado_vuelo
		JOIN
			ciudad c1 on c1.id_ciudad = a_s.ciudad
		JOIN
			ciudad c2 on c2.id_ciudad = a_l.ciudad
		Join
			Pais p1 on p1.id_pais = c1.id_pais
		join
			pais p2 on p2.id_pais = p1.id_pais
        where p.rut=p_rut_pasajero;
END;
$$;


--buscar reservas por pasajero
CREATE OR REPLACE PROCEDURE sp_vuelosPasajero(IN p_rut_pasajero varchar, OUT cursos_reservas REFCURSOR)
LANGUAGE plpgsql AS $$
BEGIN
    -- Abrimos el cursor
    OPEN cursos_reservas FOR
		SELECT
		    v.ID_VUELO,
            r.id_reserva,
		    v.Numero_Vuelo,
		    v.Fecha_Hora_Salida,
		    v.Fecha_Hora_Llegada,
		    a_s.Nombre_Aeropuerto||' - '|| c1.nombre||' - '||p1.nombre AS Aeropuerto_Salida,
		    a_l.Nombre_Aeropuerto||' - '|| c2.nombre||' - '||p2.nombre AS Aeropuerto_Llegada,
		    r.Fecha_Reserva,
		    etv.descripcion AS Estado_Reserva,
		    v.Precio
		FROM
		   Reserva r
		JOIN
		    Vuelo v ON r.ID_VUELO = v.ID_VUELO
		JOIN
		    Pasajero p ON r.RUT_PASAJERO = p.RUT
		JOIN
		    Aeropuerto a_s ON v.ID_AEROPUERTO_SALIDA = a_s.ID_AEROPUERTO
		JOIN
		    Aeropuerto a_l ON v.ID_AEROPUERTO_LLEGADA = a_l.ID_AEROPUERTO
		JOIN
		    Estado_reserva er ON r.Estado_Reserva = er.ID_ESTADO_RESERVA
		JOIN
			estado_vuelo etv on etv.id_estado_vuelo = v.id_estado_vuelo
		JOIN
			ciudad c1 on c1.id_ciudad = a_s.ciudad
		JOIN
			ciudad c2 on c2.id_ciudad = a_l.ciudad
		Join
			Pais p1 on p1.id_pais = c1.id_pais
		join
			pais p2 on p2.id_pais = c2.id_pais
        where p.rut=p_rut_pasajero;
END;
$$;




--procedimiento para obtener el tipo de usuario segun el correo
CREATE OR REPLACE PROCEDURE sp_obtener_usuario_por_correo(
    p_correo_electronico VARCHAR,
    OUT resultado_cursor REFCURSOR
)
LANGUAGE plpgsql AS $$
BEGIN
    -- Abrir un cursor
    OPEN resultado_cursor FOR
    SELECT
		u1_0.id_rol,
        CASE
            WHEN u1_1.rut IS NOT NULL THEN 1
            WHEN u1_2.rut IS NOT NULL THEN 2
            WHEN u1_0.rut IS NOT NULL THEN 0
        END AS tipo_usuario,
        u1_0.rut,
        u1_0.apellido,
        u1_0.contrasena,
        u1_0.correo_electronico,
        u1_0.documento_identidad,
        u1_0.fecha_nacimiento,
        u1_0.nombre,
        u1_0.rol,
        u1_0.telefono,
        u1_1.experiencia_anos,
        u1_1.licencia
    FROM
        Usuario u1_0
	LEFT JOIN
        Piloto u1_1 ON u1_0.correo_electronico = u1_1.correo_electronico
    LEFT JOIN
        Pasajero u1_2 ON u1_0.correo_electronico = u1_2.correo_electronico

    WHERE
        u1_0.correo_electronico = p_correo_electronico;
END;
$$;




---cancelar una reserva

CREATE OR REPLACE PROCEDURE sp_cancelar_reserva(
    p_id_reserva int
)
LANGUAGE plpgsql AS
$$
BEGIN


	delete from reserva_asiento
	where  id_reserva=p_id_reserva;

	delete from reserva
	WHERE ID_RESERVA = p_id_reserva;

	/*UPDATE Reserva
    SET Estado_Reserva = (SELECT ID_ESTADO_RESERVA FROM Estado_reserva WHERE Descripcion = 'Cancelada')
    WHERE ID_RESERVA = p_id_reserva;*/




END;
$$;




--cambiar asiento

CREATE or replace PROCEDURE sp_cambiarAsiento(
   in p_id_asiento int ,
   in p_id_reserva int,
   in p_id_asiento_org int
)
LANGUAGE plpgsql
AS $$
BEGIN

  	update reserva_asiento
	  set id_asiento=p_id_asiento
	  where id_reserva=p_id_reserva
	  and id_asiento=p_id_asiento_org;


END;
$$;







--obtener pasaje o boleto de la reserva
CREATE OR REPLACE PROCEDURE sp_getBoletoReserva(
    id_reserva_input INT,
    resultado_cursor OUT REFCURSOR
)
LANGUAGE plpgsql
AS
$$
BEGIN
    -- Abrimos el cursor con la consulta deseada
    OPEN resultado_cursor FOR
    SELECT
        -- Información del pasajero
        psj.nombre || ' ' || psj.apellido AS nombre_completo,
        psj.documento_identidad,
        psj.correo_electronico,

        -- Información del vuelo
        vl.numero_vuelo,

        -- Fecha y hora de salida (con formato)
        TO_CHAR(vl.fecha_hora_salida, 'YYYY-MM-DD HH24:MI:SS') AS fecha_salida_completa,

        -- Hora de salida (solo la hora)
        TO_CHAR(vl.fecha_hora_salida, 'HH24:MI:SS') AS hora_salida,

        -- Información de los aeropuertos de salida y llegada
        c1.nombre || '(' || arp1.codigo_iata || ')' AS aeropuerto_salida,
        c2.nombre || '(' || arp2.codigo_iata || ')' AS aeropuerto_llegada,

        -- Duración del vuelo
        EXTRACT(HOUR FROM (vl.fecha_hora_llegada - vl.fecha_hora_salida)) AS duracion_vuelo,

        -- Precio total del vuelo (subconsulta)
        (
          select sum(ps.precio) from reserva rsv
		  join reserva_asiento rsva
		  on rsva.id_reserva = rsv.id_reserva
		  join vuelo vl
		  on vl.id_vuelo = rsv.id_vuelo
		  join asiento ast
		  on ast.id_asiento = rsva.id_asiento
		  join clase_asiento cls
		  on cls.id_clase = ast.id_clase
		  join precio_asiento ps
		  on ps.id_clase = ast.id_clase
		  and ps.id_vuelo = rsv.id_vuelo
		  where rsv.id_reserva=rv.id_reserva
        ) AS precio_total

    FROM
        reserva rv
    JOIN vuelo vl ON rv.id_vuelo = vl.id_vuelo
    JOIN pasajero psj ON psj.rut = rv.rut_pasajero
    JOIN aeropuerto arp1 ON arp1.id_aeropuerto = vl.id_aeropuerto_salida
    JOIN aeropuerto arp2 ON arp2.id_aeropuerto = vl.id_aeropuerto_llegada
    JOIN ciudad c1 ON c1.id_ciudad = arp1.ciudad
    JOIN ciudad c2 ON c2.id_ciudad = arp2.ciudad
    -- Filtro por reserva
    WHERE rv.id_reserva = id_reserva_input;

END;
$$;



CREATE OR REPLACE FUNCTION fn_asignar_Nro_vuelo()
	  RETURNS TRIGGER
  LANGUAGE PLPGSQL
  AS
$$

DECLARE
    id_arp1 vuelo.id_aeropuerto_salida%TYPE;
    id_arp2 vuelo.id_aeropuerto_llegada%TYPE;
    codigoArp1 varchar(30);
    codigoArp2 varchar(30);
BEGIN
    -- Obtener códigos de los aeropuertos
    id_arp1 := new.id_aeropuerto_salida;
    id_arp2 := new.id_aeropuerto_llegada;

    -- Obtener los códigos IATA de los aeropuertos de salida y llegada
    SELECT apr1.codigo_iata, apr2.codigo_iata
    INTO codigoArp1, codigoArp2
    FROM aeropuerto apr1, aeropuerto apr2
    WHERE apr1.id_aeropuerto = id_arp1
      AND apr2.id_aeropuerto = id_arp2;

     NEW.Numero_Vuelo := codigoArp1||'-'||codigoArp2||''||new.id_vuelo;


    RETURN NEW;
END;
$$;

CREATE OR REPLACE trigger tr_asignar_Nro_vuelo
before insert on vuelo
for each row
execute function fn_asignar_Nro_vuelo();


ALTER TABLE vuelo
ENABLE TRIGGER tr_asignar_Nro_vuelo;






CREATE OR REPLACE FUNCTION fn_notificacionVueloEstado()
RETURNS TRIGGER
LANGUAGE PLPGSQL
AS
$$
DECLARE
    pasajero RECORD;
    mensaje TEXT;
BEGIN
    -- Iterar sobre cada pasajero asociado al vuelo actualizado
    FOR pasajero IN
        SELECT rsv.rut_pasajero, vl.numero_vuelo
        FROM reserva rsv
        JOIN vuelo vl ON rsv.id_vuelo = vl.id_vuelo
        WHERE vl.id_vuelo = NEW.id_vuelo
    LOOP
        -- Construir el mensaje de notificación con detalles específicos
        mensaje := 'Estimado/a pasajero/a, su vuelo número ' || pasajero.numero_vuelo ||
                   ' ha sido actualizado. ';

        -- Incluir información sobre la nueva hora de salida

		IF OLD.fecha_hora_salida IS DISTINCT FROM NEW.fecha_hora_salida THEN
			  mensaje := mensaje || 'La nueva hora de salida es: ' || TO_CHAR(NEW.fecha_hora_salida, 'DD/MM/YYYY HH24:MI') || '. ';

        /*IF NEW.fecha_hora_salida IS NOT NULL THEN
            mensaje := mensaje || 'La nueva hora de salida es: ' || TO_CHAR(NEW.fecha_hora_salida, 'DD/MM/YYYY HH24:MI') || '. ';
        ELSE
            mensaje := mensaje || 'La hora de salida no ha sido modificada. ';*/
        END IF;

        -- Incluir información sobre la nueva hora de llegada
        IF OLD.fecha_hora_llegada IS DISTINCT FROM NEW.fecha_hora_llegada THEN
            mensaje := mensaje || 'La nueva hora de llegada es: ' || TO_CHAR(NEW.fecha_hora_llegada, 'DD/MM/YYYY HH24:MI') || '. ';
        ELSE
            --mensaje := mensaje || 'La hora de llegada no ha sido modificada. ';
        END IF;

        -- Añadir información adicional si está disponible
        /*IF NEW.id_aeropuerto_salida IS NOT NULL THEN
            mensaje := mensaje || 'Aeropuerto de salida: ' || NEW.id_aeropuerto_salida || '. ';
        END IF;
        IF NEW.id_aeropuerto_llegada IS NOT NULL THEN
            mensaje := mensaje || 'Aeropuerto de llegada: ' || NEW.id_aeropuerto_llegada || '. ';
        END IF;
        IF NEW.precio IS NOT NULL THEN
            mensaje := mensaje || 'Precio del boleto: $' || NEW.precio || '. ';
        END IF;
        IF NEW.rut_piloto IS NOT NULL THEN


            mensaje := mensaje || 'Piloto a cargo: ' || NEW.rut_piloto || '. ';
        END IF;*/

        -- Insertar la notificación en la tabla correspondiente
        INSERT INTO notificacion(rut, titulo, mensaje, fecha, leida)
        VALUES (
            pasajero.rut_pasajero,
            'Actualización de Vuelo: ' || pasajero.numero_vuelo,
            mensaje,
            NOW(),
            FALSE
        );
    END LOOP;
    RETURN NEW;
END;
$$;



-- Crear el Trigger para enviar notificaciones después de actualizar un vuelo
CREATE OR REPLACE TRIGGER tr_notificacionVueloEstado
AFTER UPDATE ON vuelo
FOR EACH ROW
EXECUTE FUNCTION fn_notificacionVueloEstado();


	ALTER TABLE vuelo
	ENABLE TRIGGER tr_notificacionVueloEstado;



CREATE OR REPLACE FUNCTION fn_insertarPreciosAsientos()
RETURNS VOID AS $$
DECLARE
    -- Definir un cursor para recorrer la tabla de empleados
    cur_vuelos CURSOR FOR
        SELECT id_vuelo
        FROM vuelo;

	v_precios NUMERIC[] := ARRAY[1200, 800, 500];

    -- Variables para almacenar los valores recuperados por el cursor
    v_id INT;
BEGIN
    -- Abrir el cursor
    OPEN cur_vuelos;

    -- Loop para recorrer los registros del cursor
    LOOP
        -- Fetch (traer) una fila del cursor
        FETCH cur_vuelos INTO v_id;

        -- Si no hay más filas, salir del bucle
        EXIT WHEN NOT FOUND;

		INSERT INTO precio_asiento (id_precio_asiento, precio, id_vuelo, ID_CLASE) VALUES
		(NEXTVAL('precio_asiento_seq'), v_precios[3], (SELECT ID_VUELO FROM vuelo WHERE id_vuelo = v_id ), 1),  -- Económica
		(NEXTVAL('precio_asiento_seq'), v_precios[2], (SELECT ID_VUELO FROM vuelo WHERE id_vuelo = v_id), 2),  -- Business
		(NEXTVAL('precio_asiento_seq'), v_precios[1], (SELECT ID_VUELO FROM vuelo WHERE id_vuelo = v_id), 3);  -- Primera clase


    END LOOP;

    -- Cerrar el cursor
    CLOSE cur_vuelos;
END;
$$ LANGUAGE plpgsql;







-- Función genérica para verificar disponibilidad de recursos en un rango de fechas
CREATE OR REPLACE FUNCTION fn_verificarDisponibilidad()
RETURNS TRIGGER AS $$
BEGIN
    -- Verificar si el recurso (avión, piloto, etc.) ya tiene un compromiso en el mismo rango de fechas
    IF EXISTS (
        SELECT 1
        FROM vuelo vl
        WHERE vl.id_vuelo  = NEW.id_vuelo
        AND (
            -- Verificar si las fechas de salida y llegada se superponen
            (NEW.fecha_hora_salida BETWEEN vl.fecha_hora_salida AND vl.fecha_hora_llegada)
            OR (NEW.fecha_hora_llegada BETWEEN vl.fecha_hora_salida AND vl.fecha_hora_llegada)
        )
    ) THEN
        -- Si existe un vuelo en esas fechas, levantar una excepción
             -- Si el recurso (avión) ya tiene un vuelo en esas fechas, levantar una excepción con detalles específicos
        RAISE EXCEPTION 'El avion ya tiene un vuelo programado en el rango de fechas-> % - %.',
                         NEW.fecha_hora_salida,  -- Fecha y hora de salida del nuevo vuelo
                         NEW.fecha_hora_llegada;  -- Fecha y hora de llegada del nuevo vuelo
    END IF;

    -- Se pueden agregar más validaciones para otros recursos si es necesario
    -- Ejemplo para piloto:
    IF EXISTS (
        SELECT 1
        FROM vuelo vl
        JOIN piloto pl ON vl.rut_piloto = pl.rut
        WHERE pl.rut = NEW.rut_piloto
        AND (
            -- Verificar si las fechas de salida y llegada se superponen
            (NEW.fecha_hora_salida BETWEEN vl.fecha_hora_salida AND vl.fecha_hora_llegada)
            OR (NEW.fecha_hora_llegada BETWEEN vl.fecha_hora_salida AND vl.fecha_hora_llegada)
        )
    ) THEN
        -- Si el piloto ya tiene un vuelo en esas fechas, levantar una excepción
        RAISE EXCEPTION 'El piloto (RUT: %) ya tiene un vuelo programado en el rango de fechas-> % - %.',
                         NEW.rut_piloto,  -- RUT del piloto
                         NEW.fecha_hora_salida,  -- Fecha y hora de salida del nuevo vuelo
                         NEW.fecha_hora_llegada;  -- Fecha y hora de llegada del nuevo vuelo
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Crear el trigger genérico para verificar la disponibilidad de recursos antes de insertar un vuelo
CREATE OR REPLACE TRIGGER tr_verificarDisponibilidad
BEFORE INSERT ON vuelo
FOR EACH ROW
EXECUTE FUNCTION fn_verificarDisponibilidad();

ALTER TABLE vuelo enable TRIGGER tr_verificarDisponibilidad;

