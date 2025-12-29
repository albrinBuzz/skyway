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
            SELECT ast.numero_asiento INTO numero_asiento
            FROM asiento ast
            WHERE ast.id_asiento = id_asientoP;

            asientos_reservados := asientos_reservados || numero_asiento || ', ';
        ELSE
            -- Insertar en reserva_asiento
            INSERT INTO reserva_asiento (id_reserva, id_asiento,ID_VUELO,rut)
            VALUES (p_idReserva, id_asientoP,p_idVuelo,p_rutPasajero);
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



CREATE OR REPLACE PROCEDURE spVerificarDisponinibilidadAsientos(
    IN p_idVuelo INT,
    IN p_asientos INT[],
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
    asientos_reservados TEXT := '-';
BEGIN
    -- Obtener el avión asignado al vuelo
    SELECT vl.id_avion INTO id_avion
    FROM vuelo vl
    WHERE id_vuelo = p_idVuelo;


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
            asientos_reservados := asientos_reservados || numero_asiento || ', ';
        END IF;
    END LOOP;

    IF asientos_reservados <> '-' THEN
        p_resultado := 'ERROR: Asientos ya reservados: ' || LEFT(asientos_reservados, LENGTH(asientos_reservados) - 2);
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        -- Rollback seguro en caso de error
        RAISE NOTICE 'Ocurrió un error: %', SQLERRM;
        p_resultado := 'ERROR: No se pudo completar la reserva.'||SQLERRM;
END;
$$;


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