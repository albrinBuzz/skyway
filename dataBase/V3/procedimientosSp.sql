-- ==========================================
-- 1. PROCEDIMIENTO: CONFIRMAR RESERVA
-- ==========================================
CREATE OR REPLACE PROCEDURE spConfirmar_reserva(
    IN p_idVuelo INT,
    IN p_idReserva INT,
    IN p_asientos INT[],
    IN p_rutPasajero TEXT,
    OUT p_resultado TEXT
)
LANGUAGE plpgsql
AS $confirmar_reserva$
DECLARE
reserva_id INT;
    estado_reserva_id INT := 1;
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
WHERE vl.id_vuelo = p_idVuelo;

FOR i IN 1..array_length(p_asientos, 1)
    LOOP
        id_asientoP := p_asientos[i];
        asiento_en_reserva := 0;

        -- Verificar si el asiento ya está reservado (Sin FOR UPDATE para evitar conflicto con EXCEPTION)
SELECT 1 INTO asiento_en_reserva
FROM reserva_asiento ra
WHERE ra.ID_VUELO = p_idVuelo
  AND ra.ID_ASIENTO = id_asientoP;

IF asiento_en_reserva IS NOT NULL AND asiento_en_reserva > 0 THEN
SELECT ast.numero_asiento INTO numero_asiento
FROM asiento ast
WHERE ast.id_asiento = id_asientoP;

asientos_reservados := asientos_reservados || numero_asiento || ', ';
ELSE
            -- Insertar en reserva_asiento
            INSERT INTO reserva_asiento (id_reserva, id_asiento, ID_VUELO, rut)
            VALUES (p_idReserva, id_asientoP, p_idVuelo, p_rutPasajero);
END IF;
END LOOP;

    IF asientos_reservados <> '' THEN
        p_resultado := 'ERROR: Asientos ya reservados: ' || LEFT(asientos_reservados, LENGTH(asientos_reservados) - 2);
DELETE FROM reserva WHERE id_reserva = p_idReserva;
ELSE
        p_resultado := 'OK: Reserva realizada correctamente.';
END IF;
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'Ocurrió un error: %', SQLERRM;
DELETE FROM reserva WHERE id_reserva = p_idReserva;
p_resultado := 'ERROR: No se pudo completar la reserva. ' || SQLERRM;
END;
$confirmar_reserva$;


-- ==========================================
-- 2. PROCEDIMIENTO: VERIFICAR DISPONIBILIDAD
-- ==========================================
CREATE OR REPLACE PROCEDURE spVerificarDisponinibilidadAsientos(
    IN p_idVuelo INT,
    IN p_asientos INT[],
    OUT p_resultado TEXT
)
LANGUAGE plpgsql
AS $verificar_asientos$
DECLARE
id_asientoP INT;
    asiento_en_reserva INT;
    v_numero_asiento TEXT;
    v_asientos_erroneos TEXT := '';
BEGIN
    p_resultado := 'OK';

FOR i IN 1..array_length(p_asientos, 1)
    LOOP
        id_asientoP := p_asientos[i];
        asiento_en_reserva := NULL;

SELECT a.numero_asiento,
       (SELECT 1 FROM reserva_asiento ra
        WHERE ra.id_vuelo = p_idVuelo
          AND ra.id_asiento = id_asientoP LIMIT 1)
INTO v_numero_asiento, asiento_en_reserva
FROM asiento a
WHERE a.id_asiento = id_asientoP;

IF asiento_en_reserva IS NOT NULL THEN
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
$verificar_asientos$;


-- ==========================================
-- 3. PROCEDIMIENTO: CAMBIAR ASIENTO
-- ==========================================
CREATE OR REPLACE PROCEDURE sp_cambiarAsiento(
   IN p_id_asiento INT,
   IN p_id_reserva INT,
   IN p_id_asiento_org INT
)
LANGUAGE plpgsql
AS $cambiar_asiento$
BEGIN
UPDATE reserva_asiento
SET id_asiento = p_id_asiento
WHERE id_reserva = p_id_reserva
  AND id_asiento = p_id_asiento_org;
END;
$cambiar_asiento$;


-- ==========================================
-- 4. PROCEDIMIENTO: UPSERT PASAJERO
-- ==========================================
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
AS $upsert_pasajero$
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
$upsert_pasajero$;