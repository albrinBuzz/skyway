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
id_asientoP INT;
    asiento_en_reserva INT;
    v_numero_asiento TEXT; -- Variable para el nombre del asiento
    v_asientos_erroneos TEXT := ''; -- Inicializar vacío
BEGIN
    -- Inicializar el resultado como exitoso por defecto
    p_resultado := 'OK';

FOR i IN 1..array_length(p_asientos, 1)
    LOOP
        id_asientoP := p_asientos[i];
        asiento_en_reserva := 0;

        -- 1. Buscar el nombre del asiento y verificar si existe en reserva
        -- Usamos un LEFT JOIN o similar para obtener el nombre aunque no esté reservado
SELECT a.numero_asiento, (SELECT 1 FROM reserva_asiento ra
                          WHERE ra.id_vuelo = p_idVuelo
                            AND ra.id_asiento = id_asientoP LIMIT 1)
INTO v_numero_asiento, asiento_en_reserva
FROM asiento a
WHERE a.id_asiento = id_asientoP;

IF asiento_en_reserva IS NOT NULL THEN
            -- COALESCE evita que el NULL destruya el string
            v_asientos_erroneos := v_asientos_erroneos || v_numero_asiento || ', ';
END IF;
END LOOP;

    IF v_asientos_erroneos <> '' THEN
        p_resultado := 'ERROR: Asientos ya reservados: ' || LEFT(v_asientos_erroneos, LENGTH(v_asientos_erroneos) - 2);
END IF;

EXCEPTION
    WHEN OTHERS THEN
        p_resultado := 'ERROR: Error interno: ' || SQLERRM;
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






CREATE OR REPLACE PROCEDURE sp_upsertPasajero(
    p_rut VARCHAR,
    p_nombre VARCHAR,
    p_apellido VARCHAR,
    p_correo VARCHAR,
    p_telefono VARCHAR,
    p_documento VARCHAR,
    p_fecha_nacimiento DATE,
    p_contrasena VARCHAR
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_id_rol INT;
BEGIN
    -- 1. Insertar o actualizar usuario
    INSERT INTO Usuario(RUT, Nombre, Apellido, Correo_Electronico, Telefono, Documento_Identidad, Fecha_Nacimiento, Contrasena, Fecha_Registro)
    VALUES (p_rut, p_nombre, p_apellido, p_correo, p_telefono, p_documento, p_fecha_nacimiento, p_contrasena, NOW())
    ON CONFLICT (RUT)
    DO UPDATE SET
        Nombre = EXCLUDED.Nombre,
        Apellido = EXCLUDED.Apellido,
        Correo_Electronico = EXCLUDED.Correo_Electronico,
        Telefono = EXCLUDED.Telefono,
        Documento_Identidad = EXCLUDED.Documento_Identidad,
        Fecha_Nacimiento = EXCLUDED.Fecha_Nacimiento,
        Contrasena = EXCLUDED.Contrasena,
        Fecha_Registro = NOW();

    -- 2. Insertar o actualizar Pasajero
    INSERT INTO Pasajero(RUT, Tipo_Documento, Numero_Documento, Fecha_Nacimiento, Nacionalidad)
    VALUES (p_rut, 'DNI', p_documento, p_fecha_nacimiento, 'Desconocida')
    ON CONFLICT (RUT)
    DO UPDATE SET
        Tipo_Documento = EXCLUDED.Tipo_Documento,
        Numero_Documento = EXCLUDED.Numero_Documento,
        Fecha_Nacimiento = EXCLUDED.Fecha_Nacimiento,
        Nacionalidad = EXCLUDED.Nacionalidad;

    -- 3. Obtener id del rol "Pasajero"
    SELECT id_rol INTO v_id_rol FROM Roles WHERE nombre = 'Pasajero';

    -- 4. Insertar rol si no existe
    INSERT INTO RolUsuario(id_rol, rut_usuario)
    VALUES (v_id_rol, p_rut)
    ON CONFLICT (id_rol, rut_usuario) DO NOTHING;

END;
$$;