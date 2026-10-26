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
    v_id_avion INT;
    i INT;
    id_asientoP INT;
    v_insertadas INT;
    v_numero_asiento TEXT;
    asientos_reservados TEXT := '';
BEGIN
    SELECT vl.id_avion INTO v_id_avion
    FROM vuelo vl
    WHERE vl.id_vuelo = p_idVuelo;

    IF v_id_avion IS NULL THEN
        p_resultado := 'ERROR: El vuelo no existe.';
        RETURN;
    END IF;

    IF p_asientos IS NULL OR array_length(p_asientos, 1) IS NULL THEN
        p_resultado := 'ERROR: No se indicaron asientos.';
        RETURN;
    END IF;

    FOR i IN 1..array_length(p_asientos, 1)
    LOOP
        id_asientoP := p_asientos[i];

        -- El asiento debe pertenecer al avion asignado al vuelo.
        IF NOT EXISTS (SELECT 1 FROM asiento a WHERE a.id_asiento = id_asientoP AND a.id_avion = v_id_avion) THEN
            p_resultado := 'ERROR: El asiento ' || id_asientoP || ' no pertenece al avion del vuelo.';
            RETURN;
        END IF;

        -- Atomico: el UNIQUE(id_vuelo, id_asiento) decide quien gana la carrera; sin SELECT previo (sin TOCTOU).
        INSERT INTO reserva_asiento (id_reserva, id_asiento, ID_VUELO, rut)
        VALUES (p_idReserva, id_asientoP, p_idVuelo, p_rutPasajero)
        ON CONFLICT (id_vuelo, id_asiento) DO NOTHING;

        GET DIAGNOSTICS v_insertadas = ROW_COUNT;

        IF v_insertadas = 0 THEN
            SELECT ast.numero_asiento INTO v_numero_asiento
            FROM asiento ast
            WHERE ast.id_asiento = id_asientoP;

            asientos_reservados := asientos_reservados || v_numero_asiento || ', ';
        END IF;
    END LOOP;

    IF asientos_reservados <> '' THEN
        -- No se borra la reserva aqui: el llamador (Java) hace rollback de toda la transaccion al recibir ERROR.
        p_resultado := 'ERROR: Asientos ya reservados: ' || LEFT(asientos_reservados, LENGTH(asientos_reservados) - 2);
    ELSE
        p_resultado := 'OK: Reserva realizada correctamente.';
    END IF;
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
-- Reemplaza la version de 3 parametros (sin validacion de dueno).
DROP PROCEDURE IF EXISTS sp_cambiarAsiento(INT, INT, INT);

CREATE OR REPLACE PROCEDURE sp_cambiarAsiento(
   IN p_id_asiento INT,
   IN p_id_reserva INT,
   IN p_id_asiento_org INT,
   IN p_rut_solicitante VARCHAR
)
LANGUAGE plpgsql
AS $cambiar_asiento$
DECLARE
    v_id_vuelo INT;
    v_id_avion INT;
    v_clase_org INT;
    v_clase_new INT;
    v_avion_new INT;
    v_estado INT;
    v_cancelada INT;
    v_filas INT;
BEGIN
    IF p_rut_solicitante IS NULL OR p_rut_solicitante = '' THEN
        RAISE EXCEPTION 'Solicitante no identificado.';
    END IF;

    -- 1. La reserva debe existir, no estar cancelada y pertenecer al solicitante.
    SELECT r.estado_reserva INTO v_estado
    FROM reserva r
    WHERE r.id_reserva = p_id_reserva
      AND (r.rut_pasajero = p_rut_solicitante
           OR EXISTS (SELECT 1 FROM pasajero_reserva pr WHERE pr.id_reserva = r.id_reserva AND pr.rut = p_rut_solicitante)
           OR EXISTS (SELECT 1 FROM reserva_asiento ra WHERE ra.id_reserva = r.id_reserva AND ra.rut = p_rut_solicitante));

    IF NOT FOUND THEN
        RAISE EXCEPTION 'La reserva % no existe o no pertenece al solicitante.', p_id_reserva;
    END IF;

    SELECT id_estado_reserva INTO v_cancelada
    FROM estado_reserva
    WHERE LOWER(descripcion) LIKE '%cancelad%'
    LIMIT 1;
    IF v_cancelada IS NOT NULL AND v_estado = v_cancelada THEN
        RAISE EXCEPTION 'La reserva % esta cancelada.', p_id_reserva;
    END IF;

    -- 2. El asiento original debe estar en esa reserva; se obtiene su vuelo.
    SELECT ra.id_vuelo INTO v_id_vuelo
    FROM reserva_asiento ra
    WHERE ra.id_reserva = p_id_reserva AND ra.id_asiento = p_id_asiento_org;

    IF v_id_vuelo IS NULL THEN
        RAISE EXCEPTION 'El asiento original no pertenece a la reserva.';
    END IF;

    -- 3. Mismo avion (mismo vuelo) y misma clase: cambiar de clase requiere pagar la diferencia.
    SELECT id_avion INTO v_id_avion FROM vuelo WHERE id_vuelo = v_id_vuelo;
    SELECT id_clase INTO v_clase_org FROM asiento WHERE id_asiento = p_id_asiento_org;
    SELECT id_clase, id_avion INTO v_clase_new, v_avion_new FROM asiento WHERE id_asiento = p_id_asiento;

    IF v_avion_new IS DISTINCT FROM v_id_avion THEN
        RAISE EXCEPTION 'El asiento nuevo no pertenece al avion del vuelo.';
    END IF;

    -- TODO(negocio): para cambiar a una clase de mayor precio, cobrar la diferencia por la pasarela antes de
    -- permitirlo. Mientras no exista ese flujo, solo se permite dentro de la misma clase.
    IF v_clase_new IS DISTINCT FROM v_clase_org THEN
        RAISE EXCEPTION 'Solo se permite cambiar a un asiento de la misma clase.';
    END IF;

    -- 4. Cambio atomico; el UNIQUE(id_vuelo, id_asiento) impide tomar un asiento ocupado.
    UPDATE reserva_asiento
    SET id_asiento = p_id_asiento
    WHERE id_reserva = p_id_reserva
      AND id_asiento = p_id_asiento_org
      AND id_vuelo = v_id_vuelo
      AND NOT EXISTS (SELECT 1 FROM reserva_asiento x WHERE x.id_vuelo = v_id_vuelo AND x.id_asiento = p_id_asiento);

    GET DIAGNOSTICS v_filas = ROW_COUNT;
    IF v_filas = 0 THEN
        RAISE EXCEPTION 'El asiento solicitado ya esta ocupado.';
    END IF;
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



CREATE OR REPLACE PROCEDURE sp_upsertPasajero(
    p_rut VARCHAR,
    p_nombre VARCHAR,
    p_apellido VARCHAR,
    p_correo VARCHAR,
    p_telefono VARCHAR,
    p_documento VARCHAR,
    p_fecha_nacimiento DATE,
    p_contrasena VARCHAR,
    p_tipo_documento VARCHAR DEFAULT 'CEDULA',
    p_nacionalidad VARCHAR DEFAULT 'Chilena'
)
LANGUAGE plpgsql
AS $upsert_pasajero$
DECLARE
v_id_rol INT;
BEGIN
    -- 1. Insertar o actualizar Usuario (Si fue creado como acompañante, actualiza datos y contraseña)
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

-- 2. Insertar o actualizar Pasajero con su Tipo de Documento y Nacionalidad reales
INSERT INTO Pasajero(RUT, Tipo_Documento, Numero_Documento, Fecha_Nacimiento, Nacionalidad)
VALUES (p_rut, p_tipo_documento, p_documento, p_fecha_nacimiento, p_nacionalidad)
    ON CONFLICT (RUT)
    DO UPDATE SET
    Tipo_Documento = EXCLUDED.Tipo_Documento,
               Numero_Documento = EXCLUDED.Numero_Documento,
               Fecha_Nacimiento = EXCLUDED.Fecha_Nacimiento,
               Nacionalidad = EXCLUDED.Nacionalidad;

-- 3. Asignar Rol de Pasajero
SELECT id_rol INTO v_id_rol FROM Roles WHERE LOWER(nombre) = 'pasajero' LIMIT 1;

IF v_id_rol IS NOT NULL THEN
        INSERT INTO RolUsuario(id_rol, rut_usuario)
        VALUES (v_id_rol, p_rut)
        ON CONFLICT (id_rol, rut_usuario) DO NOTHING;
END IF;
END;
$upsert_pasajero$;





CREATE OR REPLACE PROCEDURE sp_cancelarReservaPasajero(
    p_id_reserva INT,
    p_rut_pasajero VARCHAR,
    p_motivo VARCHAR DEFAULT 'Cancelación por usuario',
    p_ip_usuario VARCHAR DEFAULT '127.0.0.1'
)
LANGUAGE plpgsql
AS $sp_cancelar$
DECLARE
v_total_reserva DECIMAL(10,2);
    v_id_estado_cancelado INT;
    v_estado_actual INT;
    v_id_cancelacion INT;
    v_pasajeros_reserva TEXT := '';
    v_asientos_count INT := 0;
    r_asiento RECORD;
    r_pasajero RECORD;
BEGIN
    -- 1. Obtención segura del ID de estado "Cancelada"
BEGIN
SELECT id_estado_reserva INTO v_id_estado_cancelado
FROM estado_reserva
WHERE LOWER(descripcion) LIKE '%cancelad%' OR LOWER(descripcion) LIKE '%anulad%'
    LIMIT 1;
EXCEPTION WHEN OTHERS THEN
        v_id_estado_cancelado := NULL;
END;

    IF v_id_estado_cancelado IS NULL THEN
        v_id_estado_cancelado := 3; -- ID numérico fallback
END IF;

    -- 2. Validar que la reserva exista y pertenezca al usuario/pasajero
SELECT total, estado_reserva INTO v_total_reserva, v_estado_actual
FROM reserva
WHERE id_reserva = p_id_reserva
  AND (rut_pasajero = p_rut_pasajero OR id_reserva IN (
    SELECT pr.id_reserva FROM pasajero_reserva pr WHERE pr.rut = p_rut_pasajero
));

IF v_estado_actual IS NULL THEN
        RAISE EXCEPTION 'La reserva N° % no existe o no corresponde al pasajero con RUT %.', p_id_reserva, p_rut_pasajero;
    ELSIF v_estado_actual = v_id_estado_cancelado THEN
        RAISE EXCEPTION 'La reserva N° % ya fue cancelada previamente.', p_id_reserva;
END IF;

    -- 3. Registrar la Cancelación en la tabla Dominio (MOTIVO LIMPIO)
INSERT INTO cancelacion (id_reserva, fecha, motivo, estado, monto_reembolso)
VALUES (
           p_id_reserva,
           NOW(),
           p_motivo,
           'Pendiente',
           v_total_reserva
       )
    RETURNING id_cancelacion INTO v_id_cancelacion;

-- 4. Copiar los asientos a la tabla específica CANCELACION_ASIENTO
FOR r_asiento IN
SELECT
    ra.id_vuelo,
    ra.id_asiento,
    v.numero_vuelo,
    a.numero_asiento,
    ca.descripcion AS clase,
    ra.rut AS rut_ocupante
FROM reserva_asiento ra
         JOIN asiento a ON a.id_asiento = ra.id_asiento
         JOIN clase_asiento ca ON ca.id_clase = a.id_clase
         JOIN vuelo v ON v.id_vuelo = ra.id_vuelo
WHERE ra.id_reserva = p_id_reserva
    LOOP
INSERT INTO cancelacion_asiento (
    id_cancelacion, id_vuelo, id_asiento, numero_vuelo, numero_asiento, clase_asiento, rut_pasajero
) VALUES (
    v_id_cancelacion, r_asiento.id_vuelo, r_asiento.id_asiento, r_asiento.numero_vuelo, r_asiento.numero_asiento, r_asiento.clase, r_asiento.rut_ocupante
    );

v_asientos_count := v_asientos_count + 1;
END LOOP;

    -- 5. Recopilar lista de pasajeros para el log transversal
FOR r_pasajero IN
SELECT DISTINCT pr.rut, u.nombre || ' ' || u.apellido AS nombre_completo
FROM pasajero_reserva pr
         JOIN usuario u ON u.rut = pr.rut
WHERE pr.id_reserva = p_id_reserva
    LOOP
        v_pasajeros_reserva := v_pasajeros_reserva || FORMAT('%s (%s); ', r_pasajero.nombre_completo, r_pasajero.rut);
END LOOP;

    -- 6. Registrar en Log_Auditoria (Log Transversal / Evento Global)
INSERT INTO log_auditoria (rut_usuario, accion, tabla_afectada, id_registro_afectado, detalle, direccion_ip)
VALUES (
           p_rut_pasajero,
           'CANCELACION_RESERVA',
           'reserva',
           p_id_reserva,
           FORMAT('CANCELACION EXITOSA | ID_CANCELACION: %s | RESERVA: %s | MONTO: $%s | ASIENTOS_LIBERADOS_CANTIDAD: %s | SOLICITANTE: %s',
                  v_id_cancelacion, p_id_reserva, v_total_reserva, v_asientos_count, p_rut_pasajero),
           p_ip_usuario
       );

-- 7. ELIMINAR físicamente de Reserva_Asiento para liberar la cabina
DELETE FROM reserva_asiento
WHERE id_reserva = p_id_reserva;

-- 8. Actualizar estado de la Reserva
UPDATE reserva
SET estado_reserva = v_id_estado_cancelado
WHERE id_reserva = p_id_reserva;

END;
$sp_cancelar$;