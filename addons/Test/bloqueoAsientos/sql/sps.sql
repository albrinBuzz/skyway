CREATE OR REPLACE FUNCTION bloquear_articulos(compra INT)
RETURNS VOID AS $$
BEGIN
    UPDATE articulo
    SET bloqueado = TRUE,
        tiempo_bloqueo = NOW(),
        bloqueado_compra_id = compra
    WHERE id IN (
        SELECT articulo_id FROM compra_articulo WHERE compra_id = compra
    )
    AND bloqueado = FALSE; -- evita doble bloqueo

    -- Validación: si no se bloquearon todos los artículos → ERROR
    IF (SELECT COUNT(*)
        FROM articulo
        WHERE bloqueado_compra_id = compra)
        <>
       (SELECT COUNT(*) FROM compra_articulo WHERE compra_id = compra)
    THEN
        RAISE EXCEPTION 'Uno o más artículos ya están bloqueados o no disponibles';
    END IF;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION liberar_articulos(compra INT)
RETURNS VOID AS $$
BEGIN
    UPDATE articulo
    SET bloqueado = FALSE,
        tiempo_bloqueo = NULL,
        bloqueado_compra_id = NULL
    WHERE bloqueado_compra_id = compra;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION marcar_compra_pagada(id_compra INT)
RETURNS VOID AS $$
BEGIN
    UPDATE compra SET pagada = TRUE WHERE id = id_compra;
END;
$$ LANGUAGE plpgsql;
