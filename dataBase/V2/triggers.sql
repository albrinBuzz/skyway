-- Crear la función del trigger
CREATE OR REPLACE FUNCTION fn_insertarAsientos()
RETURNS TRIGGER AS $$
DECLARE
    indice INTEGER;
    letra CHAR;
    asiento VARCHAR;
BEGIN
    FOR indice IN 0 .. NEW.cantidad - 1 LOOP
        letra := chr(65 + (indice % 6));  -- A-F
        asiento := (indice + 1) || letra;

        INSERT INTO Asiento (Numero_Asiento, ID_CLASE, ID_AVION)
        VALUES (asiento, NEW.ID_CLASE, NEW.ID_AVION);
    END LOOP;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;




-- Crear el trigger que llama a la función cuando se inserta un avión
CREATE TRIGGER trigger_insertar_asientos
AFTER INSERT ON Capacidad_Clase
FOR EACH ROW
EXECUTE FUNCTION fn_insertarAsientos();


/*ALTER TABLE Segmento_Vuelo DISABLE TRIGGER trg_set_orden_segmento_vuelo;

ALTER TABLE Segmento_Vuelo DISABLE TRIGGER trg_set_numero_vuelo;

ALTER TABLE Segmento_Vuelo DISABLE TRIGGER trg_set_fecha_vuelo;

ALTER TABLE Itinerario_Vuelo DISABLE TRIGGER trg_set_fecha_itinerario;

ALTER TABLE Itinerario_Vuelo DISABLE TRIGGER trg_set_orden_itinerario_vuelo;*/




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





-- Crear la función del trigger
CREATE OR REPLACE FUNCTION fn_insertarAsientos()
RETURNS TRIGGER AS $$
DECLARE
    indice INTEGER;
    letra CHAR;
    asiento VARCHAR;
BEGIN
    FOR indice IN 0 .. NEW.cantidad - 1 LOOP
        letra := chr(65 + (indice % 6));  -- A-F
        asiento := (indice + 1) || letra;

        INSERT INTO Asiento (Numero_Asiento, ID_CLASE, ID_AVION)
        VALUES (asiento, NEW.ID_CLASE, NEW.ID_AVION);
    END LOOP;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;




-- Crear el trigger que llama a la función cuando se inserta un avión
CREATE TRIGGER trigger_insertar_asientos
AFTER INSERT ON Capacidad_Clase
FOR EACH ROW
EXECUTE FUNCTION fn_insertarAsientos();



CREATE OR REPLACE FUNCTION fn_set_orden_itinerario_vuelo()
RETURNS TRIGGER AS $$
DECLARE
    ultimo_orden INT;
BEGIN

	IF NEW.ORDEN IS NOT NULL THEN
    RETURN NEW;
	END IF;


    SELECT COALESCE(MAX(ORDEN), 0)
    INTO ultimo_orden
    FROM Itinerario_Vuelo
    WHERE ID_ITINERARIO = NEW.ID_ITINERARIO;

    IF ultimo_orden = 0 THEN
        NEW.ORDEN := 1;
    ELSE
        NEW.ORDEN := ultimo_orden + 1;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;



CREATE TRIGGER trg_set_orden_itinerario_vuelo
BEFORE INSERT ON Itinerario_Vuelo
FOR EACH ROW
EXECUTE FUNCTION fn_set_orden_itinerario_vuelo();






CREATE OR REPLACE FUNCTION fn_set_orden_segmento_vuelo()
RETURNS TRIGGER AS $$
DECLARE
    ultimo_orden INT;
BEGIN
    -- Si ORDEN_SEGMENTO fue especificado manualmente, respetarlo
    IF NEW.ORDEN_SEGMENTO IS NOT NULL THEN
        RETURN NEW;
    END IF;

    -- Obtener el último ORDEN_SEGMENTO para el mismo ID_VUELO
    SELECT COALESCE(MAX(ORDEN_SEGMENTO), 0)
    INTO ultimo_orden
    FROM Segmento_Vuelo
    WHERE ID_VUELO = NEW.ID_VUELO;

    -- Asignar nuevo orden_segmento
    NEW.ORDEN_SEGMENTO := ultimo_orden + 1;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_set_orden_segmento_vuelo
BEFORE INSERT ON Segmento_Vuelo
FOR EACH ROW
EXECUTE FUNCTION fn_set_orden_segmento_vuelo();




CREATE OR REPLACE FUNCTION fn_set_numero_vuelo()
RETURNS TRIGGER AS $$
DECLARE
   	secuencia_vuelo INT;
	codigoArp1 varchar;
	codigoArp2 varchar;
	numeroVueloN varchar;
BEGIN

		SELECT currval('segmento_vuelo_seq') INTO secuencia_vuelo;

	    IF NEW.ORDEN_SEGMENTO = 1 THEN

			 SELECT apr1.Codigo_IATA INTO codigoArp1
		    FROM aeropuerto apr1
		    WHERE apr1.id_aeropuerto = NEW.ID_AEROPUERTO_ORIGEN;

		    SELECT apr2.Codigo_IATA INTO codigoArp2
		    FROM aeropuerto apr2
		    WHERE apr2.id_aeropuerto = NEW.ID_AEROPUERTO_DESTINO;

	    	numeroVueloN := codigoArp1 || '-' || codigoArp2 || secuencia_vuelo::TEXT;

	        UPDATE vuelo
	        SET numero_vuelo = numeroVueloN
	        WHERE id_vuelo = NEW.ID_VUELO;

	    ELSIF NEW.ORDEN_SEGMENTO > 1 THEN

					 SELECT apr1.Codigo_IATA INTO codigoArp1
		    FROM aeropuerto apr1
		    WHERE apr1.id_aeropuerto = NEW.ID_AEROPUERTO_ORIGEN;

		    SELECT apr2.Codigo_IATA INTO codigoArp2
		    FROM aeropuerto apr2
		    WHERE apr2.id_aeropuerto = NEW.ID_AEROPUERTO_DESTINO;

	 		numeroVueloN := codigoArp1 || '-' || codigoArp2 || secuencia_vuelo::TEXT;

	        UPDATE vuelo
	        SET numero_vuelo = numeroVueloN
	        WHERE id_vuelo = NEW.ID_VUELO;


	    END IF;


    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER trg_set_numero_vuelo
AFTER INSERT ON Segmento_Vuelo
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





CREATE OR REPLACE FUNCTION fn_set_fecha_vuelo()
RETURNS TRIGGER AS $$
DECLARE
    fechaSalida TIMESTAMP;
    fechaLlegada TIMESTAMP;
BEGIN

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

        UPDATE vuelo
        SET Fecha_Hora_Salida = fechaSalida,
            Fecha_Hora_Llegada = fechaLlegada
        WHERE ID_VUELO = NEW.ID_VUELO;



    RETURN NEW;
END;
$$ LANGUAGE plpgsql;



CREATE TRIGGER trg_set_fecha_vuelo
AFTER INSERT ON Segmento_Vuelo
FOR EACH ROW
EXECUTE FUNCTION fn_set_fecha_vuelo();


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
