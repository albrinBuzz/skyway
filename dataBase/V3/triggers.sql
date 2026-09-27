DO $$
DECLARE
reg RECORD;
BEGIN
FOR reg IN
SELECT p.proname AS function_name,
       CASE WHEN p.prokind = 'f' THEN 'FUNCTION'
            WHEN p.prokind = 'p' THEN 'PROCEDURE'
            WHEN p.prokind = 'a' THEN 'AGGREGATE'
           END AS function_type
FROM pg_proc p
         JOIN pg_namespace n ON p.pronamespace = n.oid
         JOIN pg_language l ON p.prolang = l.oid
WHERE n.nspname = 'public'
  -- 🎯 Filtro idéntico a las primeras 22 filas de tu imagen
  AND l.lanname = 'plpgsql'
  -- 🛑 Excluir cualquier función amarrada al sistema (PostGIS)
  AND p.oid NOT IN (
    SELECT objid
    FROM pg_depend
    WHERE deptype = 'e' AND classid = 'pg_proc'::regclass
    )
    LOOP
BEGIN
            RAISE NOTICE 'Eliminando componente de negocio: % (%)', reg.function_name, reg.function_type;
EXECUTE 'DROP ' || reg.function_type || ' public.' || quote_ident(reg.function_name) || ' CASCADE';
EXCEPTION
            WHEN OTHERS THEN
                RAISE NOTICE 'Saltada función protegida: %', reg.function_name;
END;
END LOOP;
END $$;



CREATE OR REPLACE FUNCTION fn_insertarAsientos()
    RETURNS TRIGGER AS $$
DECLARE
    v_id_modelo INT;
    v_config RECORD;
    v_fila INT;
    v_letras TEXT[];
    v_letra VARCHAR(2);
    v_asientos_creados INT := 0;
    v_total_letras INT;
    v_idx_letra INT;
    v_es_ventana BOOLEAN;
    v_es_pasillo BOOLEAN;
    v_numero_asiento VARCHAR(10);
BEGIN
    -- 1. Obtener el ID del modelo del avión desde la tabla Avion
    SELECT ID_MODELO INTO v_id_modelo
    FROM Avion
    WHERE ID_AVION = NEW.ID_AVION;

    -- 2. Recorrer la configuración de cabina del modelo para la clase insertada
    FOR v_config IN
        SELECT Fila_Inicio, Fila_Fin, Letras_Columnas, Es_Salida_Emergencia
        FROM Configuracion_Cabina
        WHERE ID_MODELO = v_id_modelo
          AND ID_CLASE = NEW.ID_CLASE
        ORDER BY Fila_Inicio ASC
        LOOP
            -- Convertir la cadena "A,B,C,D,E,F" en un arreglo de Postgres
            v_letras := string_to_array(v_config.Letras_Columnas, ',');
            v_total_letras := array_length(v_letras, 1);

            -- Recorrer cada fila del rango configurado (ej: Fila 1 a Fila 30)
            FOR v_fila IN v_config.Fila_Inicio .. v_config.Fila_Fin LOOP

                    -- Recorrer las letras configuradas para esa fila
                    FOR v_idx_letra IN 1 .. v_total_letras LOOP

                            -- Validar no exceder la cantidad total especificada en Capacidad_Clase
                            IF v_asientos_creados >= NEW.Cantidad THEN
                                EXIT;
                            END IF;

                            v_letra := trim(v_letras[v_idx_letra]);
                            v_numero_asiento := v_fila || v_letra;

                            -- Determinar si es Ventana (primera o última letra)
                            v_es_ventana := (v_idx_letra = 1 OR v_idx_letra = v_total_letras);

                            -- Determinar si es Pasillo (según posición relativa simple)
                            v_es_pasillo := (v_idx_letra = 2 OR v_idx_letra = v_total_letras - 1);

                            -- Insertar el asiento con todas sus coordenadas espaciales
                            INSERT INTO Asiento (
                                Numero_Asiento,
                                Fila,
                                Letra,
                                ID_CLASE,
                                ID_AVION,
                                Es_Ventana,
                                Es_Pasillo,
                                Es_Emergencia
                            )
                            VALUES (
                                       v_numero_asiento,
                                       v_fila,
                                       v_letra,
                                       NEW.ID_CLASE,
                                       NEW.ID_AVION,
                                       v_es_ventana,
                                       v_es_pasillo,
                                       v_config.Es_Salida_Emergencia
                                   );

                            v_asientos_creados := v_asientos_creados + 1;

                        END LOOP;

                    IF v_asientos_creados >= NEW.Cantidad THEN
                        EXIT;
                    END IF;

                END LOOP;

            IF v_asientos_creados >= NEW.Cantidad THEN
                EXIT;
            END IF;

        END LOOP;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_insertar_asientos ON Capacidad_Clase;

CREATE TRIGGER trigger_insertar_asientos
    AFTER INSERT ON Capacidad_Clase
    FOR EACH ROW
EXECUTE FUNCTION fn_insertarAsientos();




/*ALTER TABLE Segmento_Vuelo DISABLE TRIGGER trg_set_orden_segmento_vuelo;

ALTER TABLE Segmento_Vuelo DISABLE TRIGGER trg_set_numero_vuelo;

ALTER TABLE Segmento_Vuelo DISABLE TRIGGER trg_set_fecha_vuelo;

ALTER TABLE Itinerario_Vuelo DISABLE TRIGGER trg_set_fecha_itinerario;

ALTER TABLE Itinerario_Vuelo DISABLE TRIGGER trg_set_orden_itinerario_vuelo;*/








CREATE OR REPLACE FUNCTION fn_set_orden_itinerario_vuelo()
RETURNS TRIGGER AS $$
DECLARE
ultimo_orden INT;
BEGIN
    -- Si es UPDATE y viene NULL, rescatamos el anterior
    IF (TG_OP = 'UPDATE' AND NEW.ORDEN IS NULL) THEN
        NEW.ORDEN := OLD.ORDEN;
END IF;

    -- Si sigue siendo NULL (en INSERT), calculamos el siguiente
    IF NEW.ORDEN IS NULL THEN
SELECT COALESCE(MAX(ORDEN), 0) INTO ultimo_orden
FROM Itinerario_Vuelo
WHERE ID_ITINERARIO = NEW.ID_ITINERARIO;

NEW.ORDEN := ultimo_orden + 1;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_set_orden_itinerario_vuelo ON Itinerario_Vuelo;
CREATE TRIGGER trg_set_orden_itinerario_vuelo
    BEFORE INSERT OR UPDATE ON Itinerario_Vuelo
                         FOR EACH ROW
                         EXECUTE FUNCTION fn_set_orden_itinerario_vuelo();






CREATE OR REPLACE FUNCTION fn_set_orden_segmento_vuelo()
RETURNS TRIGGER AS $$
DECLARE
v_ultimo_orden INT;
BEGIN
    -- 1. Manejo para UPDATE
    IF (TG_OP = 'UPDATE') THEN
        -- Si JPA envió un NULL en el orden, rescatamos el valor que ya tenía el registro
        IF NEW.ORDEN_SEGMENTO IS NULL THEN
            NEW.ORDEN_SEGMENTO := OLD.ORDEN_SEGMENTO;
END IF;

        -- Si el vuelo NO cambió, ya no hay nada más que hacer, retornamos el NEW corregido
        IF (OLD.ID_VUELO = NEW.ID_VUELO) THEN
            RETURN NEW;
END IF;

        -- Si cambió el ID_VUELO y el orden seguía siendo el viejo,
        -- quizás quieras recalcularlo para el nuevo vuelo.
        -- En ese caso, podrías ponerlo en NULL aquí para que siga a la lógica de abajo.
        IF (OLD.ID_VUELO <> NEW.ID_VUELO AND NEW.ORDEN_SEGMENTO = OLD.ORDEN_SEGMENTO) THEN
             NEW.ORDEN_SEGMENTO := NULL;
END IF;
END IF;

    -- 2. Lógica de Asignación Automática (Solo si es NULL después de las validaciones de arriba)
    IF NEW.ORDEN_SEGMENTO IS NULL THEN
SELECT COALESCE(MAX(ORDEN_SEGMENTO), 0)
INTO v_ultimo_orden
FROM Segmento_Vuelo
WHERE ID_VUELO = NEW.ID_VUELO;

NEW.ORDEN_SEGMENTO := v_ultimo_orden + 1;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;



-- Trigger configurado para actuar antes de insertar o actualizar
CREATE TRIGGER trg_set_orden_segmento_vuelo
    BEFORE INSERT OR UPDATE ON Segmento_Vuelo
                         FOR EACH ROW
                         EXECUTE FUNCTION fn_set_orden_segmento_vuelo();





CREATE OR REPLACE FUNCTION fn_set_numero_vuelo()
RETURNS TRIGGER AS $$
DECLARE
codigoArp1 varchar;
    codigoArp2 varchar;
    numeroVueloN varchar;
BEGIN
    -- Obtenemos los códigos IATA de origen y destino
SELECT Codigo_IATA INTO codigoArp1 FROM aeropuerto WHERE id_aeropuerto = NEW.ID_AEROPUERTO_ORIGEN;
SELECT Codigo_IATA INTO codigoArp2 FROM aeropuerto WHERE id_aeropuerto = NEW.ID_AEROPUERTO_DESTINO;

-- Construimos el número basado en el ID real del segmento
numeroVueloN := codigoArp1 || '-' || codigoArp2 || NEW.ID_SEGMENTO::TEXT;

UPDATE vuelo
SET numero_vuelo = numeroVueloN
WHERE id_vuelo = NEW.ID_VUELO;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Lo activamos también en UPDATE por si cambian los aeropuertos
DROP TRIGGER IF EXISTS trg_set_numero_vuelo ON Segmento_Vuelo;
CREATE TRIGGER trg_set_numero_vuelo
    AFTER INSERT OR UPDATE OF ID_AEROPUERTO_ORIGEN, ID_AEROPUERTO_DESTINO ON Segmento_Vuelo
    FOR EACH ROW
    EXECUTE FUNCTION fn_set_numero_vuelo();



CREATE OR REPLACE FUNCTION fn_set_fecha_itinerario()
RETURNS TRIGGER AS $$
DECLARE
fechaSalida TIMESTAMP;
    fechaLlegada TIMESTAMP;
BEGIN
    -- Si es el primer vuelo del itinerario, establecemos hora de salida y llegada
    IF NEW.ORDEN = 1 THEN
SELECT sgv.hora_salida
INTO fechaSalida
FROM Segmento_Vuelo sgv
WHERE sgv.id_vuelo = NEW.ID_VUELO
  AND sgv.ORDEN_SEGMENTO = 1;

SELECT sgv.hora_llegada
INTO fechaLlegada
FROM Segmento_Vuelo sgv
WHERE sgv.id_vuelo = NEW.ID_VUELO
  AND sgv.ORDEN_SEGMENTO = (
    SELECT MAX(sgv2.ORDEN_SEGMENTO)
    FROM Segmento_Vuelo sgv2
    WHERE sgv2.id_vuelo = NEW.ID_VUELO
);

UPDATE Itinerario
SET HORA_SALIDA = fechaSalida,
    HORA_LLEGADA = fechaLlegada
WHERE ID_ITINERARIO = NEW.ID_ITINERARIO;

ELSIF NEW.ORDEN > 1 THEN
        -- En vuelos posteriores (conexiones), actualizamos solo la hora de llegada
SELECT sgv.hora_llegada
INTO fechaLlegada
FROM Segmento_Vuelo sgv
WHERE sgv.id_vuelo = NEW.ID_VUELO
  AND sgv.ORDEN_SEGMENTO = (
    SELECT MAX(sgv2.ORDEN_SEGMENTO)
    FROM Segmento_Vuelo sgv2
    WHERE sgv2.id_vuelo = NEW.ID_VUELO
);

UPDATE Itinerario
SET HORA_LLEGADA = fechaLlegada
WHERE ID_ITINERARIO = NEW.ID_ITINERARIO;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;




CREATE TRIGGER trg_set_fecha_itinerario
    AFTER INSERT ON Itinerario_Vuelo
    FOR EACH ROW
    EXECUTE FUNCTION fn_set_fecha_itinerario();





CREATE OR REPLACE FUNCTION fn_update_itinerario_desde_vuelo()
    RETURNS TRIGGER AS $$
DECLARE
    rec RECORD;
BEGIN
    -- Recorremos todos los itinerarios afectados por este vuelo
    FOR rec IN
        SELECT DISTINCT iv.ID_ITINERARIO
        FROM Itinerario_Vuelo iv
        WHERE iv.ID_VUELO = NEW.ID_VUELO
        LOOP
            -- Actualizar HORA_SALIDA, HORA_LLEGADA y DURACION_TOTAL en el Itinerario
            UPDATE Itinerario i
            SET HORA_SALIDA = (
                SELECT v.Fecha_Hora_Salida
                FROM Itinerario_Vuelo iv2
                         JOIN Vuelo v ON v.ID_VUELO = iv2.ID_VUELO
                WHERE iv2.ID_ITINERARIO = rec.ID_ITINERARIO
                ORDER BY iv2.ORDEN ASC
                LIMIT 1
            ),
                HORA_LLEGADA = (
                    SELECT v.Fecha_Hora_Llegada
                    FROM Itinerario_Vuelo iv2
                             JOIN Vuelo v ON v.ID_VUELO = iv2.ID_VUELO
                    WHERE iv2.ID_ITINERARIO = rec.ID_ITINERARIO
                    ORDER BY iv2.ORDEN DESC
                    LIMIT 1
                ),
                DURACION_TOTAL = (
                    (
                        SELECT v.Fecha_Hora_Llegada
                        FROM Itinerario_Vuelo iv2
                                 JOIN Vuelo v ON v.ID_VUELO = iv2.ID_VUELO
                        WHERE iv2.ID_ITINERARIO = rec.ID_ITINERARIO
                        ORDER BY iv2.ORDEN DESC
                        LIMIT 1
                    ) - (
                        SELECT v.Fecha_Hora_Salida
                        FROM Itinerario_Vuelo iv2
                                 JOIN Vuelo v ON v.ID_VUELO = iv2.ID_VUELO
                        WHERE iv2.ID_ITINERARIO = rec.ID_ITINERARIO
                        ORDER BY iv2.ORDEN ASC
                        LIMIT 1
                    )
                    )
            WHERE i.ID_ITINERARIO = rec.ID_ITINERARIO;
        END LOOP;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Re-crear el Trigger sobre Vuelo
DROP TRIGGER IF EXISTS trg_vuelo_hacia_itinerario ON Vuelo;

CREATE TRIGGER trg_vuelo_hacia_itinerario
    AFTER UPDATE OF Fecha_Hora_Salida, Fecha_Hora_Llegada ON Vuelo
    FOR EACH ROW
EXECUTE FUNCTION fn_update_itinerario_desde_vuelo();


CREATE OR REPLACE FUNCTION fn_actualizar_duracion_itinerario()
    RETURNS TRIGGER AS $$
DECLARE
    v_id_itinerario INT;
    v_hora_salida   TIMESTAMP;
    v_hora_llegada  TIMESTAMP;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        v_id_itinerario := OLD.ID_ITINERARIO;
    ELSE
        v_id_itinerario := NEW.ID_ITINERARIO;
    END IF;

    -- Obtener la hora de salida del primer vuelo (ORDEN mínimo)
    SELECT v.Fecha_Hora_Salida
    INTO v_hora_salida
    FROM Itinerario_Vuelo iv
             JOIN Vuelo v ON v.ID_VUELO = iv.ID_VUELO
    WHERE iv.ID_ITINERARIO = v_id_itinerario
    ORDER BY iv.ORDEN ASC
    LIMIT 1;

    -- Obtener la hora de llegada del último vuelo (ORDEN máximo)
    SELECT v.Fecha_Hora_Llegada
    INTO v_hora_llegada
    FROM Itinerario_Vuelo iv
             JOIN Vuelo v ON v.ID_VUELO = iv.ID_VUELO
    WHERE iv.ID_ITINERARIO = v_id_itinerario
    ORDER BY iv.ORDEN DESC
    LIMIT 1;

    -- Actualizar el itinerario
    IF v_hora_salida IS NOT NULL AND v_hora_llegada IS NOT NULL THEN
        UPDATE Itinerario
        SET HORA_SALIDA    = v_hora_salida,
            HORA_LLEGADA   = v_hora_llegada,
            DURACION_TOTAL = (v_hora_llegada - v_hora_salida)
        WHERE ID_ITINERARIO = v_id_itinerario;
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_actualizar_duracion_itinerario ON Itinerario_Vuelo;

CREATE TRIGGER trg_actualizar_duracion_itinerario
    AFTER INSERT OR UPDATE OR DELETE ON Itinerario_Vuelo
    FOR EACH ROW
EXECUTE FUNCTION fn_actualizar_duracion_itinerario();



CREATE OR REPLACE FUNCTION fn_set_fecha_vuelo()
RETURNS TRIGGER AS $$
DECLARE
v_id_vuelo INT;
    v_fecha_salida TIMESTAMP;
    v_fecha_llegada TIMESTAMP;
BEGIN
    -- 1. Determinar el ID del vuelo afectado (Maneja INSERT, UPDATE y DELETE)
    -- Si es un UPDATE y cambió el ID_VUELO, debemos actualizar el Vuelo antiguo también.
    -- Para simplificar, primero identificamos qué vuelo procesar en este hilo.
    IF (TG_OP = 'DELETE') THEN
        v_id_vuelo := OLD.ID_VUELO;
ELSE
        v_id_vuelo := NEW.ID_VUELO;
END IF;

    -- 2. Obtener la salida del primer segmento y la llegada del último en una sola consulta
    -- Esto es más eficiente que hacer dos SELECT por separado.
SELECT
    (SELECT hora_salida FROM Segmento_Vuelo WHERE id_vuelo = v_id_vuelo ORDER BY ORDEN_SEGMENTO ASC LIMIT 1),
        (SELECT hora_llegada FROM Segmento_Vuelo WHERE id_vuelo = v_id_vuelo ORDER BY ORDEN_SEGMENTO DESC LIMIT 1)
INTO v_fecha_salida, v_fecha_llegada;

-- 3. Actualizar la tabla Vuelo
UPDATE Vuelo
SET Fecha_Hora_Salida = v_fecha_salida,
    Fecha_Hora_Llegada = v_fecha_llegada
WHERE ID_VUELO = v_id_vuelo;

-- 4. Caso Especial: Si hubo un UPDATE y se cambió el ID_VUELO de un segmento,
-- debemos recalcular también el Vuelo que perdió el segmento.
IF (TG_OP = 'UPDATE' AND OLD.ID_VUELO <> NEW.ID_VUELO) THEN
UPDATE Vuelo
SET Fecha_Hora_Salida = (SELECT hora_salida FROM Segmento_Vuelo WHERE id_vuelo = OLD.ID_VUELO ORDER BY ORDEN_SEGMENTO ASC LIMIT 1),
    Fecha_Hora_Llegada = (SELECT hora_llegada FROM Segmento_Vuelo WHERE id_vuelo = OLD.ID_VUELO ORDER BY ORDEN_SEGMENTO DESC LIMIT 1)
WHERE ID_VUELO = OLD.ID_VUELO;
END IF;

RETURN NULL; -- En triggers AFTER el valor de retorno no afecta al registro
END;
$$ LANGUAGE plpgsql;

-- Definición del Trigger incluyendo DELETE
CREATE TRIGGER trg_set_fecha_vuelo
    AFTER INSERT OR UPDATE OR DELETE ON Segmento_Vuelo
    FOR EACH ROW
    EXECUTE FUNCTION fn_set_fecha_vuelo();



CREATE OR REPLACE FUNCTION fn_set_fecha_turno_automatico()
RETURNS TRIGGER AS $$
DECLARE
v_hora_inicio TIMESTAMP;
    v_hora_fin TIMESTAMP;
BEGIN
    -- 1. Obtener la hora de salida del primer segmento (Orden 1)
SELECT hora_salida
INTO v_hora_inicio
FROM Segmento_Vuelo
WHERE id_vuelo = NEW.ID_VUELO
  AND ORDEN_SEGMENTO = 1;

-- 2. Obtener la hora de llegada del último segmento (El de mayor orden)
SELECT hora_llegada
INTO v_hora_fin
FROM Segmento_Vuelo
WHERE id_vuelo = NEW.ID_VUELO
  AND ORDEN_SEGMENTO = (
    SELECT MAX(sgv2.ORDEN_SEGMENTO)
    FROM Segmento_Vuelo sgv2
    WHERE sgv2.id_vuelo = NEW.ID_VUELO
);

-- 3. Actualizar los turnos asociados a este vuelo
-- Solo se actualiza si se encontraron ambos extremos (inicio y fin)
IF v_hora_inicio IS NOT NULL AND v_hora_fin IS NOT NULL THEN
UPDATE Turno
SET Hora_Inicio = v_hora_inicio,
    Hora_Fin = v_hora_fin,
    Fecha = CAST(v_hora_inicio AS DATE) -- Se asume que la fecha del turno es el día de salida
WHERE ID_VUELO = NEW.ID_VUELO;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER trg_actualizar_fechas_turno
    AFTER INSERT OR UPDATE ON Segmento_Vuelo
                        FOR EACH ROW
                        EXECUTE FUNCTION fn_set_fecha_turno_automatico();


/*ALTER TABLE Segmento_Vuelo DISABLE TRIGGER trg_set_orden_segmento_vuelo;

ALTER TABLE Segmento_Vuelo DISABLE TRIGGER trg_set_numero_vuelo;

ALTER TABLE Segmento_Vuelo DISABLE TRIGGER trg_set_fecha_vuelo;

ALTER TABLE Itinerario_Vuelo DISABLE TRIGGER trg_set_fecha_itinerario;

ALTER TABLE Itinerario_Vuelo DISABLE TRIGGER trg_set_orden_itinerario_vuelo;*/




ALTER TABLE notificacion
    ADD COLUMN IF NOT EXISTS enviada BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS canal VARCHAR(20) DEFAULT 'Email';








UPDATE Usuario
SET Correo_Electronico = 'cr.romanz@duocuc.cl'
WHERE Correo_Electronico = 'juan.perez@piloto.com';


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
