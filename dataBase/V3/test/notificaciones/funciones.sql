CREATE OR REPLACE FUNCTION obtener_notificaciones_usuario(rut_usuario VARCHAR)
RETURNS TABLE(
    id_notificacion INT,
    titulo VARCHAR,
    mensaje TEXT,
    fecha TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT id_notificacion, titulo, mensaje, fecha
    FROM notificacion
    WHERE rut_destinatario = rut_usuario AND leido = FALSE
    ORDER BY fecha DESC;
END;
$$ LANGUAGE plpgsql;






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
        FROM reserva_itinerario rsvi
        join itinerario_vuelo itv
		on itv.id_itinerario = rsvi.id_itinerario
		join reserva rsv on rsv.id_reserva = rsvi.id_reserva
		join vuelo vl on vl.id_vuelo = itv.id_vuelo
        WHERE itv.id_vuelo = NEW.id_vuelo

    LOOP
        -- Construir el mensaje de notificación con detalles específicos
        mensaje := 'Estimado/a pasajero/a, su vuelo número ' || pasajero.numero_vuelo ||
                   ' ha sido actualizado. ';

        -- Incluir información sobre la nueva hora de salida

		IF OLD.hora_salida IS DISTINCT FROM NEW.hora_salida THEN
			  mensaje := mensaje || 'La nueva hora de salida es: ' || TO_CHAR(NEW.hora_salida, 'DD/MM/YYYY HH24:MI') || '. ';

        /*IF NEW.fecha_hora_salida IS NOT NULL THEN
            mensaje := mensaje || 'La nueva hora de salida es: ' || TO_CHAR(NEW.fecha_hora_salida, 'DD/MM/YYYY HH24:MI') || '. ';
        ELSE
            mensaje := mensaje || 'La hora de salida no ha sido modificada. ';*/
        END IF;

        -- Incluir información sobre la nueva hora de llegada
        IF OLD.hora_llegada IS DISTINCT FROM NEW.hora_llegada THEN
            mensaje := mensaje || 'La nueva hora de llegada es: ' || TO_CHAR(NEW.hora_llegada, 'DD/MM/YYYY HH24:MI') || '. ';
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
        INSERT INTO notificacion(rut_destinatario, titulo, mensaje, fecha, leido)
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
AFTER UPDATE ON Segmento_Vuelo
FOR EACH ROW
EXECUTE FUNCTION fn_notificacionVueloEstado();




CREATE OR REPLACE FUNCTION fn_notify_new_notification()
RETURNS TRIGGER AS
$$
BEGIN
    PERFORM pg_notify('nuevo_correo', NEW.id_notificacion::TEXT);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS tr_notify_new_notificacion ON notificacion;

CREATE TRIGGER tr_notify_new_notificacion
AFTER INSERT ON notificacion
FOR EACH ROW
EXECUTE FUNCTION fn_notify_new_notification();
