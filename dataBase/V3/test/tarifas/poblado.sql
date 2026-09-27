INSERT INTO Tarifa (ID_TARIFA, Nombre) VALUES
(1, 'Básica'),
(2, 'Flexible'),
(3, 'Premium');

INSERT INTO Caracteristica_Tarifa (ID_CARACTERISTICA, Nombre, Descripcion) VALUES
(1, 'Permite_Cambios', 'Permite cambiar la reserva'),
(2, 'Horas_Minimas_Cambio', 'Horas mínimas antes del vuelo para cambiar'),
(3, 'Permite_Cancelacion', 'Permite cancelar la reserva'),
(4, 'Reembolso_Permitido', 'Permite reembolso de la reserva'),
(5, 'Incluye_Equipaje', 'Cantidad de equipaje incluido');

-- Básica
INSERT INTO Tarifa_Caracteristica (ID_TARIFA, ID_CARACTERISTICA, Valor) VALUES
(1, 1, 'false'),  -- Permite_Cambios
(1, 3, 'false'),  -- Permite_Cancelacion
(1, 4, 'false');  -- Reembolso_Permitido

-- Flexible
INSERT INTO Tarifa_Caracteristica (ID_TARIFA, ID_CARACTERISTICA, Valor) VALUES
(2, 1, 'true'),   -- Permite_Cambios
(2, 2, '48'),     -- Horas_Minimas_Cambio
(2, 3, 'true'),   -- Permite_Cancelacion
(2, 4, 'true');   -- Reembolso_Permitido

-- Premium
INSERT INTO Tarifa_Caracteristica (ID_TARIFA, ID_CARACTERISTICA, Valor) VALUES
(3, 1, 'true'),          -- Permite_Cambios
(3, 2, '2'),             -- Horas_Minimas_Cambio
(3, 3, 'true'),          -- Permite_Cancelacion
(3, 4, 'true'),          -- Reembolso_Permitido
(3, 5, '2 maletas');     -- Incluye_Equipaje

INSERT INTO Itinerario (
    ID_ITINERARIO, FECHA_CREACION, ORIGEN_AEROPUERTO, DESTINO_AEROPUERTO,
    HORA_SALIDA, HORA_LLEGADA, DURACION_TOTAL, NUMERO_ESCALAS, Precio_Base
) VALUES (
    1, NOW(), 1, 2,
    NOW() + interval '1 day', NOW() + interval '1 day' + interval '2 hours',
    interval '2 hours', 0, 100.00
);

INSERT INTO Itinerario_Tarifa (ID_ITINERARIO_TARIFA, ID_ITINERARIO, ID_TARIFA, Precio) VALUES
(1, 1, 1, 100.00),
(2, 1, 2, 150.00),
(3, 1, 3, 200.00);
