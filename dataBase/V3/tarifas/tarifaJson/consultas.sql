SELECT Nombre, Descripcion
FROM Tarifa
WHERE (Configuracion -> 'equipaje' ->> 'maletas_bodega')::integer > 0;
-- Resultado: Solo devolverá la tarifa "Premium Flex".


-- El operador @> significa "contiene este fragmento JSON"
SELECT Nombre
FROM Tarifa
WHERE Configuracion @> '{"asientos": {"seleccion_gratuita": true}}';
-- Resultado: Devolverá "Plus" y "Premium Flex".


SELECT Nombre
FROM Tarifa
WHERE Configuracion -> 'beneficios_extra' ->> 'acceso_sala_vip' = 'true';
-- Resultado: Premium Flex. (No dará error en las otras tarifas, simplemente retornará falso).


-- Actualizando una tarifa existente para añadirle una nueva regla
UPDATE Tarifa
SET Configuracion = jsonb_set(Configuracion, '{beneficios_extra, wifi_abordo}', 'true', true)
WHERE Nombre = 'Premium Flex';