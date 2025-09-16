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


SELECT
    c1.nombre || '-' || aprt1.nombre_aeropuerto || ' ' || aprt1.codigo_iata AS origen,
    c2.nombre || '-' || aprt2.nombre_aeropuerto || ' ' || aprt2.codigo_iata AS destino,
    it.hora_salida,
	it.hora_llegada
FROM itinerario it
JOIN aeropuerto aprt1 ON aprt1.id_aeropuerto = it.origen_aeropuerto
JOIN aeropuerto aprt2 ON aprt2.id_aeropuerto = it.destino_aeropuerto
JOIN ciudad c1 ON c1.id_ciudad = aprt1.id_ciudad
JOIN ciudad c2 ON c2.id_ciudad = aprt2.id_ciudad
WHERE it.hora_salida >= NOW()
  AND it.hora_salida < NOW() + INTERVAL '7 days'
ORDER BY it.hora_salida ASC;

SELECT
    v.id_vuelo,
    v.numero_vuelo,
    c.descripcion,
    MAX(p.precio) AS precio_maximo
FROM vuelo v
JOIN precio_asiento p ON p.id_vuelo = v.id_vuelo
JOIN clase_asiento c ON c.id_clase = p.id_clase
GROUP BY v.id_vuelo, v.numero_vuelo, c.descripcion
ORDER BY precio_maximo DESC
LIMIT 20;

--CREATE INDEX idx_precioAsientoIdx ON precio_asiento(precio);

--CREATE INDEX idx_aeropuertoOrg ON segmento_vuelo(id_aeropuerto_origen);

--CREATE INDEX idx_aeropuertoDest ON segmento_vuelo(id_aeropuerto_destino);

--drop INDEX idx_aeropuertoDest;


select
sgm.id_vuelo
from segmento_vuelo sgm
where sgm.id_aeropuerto_destino=120 and sgm.id_aeropuerto_origen=90;


--select
--ALTER TABLE Reserva_Asiento ADD CONSTRAINT unique_reserva_asiento UNIQUE (ID_VUELO, ID_ASIENTO);
