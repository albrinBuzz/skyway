-- Poblando los continentes
INSERT INTO Continente (Nombre) VALUES
('Asia'),
('Europa'),
('América del Norte'),
('América del Sur'),
('África'),
('Oceanía');


-- Poblando los países
INSERT INTO Pais (Nombre, ID_CONTINENTE) VALUES
('Argentina', 4), -- América del Sur
('Brasil', 4),    -- América del Sur
('Estados Unidos', 3),  -- América del Norte
('México', 3),    -- América del Norte
('Francia', 2),   -- Europa
('España', 2),    -- Europa
('Japón', 1),     -- Asia
('India', 1),     -- Asia
('Sudáfrica', 5), -- África
('Australia', 6); -- Oceanía

-- Poblando las ciudades
INSERT INTO Ciudad (Nombre, ID_PAIS) VALUES
('Buenos Aires', 1),  -- Argentina
('São Paulo', 2),     -- Brasil
('Nueva York', 3),    -- Estados Unidos
('Ciudad de México', 4), -- México
('París', 5),         -- Francia
('Madrid', 6),        -- España
('Tokio', 7),         -- Japón
('Delhi', 8),         -- India
('Johannesburgo', 9), -- Sudáfrica
('Sídney', 10);       -- Australia


-- Poblando los aeropuertos
INSERT INTO Aeropuerto (Nombre_Aeropuerto, ID_CIUDAD, Codigo_IATA) VALUES
('Aeropuerto Internacional Ministro Pistarini', 1, 'EZE'),  -- Buenos Aires
('Aeropuerto Internacional de São Paulo-Guarulhos', 2, 'GRU'), -- São Paulo
('Aeropuerto Internacional John F. Kennedy', 3, 'JFK'),  -- Nueva York
('Aeropuerto Internacional de la Ciudad de México', 4, 'MEX'),  -- Ciudad de México
('Aeropuerto de París-Charles de Gaulle', 5, 'CDG'),  -- París
('Aeropuerto Adolfo Suárez Madrid-Barajas', 6, 'MAD'), -- Madrid
('Aeropuerto Internacional de Narita', 7, 'NRT'),  -- Tokio
('Aeropuerto Internacional Indira Gandhi', 8, 'DEL'), -- Delhi
('Aeropuerto Internacional O. R. Tambo', 9, 'JNB'),  -- Johannesburgo
('Aeropuerto de Sídney Kingsford Smith', 10, 'SYD'); -- Sídney


-- Poblando las puertas de embarque
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('A01', 'Terminal 1', 1),  -- Aeropuerto Internacional Ministro Pistarini
('B03', 'Terminal 2', 2),  -- Aeropuerto Internacional de São Paulo-Guarulhos
('C02', 'Terminal 4', 3),  -- Aeropuerto Internacional John F. Kennedy
('D04', 'Terminal 2', 4),  -- Aeropuerto Internacional de la Ciudad de México
('E05', 'Terminal 1', 5),  -- Aeropuerto de París-Charles de Gaulle
('F01', 'Terminal 4', 6),  -- Aeropuerto Adolfo Suárez Madrid-Barajas
('G03', 'Terminal 2', 7),  -- Aeropuerto Internacional de Narita
('H02', 'Terminal 3', 8),  -- Aeropuerto Internacional Indira Gandhi
('I04', 'Terminal A', 9),  -- Aeropuerto Internacional O. R. Tambo
('J01', 'Terminal 1', 10); -- Aeropuerto de Sídney Kingsford Smith
