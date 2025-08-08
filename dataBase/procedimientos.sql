CREATE OR REPLACE PROCEDURE spConfirmar_reserva(
    IN p_idVuelo INT,
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
    id_asiento INT;
    asiento_en_reserva INT;
    numero_asiento TEXT;
    asientos_reservados TEXT := '';
BEGIN
    -- Obtener el avión asignado al vuelo
    SELECT id_avion INTO id_avion
    FROM vuelo
    WHERE id_vuelo = p_idVuelo;

    -- Iniciar transacción (implícita en SP)
    -- Crear la reserva
    INSERT INTO reserva (rut_pasajero, fecha_reserva, estado_reserva, total)
    VALUES (p_rutPasajero, CURRENT_TIMESTAMP, estado_reserva_id, 0)
    RETURNING id_reserva INTO reserva_id;

    FOR i IN 1..array_length(p_asientos, 1)
    LOOP
        id_asiento := p_asientos[i];

        -- Verificar si el asiento ya está reservado en este vuelo
        SELECT COUNT(*) INTO asiento_en_reserva
        FROM reserva_asiento ra
        JOIN reserva r ON ra.id_reserva = r.id_reserva
        WHERE ra.id_asiento = id_asiento
          AND r.id_reserva IN (
              SELECT id_reserva FROM reserva_vuelo WHERE id_vuelo = p_idVuelo
          )
        FOR UPDATE;

        IF asiento_en_reserva > 0 THEN
            SELECT numero_asiento INTO numero_asiento
            FROM asiento
            WHERE id_asiento = id_asiento;

            asientos_reservados := asientos_reservados || numero_asiento || ', ';
        ELSE
            -- Insertar en reserva_asiento
            INSERT INTO reserva_asiento (id_reserva, id_asiento)
            VALUES (reserva_id, id_asiento);
        END IF;
    END LOOP;

    IF asientos_reservados <> '' THEN
        p_resultado := 'ERROR: Asientos ya reservados: ' || LEFT(asientos_reservados, LENGTH(asientos_reservados) - 2);
        -- Puedes eliminar la reserva si quedó sin asientos
        DELETE FROM reserva WHERE id_reserva = reserva_id;
    ELSE
        -- Asociar la reserva con el vuelo
        INSERT INTO reserva_vuelo (id_reserva, id_vuelo)
        VALUES (reserva_id, p_idVuelo);

        p_resultado := 'OK: Reserva realizada correctamente.';
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        -- Rollback seguro en caso de error
        RAISE NOTICE 'Ocurrió un error: %', SQLERRM;
        DELETE FROM reserva WHERE id_reserva = reserva_id;
        p_resultado := 'ERROR: No se pudo completar la reserva.';
END;
$$;
