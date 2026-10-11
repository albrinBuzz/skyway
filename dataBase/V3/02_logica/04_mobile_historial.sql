-- =============================================================================
-- SKYWAY MOBILE - HISTORIAL OPERACIONAL Y ALERTAS
-- =============================================================================
-- Complementa la tabla NOTIFICACION ya existente. No crea tablas nuevas.
-- Registra a futuro cambios de horario de segmentos y cambios de avión/estado
-- del vuelo para los pasajeros asociados a una reserva.
--
-- IMPORTANTE: no puede reconstruir modificaciones ocurridas antes de instalar
-- estos triggers. Desde su instalación, cada cambio queda persistido como una
-- notificación y se muestra tanto en Alertas como en Historial > Cambios.
-- =============================================================================

CREATE OR REPLACE FUNCTION fn_mobile_pasajeros_vuelo(p_id_vuelo INTEGER)
RETURNS TABLE(rut VARCHAR)
LANGUAGE SQL
AS $$
    SELECT DISTINCT x.rut
    FROM (
        SELECT r.rut_pasajero::VARCHAR AS rut
        FROM reserva_itinerario ri
        JOIN itinerario_vuelo iv ON iv.id_itinerario = ri.id_itinerario
        JOIN reserva r ON r.id_reserva = ri.id_reserva
        WHERE iv.id_vuelo = p_id_vuelo
          AND r.rut_pasajero IS NOT NULL

        UNION

        SELECT pr.rut::VARCHAR AS rut
        FROM reserva_itinerario ri
        JOIN itinerario_vuelo iv ON iv.id_itinerario = ri.id_itinerario
        JOIN pasajero_reserva pr ON pr.id_reserva = ri.id_reserva
        WHERE iv.id_vuelo = p_id_vuelo
          AND pr.rut IS NOT NULL
    ) x;
$$;

-- -----------------------------------------------------------------------------
-- Cambios de hora de salida/llegada en un segmento.
-- Sustituye el trigger histórico por uno acotado solo a cambios de horario.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_notificacionVueloEstado()
RETURNS TRIGGER
LANGUAGE PLPGSQL
AS $$
DECLARE
    pasajero RECORD;
    v_numero VARCHAR;
    mensaje TEXT;
BEGIN
    IF OLD.hora_salida IS NOT DISTINCT FROM NEW.hora_salida
       AND OLD.hora_llegada IS NOT DISTINCT FROM NEW.hora_llegada THEN
        RETURN NEW;
    END IF;

    SELECT numero_vuelo INTO v_numero
    FROM vuelo
    WHERE id_vuelo = NEW.id_vuelo;

    FOR pasajero IN SELECT rut FROM fn_mobile_pasajeros_vuelo(NEW.id_vuelo)
    LOOP
        mensaje := 'El vuelo ' || COALESCE(v_numero, NEW.id_vuelo::TEXT) || ' tuvo un cambio de horario. ';

        IF OLD.hora_salida IS DISTINCT FROM NEW.hora_salida THEN
            mensaje := mensaje || 'Salida: ' ||
                COALESCE(TO_CHAR(OLD.hora_salida, 'DD/MM/YYYY HH24:MI'), 'sin definir') ||
                ' → ' || COALESCE(TO_CHAR(NEW.hora_salida, 'DD/MM/YYYY HH24:MI'), 'sin definir') || '. ';
        END IF;

        IF OLD.hora_llegada IS DISTINCT FROM NEW.hora_llegada THEN
            mensaje := mensaje || 'Llegada: ' ||
                COALESCE(TO_CHAR(OLD.hora_llegada, 'DD/MM/YYYY HH24:MI'), 'sin definir') ||
                ' → ' || COALESCE(TO_CHAR(NEW.hora_llegada, 'DD/MM/YYYY HH24:MI'), 'sin definir') || '. ';
        END IF;

        INSERT INTO notificacion(rut_destinatario, titulo, mensaje, fecha, leido)
        VALUES (
            pasajero.rut,
            'Cambio de horario: ' || COALESCE(v_numero, NEW.id_vuelo::TEXT),
            BTRIM(mensaje),
            NOW(),
            FALSE
        );
    END LOOP;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS tr_notificacionVueloEstado ON segmento_vuelo;
CREATE TRIGGER tr_notificacionVueloEstado
AFTER UPDATE OF hora_salida, hora_llegada ON segmento_vuelo
FOR EACH ROW
EXECUTE FUNCTION fn_notificacionVueloEstado();

-- -----------------------------------------------------------------------------
-- Cambios de avión y/o estado del vuelo (incluye cancelaciones operacionales).
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_mobile_notificar_cambio_vuelo()
RETURNS TRIGGER
LANGUAGE PLPGSQL
AS $$
DECLARE
    pasajero RECORD;
    modelo_anterior TEXT;
    modelo_nuevo TEXT;
    estado_anterior TEXT;
    estado_nuevo TEXT;
    mensaje TEXT;
BEGIN
    IF OLD.id_avion IS NOT DISTINCT FROM NEW.id_avion
       AND OLD.id_estado_vuelo IS NOT DISTINCT FROM NEW.id_estado_vuelo THEN
        RETURN NEW;
    END IF;

    SELECT COALESCE(ma.nombre, a.numero_de_registro)
      INTO modelo_anterior
      FROM avion a
      LEFT JOIN modelo_avion ma ON ma.id_modelo = a.id_modelo
     WHERE a.id_avion = OLD.id_avion;

    SELECT COALESCE(ma.nombre, a.numero_de_registro)
      INTO modelo_nuevo
      FROM avion a
      LEFT JOIN modelo_avion ma ON ma.id_modelo = a.id_modelo
     WHERE a.id_avion = NEW.id_avion;

    SELECT COALESCE(ev.descripcion, ev.estado)
      INTO estado_anterior
      FROM estado_vuelo ev
     WHERE ev.id_estado_vuelo = OLD.id_estado_vuelo;

    SELECT COALESCE(ev.descripcion, ev.estado)
      INTO estado_nuevo
      FROM estado_vuelo ev
     WHERE ev.id_estado_vuelo = NEW.id_estado_vuelo;

    FOR pasajero IN SELECT rut FROM fn_mobile_pasajeros_vuelo(NEW.id_vuelo)
    LOOP
        mensaje := 'El vuelo ' || COALESCE(NEW.numero_vuelo, NEW.id_vuelo::TEXT) || ' fue actualizado. ';

        IF OLD.id_avion IS DISTINCT FROM NEW.id_avion THEN
            mensaje := mensaje || 'Avión: ' || COALESCE(modelo_anterior, 'sin definir') ||
                       ' → ' || COALESCE(modelo_nuevo, 'sin definir') || '. ';
        END IF;

        IF OLD.id_estado_vuelo IS DISTINCT FROM NEW.id_estado_vuelo THEN
            mensaje := mensaje || 'Estado: ' || COALESCE(estado_anterior, 'sin definir') ||
                       ' → ' || COALESCE(estado_nuevo, 'sin definir') || '. ';
        END IF;

        INSERT INTO notificacion(rut_destinatario, titulo, mensaje, fecha, leido)
        VALUES (
            pasajero.rut,
            'Cambio operacional: ' || COALESCE(NEW.numero_vuelo, NEW.id_vuelo::TEXT),
            BTRIM(mensaje),
            NOW(),
            FALSE
        );
    END LOOP;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS tr_mobile_cambio_vuelo ON vuelo;
CREATE TRIGGER tr_mobile_cambio_vuelo
AFTER UPDATE OF id_avion, id_estado_vuelo ON vuelo
FOR EACH ROW
EXECUTE FUNCTION fn_mobile_notificar_cambio_vuelo();

-- -----------------------------------------------------------------------------
-- Cambios de estado de una reserva (por ejemplo Confirmada -> Cancelada).
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_mobile_notificar_estado_reserva()
RETURNS TRIGGER
LANGUAGE PLPGSQL
AS $$
DECLARE
    estado_anterior TEXT;
    estado_nuevo TEXT;
    destinatario RECORD;
BEGIN
    IF OLD.estado_reserva IS NOT DISTINCT FROM NEW.estado_reserva THEN
        RETURN NEW;
    END IF;

    SELECT descripcion INTO estado_anterior
      FROM estado_reserva WHERE id_estado_reserva = OLD.estado_reserva;
    SELECT descripcion INTO estado_nuevo
      FROM estado_reserva WHERE id_estado_reserva = NEW.estado_reserva;

    FOR destinatario IN
        SELECT DISTINCT x.rut
        FROM (
            SELECT NEW.rut_pasajero::VARCHAR AS rut
            UNION
            SELECT pr.rut::VARCHAR FROM pasajero_reserva pr WHERE pr.id_reserva = NEW.id_reserva
        ) x
        WHERE x.rut IS NOT NULL
    LOOP
        INSERT INTO notificacion(rut_destinatario, titulo, mensaje, fecha, leido)
        VALUES (
            destinatario.rut,
            'Estado de reserva #' || NEW.id_reserva,
            'La reserva #' || NEW.id_reserva || ' cambió de ' ||
                COALESCE(estado_anterior, 'sin estado') || ' a ' ||
                COALESCE(estado_nuevo, 'sin estado') || '.',
            NOW(),
            FALSE
        );
    END LOOP;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS tr_mobile_estado_reserva ON reserva;
CREATE TRIGGER tr_mobile_estado_reserva
AFTER UPDATE OF estado_reserva ON reserva
FOR EACH ROW
EXECUTE FUNCTION fn_mobile_notificar_estado_reserva();

-- -----------------------------------------------------------------------------
-- Cambios de puerta de embarque.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_mobile_notificar_cambio_puerta()
RETURNS TRIGGER
LANGUAGE PLPGSQL
AS $$
DECLARE
    v_id_vuelo INTEGER;
    v_numero TEXT;
    puerta_anterior TEXT;
    puerta_nueva TEXT;
    destinatario RECORD;
BEGIN
    IF OLD.id_puerta IS NOT DISTINCT FROM NEW.id_puerta THEN
        RETURN NEW;
    END IF;

    SELECT sv.id_vuelo INTO v_id_vuelo
    FROM segmento_vuelo sv
    WHERE sv.id_segmento = NEW.id_segmento;

    IF v_id_vuelo IS NULL THEN
        RETURN NEW;
    END IF;

    SELECT numero_vuelo INTO v_numero FROM vuelo WHERE id_vuelo = v_id_vuelo;
    SELECT codigo_puerta INTO puerta_anterior FROM puerta_embarque WHERE id_puerta = OLD.id_puerta;
    SELECT codigo_puerta INTO puerta_nueva FROM puerta_embarque WHERE id_puerta = NEW.id_puerta;

    FOR destinatario IN SELECT rut FROM fn_mobile_pasajeros_vuelo(v_id_vuelo)
    LOOP
        INSERT INTO notificacion(rut_destinatario, titulo, mensaje, fecha, leido)
        VALUES (
            destinatario.rut,
            'Cambio de puerta: ' || COALESCE(v_numero, v_id_vuelo::TEXT),
            'La puerta del vuelo ' || COALESCE(v_numero, v_id_vuelo::TEXT) ||
                ' cambió de ' || COALESCE(puerta_anterior, 'sin asignar') ||
                ' a ' || COALESCE(puerta_nueva, 'sin asignar') || '.',
            NOW(),
            FALSE
        );
    END LOOP;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS tr_mobile_cambio_puerta ON asignacion_puerta;
CREATE TRIGGER tr_mobile_cambio_puerta
AFTER UPDATE OF id_puerta ON asignacion_puerta
FOR EACH ROW
EXECUTE FUNCTION fn_mobile_notificar_cambio_puerta();
