CREATE OR REPLACE FUNCTION confirmar_reserva(
    vuelo_seleccionado_id INT,
    asientos_seleccionados INT[],
    usuario_id INT
) RETURNS VOID AS $$
DECLARE
    reserva_id INT;
    reserva_fecha TIMESTAMP;
    estado_reserva_id INT := 1;  -- El estado 1 corresponde a "Reserva Confirmada" (ajustar según sea necesario)
    asiento_id INT;
    pasajero_id INT;
BEGIN
    -- Logica para verificar si el usuario es un pasajero
    SELECT id_usuario INTO pasajero_id
    FROM usuario
    WHERE id_usuario = usuario_id AND tipo = 'Pasajero';

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Usuario no es un pasajero o no existe';
    END IF;

    -- Guardar la reserva
    INSERT INTO reserva (id_usuario, id_vuelo, fecha_reserva, id_estado_reserva)
    VALUES (usuario_id, vuelo_seleccionado_id, CURRENT_TIMESTAMP, estado_reserva_id)
    RETURNING id_reserva INTO reserva_id;

    -- Guardar los asientos seleccionados
    FOREACH asiento_id IN ARRAY asientos_seleccionados
    LOOP
        INSERT INTO reserva_asiento (id_reserva, id_asiento)
        VALUES (reserva_id, asiento_id);
    END LOOP;

    -- Actualizar el estado de la reserva si es necesario, según la lógica de negocio
    -- (Si es necesario un cambio de estado adicional, se puede añadir aquí)

    -- Enviar un mensaje de éxito
    RAISE NOTICE 'Reserva confirmada con ID: %, para el vuelo ID: %, asientos: %, usuario: %',
        reserva_id, vuelo_seleccionado_id, asientos_seleccionados, usuario_id;
END;
$$ LANGUAGE plpgsql;
ABLE aeropuerto ADD CONSTRAINT FK_AEROPUERTO_ON_CIUDAD FOREIGN KEY (ciudad) REFERENCES ciudad (id_ciudad);