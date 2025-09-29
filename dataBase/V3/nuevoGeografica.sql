-- Nuevos países (Europa)
INSERT INTO Pais (ID_PAIS, Nombre, ID_CONTINENTE) VALUES
(32, 'Países Bajos', 3),
(33, 'Bélgica', 3),
(34, 'Suiza', 3),
(35, 'Austria', 3),
(36, 'Dinamarca', 3),
(37, 'Suecia', 3),
(38, 'Portugal', 3),
(39, 'Finlandia', 3),
(40, 'Noruega', 3),
(41, 'República Checa', 3);

-- Nuevas ciudades Europa (IDs 50 a 59)
INSERT INTO Ciudad (ID_CIUDAD, Nombre, ID_PAIS) VALUES
(50, 'Ámsterdam', 32),
(51, 'Bruselas', 33),
(52, 'Zurich', 34),
(53, 'Viena', 35),
(54, 'Copenhague', 36),
(55, 'Estocolmo', 37),
(56, 'Lisboa', 38),
(57, 'Helsinki', 39),
(58, 'Oslo', 40),
(59, 'Praga', 41);

-- Nuevos aeropuertos Europa (IDs 51 a 60)
INSERT INTO Aeropuerto (ID_AEROPUERTO, Nombre_Aeropuerto, Codigo_IATA, ID_CIUDAD) VALUES
(51, 'Amsterdam Schiphol Airport', 'AMS', 50),
(52, 'Brussels Airport', 'BRU', 51),
(53, 'Zurich Airport', 'ZRH', 52),
(54, 'Vienna International Airport', 'VIE', 53),
(55, 'Copenhagen Airport', 'CPH', 54),
(56, 'Stockholm Arlanda Airport', 'ARN', 55),
(57, 'Humberto Delgado Airport', 'LIS', 56),
(58, 'Helsinki-Vantaa Airport', 'HEL', 57),
(59, 'Oslo Gardermoen Airport', 'OSL', 58),
(60, 'Václav Havel Airport Prague', 'PRG', 59);

-- Puertas de embarque Europa (Aeropuertos 51–60)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
-- AMS
('AAA1', 'Terminal 1', 51),
('AAA2', 'Terminal 1', 51),
('AAA3', 'Terminal 2', 51),
-- BRU
('BBB1', 'Terminal A', 52),
('BBB2', 'Terminal A', 52),
('BBB3', 'Terminal B', 52),
-- ZRH
('CCC1', 'Terminal A', 53),
('CCC2', 'Terminal A', 53),
('CCC3', 'Terminal B', 53),
-- VIE
('DDD1', 'Terminal 1', 54),
('DDD2', 'Terminal 2', 54),
('DDD3', 'Terminal 2', 54),
-- CPH
('EEE1', 'Terminal 1', 55),
('EEE2', 'Terminal 1', 55),
('EEE3', 'Terminal 3', 55),
-- ARN
('FFF1', 'Terminal 2', 56),
('FFF2', 'Terminal 2', 56),
('FFF3', 'Terminal 5', 56),
-- LIS
('GGG1', 'Terminal 1', 57),
('GGG2', 'Terminal 1', 57),
('GGG3', 'Terminal 2', 57),
-- HEL
('HHH1', 'Terminal 1', 58),
('HHH2', 'Terminal 1', 58),
('HHH3', 'Terminal 2', 58),
-- OSL
('III1', 'Terminal 1', 59),
('III2', 'Terminal 1', 59),
('III3', 'Terminal 2', 59),
-- PRG
('JJJ1', 'Terminal 1', 60),
('JJJ2', 'Terminal 1', 60),
('JJJ3', 'Terminal 2', 60);

-- Puertas de embarque para Ninoy Aquino Intl. (MNL)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('P001', 'Terminal 1', 39),
('P002', 'Terminal 1', 39),
('P003', 'Terminal 2', 39);

-- Puertas de embarque para Cape Town Intl. (CPT)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('P004', 'Terminal A', 33),
('P005', 'Terminal B', 33),
('P006', 'Terminal C', 33);

-- Puertas de embarque para Indira Gandhi Intl. (DEL)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('P007', 'Terminal 1', 31),
('P008', 'Terminal 2', 31),
('P009', 'Terminal 3', 31);

-- Puertas de embarque para Cairo Intl. (CAI)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('P010', 'Terminal 1', 34),
('P011', 'Terminal 2', 34),
('P012', 'Terminal 3', 34);

-- Puertas de embarque para Suvarnabhumi Airport (BKK)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('P013', 'Terminal 1', 40),
('P014', 'Terminal 2', 40),
('P015', 'Terminal 3', 40);

-- Puertas de embarque para Istanbul Airport (IST)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('P016', 'Terminal 1', 37),
('P017', 'Terminal 2', 37),
('P018', 'Terminal 3', 37);

-- Puertas de embarque para Incheon Intl. (ICN)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('P019', 'Terminal 1', 32),
('P020', 'Terminal 2', 32),
('P021', 'Terminal 3', 32);

-- Puertas de embarque para Soekarno-Hatta Intl. (CGK)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('P022', 'Terminal 1', 38),
('P023', 'Terminal 2', 38),
('P024', 'Terminal 3', 38);

-- Puertas de embarque para Sheremetyevo Intl. (SVO)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('P025', 'Terminal A', 36),
('P026', 'Terminal B', 36),
('P027', 'Terminal C', 36);

-- Puertas de embarque para Auckland Intl. (AKL)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('P028', 'Terminal 1', 35),
('P029', 'Terminal 2', 35),
('P030', 'Terminal 3', 35);



select * from pais
join continente on continente.id_continente = pais.id_continente;


select * from aeropuerto arp
left join puerta_embarque prt ON prt.id_aeropuerto = arp.id_aeropuerto
where prt.id_aeropuerto is null;

