-- ============================================================================
-- MIGRACIÓN / PROCEDIMIENTO ROBUSTO: CAMBIO DE ASIENTO CON UPGRADE Y AUDITORÍA
-- ============================================================================

CREATE OR REPLACE PROCEDURE sp_cambiarAsiento(
   IN p_id_asiento INT,
   IN p_id_reserva INT,
   IN p_id_asiento_org INT
)
LANGUAGE plpgsql
AS $cambiar_asiento$
DECLARE
    v_id_vuelo INT;
    v_rut_pasajero VARCHAR(12);
    v_asiento_ocupado INT;
    v_num_asiento_org VARCHAR(10);
    v_id_clase_org INT;
    v_num_asiento_nuevo VARCHAR(10);
    v_id_clase_nueva INT;
BEGIN
    -- 1. Bloqueo pesimista del registro original para prevenir condiciones de carrera (Paso A)
    SELECT id_vuelo, rut INTO v_id_vuelo, v_rut_pasajero
    FROM reserva_asiento
    WHERE id_reserva = p_id_reserva
      AND id_asiento = p_id_asiento_org
    FOR UPDATE;

    IF v_id_vuelo IS NULL THEN
        RAISE EXCEPTION 'El asiento original % no pertenece a la reserva %.', p_id_asiento_org, p_id_reserva;
    END IF;

    -- 2. Verificar disponibilidad del asiento destino en el mismo vuelo
    SELECT 1 INTO v_asiento_ocupado
    FROM reserva_asiento
    WHERE id_vuelo = v_id_vuelo
      AND id_asiento = p_id_asiento;

    IF v_asiento_ocupado IS NOT NULL THEN
        RAISE EXCEPTION 'El asiento destino % ya se encuentra reservado en el vuelo %.', p_id_asiento, v_id_vuelo;
    END IF;

    -- 3. Obtener metadatos de asientos para trazabilidad
    SELECT numero_asiento, id_clase INTO v_num_asiento_org, v_id_clase_org
    FROM asiento WHERE id_asiento = p_id_asiento_org;

    SELECT numero_asiento, id_clase INTO v_num_asiento_nuevo, v_id_clase_nueva
    FROM asiento WHERE id_asiento = p_id_asiento;

    -- 4. Actualizar asignación de butaca
    UPDATE reserva_asiento
    SET id_asiento = p_id_asiento
    WHERE id_reserva = p_id_reserva
      AND id_asiento = p_id_asiento_org;

    -- 5. Sincronizar Check-in para forzar actualización de tarjeta de embarque y QR
    UPDATE checkin
    SET fecha_hora = NOW()
    WHERE id_reserva = p_id_reserva;

    -- 6. Auditoría transversal en Log_Auditoria
    INSERT INTO log_auditoria (
        rut_usuario, accion, nivel, tabla_afectada, id_registro_afectado, detalle, datos_anteriores, datos_nuevos
    ) VALUES (
        v_rut_pasajero,
        'CAMBIO_ASIENTO',
        'INFO',
        'reserva_asiento',
        p_id_reserva,
        FORMAT('Cambio de asiento exitoso en vuelo %s: de %s a %s para pasajero %s', v_id_vuelo, v_num_asiento_org, v_num_asiento_nuevo, v_rut_pasajero),
        jsonb_build_object('id_asiento', p_id_asiento_org, 'numero_asiento', v_num_asiento_org, 'id_clase', v_id_clase_org),
        jsonb_build_object('id_asiento', p_id_asiento, 'numero_asiento', v_num_asiento_nuevo, 'id_clase', v_id_clase_nueva)
    );
END;
$cambiar_asiento$;
