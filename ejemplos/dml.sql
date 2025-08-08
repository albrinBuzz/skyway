CREATE OR REPLACE FUNCTION fnConfirmar_reserva(
    p_idVuelo INT,
    p_asientos INT[],
    p_rutPasajero TEXT
) RETURNS VOID AS $$
DECLARE
    reserva_id INT;
    estado_reserva_id INT := 1;
    asiento RECORD;
    vuelo_seleccionado_id INT;
	 size integer := array_length(p_asientos, 1);
    i integer;
	estadoAst integer;
	numeroAsiento text;
	asientos_reservados TEXT := '';
BEGIN

    INSERT INTO reserva (rut_pasajero, id_vuelo, fecha_reserva, estado_reserva)
    VALUES (p_rutPasajero, p_idVuelo, CURRENT_TIMESTAMP, estado_reserva_id)
    RETURNING id_reserva INTO reserva_id;

    FOR i IN 1..size
    LOOP
		 SELECT
		    (CASE
        	WHEN ra.ID_RESERVA IS NOT NULL THEN 1
        		ELSE 0
    		END),
			a.numero_asiento::text
			into estadoAst,numeroAsiento
		FROM
		    Asiento a
		JOIN
		    Avion av ON a.ID_AVION = av.ID_AVION
		LEFT JOIN
		    Reserva_Asiento ra ON a.ID_ASIENTO = ra.ID_ASIENTO
		WHERE
		    av.ID_AVION = (SELECT ID_AVION FROM Vuelo WHERE id_vuelo = p_idVuelo)
		    AND a.id_asiento =  p_asientos[i];

		if estadoAst = 1 then


			  asientos_reservados := asientos_reservados || numeroAsiento || ', ';

	  	ELSE
		  INSERT INTO reserva_asiento (id_reserva, id_asiento)
	        VALUES (reserva_id, p_asientos[i]);
		END IF;


    END LOOP;
	 IF asientos_reservados <> '' THEN
        RAISE EXCEPTION 'Los siguientes asientos ya están reservados: %', left(asientos_reservados, length(asientos_reservados) - 2);
    	END IF;

END;
$$ LANGUAGE plpgsql;


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
    estado_reserva_id INT := 1;
    i INT;
    estadoAst INT;
    numeroAsiento TEXT;
    asientos_reservados TEXT := '';
BEGIN
    -- Insertar reserva
    INSERT INTO reserva (rut_pasajero, id_vuelo, fecha_reserva, estado_reserva)
    VALUES (p_rutPasajero, p_idVuelo, CURRENT_TIMESTAMP, estado_reserva_id)
    RETURNING id_reserva INTO reserva_id;

    FOR i IN 1..array_length(p_asientos, 1)
    LOOP
        SELECT
            CASE WHEN ra.ID_RESERVA IS NOT NULL THEN 1 ELSE 0 END,
            a.numero_asiento::text
        INTO estadoAst, numeroAsiento
        FROM Asiento a
        JOIN Avion av ON a.ID_AVION = av.ID_AVION
        LEFT JOIN Reserva_Asiento ra ON a.ID_ASIENTO = ra.ID_ASIENTO
        WHERE av.ID_AVION = (SELECT ID_AVION FROM Vuelo WHERE id_vuelo = p_idVuelo)
          AND a.id_asiento = p_asientos[i];

        IF estadoAst = 1 THEN
            asientos_reservados := asientos_reservados || numeroAsiento || ', ';
        ELSE
            INSERT INTO reserva_asiento (id_reserva, id_asiento)
            VALUES (reserva_id, p_asientos[i]);
        END IF;
    END LOOP;

    IF asientos_reservados <> '' THEN
        p_resultado := 'ERROR:Asientos ya reservados: ' || LEFT(asientos_reservados, LENGTH(asientos_reservados) - 2);
        -- Rollback manual si quieres desde Java (ver siguiente paso)
    ELSE
        p_resultado := 'OK:Reserva realizada correctamente.';
    END IF;
END;
$$;





--SELECT fnConfirmar_reserva(6, ARRAY[181, 161, 163], '12345678-0'::TEXT);

select * from reserva;
