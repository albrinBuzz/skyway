INSERT INTO Continente (ID_CONTINENTE, Nombre) VALUES
(1, 'América del Sur'),
(2, 'América del Norte'),
(3, 'Europa'),
(4, 'Asia'),
(5, 'Oceanía'),
(6, 'África'),
(7, 'Oriente Medio');


INSERT INTO Pais (ID_PAIS, Nombre) VALUES
(1, 'Chile'),
(2, 'Uruguay'),
(3, 'Brasil'),
(4, 'USA'),
(5, 'Perú'),
(6, 'Colombia'),
(7, 'España'),
(8, 'Francia'),
(9, 'Reino Unido'),
(10, 'Alemania'),
(11, 'Italia'),
(12, 'Canada'),
(13, 'Mexico'),
(14, 'Australia'),
(15, 'China'),
(16, 'Japon'),
(17, 'Argentina'),
(18, 'Panama'),
(19, 'Qatar'),
(20, 'Emiratos Arabes Unidos'),
(21, 'India'),
(22, 'Corea del Sur'),
(23, 'Sudáfrica'),
(24, 'Egipto'),
(25, 'Nueva Zelanda'),
(26, 'Rusia'),
(27, 'Turquía'),
(28, 'Indonesia'),
(29, 'Filipinas'),
(30, 'Tailandia');
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
(41, 'República Checa', 3),
(42, 'Polonia', 3),           -- Europa
(43, 'Hungría', 3),           -- Europa
(44, 'Rumanía', 3),           -- Europa
(45, 'Bulgaria', 3),          -- Europa
(46, 'Croacia', 3),           -- Europa
(47, 'Eslovenia', 3),         -- Europa
(48, 'Serbia', 3),            -- Europa
(49, 'Albania', 3),           -- Europa
(50, 'Macedonia del Norte', 3); -- Europa



-- Asignar Continente América del Sur
UPDATE Pais SET ID_CONTINENTE = 1 WHERE Nombre IN ('Chile', 'Uruguay', 'Brasil', 'Perú', 'Colombia', 'Argentina');

-- Asignar Continente América del Norte
UPDATE Pais SET ID_CONTINENTE = 2 WHERE Nombre IN ('USA', 'Canada', 'Mexico', 'Panama');

-- Asignar Continente Europa
UPDATE Pais SET ID_CONTINENTE = 3 WHERE Nombre IN ('España', 'Francia', 'Reino Unido', 'Alemania', 'Italia');

-- Asignar Continente Asia
UPDATE Pais SET ID_CONTINENTE = 4 WHERE Nombre IN ('China', 'Japon', 'India', 'Corea del Sur', 'Rusia', 'Turquía', 'Indonesia', 'Filipinas', 'Tailandia');

-- Asignar Continente Oceanía
UPDATE Pais SET ID_CONTINENTE = 5 WHERE Nombre IN ('Australia', 'Nueva Zelanda');

-- Asignar Continente Oriente Medio
UPDATE Pais SET ID_CONTINENTE = 7 WHERE Nombre IN ('Qatar', 'Emiratos Arabes Unidos');

-- Asignar Continente África
UPDATE Pais SET ID_CONTINENTE = 6 WHERE Nombre IN ('Sudáfrica', 'Egipto');




-- Insertar datos en la tabla Ciudad
INSERT INTO Ciudad (ID_CIUDAD, Nombre, ID_PAIS) VALUES
(1, 'Santiago', 1),
(2, 'Montevideo', 2),
(3, 'SaoPaulo', 3),
(4, 'NuevaYork', 4),
(5, 'LosAngeles', 4),
(6, 'Lima', 5),
(7, 'Atlanta', 4),
(8, 'Bogota', 6),
(9, 'Miami', 4),
(10, 'Madrid', 7),
(11, 'Barcelona', 7),
(12, 'Paris', 8),
(13, 'Londres', 9),
(14, 'Berlin', 10),
(15, 'Roma', 11),
(16, 'Toronto', 12),
(17, 'CiudadDeMexico', 13),
(18, 'Vancouver', 12),
(19, 'Melbourne', 14),
(20, 'Sidney', 14),
(21, 'Beijing', 15),
(22, 'Tokyo', 16),
(23, 'BuenosAires', 17),
(24, 'Doha', 19),
(25, 'Dubai', 20),
(26, 'Frankfurt', 10),
(27, 'PanamaCity', 18),
(28, 'Chicago', 4),
(29, 'Houston', 4),
(30, 'NuevaDelhi', 21),
(31, 'Seul', 22),
(32, 'CiudadDelCabo', 23),
(33, 'ElCairo', 24),
(34, 'Auckland', 25),
(35, 'Moscu', 26),
(36, 'Estambul', 27),
(37, 'Yakarta', 28),
(38, 'Manila', 29),
(39, 'Bangkok', 30);
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
(59, 'Praga', 41),
(60, 'San Francisco', 4),
(61, 'Washington D.C.', 4),
(62, 'Boston', 4),
(63, 'Budapest', 43),              -- Hungría
(64, 'Cluj-Napoca', 44),           -- Rumanía
(65, 'Sofia', 45),                 -- Bulgaria
(66, 'Zagreb', 46),                -- Croacia
(67, 'Ljubljana', 47),             -- Eslovenia
(68, 'Belgrado', 48),              -- Serbia
(69, 'Tirana', 49),                -- Albania
(70, 'Skopie', 50),                -- Macedonia del Norte
(71, 'Melbourne', 14),             -- Australia
(72, 'Auckland', 25),              -- Nueva Zelanda
(73, 'Brisbane', 14),              -- Australia
(74, 'Wellington', 25);            -- Nueva Zelanda

INSERT INTO Aeropuerto (ID_AEROPUERTO, Nombre_Aeropuerto, Codigo_IATA, ID_CIUDAD) VALUES
(1, 'Aeropuerto Internacional Comodoro Arturo Merino Benítez', 'SCL', 1),
(2, 'Carrasco Intl.', 'MVD', 2),
(3, 'Guarulhos Intl.', 'GRU', 3),
(4, 'John F Kennedy', 'JFK', 4),
(5, 'Los Angeles Intl.', 'LAX', 5),
(6, 'J Chavez Intl.', 'LIM', 6),
(7, 'Hartsfield Jackson Atlanta Int.', 'ATL', 7),
(8, 'El Dorado International Airport', 'BOG', 8),
(9, 'Miami International Airport', 'MIA', 9),
(10, 'Adolfo Suarez Madrid Barajas', 'MAD', 10),
(11, 'El Prat Barcelona', 'BCN', 11),
(12, 'Charles de Gaulle', 'CDG', 12),
(13, 'Heathrow Airport', 'LHR', 13),
(14, 'Berlin Brandenburg', 'BER', 14),
(15, 'Leonardo da Vinci Fiumicino', 'FCO', 15),
(16, 'Toronto Pearson Intl.', 'YYZ', 16),
(17, 'Benito Juarez Intl.', 'MEX', 17),
(18, 'Vancouver Intl.', 'YVR', 18),
(19, 'Melbourne Airport', 'MEL', 19),
(20, 'Sydney Kingsford Smith', 'SYD', 20),
(21, 'Beijing Capital Intl.', 'PEK', 21),
(22, 'Tokyo Haneda', 'HND', 22),
(23, 'Ezeiza Ministro Pistarini', 'EZE', 23),
(24, 'Hamad Intl. Airport', 'DOH', 24),
(25, 'Dubai Intl. Airport', 'DXB', 25),
(26, 'Frankfurt am Main', 'FRA', 26),
(27, 'Tocumen Intl.', 'PTY', 27),
(28, 'Chicago OHare', 'ORD', 28),
(29, 'George Bush Intercontinental', 'IAH', 29),
(30, 'Gatwick Airport', 'LGW', 13),
(31, 'Indira Gandhi Intl.', 'DEL', 30),
(32, 'Incheon Intl.', 'ICN', 31),
(33, 'Cape Town Intl.', 'CPT', 32),
(34, 'Cairo Intl.', 'CAI', 33),
(35, 'Auckland Intl.', 'AKL', 34),
(36, 'Sheremetyevo Intl.', 'SVO', 35),
(37, 'Istanbul Airport', 'IST', 36),
(38, 'Soekarno-Hatta Intl.', 'CGK', 37),
(39, 'Ninoy Aquino Intl.', 'MNL', 38),
(40, 'Suvarnabhumi Airport', 'BKK', 39);

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
(60, 'Václav Havel Airport Prague', 'PRG', 59),
(61, 'San Francisco International Airport', 'SFO', 60),
(62, 'Washington D.C. Dulles International Airport', 'IAD', 61),
(63, 'Logan International Airport', 'BOS', 62),
(64, 'Budapest Ferenc Liszt International Airport', 'BUD', 63),   -- Budapest, Hungría
(65, 'Cluj-Napoca International Airport', 'CLJ', 64),            -- Cluj-Napoca, Rumanía
(66, 'Sofia Airport', 'SOF', 65),                                 -- Sofia, Bulgaria
(67, 'Zagreb International Airport', 'ZAG', 66),                 -- Zagreb, Croacia
(68, 'Ljubljana Jože Pučnik Airport', 'LJU', 67),                -- Ljubljana, Eslovenia
(69, 'Belgrade Nikola Tesla Airport', 'BEG', 68),                -- Belgrado, Serbia
(70, 'Tirana International Airport', 'TIA', 69),                 -- Tirana, Albania
(71, 'Skopje Alexander the Great Airport', 'SKP', 70),           -- Skopie, Macedonia del Norte
(72, 'Brisbane Airport', 'BNE', 73),                              -- Brisbane, Australia
(73, 'Wellington Airport', 'WLG', 74);                            -- Wellington, Nueva Zelanda


-- Suponiendo que ya existen Aeropuertos con ID 1 al 5

INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('A1', 'Terminal 1', 1),
('A2', 'Terminal 1', 1),
('A3', 'Terminal 1', 1),
('B1', 'Terminal 2', 1),
('B2', 'Terminal 2', 1),
('B3', 'Terminal 2', 1),
('C1', 'Terminal 3', 2),
('C2', 'Terminal 3', 2),
('C3', 'Terminal 3', 2),
('D1', 'Terminal 4', 2),
('D2', 'Terminal 4', 2),
('E1', 'Terminal 1', 3),
('E2', 'Terminal 1', 3),
('E3', 'Terminal 1', 3),
('F1', 'Terminal 2', 3),
('F2', 'Terminal 2', 3),
('G1', 'Terminal 1', 4),
('G2', 'Terminal 1', 4),
('G3', 'Terminal 1', 4),
('H1', 'Terminal 2', 5),
('H2', 'Terminal 2', 5),
('I1', 'Terminal 3', 5),
('I2', 'Terminal 3', 5),
('I3', 'Terminal 3', 5);

-- Aeropuerto 6 (LIM)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('J1', 'Terminal Nacional', 6),
('J2', 'Terminal Internacional', 6),
('J3', 'Terminal Internacional', 6);

-- Aeropuerto 7 (ATL)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('K1', 'Terminal South', 7),
('K2', 'Terminal South', 7),
('K3', 'Terminal North', 7);

-- Aeropuerto 8 (BOG)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('L1', 'Terminal 1', 8),
('L2', 'Terminal 1', 8),
('L3', 'Terminal 2', 8);

-- Aeropuerto 9 (MIA)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('M1', 'Concourse D', 9),
('M2', 'Concourse E', 9),
('M3', 'Concourse E', 9);

-- Aeropuerto 10 (MAD)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('N1', 'T1', 10),
('N2', 'T1', 10),
('N3', 'T2', 10);

-- Aeropuerto 11 (BCN)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('O1', 'T1', 11),
('O2', 'T1', 11),
('O3', 'T2', 11);

-- Aeropuerto 12 (CDG)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('P1', 'Terminal 2E', 12),
('P2', 'Terminal 2F', 12),
('P3', 'Terminal 2G', 12);

-- Aeropuerto 13 (LHR)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('Q1', 'Terminal 5', 13),
('Q2', 'Terminal 5', 13),
('Q3', 'Terminal 3', 13);

-- Aeropuerto 14 (BER)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('R1', 'T1', 14),
('R2', 'T1', 14),
('R3', 'T2', 14);

-- Aeropuerto 15 (FCO)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('S1', 'Terminal 3', 15),
('S2', 'Terminal 3', 15),
('S3', 'Terminal 1', 15);

-- Aeropuerto 16 (YYZ)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('T1', 'Terminal 1', 16),
('T2', 'Terminal 3', 16),
('T3', 'Terminal 3', 16);

-- Aeropuerto 17 (MEX)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('U1', 'Terminal 1', 17),
('U2', 'Terminal 2', 17),
('U3', 'Terminal 2', 17);

-- Aeropuerto 18 (YVR)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('V1', 'Domestic Terminal', 18),
('V2', 'International Terminal', 18),
('V3', 'International Terminal', 18);

-- Aeropuerto 19 (MEL)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('W1', 'Terminal 2', 19),
('W2', 'Terminal 2', 19),
('W3', 'Terminal 1', 19);

-- Aeropuerto 20 (SYD)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('X1', 'T1', 20),
('X2', 'T1', 20),
('X3', 'T2', 20);

-- Aeropuerto 21 (PEK)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('Y1', 'Terminal 3', 21),
('Y2', 'Terminal 3', 21),
('Y3', 'Terminal 2', 21);

-- Aeropuerto 22 (HND)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('Z1', 'Terminal 1', 22),
('Z2', 'Terminal 2', 22),
('Z3', 'Terminal 3', 22);

-- Aeropuerto 23 (EZE)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('AA1', 'Terminal A', 23),
('AA2', 'Terminal B', 23),
('AA3', 'Terminal C', 23);

-- Aeropuerto 24 (DOH)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('BB1', 'Main Terminal', 24),
('BB2', 'Main Terminal', 24),
('BB3', 'Main Terminal', 24);

-- Aeropuerto 25 (DXB)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('CC1', 'Terminal 3', 25),
('CC2', 'Terminal 1', 25),
('CC3', 'Terminal 2', 25);

-- Aeropuerto 26 (FRA)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('DD1', 'Terminal 1', 26),
('DD2', 'Terminal 2', 26),
('DD3', 'Terminal 1', 26);

-- Aeropuerto 27 (PTY)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('EE1', 'North Terminal', 27),
('EE2', 'South Terminal', 27),
('EE3', 'South Terminal', 27);

-- Aeropuerto 28 (ORD)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('FF1', 'Terminal 1', 28),
('FF2', 'Terminal 3', 28),
('FF3', 'Terminal 5', 28);

-- Aeropuerto 29 (IAH)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('GG1', 'Terminal A', 29),
('GG2', 'Terminal B', 29),
('GG3', 'Terminal E', 29);

-- Aeropuerto 30 (LGW)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('HH1', 'North Terminal', 30),
('HH2', 'South Terminal', 30),
('HH3', 'South Terminal', 30);


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

-- Puertas de embarque para el Aeropuerto Internacional de San Francisco
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('A1', 'Terminal 1', 60),    -- San Francisco International Airport
('A2', 'Terminal 1', 60),    -- San Francisco International Airport
('B1', 'Terminal 2', 60),    -- San Francisco International Airport
('B2', 'Terminal 2', 60),    -- San Francisco International Airport
('C1', 'Terminal 3', 60),    -- San Francisco International Airport
('C2', 'Terminal 3', 60);    -- San Francisco International Airport

-- Puertas de embarque para el Aeropuerto Internacional Dulles de Washington D.C.
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('A10', 'Terminal A', 61),    -- Washington D.C. Dulles International Airport
('A11', 'Terminal A', 61),    -- Washington D.C. Dulles International Airport
('B10', 'Terminal B', 61),    -- Washington D.C. Dulles International Airport
('B11', 'Terminal B', 61),    -- Washington D.C. Dulles International Airport
('C10', 'Terminal C', 61),    -- Washington D.C. Dulles International Airport
('C11', 'Terminal C', 61);    -- Washington D.C. Dulles International Airport

-- Puertas de embarque para el Aeropuerto Internacional Logan de Boston
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('A2', 'Terminal A', 62),    -- Logan International Airport (Boston, USA)
('A3', 'Terminal A', 62),    -- Logan International Airport (Boston, USA)
('B3', 'Terminal B', 62),    -- Logan International Airport (Boston, USA)
('B4', 'Terminal B', 62),    -- Logan International Airport (Boston, USA)
('C1', 'Terminal C', 62),    -- Logan International Airport (Boston, USA)
('C2', 'Terminal C', 62);    -- Logan International Airport (Boston, USA)

-- Puertas de embarque para el Aeropuerto Internacional Ferenc Liszt de Budapest
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('1A', 'Terminal A', 63),    -- Budapest Ferenc Liszt International Airport (Budapest, Hungría)
('1B', 'Terminal A', 63),    -- Budapest Ferenc Liszt International Airport (Budapest, Hungría)
('2A', 'Terminal B', 63),    -- Budapest Ferenc Liszt International Airport (Budapest, Hungría)
('2B', 'Terminal B', 63),    -- Budapest Ferenc Liszt International Airport (Budapest, Hungría)
('3A', 'Terminal C', 63),    -- Budapest Ferenc Liszt International Airport (Budapest, Hungría)
('3B', 'Terminal C', 63);    -- Budapest Ferenc Liszt International Airport (Budapest, Hungría)

-- Puertas de embarque para el Aeropuerto Internacional de Cluj-Napoca
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('10', 'Terminal 1', 64),    -- Cluj-Napoca International Airport (Cluj-Napoca, Rumanía)
('11', 'Terminal 1', 64),    -- Cluj-Napoca International Airport (Cluj-Napoca, Rumanía)
('12', 'Terminal 2', 64),    -- Cluj-Napoca International Airport (Cluj-Napoca, Rumanía)
('13', 'Terminal 2', 64);    -- Cluj-Napoca International Airport (Cluj-Napoca, Rumanía)

-- Puertas de embarque para el Aeropuerto Internacional de Sofia
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('1', 'Terminal 1', 65),    -- Sofia Airport (Sofia, Bulgaria)
('2', 'Terminal 1', 65),    -- Sofia Airport (Sofia, Bulgaria)
('3', 'Terminal 2', 65),    -- Sofia Airport (Sofia, Bulgaria)
('4', 'Terminal 2', 65);    -- Sofia Airport (Sofia, Bulgaria)

-- Puertas de embarque para el Aeropuerto Internacional de Zagreb
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('A1', 'Terminal A', 66),    -- Zagreb International Airport (Zagreb, Croacia)
('A2', 'Terminal A', 66),    -- Zagreb International Airport (Zagreb, Croacia)
('B1', 'Terminal B', 66),    -- Zagreb International Airport (Zagreb, Croacia)
('B2', 'Terminal B', 66);    -- Zagreb International Airport (Zagreb, Croacia)

-- Puertas de embarque para el Aeropuerto Internacional de Ljubljana
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('1', 'Terminal 1', 67),    -- Ljubljana Jože Pučnik Airport (Ljubljana, Eslovenia)
('2', 'Terminal 1', 67),    -- Ljubljana Jože Pučnik Airport (Ljubljana, Eslovenia)
('3', 'Terminal 2', 67),    -- Ljubljana Jože Pučnik Airport (Ljubljana, Eslovenia)
('4', 'Terminal 2', 67);    -- Ljubljana Jože Pučnik Airport (Ljubljana, Eslovenia)

-- Puertas de embarque para el Aeropuerto Internacional Nikola Tesla de Belgrado
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('A1', 'Terminal A', 68),    -- Belgrade Nikola Tesla Airport (Belgrado, Serbia)
('A2', 'Terminal A', 68),    -- Belgrade Nikola Tesla Airport (Belgrado, Serbia)
('B1', 'Terminal B', 68),    -- Belgrade Nikola Tesla Airport (Belgrado, Serbia)
('B2', 'Terminal B', 68);    -- Belgrade Nikola Tesla Airport (Belgrado, Serbia)

-- Puertas de embarque para el Aeropuerto Internacional de Tirana
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('1', 'Terminal 1', 69),    -- Tirana International Airport (Tirana, Albania)
('2', 'Terminal 1', 69),    -- Tirana International Airport (Tirana, Albania)
('3', 'Terminal 2', 69);    -- Tirana International Airport (Tirana, Albania)

-- Puertas de embarque para -- Tirana International Airport (Tirana, Albania)
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('A1', 'Terminal 1', 70),
('A2', 'Terminal 1', 70),
('B1', 'Terminal 2', 70);

-- Puertas de embarque para el Aeropuerto Internacional de Skopje
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('A1', 'Terminal 1', 71),    -- Skopje Alexander the Great Airport (Skopie, Macedonia del Norte)
('A2', 'Terminal 1', 71),    -- Skopje Alexander the Great Airport (Skopie, Macedonia del Norte)
('B1', 'Terminal 2', 71);    -- Skopje Alexander the Great Airport (Skopie, Macedonia del Norte)

-- Puertas de embarque para el Aeropuerto Internacional de Brisbane
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('1A', 'Terminal 1', 72),    -- Brisbane Airport (Brisbane, Australia)
('1B', 'Terminal 1', 72),    -- Brisbane Airport (Brisbane, Australia)
('2A', 'Terminal 2', 72),    -- Brisbane Airport (Brisbane, Australia)
('2B', 'Terminal 2', 72);    -- Brisbane Airport (Brisbane, Australia)

-- Puertas de embarque para el Aeropuerto Internacional de Wellington
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO) VALUES
('1', 'Terminal 1', 73),    -- Wellington Airport (Wellington, Nueva Zelanda)
('2', 'Terminal 2', 73);    -- Wellington Airport (Wellington, Nueva Zelanda)

select * from aeropuerto where id_aeropuerto=71;


INSERT INTO Aerolinea (ID_AEROLINEA, Nombre, Codigo) VALUES
  (1, 'LATAM Airlines Brasil', 'JJ'),
  (2, 'Delta Air Lines', 'DL'),
  (3, 'American Airlines', 'AA'),
  (4, 'British Airways', 'BA'),
  (5, 'Air France', 'AF'),
  (6, 'Lufthansa', 'LH'),
  (7, 'Emirates', 'EK'),
  (8, 'Qatar Airways', 'QR'),
  (9, 'Air Canada', 'AC'),
  (10, 'Aeromexico', 'AM');


INSERT INTO Clase_asiento (Descripcion) VALUES
('Económica'),
('Ejecutiva'),
('Primera Clase');


INSERT INTO Fabricante (Nombre)
VALUES
    ('Airbus'),
    ('Boeing'),
    ('Embraer');

-- Insertar los modelos de aviones
INSERT INTO Modelo_Avion (Nombre, ID_FABRICANTE)
VALUES
    ('Airbus A320', (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Airbus')),
    ('Boeing 747', (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Boeing')),
    ('Airbus A350', (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Airbus')),
    ('Boeing 787', (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Boeing')),
    ('Embraer E195', (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Embraer')),
    ('Boeing 777', (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Boeing')),
    ('Airbus A380', (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Airbus')),
    ('Airbus A330', (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Airbus')),
    ('Boeing 757', (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Boeing'));

-- Insertar aviones con los datos correspondientes
INSERT INTO Avion (
    Numero_de_Registro,
    ID_MODELO,
    Ano_de_Fabricacion,
    Capacidad_de_Pasajeros,
    Capacidad_de_Carga,
    Estado_de_Mantenimiento,
    Fecha_Proximo_Mantenimiento
)
VALUES
    ('DEF456', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Airbus A320'), 2018, 185, 15000, 'En mantenimiento', NULL),
    ('GHI789', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Boeing 747'), 2005, 380, 45000, 'Operativo', NULL),
    ('JKL012', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Airbus A350'), 2019, 310, 40000, 'Operativo', NULL),
    ('MNO345', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Boeing 787'), 2020, 220, 35000, 'En servicio', NULL),
    ('PQR678', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Embraer E195'), 2016, 120, 12000, 'Operativo', NULL),
    ('XYZ123', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Boeing 777'), 2014, 450, 50000, 'En servicio', NULL),
    ('LMN987', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Airbus A380'), 2018, 650, 75000, 'Operativo', NULL),
    ('STU456', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Airbus A330'), 2017, 250, 35000, 'En mantenimiento', NULL),
    ('WXY543', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Boeing 757'), 2003, 190, 22000, 'Operativo', NULL);

INSERT INTO Capacidad_Clase (ID_AVION, ID_CLASE, Cantidad)
SELECT avion.ID_AVION, clase.ID_CLASE, capacidad
FROM (
    VALUES
        ('DEF456', 'Económica', 150), ('DEF456', 'Ejecutiva', 30), ('DEF456', 'Primera Clase', 5),
        ('GHI789', 'Económica', 300), ('GHI789', 'Ejecutiva', 50), ('GHI789', 'Primera Clase', 30),
        ('JKL012', 'Económica', 250), ('JKL012', 'Ejecutiva', 40), ('JKL012', 'Primera Clase', 20),
        ('MNO345', 'Económica', 180), ('MNO345', 'Ejecutiva', 40), ('MNO345', 'Primera Clase', 15),
        ('PQR678', 'Económica', 100), ('PQR678', 'Ejecutiva', 10), ('PQR678', 'Primera Clase', 5),
        ('XYZ123', 'Económica', 350), ('XYZ123', 'Ejecutiva', 70), ('XYZ123', 'Primera Clase', 30),
        ('LMN987', 'Económica', 500), ('LMN987', 'Ejecutiva', 100), ('LMN987', 'Primera Clase', 50),
        ('STU456', 'Económica', 200), ('STU456', 'Ejecutiva', 40), ('STU456', 'Primera Clase', 10),
        ('WXY543', 'Económica', 150), ('WXY543', 'Ejecutiva', 30), ('WXY543', 'Primera Clase', 10)
) AS datos(numero_registro, descripcion_clase, capacidad)
JOIN Avion avion ON avion.Numero_de_Registro = datos.numero_registro
JOIN Clase_asiento clase ON clase.Descripcion = datos.descripcion_clase;



-- Insertar datos en la tabla Estado_Vuelo
INSERT INTO Estado_Vuelo (Descripcion, Estado) VALUES
('Vuelo Programado', 'Programado'),
('Vuelo Cancelado', 'Cancelado'),
('Vuelo Retrasado', 'Retrasado'),
('Programado', 'Activo'),
('En vuelo', 'Activo'),
('Aterrizado', 'Finalizado'),
('Cancelado', 'Inactivo'),
('Demorado', 'Activo');



-- Insertar estados de reserva
INSERT INTO Estado_reserva (Descripcion) VALUES
('Pendiente'),
('Confirmada'),
('Cancelada'),
('Check-in Realizado');

-- Insertar los tipos de método de pago
-- Insertar métodos de pago
INSERT INTO Metodo_Pago (Descripcion) VALUES
('Tarjeta Visa'),
('MasterCard'),
('Transferencia'),
('PayPal');


INSERT INTO Tipo_Equipaje (ID_TIPO, Nombre) VALUES
(1, 'Equipaje de Mano'),
(2, 'Equipaje Facturado'),
(3, 'Equipaje Especial');

INSERT INTO Tipo_Turno (ID_TIPO, Nombre) VALUES
(1, 'Vuelo'),
(2, 'Capacitación'),
(3, 'Descanso');


-- Inserta roles una vez, explícitamente
INSERT INTO Roles (nombre, descripcion)
VALUES
('Admin', 'Administrador del sistema'),
('Pasajero', 'Cliente registrado que puede reservar vuelos'),
('Piloto', 'Piloto de la aerolínea'),
('Tripulacion', 'Miembro del equipo de vuelo (azafatas, copilotos, etc.)'),
('Administrativo', 'Empleado administrativo de la aerolínea');

-- Insertar un usuario con rol 'admin'
-- ===========================================================
-- Este INSERT agrega un usuario con el rol de 'admin'.
-- El 'id_rol' para 'admin' es 1.
-- Posteriormente, se asigna el rol 'admin' al usuario en la tabla RolUsuario.
INSERT INTO Usuario (RUT, Nombre, Apellido, Correo_Electronico, Telefono, Documento_Identidad, Fecha_Nacimiento, Contrasena, Fecha_Registro)
VALUES ('123456789-0', 'Juan', 'Pérez', 'juan.perez@correo.com', '123456789', '987654321', '1985-05-15', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', NOW());

-- Asignar el rol 'admin' al usuario en la tabla RolUsuario
-- ===========================================================
-- Este INSERT asigna el rol 'admin' (id_rol = 1) al usuario 'Juan Pérez'.
INSERT INTO RolUsuario (rut_usuario, id_rol)
VALUES ('123456789-0', 1);


-- ===========================================================
-- 2. Insertar Pilotos en la tabla Usuario
-- ===========================================================
-- Este bloque inserta a varios pilotos en la tabla 'Usuario' con sus datos.
-- Estos usuarios tienen el rol 'Piloto' que se asignará en el siguiente paso.
INSERT INTO Usuario (RUT, Nombre, Apellido, Correo_Electronico, Telefono, Documento_Identidad, Fecha_Nacimiento, Contrasena, Fecha_Registro)
VALUES
('12345678-9', 'Juan', 'Pérez', 'juan.perez@piloto.com', '123456789', '12345678A', '1980-05-20', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', NOW()),
('87654321-0', 'María', 'Gómez', 'maria.gomez@piloto.com', '987654321', '87654321B', '1985-08-15', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', NOW()),
('11223344-1', 'Diego', 'Fernández', 'diego.fernandez@piloto.com', '456456456', '11223344E', '1975-03-10', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', NOW()),
('55667788-2', 'Sofía', 'Romero', 'sofia.romero@piloto.com', '654654654', '55667788F', '1988-12-25', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', NOW()),
('98765432-1', 'Andrés', 'Martinez', 'andres.martinez@piloto.com', '987987987', '99887766G', '1992-07-19', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', NOW());

-- ===========================================================
-- 3. Asignar rol 'Piloto' a los usuarios
-- ===========================================================
-- Este bloque asigna el rol 'Piloto' (id_rol = 2) a los usuarios insertados anteriormente.
-- Esto asegura que los usuarios sean reconocidos como pilotos.
INSERT INTO RolUsuario (rut_usuario, id_rol)
VALUES
('12345678-9', 3),  -- Juan Pérez
('87654321-0', 3),  -- María Gómez
('11223344-1', 3),  -- Diego Fernández
('55667788-2', 3),  -- Sofía Romero
('98765432-1', 3);  -- Andrés Martínez

-- ===========================================================
-- 4. Insertar detalles de los Pilotos en la tabla 'Piloto'
-- ===========================================================
-- En esta sección, insertamos información más específica de cada piloto,
-- como la licencia, experiencia y especializaciones.
INSERT INTO Piloto (RUT, Licencia, Experiencia_anos, Especializaciones)
VALUES
('12345678-9', 'Licencia A', 9, 'Vuelo comercial'),
('87654321-0', 'Licencia A', 8, 'Vuelo comercial'),
('11223344-1', 'Licencia A', 7, 'Vuelo comercial'),
('55667788-2', 'Licencia A', 12, 'Vuelo comercial'),
('98765432-1', 'Licencia A', 10, 'Vuelo comercial');
-- ===========================================================
-- 5. Insertar Azafatas en la tabla Usuario
-- ===========================================================
-- Aquí insertamos usuarios con el rol de 'Azafata'. Tienen sus respectivos datos.
INSERT INTO Usuario (RUT, Nombre, Apellido, Correo_Electronico, Telefono, Documento_Identidad, Fecha_Nacimiento, Contrasena)
VALUES
('55601234-5', 'Roberto', 'García', 'roberto.garcia@azafata.com', '555123456', '55601234K', '1985-03-25', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m'),
('23456789-1', 'María', 'García', 'maria.garcia@azafata.com', '555654321', '23456789L', '1990-06-17', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m'),
('34567890-2', 'Pedro', 'Martínez', 'pedro.martinez@azafata.com', '555987654', '34567890M', '1988-11-10', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m'),
('45678901-3', 'Ana', 'Ramírez', 'ana.ramirez@azafata.com', '555321987', '45678901N', '1993-04-28', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m');
-- ===========================================================
-- 6. Asignar rol 'Azafata' a los usuarios
-- ===========================================================
-- Aquí asignamos el rol 'Azafata' (id_rol = 3) a los usuarios de la sección anterior.
INSERT INTO RolUsuario (rut_usuario, id_rol)
VALUES
('55601234-5', 4),  -- Azafata 1
('23456789-1', 4),  -- Azafata 2
('34567890-2', 4),  -- Azafata 3
('45678901-3', 4);  -- Azafata 4

-- ===========================================================
-- 7. Insertar Azafatas en la tabla Tripulacion
-- ===========================================================
-- Aquí insertamos a las azafatas en la tabla 'Tripulacion', donde se les asigna
-- el cargo correspondiente y la fecha de ingreso.
INSERT INTO Tripulacion (RUT, Cargo, Fecha_Ingreso)
VALUES
('55601234-5', 'Azafata', NOW()),  -- Roberto García
('23456789-1', 'Azafata', NOW()),  -- María García
('34567890-2', 'Azafata', NOW()),  -- Pedro Martínez
('45678901-3', 'Azafata', NOW());  -- Ana Ramírez
-- ===========================================================
-- 8. Insertar Pasajeros en la tabla Usuario
-- ===========================================================
-- Este bloque inserta usuarios con el rol de 'Pasajero'.
-- Cada uno tiene datos personales como nombre, correo, etc.
INSERT INTO Usuario (RUT, Nombre, Apellido, Correo_Electronico, Telefono, Documento_Identidad, Fecha_Nacimiento, Contrasena)
VALUES
--('98765432-1', 'Carlos', 'Pérez', 'carlos.perez@pasajero.com', '555456123', '98765432B', '1995-07-15', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m'),
('87654321-2', 'Lucía', 'Martínez', 'lucia.martinez@pasajero.com', '555987123', '87654321C', '1992-03-22', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m'),
('76543210-3', 'Juan', 'López', 'juan.lopez@pasajero.com', '555321654', '76543210D', '1988-11-05', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m'),
('65432109-4', 'Ana', 'González', 'ana.gonzalez@pasajero.com', '555123987', '65432109E', '1987-01-10', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m');

INSERT INTO RolUsuario (rut_usuario, id_rol)
VALUES
('98765432-1', 2),  -- Carlos Pérez
('87654321-2', 2),  -- Lucía Martínez
('76543210-3', 2),  -- Juan López
('65432109-4', 2);  -- Ana González
INSERT INTO RolUsuario (rut_usuario, id_rol)
VALUES
('12345678-9', 2);  -- Rol de Pasajero (id_rol = 4)

INSERT INTO Pasajero (RUT, Nacionalidad)
VALUES
('98765432-1', 'Chilena'),  -- Carlos Pérez
('87654321-2', 'Argentina'),  -- Lucía Martínez
('76543210-3', 'Peruana'),  -- Juan López
('65432109-4', 'Colombiana');  -- Ana González
INSERT INTO Pasajero (RUT, Nacionalidad)
VALUES
('12345678-9', 'Chilena');


select * from aeropuerto arp
join puerta_embarque prt
on prt.id_aeropuerto = arp.id_aeropuerto
where prt.id_aeropuerto is null;



-- Insertar vuelos


-- Insertar vuelos con los RUTs de los pilotos correctos y estados de vuelo
INSERT INTO Vuelo (ID_VUELO, Numero_Vuelo, Fecha_Hora_Salida, Fecha_Hora_Llegada, ID_ESTADO_VUELO, ID_AVION, RUT_PILOTO, ID_AEROLINEA) VALUES
  (nextval('vuelo_seq'),'LA8117', '2025-08-01 14:30', '2025-08-01 17:45', 1, 1, '12345678-9', 1), -- Vuelo 1: SCL -> MVD (Parte del Itinerario 1)
  (nextval('vuelo_seq'),'LA8117', '2025-08-01 18:55', '2025-08-01 21:30', 1, 1, '12345678-9', 1), -- Vuelo 2: MVD -> GRU (Parte del Itinerario 1)
  (nextval('vuelo_seq'),'LA8180', '2025-08-01 22:50', '2025-08-02 07:35', 1, 2, '98765432-1', 1), -- Vuelo 3: GRU -> JFK (Parte del Itinerario 1)
  (nextval('vuelo_seq'),'LA8989', '2025-08-02 09:55', '2025-08-02 12:50', 1, 3, '98765432-1', 2), -- Vuelo 4: JFK -> LAX (Parte del Itinerario 1)
  (nextval('vuelo_seq'),'LA650',  '2025-08-01 07:50', '2025-08-01 10:40', 1, 1, '12345678-9', 1), -- Vuelo 5: SCL -> LIM (Itinerarios 2, 3 y 5)
  (nextval('vuelo_seq'),'LA2482', '2025-08-01 12:00', '2025-08-01 20:15', 1, 3, '12345678-9', 1), -- Vuelo 6: LIM -> ATL (Itinerarios 3 y 5)
  (nextval('vuelo_seq'),'LA8954', '2025-08-01 22:30', '2025-08-02 01:06', 1, 1, '98765432-1', 2), -- Vuelo 7: ATL -> JFK (Itinerario 3)
  (nextval('vuelo_seq'),'LA8120', '2025-08-02 15:00', '2025-08-02 17:30', 1, 2, '12345678-9', 1), -- Vuelo 8: MVD -> SCL (no está en un itinerario actual)
  (nextval('vuelo_seq'),'LA8130', '2025-08-02 19:00', '2025-08-02 21:45', 1, 1, '98765432-1', 1), -- Vuelo 9: GRU -> MVD (no está en un itinerario actual)
  (nextval('vuelo_seq'),'LA8140', '2025-08-02 22:30', '2025-08-03 01:30', 1, 3, '12345678-9', 2), -- Vuelo 10: LAX -> ATL (no está en un itinerario actual)
  (nextval('vuelo_seq'),'LA8150', '2025-08-03 03:00', '2025-08-03 06:00', 1, 2, '98765432-1', 2), -- Vuelo 11: ATL -> MVD (no está en un itinerario actual)
  (nextval('vuelo_seq'),'LA8160', '2025-08-03 08:00', '2025-08-03 10:30', 1, 1, '12345678-9', 1), -- Vuelo 12: SCL -> MVD (Itinerario 4)
  (nextval('vuelo_seq'),'LA8170', '2025-08-03 11:30', '2025-08-03 14:00', 1, 3, '98765432-1', 1), -- Vuelo 13: MVD -> GRU (Itinerario 4)
  (nextval('vuelo_seq'),'LA8185', '2025-08-03 15:00', '2025-08-03 20:30', 1, 2, '12345678-9', 1), -- Vuelo 14: GRU -> JFK (Itinerarios 4 y 6)
  (nextval('vuelo_seq'),'LA9000', '2025-08-08 06:00', '2025-08-08 13:00', 1, 1, '12345678-9', 1), -- LAX -> ATL
  (nextval('vuelo_seq'),'LA9001', '2025-08-08 15:00', '2025-08-08 22:00', 1, 3, '98765432-1', 1), -- ATL -> LIM
  (nextval('vuelo_seq'),'LA9002', '2025-08-09 06:00', '2025-08-09 10:00', 1, 2, '12345678-9', 1),-- LIM -> SCL
  (nextval('vuelo_seq'),'LA9000', '2025-08-03 15:00', '2025-08-03 19:30', 1, 1, '12345678-9', 1), -- Vuelo 13: MIA -> SCL (directo)
  (nextval('vuelo_seq'),'LA9200', '2025-08-04 08:00', '2025-08-04 11:30', 1, 1, '12345678-9', 1), -- GRU -> BOG
  (nextval('vuelo_seq'),'LA9201', '2025-08-04 13:00', '2025-08-04 16:00', 1, 2, '98765432-1', 1), -- BOG -> MIA
  (nextval('vuelo_seq'),'LA9202', '2025-08-04 18:00', '2025-08-04 21:00', 1, 3, '12345678-9', 1); -- MIA -> JFK
-- Vuelo directo SCL -> JFK
INSERT INTO Vuelo VALUES
  (nextval('vuelo_seq'), 'LA9900', '2025-08-01 23:55', '2025-08-02 09:30', 1, 1, '12345678-9', 1);

-- SCL -> BOG
INSERT INTO Vuelo VALUES
  (nextval('vuelo_seq'), 'LA9901', '2025-08-01 09:00', '2025-08-01 15:00', 1, 2, '98765432-1', 1);

-- BOG -> JFK
INSERT INTO Vuelo VALUES
  (nextval('vuelo_seq'),'LA9902', '2025-08-01 17:00', '2025-08-01 22:00', 1, 2, '12345678-9', 1);

INSERT INTO Vuelo (ID_VUELO, Numero_Vuelo, Fecha_Hora_Salida, Fecha_Hora_Llegada, ID_ESTADO_VUELO, ID_AVION, RUT_PILOTO, ID_AEROLINEA)
VALUES (
  nextval('vuelo_seq'), 'LA9400', '2025-08-10 08:00', '2025-08-10 15:00', 1, 2, '98765432-1', 1
);

INSERT INTO Vuelo (ID_VUELO, Numero_Vuelo, Fecha_Hora_Salida, Fecha_Hora_Llegada, ID_ESTADO_VUELO, ID_AVION, RUT_PILOTO, ID_AEROLINEA)
VALUES (
  nextval('vuelo_seq'), 'LA9500', '2025-08-10 17:00', '2025-08-10 23:30', 1, 2, '98765432-1', 1
);


-- Insertar precios para el vuelo LA8117 (SCL -> MVD)
INSERT INTO Precio_Asiento (ID_VUELO, ID_CLASE, PRECIO) VALUES
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8117' AND Fecha_Hora_Salida = '2025-08-01 14:30'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'), 100),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8117' AND Fecha_Hora_Salida = '2025-08-01 14:30'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Ejecutiva'), 250),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8117' AND Fecha_Hora_Salida = '2025-08-01 14:30'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Primera Clase'), 500);

-- Insertar precios para el vuelo LA8180 (GRU -> JFK)
INSERT INTO Precio_Asiento (ID_VUELO, ID_CLASE, PRECIO) VALUES
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8180' AND Fecha_Hora_Salida = '2025-08-01 22:50'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'), 100),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8180' AND Fecha_Hora_Salida = '2025-08-01 22:50'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Ejecutiva'), 250),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8180' AND Fecha_Hora_Salida = '2025-08-01 22:50'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Primera Clase'), 500);

-- Insertar precios para el vuelo LA8989 (JFK -> LAX)
INSERT INTO Precio_Asiento (ID_VUELO, ID_CLASE, PRECIO) VALUES
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8989' AND Fecha_Hora_Salida = '2025-08-02 09:55'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'), 100),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8989' AND Fecha_Hora_Salida = '2025-08-02 09:55'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Ejecutiva'), 250),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8989' AND Fecha_Hora_Salida = '2025-08-02 09:55'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Primera Clase'), 500);

-- Insertar precios para el vuelo LA650 (SCL -> LIM)
INSERT INTO Precio_Asiento (ID_VUELO, ID_CLASE, PRECIO) VALUES
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA650' AND Fecha_Hora_Salida = '2025-08-01 07:50'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'), 100),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA650' AND Fecha_Hora_Salida = '2025-08-01 07:50'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Ejecutiva'), 250),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA650' AND Fecha_Hora_Salida = '2025-08-01 07:50'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Primera Clase'), 500);

-- (Y así sucesivamente para los demás vuelos...)
-- Insertar precios para el vuelo LA2482 (LIM -> ATL)
INSERT INTO Precio_Asiento (ID_VUELO, ID_CLASE, PRECIO) VALUES
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA2482' AND Fecha_Hora_Salida = '2025-08-01 12:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'), 100),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA2482' AND Fecha_Hora_Salida = '2025-08-01 12:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Ejecutiva'), 250),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA2482' AND Fecha_Hora_Salida = '2025-08-01 12:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Primera Clase'), 500);

-- Insertar precios para el vuelo LA8954 (ATL -> JFK)
INSERT INTO Precio_Asiento (ID_VUELO, ID_CLASE, PRECIO) VALUES
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8954' AND Fecha_Hora_Salida = '2025-08-01 22:30'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'), 100),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8954' AND Fecha_Hora_Salida = '2025-08-01 22:30'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Ejecutiva'), 250),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8954' AND Fecha_Hora_Salida = '2025-08-01 22:30'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Primera Clase'), 500);

-- Insertar precios para el vuelo LA8120 (MVD -> SCL)
INSERT INTO Precio_Asiento (ID_VUELO, ID_CLASE, PRECIO) VALUES
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8120' AND Fecha_Hora_Salida = '2025-08-02 15:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'), 100),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8120' AND Fecha_Hora_Salida = '2025-08-02 15:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Ejecutiva'), 250),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8120' AND Fecha_Hora_Salida = '2025-08-02 15:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Primera Clase'), 500);

-- Insertar precios para el vuelo LA8130 (GRU -> MVD)
INSERT INTO Precio_Asiento (ID_VUELO, ID_CLASE, PRECIO) VALUES
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8130' AND Fecha_Hora_Salida = '2025-08-02 19:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'), 100),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8130' AND Fecha_Hora_Salida = '2025-08-02 19:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Ejecutiva'), 250),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8130' AND Fecha_Hora_Salida = '2025-08-02 19:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Primera Clase'), 500);

-- Insertar precios para el vuelo LA8140 (LAX -> ATL)
INSERT INTO Precio_Asiento (ID_VUELO, ID_CLASE, PRECIO) VALUES
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8140' AND Fecha_Hora_Salida = '2025-08-02 22:30'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'), 100),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8140' AND Fecha_Hora_Salida = '2025-08-02 22:30'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Ejecutiva'), 250),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8140' AND Fecha_Hora_Salida = '2025-08-02 22:30'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Primera Clase'), 500);

-- Insertar precios para el vuelo LA8150 (ATL -> MVD)
INSERT INTO Precio_Asiento (ID_VUELO, ID_CLASE, PRECIO) VALUES
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8150' AND Fecha_Hora_Salida = '2025-08-03 03:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'), 100),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8150' AND Fecha_Hora_Salida = '2025-08-03 03:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Ejecutiva'), 250),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8150' AND Fecha_Hora_Salida = '2025-08-03 03:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Primera Clase'), 500);

-- Insertar precios para el vuelo LA8160 (SCL -> MVD)
INSERT INTO Precio_Asiento (ID_VUELO, ID_CLASE, PRECIO) VALUES
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8160' AND Fecha_Hora_Salida = '2025-08-03 08:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'), 100),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8160' AND Fecha_Hora_Salida = '2025-08-03 08:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Ejecutiva'), 250),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8160' AND Fecha_Hora_Salida = '2025-08-03 08:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Primera Clase'), 500);

-- Insertar precios para el vuelo LA8170 (MVD -> GRU)
INSERT INTO Precio_Asiento (ID_VUELO, ID_CLASE, PRECIO) VALUES
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8170' AND Fecha_Hora_Salida = '2025-08-03 11:30'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'), 100),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8170' AND Fecha_Hora_Salida = '2025-08-03 11:30'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Ejecutiva'), 250),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8170' AND Fecha_Hora_Salida = '2025-08-03 11:30'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Primera Clase'), 500);

-- Insertar precios para el vuelo LA8185 (GRU -> JFK)
INSERT INTO Precio_Asiento (ID_VUELO, ID_CLASE, PRECIO) VALUES
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8185' AND Fecha_Hora_Salida = '2025-08-03 15:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'), 100),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8185' AND Fecha_Hora_Salida = '2025-08-03 15:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Ejecutiva'), 250),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA8185' AND Fecha_Hora_Salida = '2025-08-03 15:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Primera Clase'), 500);

-- Insertar precios para el vuelo LA9000 (LAX -> ATL)
INSERT INTO Precio_Asiento (ID_VUELO, ID_CLASE, PRECIO) VALUES
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA9000' AND Fecha_Hora_Salida = '2025-08-08 06:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'), 100),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA9000' AND Fecha_Hora_Salida = '2025-08-08 06:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Ejecutiva'), 250),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA9000' AND Fecha_Hora_Salida = '2025-08-08 06:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Primera Clase'), 500);

-- Insertar precios para el vuelo LA9001 (ATL -> LIM)
INSERT INTO Precio_Asiento (ID_VUELO, ID_CLASE, PRECIO) VALUES
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA9001' AND Fecha_Hora_Salida = '2025-08-08 15:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'), 100),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA9001' AND Fecha_Hora_Salida = '2025-08-08 15:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Ejecutiva'), 250),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA9001' AND Fecha_Hora_Salida = '2025-08-08 15:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Primera Clase'), 500);

-- Insertar precios para el vuelo LA9002 (LIM -> SCL)
INSERT INTO Precio_Asiento (ID_VUELO, ID_CLASE, PRECIO) VALUES
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA9002' AND Fecha_Hora_Salida = '2025-08-09 06:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'), 100),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA9002' AND Fecha_Hora_Salida = '2025-08-09 06:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Ejecutiva'), 250),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA9002' AND Fecha_Hora_Salida = '2025-08-09 06:00'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Primera Clase'), 500);

INSERT INTO Precio_Asiento (ID_VUELO, ID_CLASE, PRECIO)
VALUES
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA9400'), (SELECT ID_CLASE FROM Clase_Asiento WHERE Descripcion = 'Económica'), 100),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA9400'), (SELECT ID_CLASE FROM Clase_Asiento WHERE Descripcion = 'Ejecutiva'), 250),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA9400'), (SELECT ID_CLASE FROM Clase_Asiento WHERE Descripcion = 'Primera Clase'), 500);


-- Precios para LA9500
INSERT INTO Precio_Asiento (ID_VUELO, ID_CLASE, PRECIO)
VALUES
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA9500'), (SELECT ID_CLASE FROM Clase_Asiento WHERE Descripcion = 'Económica'), 120),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA9500'), (SELECT ID_CLASE FROM Clase_Asiento WHERE Descripcion = 'Ejecutiva'), 280),
  ((SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'LA9500'), (SELECT ID_CLASE FROM Clase_Asiento WHERE Descripcion = 'Primera Clase'), 550);

-- 7. Segmentos de vuelo (IDs hardcoded) - principales y adicionales juntos
-- 7. Segmentos de vuelo con fechas relativas a la fecha actual
INSERT INTO Segmento_Vuelo (ID_SEGMENTO, ID_VUELO, ORDEN_SEGMENTO, ID_AEROPUERTO_ORIGEN, ID_AEROPUERTO_DESTINO, HORA_SALIDA, HORA_LLEGADA) VALUES
  (nextval('segmento_vuelo_seq'), 1, 1, 1, 2, now() + interval '1 day 14 hours 30 minutes', now() + interval '1 day 17 hours 45 minutes'), -- SCL -> MVD
  (nextval('segmento_vuelo_seq'), 1, 2, 2, 3, now() + interval '1 day 18 hours 55 minutes', now() + interval '1 day 21 hours 30 minutes'), -- MVD -> GRU
  (nextval('segmento_vuelo_seq'), 3, 1, 3, 4, now() + interval '1 day 22 hours 50 minutes', now() + interval '2 day 7 hours 35 minutes'),  -- GRU -> JFK
  (nextval('segmento_vuelo_seq'), 4, 1, 4, 5, now() + interval '2 day 9 hours 55 minutes', now() + interval '2 day 12 hours 50 minutes'), -- JFK -> LAX
  (nextval('segmento_vuelo_seq'), 5, 1, 1, 6, now() + interval '1 day 7 hours 50 minutes', now() + interval '1 day 10 hours 40 minutes'), -- SCL -> LIM
  (nextval('segmento_vuelo_seq'), 6, 1, 6, 7, now() + interval '1 day 12 hours', now() + interval '1 day 20 hours 15 minutes'),           -- LIM -> ATL
  (nextval('segmento_vuelo_seq'), 7, 1, 7, 4, now() + interval '1 day 22 hours 30 minutes', now() + interval '2 day 1 hours 6 minutes'),  -- ATL -> JFK
  (nextval('segmento_vuelo_seq'), 8, 1, 2, 1, now() + interval '2 day 15 hours', now() + interval '2 day 17 hours 30 minutes'),           -- MVD -> SCL
  (nextval('segmento_vuelo_seq'), 9, 1, 3, 2, now() + interval '2 day 19 hours', now() + interval '2 day 21 hours 45 minutes'),           -- GRU -> MVD
  (nextval('segmento_vuelo_seq'), 10, 1, 5, 7, now() + interval '2 day 22 hours 30 minutes', now() + interval '3 day 1 hours 30 minutes'),-- LAX -> ATL
  (nextval('segmento_vuelo_seq'), 11, 1, 7, 2, now() + interval '3 day 3 hours', now() + interval '3 day 6 hours'),                       -- ATL -> MVD
  (nextval('segmento_vuelo_seq'), 12, 1, 1, 2, now() + interval '3 day 8 hours', now() + interval '3 day 10 hours 30 minutes'),           -- SCL -> MVD
  (nextval('segmento_vuelo_seq'), 13, 1, 2, 3, now() + interval '3 day 11 hours 30 minutes', now() + interval '3 day 14 hours'),          -- MVD -> GRU
  (nextval('segmento_vuelo_seq'), 14, 1, 3, 8, now() + interval '3 day 15 hours', now() + interval '3 day 18 hours 30 minutes'),          -- GRU -> BOG
  (nextval('segmento_vuelo_seq'), 14, 2, 8, 9, now() + interval '3 day 19 hours 15 minutes', now() + interval '3 day 21 hours 45 minutes'),-- BOG -> MIA
  (nextval('segmento_vuelo_seq'), 14, 3, 9, 4, now() + interval '3 day 22 hours 30 minutes', now() + interval '4 day 1 hours'),           -- MIA -> JFK
  (nextval('segmento_vuelo_seq'), 15, 1, 5, 7, now() + interval '8 day 6 hours', now() + interval '8 day 13 hours'),                      -- LAX -> ATL
  (nextval('segmento_vuelo_seq'), 16, 1, 7, 6, now() + interval '8 day 15 hours', now() + interval '8 day 22 hours'),                     -- ATL -> LIM
  (nextval('segmento_vuelo_seq'), 17, 1, 6, 1, now() + interval '9 day 6 hours', now() + interval '9 day 10 hours'),                      -- LIM -> SCL
  (nextval('segmento_vuelo_seq'), 18, 1, 9, 1, now() + interval '3 day 15 hours', now() + interval '3 day 19 hours 30 minutes'),          -- MIA -> SCL
  (nextval('segmento_vuelo_seq'), 19, 1, 3, 8, now() + interval '4 day 8 hours', now() + interval '4 day 11 hours 30 minutes'),           -- GRU -> BOG
  (nextval('segmento_vuelo_seq'), 20, 1, 8, 9, now() + interval '4 day 13 hours', now() + interval '4 day 16 hours'),                     -- BOG -> MIA
  (nextval('segmento_vuelo_seq'), 21, 1, 9, 4, now() + interval '4 day 18 hours', now() + interval '4 day 21 hours');                     -- MIA -> JFK

-- Segmento directo SCL -> JFK
INSERT INTO Segmento_Vuelo VALUES
  (nextval('segmento_vuelo_seq'), 22, 1, 1, 4, now() + interval '1 day 23 hours 55 minutes', now() + interval '2 day 9 hours 30 minutes');

-- Segmento SCL -> BOG
INSERT INTO Segmento_Vuelo VALUES
  (nextval('segmento_vuelo_seq'), 23, 1, 1, 8, now() + interval '1 day 9 hours', now() + interval '1 day 15 hours');

-- Segmento BOG -> JFK
INSERT INTO Segmento_Vuelo VALUES
  (nextval('segmento_vuelo_seq'), 24, 1, 8, 4, now() + interval '1 day 17 hours', now() + interval '1 day 22 hours');

-- Segmento único: LAX -> MIA
INSERT INTO Segmento_Vuelo (ID_SEGMENTO, ID_VUELO, ORDEN_SEGMENTO, ID_AEROPUERTO_ORIGEN, ID_AEROPUERTO_DESTINO, HORA_SALIDA, HORA_LLEGADA) VALUES
  (nextval('segmento_vuelo_seq'), 25, 1, 5, 9, now() + interval '3 day 8 hours', now() + interval '3 day 15 hours'), -- LAX -> MIA
  (nextval('segmento_vuelo_seq'), 26, 1, 9, 1, now() + interval '3 day 17 hours', now() + interval '3 day 23 hours 30 minutes'); -- MIA -> SCL

-- Asignar puerta A1 al segmento 1 (SCL -> MVD)
INSERT INTO Asignacion_Puerta (ID_SEGMENTO, ID_PUERTA) VALUES (1, 1);

-- Asignar puerta C2 al segmento 2 (MVD -> GRU)
INSERT INTO Asignacion_Puerta (ID_SEGMENTO, ID_PUERTA) VALUES (2, 8);

-- Asignar puerta E1 al segmento 3 (GRU -> JFK)
INSERT INTO Asignacion_Puerta (ID_SEGMENTO, ID_PUERTA) VALUES (3, 12);

-- Asignar puerta G3 al segmento 4 (JFK -> LAX)
INSERT INTO Asignacion_Puerta (ID_SEGMENTO, ID_PUERTA) VALUES (4, 18);

-- Asignar puerta A2 al segmento 5 (SCL -> LIM)
INSERT INTO Asignacion_Puerta (ID_SEGMENTO, ID_PUERTA) VALUES (5, 2);

-- Asignar puerta E2 al segmento 6 (LIM -> ATL)
INSERT INTO Asignacion_Puerta (ID_SEGMENTO, ID_PUERTA) VALUES (6, 13);

-- Asignar puerta F1 al segmento 7 (ATL -> JFK)
INSERT INTO Asignacion_Puerta (ID_SEGMENTO, ID_PUERTA) VALUES (7, 15);

-- Asignar puerta A3 al segmento 12 (SCL -> MVD - Itinerario 4)
INSERT INTO Asignacion_Puerta (ID_SEGMENTO, ID_PUERTA) VALUES (12, 3);

-- Asignar puerta C1 al segmento 13 (MVD -> GRU)
INSERT INTO Asignacion_Puerta (ID_SEGMENTO, ID_PUERTA) VALUES (13, 7);

INSERT INTO asignacion_puerta (id_segmento, id_puerta)
VALUES (14, 13);

-- Asignar puerta C1 al segmento 13 -- LAX -> MIA
INSERT INTO Asignacion_Puerta (ID_SEGMENTO, ID_PUERTA) VALUES (15, 34);

-- Asignar puerta C1 al segmento 13 -- MIA -> SCL
INSERT INTO Asignacion_Puerta (ID_SEGMENTO, ID_PUERTA) VALUES (16, 20);


INSERT INTO Tarifa (ID_TARIFA, Nombre) VALUES
(1, 'Básica'),
(2, 'Flexible'),
(3, 'Premium');

INSERT INTO Caracteristica_Tarifa (ID_CARACTERISTICA, Nombre, Descripcion) VALUES
(1, 'Permite Cambios', 'Permite cambiar la reserva'),
(2, 'Horas Minimas Cambio', 'Horas mínimas antes del vuelo para cambiar'),
(3, 'Permite Cancelacion', 'Permite cancelar la reserva');

-- Básica
INSERT INTO Tarifa_Caracteristica (ID_TARIFA, ID_CARACTERISTICA, Valor) VALUES
(1, 1, 'false'),  -- Permite_Cambios
(1, 3, 'true');  -- Permite_Cancelacion

-- Flexible
INSERT INTO Tarifa_Caracteristica (ID_TARIFA, ID_CARACTERISTICA, Valor) VALUES
(2, 1, 'true'),   -- Permite_Cambios
(2, 2, '48'),     -- Horas_Minimas_Cambio
(2, 3, 'true');   -- Permite_Cancelacion

-- Premium
INSERT INTO Tarifa_Caracteristica (ID_TARIFA, ID_CARACTERISTICA, Valor) VALUES
(3, 1, 'true'),          -- Permite_Cambios
(3, 2, '24'),             -- Horas_Minimas_Cambio
(3, 3, 'true');          -- Permite_Cancelacion


-- 10. Itinerarios (IDs hardcoded)
INSERT INTO Itinerario ( ORIGEN_AEROPUERTO, DESTINO_AEROPUERTO, DURACION_TOTAL, NUMERO_ESCALAS,Precio_Base) VALUES
  (1, 5, '1 day 22:20:00'::interval, 3,850000), -- Itinerario 1: SCL -> MVD -> GRU -> JFK -> LAX
  (1, 6, '03:50:00'::interval, 0,210000),       -- Itinerario 2: SCL -> LIM (directo)
  (1, 4, '17:16:00'::interval, 2,350000),       -- Itinerario 3: SCL -> LIM -> ATL -> JFK
  (1, 4, '1 day 03:30:00'::interval, 2,450000), -- Itinerario 4: SCL -> MVD -> GRU -> JFK
  (1, 7, '12:00:00'::interval, 1,750000),       -- Itinerario 5: SCL -> LIM -> ATL
  (3, 4, '5:30:00'::interval, 0,650000),        -- Itinerario 6: GRU -> JFK (directo)
  (5, 1, '1 day 04:00:00'::interval, 2,930000), -- LAX -> ATL -> LIM -> SCL
  (9, 1, '7 hours 00 minutes'::interval, 0,550000), -- Itinerario 8: MIA -> SCL (directo)
  (3, 4, '13:00:00'::interval, 2,570000), -- GRU -> JFK con 2 escalas (en BOG, MIA)
  ( 8, 9, '03:00:00'::interval, 0,450000),
-- Itinerario 11: Vuelo directo SCL -> JFK
  ( 1, 4, '09:35:00'::interval, 0, 690000),

-- Itinerario 12: SCL -> BOG -> JFK
  ( 1, 4, '13:00:00'::interval, 1, 460000);

INSERT INTO Itinerario (ORIGEN_AEROPUERTO,DESTINO_AEROPUERTO,HORA_SALIDA,HORA_LLEGADA,DURACION_TOTAL,NUMERO_ESCALAS, Precio_Base
)
VALUES (
    5, -- LAX
    1, -- SCL
    '2025-08-10 07:00',
    '2025-08-11 05:00',
    INTERVAL '21 hours',
    1,
    650  -- Precio base estimado
);


  INSERT INTO Itinerario_Vuelo (ID_ITINERARIO, ID_VUELO, ORDEN, TIEMPO_ESPERA, TIPO_CONEXION) VALUES
  -- Itinerario 1: SCL -> MVD -> GRU -> JFK -> LAX
  (1, 1, 1, '1 hour 10 minutes'::interval, 'Escala en Montevideo'),    -- Vuelo 1: SCL -> MVD
  --(1, 1, 2, '1 hour 20 minutes'::interval, 'Cambio de avión en GRU'),  -- Vuelo 2: MVD -> GRU
  (1, 3, 3, '2 hour 20 minutes'::interval, 'Cambio de avión en JFK'),  -- Vuelo 3: GRU -> JFK
  (1, 4, 4, '0'::interval, 'Vuelo Final'),                             -- Vuelo 4: JFK -> LAX

  -- Itinerario 2: SCL -> LIM (directo)
  (2, 5, 1, '0'::interval, 'Vuelo directo SCL a LIM'),

  -- Itinerario 3: SCL -> LIM -> ATL -> JFK
  (3, 5, 1, '1 hour 20 minutes'::interval, 'Cambio de avión en Lima (LIM)'), -- Vuelo 5: SCL -> LIM
  (3, 6, 2, '2 hours 15 minutes'::interval, 'Cambio de avión en Atlanta (ATL)'), -- Vuelo 6: LIM -> ATL
  (3, 7, 3, '0'::interval, 'Vuelo final hacia JFK'),                         -- Vuelo 7: ATL -> JFK

  -- Itinerario 4: SCL -> MVD -> GRU -> JFK
  (4, 12, 1, '2 hours 0 minutes'::interval, 'Escala en Montevideo'), -- Vuelo 12: SCL -> MVD
  (4, 13, 2, '1 hour 30 minutes'::interval, 'Escala en GRU'),        -- Vuelo 13: MVD -> GRU
  (4, 14, 3, '0'::interval, 'Escala en Bogotá, Miami antes de JFK'),  -- Vuelo 14: GRU -> JFK

  -- Itinerario 5: SCL -> LIM -> ATL
  (5, 5, 1, '1 hour 20 minutes'::interval, 'Escala en Lima'),        -- Vuelo 5: SCL -> LIM
  (5, 6, 2, '0'::interval, 'Vuelo final'),                           -- Vuelo 6: LIM -> ATL

  -- Itinerario 6: GRU -> JFK (directo)
  (6, 14, 1, '0'::interval, 'Vuelo directo GRU a JFK'),

  -- Itinerario 7: GRU -> LAX -> ATL -> LIM -> SCL (directo)
  (7, 15, 1, '2 hours'::interval, 'Escala en Atlanta'),
  (7, 16, 2, '8 hours'::interval, 'Escala en Lima'),
  (7, 17, 3, NULL, 'Destino Final'),

  -- Vuelo directo desde Miami hasta Santiago
  (8, 18, 1, '0'::interval, 'Vuelo directo MIA -> SCL'),

  (9, 19, 1, '1 hour 30 minutes'::interval, 'Escala en Bogotá (BOG)'), -- GRU -> BOG
  (9, 20, 2, '2 hours'::interval, 'Escala en Miami (MIA)'),           -- BOG -> MIA
  (9, 21, 3, '0'::interval, 'Vuelo final a Nueva York (JFK)'),        -- MIA -> JFK

  -- Vuelo directo desde Bogotá a Miami
  (10, 19, 1, '0'::interval, 'Vuelo directo BOG a MIA'),

  -- Itinerario 11: Vuelo directo SCL -> JFK
  (11, 22, 1, '0'::interval, 'Vuelo directo SCL -> JFK'),

  -- Itinerario 12: SCL -> BOG -> JFK
  (12, 23, 1, '2 hours'::interval, 'Escala en Bogotá'),
  (12, 24, 2, '0'::interval, 'Vuelo final');

INSERT INTO Itinerario_Vuelo (ID_ITINERARIO,ID_VUELO,ORDEN,TIEMPO_ESPERA,TIPO_CONEXION)
VALUES
(13, 25, 1, INTERVAL '2 hours', 'Escala'), -- LA9400: LAX → MIA
(13, 26, 2, INTERVAL '0', 'Final');        -- LA9500: MIA → SCL

INSERT INTO Itinerario_Tarifa (ID_ITINERARIO, ID_TARIFA, Precio) VALUES
(1, 1, 850000.00), (1, 2, 950000.00), (1, 3, 1050000.00),
(2, 1, 210000.00), (2, 2, 250000.00), (2, 3, 280000.00),
(3, 1, 350000.00), (3, 2, 400000.00), (3, 3, 450000.00),
(4, 1, 450000.00), (4, 2, 510000.00), (4, 3, 570000.00),
(5, 1, 750000.00), (5, 2, 810000.00), (5, 3, 900000.00),
(6, 1, 650000.00), (6, 2, 700000.00), (6, 3, 750000.00),
(7, 1, 930000.00), (7, 2, 1000000.00), (7, 3, 1080000.00),
(8, 1, 550000.00), (8, 2, 600000.00), (8, 3, 650000.00),
(9, 1, 570000.00), (9, 2, 620000.00), (9, 3, 670000.00),
(10,1, 450000.00), (10,2, 480000.00), (10,3, 520000.00),
(11,1, 690000.00), (11,2, 730000.00), (11,3, 770000.00),
(12,1, 460000.00), (12,2, 490000.00), (12,3, 530000.00),
(13,1, 650000.00), (13,2, 690000.00), (13,3, 730000.00);

-- Crear reservas para tres pasajeros en el Itinerario 10
INSERT INTO Reserva (Fecha_Reserva, Estado_Reserva, RUT_PASAJERO, Total) VALUES
('2025-08-04 10:00', 1, '87654321-2', 530000.00), -- Lucía
('2025-08-04 10:05', 1, '76543210-3', 530000.00), -- Juan
('2025-08-04 10:10', 1, '65432109-4', 530000.00); -- Ana

-- Asociar reservas con el Itinerario 10 (GRU → JFK)
INSERT INTO reserva_itinerario (id_reserva, id_itinerario, id_itinerario_tarifa)
VALUES (1, 10, 1), -- Aquí 1 es el id_tarifa que corresponde a 'Básica' o el que corresponda
       (2, 10, 1),
       (3, 10, 1);



-- Ejemplo de asignación de asientos (asumiendo IDs de asiento disponibles)
INSERT INTO Reserva_Asiento (ID_RESERVA, ID_VUELO, ID_ASIENTO) VALUES
-- Lucía
(1, 19, 2032), -- GRU -> BOG
(1, 20, 2034), -- BOG -> MIA
(1, 21, 1733), -- MIA -> JFK

-- Juan
(2, 19, 2037),
(2, 20, 2044),
(2, 21, 1745),

-- Ana
(3, 19, 2056),
(3, 20, 2047),
(3, 21, 1766);




	select
		it.ID_ITINERARIO,
		ci1.nombre || ' - ' || arp1.nombre_aeropuerto || ' (' || arp1.codigo_iata || ')' as salida,
        ci2.nombre || ' - ' || arp2.nombre_aeropuerto || ' (' || arp2.codigo_iata || ')' as destino,
		it.NUMERO_ESCALAS as PARADAS,
		TO_CHAR(it.precio_base, '"CLP$"999G999G999') AS  PRECIO,
		IT.DURACION_TOTAL




	from itinerario it
	join itinerario_vuelo itv
		on itv.id_itinerario = it.id_itinerario
	JOIN Itinerario_Vuelo iv ON iv.ID_ITINERARIO = it.ID_ITINERARIO
	JOIN Vuelo v ON v.ID_VUELO = iv.ID_VUELO
	join aeropuerto arp1
	on arp1.id_aeropuerto = it.origen_aeropuerto
	join aeropuerto arp2
	on arp2.id_aeropuerto = it.destino_aeropuerto
	join ciudad ci1
	on ci1.id_ciudad = arp1.id_ciudad
	join ciudad ci2
	on ci2.id_ciudad = arp2.id_ciudad
	group by it.id_itinerario, ci1.nombre || ' - ' || arp1.nombre_aeropuerto || ' (' || arp1.codigo_iata || ')',
        ci2.nombre || ' - ' || arp2.nombre_aeropuerto || ' (' || arp2.codigo_iata || ')',
		arp1.id_aeropuerto,
		arp2.id_aeropuerto ,it.origen_aeropuerto,
		it.destino_aeropuerto
	order by it.ID_ITINERARIO;




-- Obtener todos los roles de Juan Pérez
SELECT *
FROM Usuario u
JOIN RolUsuario rls ON u.RUT = rls.rut_usuario
JOIN Roles rlu ON rls.id_rol = rlu.id_rol
WHERE u.correo_electronico = 'juan.perez@piloto.com';


-- Verificar los roles de Juan Pérez en la tabla RolUsuario
SELECT rls.rut_usuario, rlu.nombre AS Rol
FROM RolUsuario rls
JOIN Roles rlu ON rls.id_rol = rlu.id_rol
WHERE rls.rut_usuario = '12345678-9';


-- Verificar los roles de Juan Pérez
SELECT *
FROM RolUsuario
WHERE rut_usuario = '12345678-9';

SELECT * FROM pg_class c WHERE c.relkind = 'S' order BY c.relname;


SELECT  sequence_name
FROM information_schema.sequences
ORDER BY sequence_name;

select * from vuelo;
select * from aeropuerto;

select * from ciudad;

SELECT MAX(id_vuelo) FROM vuelo;

select * from asiento;

select * from precio_asiento;

select * from segmento_vuelo;

select * from itinerario;

select * from itinerario_vuelo;

select * from asignacion_puerta;



