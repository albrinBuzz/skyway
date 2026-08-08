SELECT
    i.ID_ITINERARIO,
    t.Nombre AS Tarifa,
    c.Nombre AS Caracteristica,
    tc.Valor,
    it.Precio
FROM Itinerario i
JOIN Itinerario_Tarifa it ON i.ID_ITINERARIO = it.ID_ITINERARIO
JOIN Tarifa t ON it.ID_TARIFA = t.ID_TARIFA
JOIN Tarifa_Caracteristica tc ON t.ID_TARIFA = tc.ID_TARIFA
JOIN Caracteristica_Tarifa c ON c.ID_CARACTERISTICA = tc.ID_CARACTERISTICA
WHERE i.ID_ITINERARIO = 1
ORDER BY t.Nombre, c.Nombre;


SELECT
    t.Nombre AS Tarifa,
    it.Precio,
    json_agg(json_build_object('Caracteristica', c.Nombre, 'Valor', tc.Valor)) AS Caracteristicas
FROM Itinerario_Tarifa it
JOIN Tarifa t ON it.ID_TARIFA = t.ID_TARIFA
JOIN Tarifa_Caracteristica tc ON t.ID_TARIFA = tc.ID_TARIFA
JOIN Caracteristica_Tarifa c ON c.ID_CARACTERISTICA = tc.ID_CARACTERISTICA
WHERE it.ID_ITINERARIO = 1
GROUP BY t.Nombre, it.Precio
ORDER BY t.Nombre;

SELECT
    t.ID_TARIFA,
    t.Nombre AS Tarifa,
    it.Precio,
    json_object_agg(c.Nombre, tc.Valor) AS Caracteristicas
FROM Itinerario_Tarifa it
JOIN Tarifa t ON it.ID_TARIFA = t.ID_TARIFA
JOIN Tarifa_Caracteristica tc ON t.ID_TARIFA = tc.ID_TARIFA
JOIN Caracteristica_Tarifa c ON c.ID_CARACTERISTICA = tc.ID_CARACTERISTICA
WHERE it.ID_ITINERARIO = 1
GROUP BY t.ID_TARIFA, t.Nombre, it.Precio
ORDER BY t.Nombre;


WITH CaracteristicasPorTarifa AS (
    SELECT
        it.ID_ITINERARIO,
        t.ID_TARIFA,
        t.Nombre AS Tarifa,
        it.Precio,
        json_object_agg(c.Nombre, tc.Valor) AS Caracteristicas
    FROM Itinerario_Tarifa it
    JOIN Tarifa t ON it.ID_TARIFA = t.ID_TARIFA
    JOIN Tarifa_Caracteristica tc ON tc.ID_TARIFA = t.ID_TARIFA
    JOIN Caracteristica_Tarifa c ON c.ID_CARACTERISTICA = tc.ID_CARACTERISTICA
    WHERE it.ID_ITINERARIO = 1
    GROUP BY it.ID_ITINERARIO, t.ID_TARIFA, t.Nombre, it.Precio
)

SELECT json_agg(
    json_build_object(
        'Tarifa', Tarifa,
        'Precio', Precio,
        'Caracteristicas', Caracteristicas
    )
) AS Tarifas
FROM CaracteristicasPorTarifa;



SELECT
    i.ID_ITINERARIO,
    t.Nombre AS Tarifa,
    c.Nombre AS Caracteristica,
    tc.Valor,
    it.Precio
FROM Itinerario i
JOIN Itinerario_Tarifa it ON i.ID_ITINERARIO = it.ID_ITINERARIO
JOIN Tarifa t ON it.ID_TARIFA = t.ID_TARIFA
JOIN Tarifa_Caracteristica tc ON t.ID_TARIFA = tc.ID_TARIFA
JOIN Caracteristica_Tarifa c ON c.ID_CARACTERISTICA = tc.ID_CARACTERISTICA
WHERE i.ID_ITINERARIO = 1  -- Reemplaza con el ID real
ORDER BY t.Nombre, c.Nombre;
