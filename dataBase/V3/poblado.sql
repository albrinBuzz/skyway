-- =========================================================================
-- 1. MAESTRO DE CONTINENTES
-- =========================================================================
INSERT INTO Continente (ID_CONTINENTE, Nombre) VALUES
                                                   (1, 'América del Sur'),
                                                   (2, 'América del Norte'),
                                                   (3, 'Europa'),
                                                   (4, 'Asia'),
                                                   (5, 'Oceanía'),
                                                   (6, 'África'),
                                                   (7, 'Oriente Medio');

-- =========================================================================
-- 2. MAESTRO DE PAÍSES
-- =========================================================================
INSERT INTO Pais (ID_PAIS, Nombre, ID_CONTINENTE) VALUES
                                                      (1, 'Chile', 1),
                                                      (2, 'Uruguay', 1),
                                                      (3, 'Brasil', 1),
                                                      (4, 'USA', 2),
                                                      (5, 'Perú', 1),
                                                      (6, 'Colombia', 1),
                                                      (7, 'España', 3),
                                                      (8, 'Francia', 3),
                                                      (9, 'Reino Unido', 3),
                                                      (10, 'Alemania', 3),
                                                      (11, 'Italia', 3),
                                                      (12, 'Canada', 2),
                                                      (13, 'Mexico', 2),
                                                      (14, 'Australia', 5),
                                                      (15, 'China', 4),
                                                      (16, 'Japon', 4),
                                                      (17, 'Argentina', 1),
                                                      (18, 'Panama', 2),
                                                      (19, 'Qatar', 7),
                                                      (20, 'Emiratos Arabes Unidos', 7),
                                                      (21, 'India', 4),
                                                      (22, 'Corea del Sur', 4),
                                                      (23, 'Sudáfrica', 6),
                                                      (24, 'Egipto', 6),
                                                      (25, 'Nueva Zelanda', 5),
                                                      (26, 'Rusia', 3),
                                                      (27, 'Turquía', 7),
                                                      (28, 'Indonesia', 4),
                                                      (29, 'Filipinas', 4),
                                                      (30, 'Tailandia', 4),
                                                      (31, 'Países Bajos', 3),
                                                      (32, 'Bélgica', 3),
                                                      (33, 'Suiza', 3),
                                                      (34, 'Austria', 3),
                                                      (35, 'Dinamarca', 3),
                                                      (36, 'Suecia', 3),
                                                      (37, 'Portugal', 3),
                                                      (38, 'Finlandia', 3),
                                                      (39, 'Noruega', 3),
                                                      (40, 'República Checa', 3),
                                                      (41, 'Polonia', 3),
                                                      (42, 'Hungría', 3),
                                                      (43, 'Rumanía', 3),
                                                      (44, 'Bulgaria', 3),
                                                      (45, 'Croacia', 3),
                                                      (46, 'Eslovenia', 3),
                                                      (47, 'Serbia', 3),
                                                      (48, 'Albania', 3),
                                                      (49, 'Macedonia del Norte', 3),
                                                      (50, 'Singapur', 4),

                                                      (51, 'Cuba',                    2),
                                                      (52, 'República Dominicana',    2),
                                                      (53, 'Jamaica',                 2),
                                                      (54, 'Costa Rica',              2),
                                                      (55, 'Guatemala',               2),
                                                      (56, 'Honduras',                2),
                                                      (57, 'El Salvador',             2),
                                                      (58, 'Nicaragua',               2),
                                                      (59, 'Bolivia',                 1),
                                                      (60, 'Paraguay',                1),
                                                      (61, 'Ecuador',                 1),
                                                      (62, 'Venezuela',               1),
                                                      (63, 'Trinidad y Tobago',       1),

-- Europa
                                                      (64, 'Irlanda',                 3),
                                                      (65, 'Islandia',                3),
                                                      (66, 'Grecia',                  3),
                                                      (67, 'Chipre',                  3),
                                                      (68, 'Malta',                   3),
                                                      (69, 'Luxemburgo',              3),
                                                      (70, 'Eslovaquia',              3),
                                                      (71, 'Lituania',                3),
                                                      (72, 'Letonia',                 3),
                                                      (73, 'Estonia',                 3),
                                                      (74, 'Bielorrusia',             3),
                                                      (75, 'Ucrania',                 3),
                                                      (76, 'Moldavia',                3),
                                                      (77, 'Bosnia y Herzegovina',    3),
                                                      (78, 'Montenegro',              3),
                                                      (79, 'Kosovo',                  3),

-- África
                                                      (80, 'Marruecos',               6),
                                                      (81, 'Túnez',                   6),
                                                      (82, 'Argelia',                 6),
                                                      (83, 'Libia',                   6),
                                                      (84, 'Nigeria',                 6),
                                                      (85, 'Ghana',                   6),
                                                      (86, 'Kenia',                   6),
                                                      (87, 'Tanzania',                6),
                                                      (88, 'Etiopía',                 6),
                                                      (89, 'Senegal',                 6),
                                                      (90, 'Costa de Marfil',         6),
                                                      (91, 'Camerún',                 6),
                                                      (92, 'Angola',                  6),
                                                      (93, 'Mozambique',              6),
                                                      (94, 'Zimbabwe',                6),
                                                      (95, 'Zambia',                  6),
                                                      (96, 'Uganda',                  6),
                                                      (97, 'Rwanda',                  6),

-- Asia
                                                      (98,  'Vietnam',                4),
                                                      (99,  'Malasia',                4),
                                                      (100, 'Myanmar',                4),
                                                      (101, 'Camboya',                4),
                                                      (102, 'Sri Lanka',              4),
                                                      (103, 'Bangladesh',             4),
                                                      (104, 'Nepal',                  4),
                                                      (105, 'Pakistán',               4),
                                                      (106, 'Kazajistán',             4),
                                                      (107, 'Uzbekistán',             4),
                                                      (108, 'Azerbaiyán',             4),
                                                      (109, 'Georgia',                4),
                                                      (110, 'Armenia',                4),
                                                      (111, 'Mongolia',               4),
                                                      (112, 'Taiwan',                 4),
                                                      (113, 'Hong Kong',              4),
                                                      (114, 'Macao',                  4),

-- Oriente Medio
                                                      (115, 'Arabia Saudita',         7),
                                                      (116, 'Israel',                 7),
                                                      (117, 'Jordania',               7),
                                                      (118, 'Líbano',                 7),
                                                      (119, 'Kuwait',                 7),
                                                      (120, 'Bahréin',                7),
                                                      (121, 'Omán',                   7),
                                                      (122, 'Irak',                   7),
                                                      (123, 'Irán',                   7),

-- Oceanía / Pacífico
                                                      (124, 'Papúa Nueva Guinea',     5),
                                                      (125, 'Fiji',                   5),
                                                      (126, 'Polinesia Francesa',     5),

-- América del Sur adicional
                                                      (127, 'Guyana',                 1),
                                                      (128, 'Surinam',                1),
                                                      (129, 'Guyana Francesa',        1);

-- =========================================================================
-- 3. MAESTRO DE CIUDADES
-- =========================================================================
INSERT INTO Ciudad (ID_CIUDAD, Nombre, ID_PAIS) VALUES
                                                    (1, 'Santiago', 1),
                                                    (2, 'Montevideo', 2),
                                                    (3, 'Sao Paulo', 3),
                                                    (4, 'Nueva York', 4),
                                                    (5, 'Los Angeles', 4),
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
                                                    (17, 'Ciudad de Mexico', 13),
                                                    (18, 'Vancouver', 12),
                                                    (19, 'Melbourne', 14),
                                                    (20, 'Sidney', 14),
                                                    (21, 'Beijing', 15),
                                                    (22, 'Tokyo', 16),
                                                    (23, 'Buenos Aires', 17),
                                                    (24, 'Doha', 19),
                                                    (25, 'Dubai', 20),
                                                    (26, 'Frankfurt', 10),
                                                    (27, 'Panama City', 18),
                                                    (28, 'Chicago', 4),
                                                    (29, 'Houston', 4),
                                                    (30, 'Nueva Delhi', 21),
                                                    (31, 'Seul', 22),
                                                    (32, 'Ciudad del Cabo', 23),
                                                    (33, 'El Cairo', 24),
                                                    (34, 'Auckland', 25),
                                                    (35, 'Moscu', 26),
                                                    (36, 'Estambul', 27),
                                                    (37, 'Yakarta', 28),
                                                    (38, 'Manila', 29),
                                                    (39, 'Bangkok', 30),
                                                    (40, 'Amsterdam', 31), -- Corregido ID_PAIS correspondiente a Pases Bajos
                                                    (41, 'Bruselas', 32),  -- Corregido ID_PAIS correspondiente a Blgica
                                                    (42, 'Zurich', 33),    -- Corregido ID_PAIS correspondiente a Suiza
                                                    (43, 'Viena', 34),     -- Corregido ID_PAIS correspondiente a Austria
                                                    (44, 'Copenhague', 35), -- Corregido ID_PAIS correspondiente a Dinamarca
                                                    (45, 'Estocolmo', 36),  -- Corregido ID_PAIS correspondiente a Suecia
                                                    (46, 'Lisboa', 37),     -- Corregido ID_PAIS correspondiente a Portugal
                                                    (47, 'Helsinki', 38),   -- Corregido ID_PAIS correspondiente a Finlandia
                                                    (48, 'Oslo', 39),       -- Corregido ID_PAIS correspondiente a Noruega
                                                    (49, 'Praga', 40),      -- Corregido ID_PAIS correspondiente a Rep. Checa
                                                    (50, 'San Francisco', 4),
                                                    (51, 'Washington D.C.', 4),
                                                    (52, 'Boston', 4),
                                                    (53, 'Budapest', 42),
                                                    (54, 'Cluj-Napoca', 43),
                                                    (55, 'Sofia', 44),
                                                    (56, 'Zagreb', 45),
                                                    (57, 'Ljubljana', 46),
                                                    (58, 'Belgrado', 47),
                                                    (59, 'Tirana', 48),
                                                    (60, 'Skopie', 49),
                                                    (61, 'Brisbane', 14),
                                                    (62, 'Wellington', 25),

                                                    (63,  'La Habana',          51),
                                                    (64,  'Santo Domingo',      52),
                                                    (65,  'Kingston',           53),
                                                    (66,  'San José',           54),
                                                    (67,  'Ciudad de Guatemala',55),
                                                    (68,  'Tegucigalpa',        56),
                                                    (69,  'San Salvador',       57),
                                                    (70,  'Managua',            58),
                                                    (71,  'La Paz',             59),
                                                    (72,  'Asunción',           60),
                                                    (73,  'Quito',              61),
                                                    (74,  'Guayaquil',          61),
                                                    (75,  'Caracas',            62),
                                                    (76,  'Port of Spain',      63),

-- Europa adicional
                                                    (77,  'Dublín',             64),
                                                    (78,  'Reikiavik',          65),
                                                    (79,  'Atenas',             66),
                                                    (80,  'Tesalónica',         66),
                                                    (81,  'Nicosia',            67),
                                                    (82,  'La Valeta',          68),
                                                    (83,  'Luxemburgo',         69),
                                                    (84,  'Bratislava',         70),
                                                    (85,  'Vilna',              71),
                                                    (86,  'Riga',               72),
                                                    (87,  'Tallin',             73),
                                                    (88,  'Minsk',              74),
                                                    (89,  'Kiev',               75),
                                                    (90,  'Odesa',              75),
                                                    (91,  'Kharkiv',            75),
                                                    (92,  'Chisinau',           76),
                                                    (93,  'Sarajevo',           77),
                                                    (94,  'Podgorica',          78),
                                                    (95,  'Pristina',           79),

-- África
                                                    (96,  'Casablanca',         80),
                                                    (97,  'Marrakech',          80),
                                                    (98,  'Túnez',              81),
                                                    (99,  'Argel',              82),
                                                    (100, 'Trípoli',            83),
                                                    (101, 'Lagos',              84),
                                                    (102, 'Abuya',              84),
                                                    (103, 'Acra',               85),
                                                    (104, 'Nairobi',            86),
                                                    (105, 'Dar es Salaam',      87),
                                                    (106, 'Zanzíbar',           87),
                                                    (107, 'Adís Abeba',         88),
                                                    (108, 'Dakar',              89),
                                                    (109, 'Abidján',            90),
                                                    (110, 'Duala',              91),
                                                    (111, 'Luanda',             92),
                                                    (112, 'Maputo',             93),
                                                    (113, 'Harare',             94),
                                                    (114, 'Lusaka',             95),
                                                    (115, 'Kampala',            96),
                                                    (116, 'Kigali',             97),
                                                    (117, 'Johannesburgo',      23),
                                                    (118, 'Durban',             23),

-- Asia
                                                    (119, 'Ho Chi Minh',        98),
                                                    (120, 'Hanói',              98),
                                                    (121, 'Kuala Lumpur',       99),
                                                    (122, 'Rangún',             100),
                                                    (123, 'Phnom Penh',         101),
                                                    (124, 'Colombo',            102),
                                                    (125, 'Dhaka',              103),
                                                    (126, 'Katmandú',           104),
                                                    (127, 'Karachi',            105),
                                                    (128, 'Lahore',             105),
                                                    (129, 'Islamabad',          105),
                                                    (130, 'Almaty',             106),
                                                    (131, 'Nur-Sultán',         106),
                                                    (132, 'Taskent',            107),
                                                    (133, 'Bakú',               108),
                                                    (134, 'Tiflis',             109),
                                                    (135, 'Ereván',             110),
                                                    (136, 'Ulán Bator',         111),
                                                    (137, 'Taipéi',             112),
                                                    (138, 'Hong Kong',          113),
                                                    (139, 'Osaka',              16),
                                                    (140, 'Nagoya',             16),
                                                    (141, 'Sapporo',            16),
                                                    (142, 'Shanghái',           15),
                                                    (143, 'Guangzhou',          15),
                                                    (144, 'Shenzhen',           15),
                                                    (145, 'Chengdu',            15),
                                                    (146, 'Xian',              15),
                                                    (147, 'Singapur',           50),

-- Oriente Medio
                                                    (148, 'Riad',               115),
                                                    (149, 'Yeda',               115),
                                                    (150, 'Medina',             115),
                                                    (151, 'Tel Aviv',           116),
                                                    (152, 'Ammán',              117),
                                                    (153, 'Beirut',             118),
                                                    (154, 'Kuwait City',        119),
                                                    (155, 'Manama',             120),
                                                    (156, 'Mascate',            121),
                                                    (157, 'Bagdad',             122),
                                                    (158, 'Teherán',            123),
                                                    (159, 'Abu Dabi',           20),

-- Oceanía / Pacífico
                                                    (160, 'Port Moresby',       124),
                                                    (161, 'Suva',               125),
                                                    (162, 'Papeete',            126),
                                                    (163, 'Cairns',             14),
                                                    (164, 'Perth',              14),
                                                    (165, 'Darwin',             14),
                                                    (166, 'Christchurch',       25),
                                                    (167, 'Dunedin',            25),

-- América del Sur adicional
                                                    (168, 'Georgetown',         127),
                                                    (169, 'Paramaribo',         128),
                                                    (170, 'Cayena',             129),
                                                    (171, 'Recife',             3),
                                                    (172, 'Salvador',           3),
                                                    (173, 'Brasilia',           3),
                                                    (174, 'Belo Horizonte',     3),
                                                    (175, 'Manaos',             3),
                                                    (176, 'Medellín',           6),
                                                    (177, 'Cali',               6),
                                                    (178, 'Cartagena',          6),
                                                    (179, 'Córdoba',            17),
                                                    (180, 'Mendoza',            17),
                                                    (181, 'Rosario',            17),
                                                    (182, 'Valparaíso',         1),
                                                    (183, 'Antofagasta',        1),
                                                    (184, 'Concepción',         1),
                                                    (185, 'Puerto Montt',       1),
                                                    (186, 'Punta Arenas',       1),
                                                    (187, 'Iquique',            1),
                                                    (188, 'Arequipa',           5),
                                                    (189, 'Cusco',              5),
                                                    (190, 'Santa Cruz',         59),
                                                    (191, 'Cochabamba',         59);

-- =========================================================================
-- 4. MAESTRO DE AEROPUERTOS CON GEOLOCALIZACIÓN REAL (PostGIS)
-- =========================================================================
-- ============================================================
-- AEROPUERTOS CON COORDENADAS GPS REALES (PostGIS WGS84)
-- posicion = GEOGRAPHY(Point, 4326) → ST_MakePoint(longitud, latitud)
-- ============================================================

INSERT INTO Aeropuerto (ID_AEROPUERTO, Nombre_Aeropuerto, Codigo_IATA, ID_CIUDAD, posicion) VALUES

-- AMÉRICA DEL SUR
(1,  'Aeropuerto Internacional Comodoro Arturo Merino Benítez', 'SCL',  1,  ST_MakePoint(-70.7858,  -33.3929)::geography),
(2,  'Carrasco Intl.',                                           'MVD',  2,  ST_MakePoint(-56.0308,  -34.8384)::geography),
(3,  'Guarulhos Intl.',                                          'GRU',  3,  ST_MakePoint(-46.4731,  -23.4356)::geography),
(6,  'J Chavez Intl.',                                           'LIM',  6,  ST_MakePoint(-77.1143,  -12.0219)::geography),
(8,  'El Dorado International Airport',                          'BOG',  8,  ST_MakePoint(-74.1469,    4.7016)::geography),
(23, 'Ezeiza Ministro Pistarini',                                'EZE',  23, ST_MakePoint(-58.5358,  -34.8222)::geography),

-- AMÉRICA DEL NORTE - ESTADOS UNIDOS
(4,  'John F Kennedy',                                           'JFK',  4,  ST_MakePoint(-73.7789,   40.6413)::geography),
(5,  'Los Angeles Intl.',                                        'LAX',  5,  ST_MakePoint(-118.4085,  33.9425)::geography),
(7,  'Hartsfield Jackson Atlanta Int.',                          'ATL',  7,  ST_MakePoint(-84.4277,   33.6407)::geography),
(9,  'Miami International Airport',                              'MIA',  9,  ST_MakePoint(-80.2870,   25.7959)::geography),
(28, 'Chicago OHare',                                            'ORD',  28, ST_MakePoint(-87.9048,   41.9742)::geography),
(29, 'George Bush Intercontinental',                             'IAH',  29, ST_MakePoint(-95.3414,   29.9902)::geography),
(51, 'San Francisco International Airport',                      'SFO',  50, ST_MakePoint(-122.3789,  37.6213)::geography),
(52, 'Washington D.C. Dulles International Airport',             'IAD',  51, ST_MakePoint(-77.4558,   38.9531)::geography),
(53, 'Logan International Airport',                              'BOS',  52, ST_MakePoint(-71.0052,   42.3656)::geography),

-- AMÉRICA DEL NORTE - CANADÁ / MÉXICO / PANAMÁ
(16, 'Toronto Pearson Intl.',                                    'YYZ',  16, ST_MakePoint(-79.6306,   43.6777)::geography),
(17, 'Benito Juarez Intl.',                                      'MEX',  17, ST_MakePoint(-99.0721,   19.4363)::geography),
(18, 'Vancouver Intl.',                                          'YVR',  18, ST_MakePoint(-123.1839,  49.1967)::geography),
(27, 'Tocumen Intl.',                                            'PTY',  27, ST_MakePoint(-79.3835,    9.0714)::geography),

-- EUROPA - ESPAÑA / FRANCIA / UK / ALEMANIA / ITALIA
(10, 'Adolfo Suarez Madrid Barajas',                             'MAD',  10, ST_MakePoint(-3.5673,    40.4936)::geography),
(11, 'El Prat Barcelona',                                        'BCN',  11, ST_MakePoint(2.0785,     41.2974)::geography),
(12, 'Charles de Gaulle',                                        'CDG',  12, ST_MakePoint(2.5479,     49.0097)::geography),
(13, 'Heathrow Airport',                                         'LHR',  13, ST_MakePoint(-0.4543,    51.4700)::geography),
(14, 'Berlin Brandenburg',                                       'BER',  14, ST_MakePoint(13.5033,    52.3667)::geography),
(15, 'Leonardo da Vinci Fiumicino',                              'FCO',  15, ST_MakePoint(12.2389,    41.8003)::geography),
(26, 'Frankfurt am Main',                                        'FRA',  26, ST_MakePoint(8.5706,     50.0333)::geography),
(30, 'Gatwick Airport',                                          'LGW',  13, ST_MakePoint(-0.1821,    51.1537)::geography),

-- EUROPA - RESTO
(41, 'Amsterdam Schiphol Airport',                               'AMS',  40, ST_MakePoint(4.7683,     52.3086)::geography),
(42, 'Brussels Airport',                                         'BRU',  41, ST_MakePoint(4.4844,     50.9010)::geography),
(43, 'Zurich Airport',                                           'ZRH',  42, ST_MakePoint(8.5492,     47.4647)::geography),
(44, 'Vienna International Airport',                             'VIE',  43, ST_MakePoint(16.5697,    48.1103)::geography),
(45, 'Copenhagen Airport',                                       'CPH',  44, ST_MakePoint(12.6561,    55.6181)::geography),
(46, 'Stockholm Arlanda Airport',                                'ARN',  45, ST_MakePoint(17.9186,    59.6519)::geography),
(47, 'Humberto Delgado Airport',                                 'LIS',  46, ST_MakePoint(-9.1354,    38.7756)::geography),
(48, 'Helsinki-Vantaa Airport',                                  'HEL',  47, ST_MakePoint(24.9633,    60.3172)::geography),
(49, 'Oslo Gardermoen Airport',                                   'OSL',  48, ST_MakePoint(11.1004,    60.1939)::geography),
(50, 'Václav Havel Airport Prague',                              'PRG',  49, ST_MakePoint(14.2600,    50.1008)::geography),
(54, 'Budapest Ferenc Liszt International Airport',              'BUD',  53, ST_MakePoint(19.2611,    47.4298)::geography),
(55, 'Cluj-Napoca International Airport',                        'CLJ',  54, ST_MakePoint(23.6861,    46.7852)::geography),
(56, 'Sofia Airport',                                            'SOF',  55, ST_MakePoint(23.4114,    42.6967)::geography),
(57, 'Zagreb International Airport',                             'ZAG',  56, ST_MakePoint(16.0688,    45.7429)::geography),
(58, 'Ljubljana Jože Pučnik Airport',                            'LJU',  57, ST_MakePoint(14.4576,    46.2237)::geography),
(59, 'Belgrade Nikola Tesla Airport',                            'BEG',  58, ST_MakePoint(20.3091,    44.8184)::geography),
(60, 'Tirana International Airport',                             'TIA',  59, ST_MakePoint(19.7206,    41.4147)::geography),
(61, 'Skopje Alexander the Great Airport',                       'SKP',  60, ST_MakePoint(21.6214,    41.9616)::geography),

-- ASIA
(21, 'Beijing Capital Intl.',                                    'PEK',  21, ST_MakePoint(116.5844,   40.0799)::geography),
(22, 'Tokyo Haneda',                                             'HND',  22, ST_MakePoint(139.7811,   35.5494)::geography),
(31, 'Indira Gandhi Intl.',                                      'DEL',  30, ST_MakePoint(77.0889,    28.5562)::geography),
(32, 'Incheon Intl.',                                            'ICN',  31, ST_MakePoint(126.4407,   37.4602)::geography),
(36, 'Sheremetyevo Intl.',                                       'SVO',  35, ST_MakePoint(37.4146,    55.9726)::geography),
(37, 'Istanbul Airport',                                         'IST',  36, ST_MakePoint(28.7519,    41.2608)::geography),
(38, 'Soekarno-Hatta Intl.',                                     'CGK',  37, ST_MakePoint(106.6559,   -6.1256)::geography),
(39, 'Ninoy Aquino Intl.',                                       'MNL',  38, ST_MakePoint(121.0197,   14.5086)::geography),
(40, 'Suvarnabhumi Airport',                                     'BKK',  39, ST_MakePoint(100.7501,   13.6900)::geography),

-- ORIENTE MEDIO
(24, 'Hamad Intl. Airport',                                      'DOH',  24, ST_MakePoint(51.6138,    25.2731)::geography),
(25, 'Dubai Intl. Airport',                                      'DXB',  25, ST_MakePoint(55.3644,    25.2532)::geography),

-- OCEANÍA
(19, 'Melbourne Airport',                                        'MEL',  19, ST_MakePoint(144.8410,  -37.6690)::geography),
(20, 'Sydney Kingsford Smith',                                   'SYD',  20, ST_MakePoint(151.1772,  -33.9399)::geography),
(35, 'Auckland Intl.',                                           'AKL',  34, ST_MakePoint(174.7850,  -37.0082)::geography),
(62, 'Brisbane Airport',                                         'BNE',  61, ST_MakePoint(153.1175,  -27.3842)::geography),
(63, 'Wellington Airport',                                       'WLG',  62, ST_MakePoint(174.8050,  -41.3272)::geography),

-- ÁFRICA
(33, 'Cape Town Intl.',                                          'CPT',  32, ST_MakePoint(18.5997,   -33.9715)::geography),
(34, 'Cairo Intl.',                                              'CAI',  33, ST_MakePoint(31.4056,    30.1219)::geography),

-- CHILE (aeropuertos regionales)
(64,  'Aeropuerto Diego Aracena',                        'IQQ', 187, ST_MakePoint(-70.1813, -20.5352)::geography),
(65,  'Aeropuerto Cerro Moreno',                         'ANF', 183, ST_MakePoint(-70.4451, -23.4444)::geography),
(66,  'Aeropuerto Arturo Merino Benítez (Concepción)',   'CCP', 184, ST_MakePoint(-73.0631, -36.7726)::geography),
(67,  'Aeropuerto El Tepual',                            'PMC', 185, ST_MakePoint(-73.0940, -41.4389)::geography),
(68,  'Aeropuerto Carlos Ibáñez del Campo',              'PUQ', 186, ST_MakePoint(-70.8545, -53.0026)::geography),

-- COLOMBIA (adicionales)
(69,  'Aeropuerto José María Córdova',                   'MDE', 176, ST_MakePoint(-75.4231,   6.1645)::geography),
(70,  'Aeropuerto Alfonso Bonilla Aragón',               'CLO', 177, ST_MakePoint(-76.3816,   3.5432)::geography),
(71,  'Aeropuerto Rafael Núñez',                         'CTG', 178, ST_MakePoint(-75.5130,  10.4424)::geography),

-- BRASIL (adicionales)
(72,  'Aeropuerto Internacional de Brasilia',            'BSB', 173, ST_MakePoint(-47.9187,  -15.8711)::geography),
(73,  'Aeropuerto Internacional Tancredo Neves',         'CNF', 174, ST_MakePoint(-43.9719,  -19.6244)::geography),
(74,  'Aeropuerto Internacional dos Guararapes',         'REC', 171, ST_MakePoint(-34.9228,   -8.1265)::geography),
(75,  'Aeropuerto Internacional Deputado Luís Eduardo',  'SSA', 172, ST_MakePoint(-38.3322,  -12.9086)::geography),
(76,  'Aeropuerto Internacional Eduardo Gomes',          'MAO', 175, ST_MakePoint(-60.0497,   -3.0386)::geography),

-- ARGENTINA (adicionales)
(77,  'Aeropuerto Internacional Ingeniero Ambrosio',     'COR', 179, ST_MakePoint(-64.2080,  -31.3236)::geography),
(78,  'Aeropuerto Internacional Gov. Francisco Gabrielli','MDZ', 180, ST_MakePoint(-68.7929,  -32.8317)::geography),
(79,  'Aeropuerto Internacional Islas Malvinas',         'ROS', 181, ST_MakePoint(-60.7856,  -32.9036)::geography),

-- PERÚ (adicionales)
(80,  'Aeropuerto Internacional Alejandro Velasco Astete','CUZ', 189, ST_MakePoint(-71.9388,  -13.5357)::geography),
(81,  'Aeropuerto Internacional Rodríguez Ballón',       'AQP', 188, ST_MakePoint(-71.5830,  -16.3411)::geography),

-- BOLIVIA
(82,  'Aeropuerto Internacional Viru Viru',              'VVI', 190, ST_MakePoint(-63.1354,  -17.6448)::geography),
(83,  'Aeropuerto Internacional Jorge Wilstermann',      'CBB', 191, ST_MakePoint(-66.1771,  -17.4211)::geography),
(84,  'Aeropuerto Internacional El Alto',                'LPB', 71,  ST_MakePoint(-68.1923,  -16.5133)::geography),

-- ECUADOR
(85,  'Aeropuerto Internacional Mariscal Sucre',         'UIO', 73,  ST_MakePoint(-78.3575,   -0.1292)::geography),
(86,  'Aeropuerto Internacional José Joaquín de Olmedo','GYE', 74,  ST_MakePoint(-79.8836,   -2.1574)::geography),

-- VENEZUELA
(87,  'Aeropuerto Internacional Simón Bolívar',          'CCS', 75,  ST_MakePoint(-66.9909,   10.6031)::geography),

-- CARIBE / CENTROAMÉRICA
(88,  'Aeropuerto Internacional José Martí',             'HAV', 63,  ST_MakePoint(-82.4091,   22.9892)::geography),
(89,  'Aeropuerto Internacional Las Américas',           'SDQ', 64,  ST_MakePoint(-69.6689,   18.4297)::geography),
(90,  'Aeropuerto Internacional Norman Manley',          'KIN', 65,  ST_MakePoint(-76.7875,   17.9357)::geography),
(91,  'Aeropuerto Internacional Juan Santamaría',        'SJO', 66,  ST_MakePoint(-84.2088,    9.9939)::geography),
(92,  'Aeropuerto Internacional La Aurora',              'GUA', 67,  ST_MakePoint(-90.5275,   14.5833)::geography),
(93,  'Aeropuerto Internacional Toncontín',              'TGU', 68,  ST_MakePoint(-87.2172,   14.0608)::geography),
(94,  'Aeropuerto Internacional Monseñor Óscar Romero', 'SAL', 69,  ST_MakePoint(-89.0557,   13.4409)::geography),
(95,  'Aeropuerto Internacional Augusto C. Sandino',    'MGA', 70,  ST_MakePoint(-86.1681,   12.1415)::geography),
(96,  'Aeropuerto Internacional de Trinidad',            'POS', 76,  ST_MakePoint(-61.3373,   10.5954)::geography),

-- EUROPA - ISLAS BRITÁNICAS / NORTE
(97,  'Aeropuerto de Dublín',                            'DUB', 77,  ST_MakePoint(-6.2700,    53.4213)::geography),
(98,  'Aeropuerto de Keflavík',                          'KEF', 78,  ST_MakePoint(-22.6056,   63.9850)::geography),

-- EUROPA - MEDITERRÁNEO / BALCANES
(99,  'Aeropuerto Internacional de Atenas',              'ATH', 79,  ST_MakePoint(23.9444,    37.9364)::geography),
(100, 'Aeropuerto Internacional de Tesalónica',          'SKG', 80,  ST_MakePoint(22.9709,    40.5197)::geography),
(101, 'Aeropuerto Internacional de Larnaca',             'LCA', 81,  ST_MakePoint(33.6249,    34.8751)::geography),
(102, 'Aeropuerto Internacional de Malta',               'MLA', 82,  ST_MakePoint(14.4775,    35.8575)::geography),
(103, 'Aeropuerto de Luxemburgo Findel',                 'LUX', 83,  ST_MakePoint(6.2044,     49.6233)::geography),
(104, 'Aeropuerto de Bratislava',                        'BTS', 84,  ST_MakePoint(17.2127,    48.1702)::geography),
(105, 'Aeropuerto Internacional de Vilna',               'VNO', 85,  ST_MakePoint(25.2858,    54.6341)::geography),
(106, 'Aeropuerto Internacional de Riga',                'RIX', 86,  ST_MakePoint(23.9711,    56.9236)::geography),
(107, 'Aeropuerto Internacional de Tallin',              'TLL', 87,  ST_MakePoint(24.8328,    59.4133)::geography),
(108, 'Aeropuerto Internacional de Minsk',               'MSQ', 88,  ST_MakePoint(28.0306,    53.8825)::geography),
(109, 'Aeropuerto Internacional Boryspil',               'KBP', 89,  ST_MakePoint(30.8947,    50.3450)::geography),
(110, 'Aeropuerto Internacional de Chisinau',            'KIV', 92,  ST_MakePoint(28.9309,    46.9277)::geography),
(111, 'Aeropuerto Internacional de Sarajevo',            'SJJ', 93,  ST_MakePoint(18.3315,    43.8246)::geography),
(112, 'Aeropuerto Internacional de Podgorica',           'TGD', 94,  ST_MakePoint(19.2519,    42.3594)::geography),
(113, 'Aeropuerto Internacional de Pristina',            'PRN', 95,  ST_MakePoint(21.0358,    42.5728)::geography),

-- ÁFRICA DEL NORTE
(114, 'Aeropuerto Internacional Mohammed V',             'CMN', 96,  ST_MakePoint(-7.5897,    33.3675)::geography),
(115, 'Aeropuerto Internacional de Marrakech',           'RAK', 97,  ST_MakePoint(-8.0363,    31.6069)::geography),
(116, 'Aeropuerto Internacional de Túnez-Cartago',       'TUN', 98,  ST_MakePoint(10.2272,    36.8510)::geography),
(117, 'Aeropuerto Internacional Houari Boumediene',      'ALG', 99,  ST_MakePoint(3.2154,     36.6910)::geography),

-- ÁFRICA SUBSAHARIANA
(118, 'Aeropuerto Internacional Murtala Muhammed',       'LOS', 101, ST_MakePoint(3.3212,      6.5774)::geography),
(119, 'Aeropuerto Internacional Kotoka',                 'ACC', 103, ST_MakePoint(-0.1668,     5.6052)::geography),
(120, 'Aeropuerto Internacional Jomo Kenyatta',          'NBO', 104, ST_MakePoint(36.9275,    -1.3192)::geography),
(121, 'Aeropuerto Internacional Julius Nyerere',         'DAR', 105, ST_MakePoint(39.2026,    -6.8781)::geography),
(122, 'Aeropuerto Internacional Abeid Amani Karume',     'ZNZ', 106, ST_MakePoint(39.2248,    -6.2220)::geography),
(123, 'Aeropuerto Internacional Bole',                   'ADD', 107, ST_MakePoint(38.7993,     8.9779)::geography),
(124, 'Aeropuerto Internacional Léopold Sédar Senghor',  'DKR', 108, ST_MakePoint(-17.4902,   14.7397)::geography),
(125, 'Aeropuerto Internacional Félix-Houphouët-Boigny', 'ABJ', 109, ST_MakePoint(-3.9263,     5.2614)::geography),
(126, 'Aeropuerto Internacional de Luanda',              'LAD', 111, ST_MakePoint(13.2312,    -8.8583)::geography),
(127, 'Aeropuerto Internacional de Maputo',              'MPM', 112, ST_MakePoint(32.5726,   -25.9208)::geography),
(128, 'Aeropuerto Internacional Robert Gabriel Mugabe',  'HRE', 113, ST_MakePoint(31.0928,   -17.9318)::geography),
(129, 'Aeropuerto Internacional Kenneth Kaunda',         'LUN', 114, ST_MakePoint(28.4526,   -15.3308)::geography),
(130, 'Aeropuerto Internacional Entebbe',                'EBB', 115, ST_MakePoint(32.4435,    0.0424)::geography),
(131, 'Aeropuerto Internacional de Kigali',              'KGL', 116, ST_MakePoint(30.1395,    -1.9686)::geography),
(132, 'Aeropuerto Internacional OR Tambo',               'JNB', 117, ST_MakePoint(28.2460,   -26.1392)::geography),
(133, 'Aeropuerto Internacional King Shaka',             'DUR', 118, ST_MakePoint(31.1197,   -29.6144)::geography),

-- ASIA - SUDESTE
(134, 'Aeropuerto Internacional de Ho Chi Minh',         'SGN', 119, ST_MakePoint(106.6519,   10.8188)::geography),
(135, 'Aeropuerto Internacional Nội Bài',                'HAN', 120, ST_MakePoint(105.8067,   21.2212)::geography),
(136, 'Aeropuerto Internacional de Kuala Lumpur',        'KUL', 121, ST_MakePoint(101.7015,    2.7456)::geography),
(137, 'Aeropuerto Internacional de Rangún',              'RGN', 122, ST_MakePoint(96.1332,    16.9073)::geography),
(138, 'Aeropuerto Internacional de Phnom Penh',          'PNH', 123, ST_MakePoint(104.8440,   11.5466)::geography),
(139, 'Aeropuerto Internacional Bandaranaike',           'CMB', 124, ST_MakePoint(79.8841,     7.1808)::geography),
(140, 'Aeropuerto Internacional de Singapur Changi',     'SIN', 147, ST_MakePoint(103.9915,    1.3644)::geography),

-- ASIA - SUR
(141, 'Aeropuerto Internacional Hazrat Shahjalal',       'DAC', 125, ST_MakePoint(90.3978,    23.8433)::geography),
(142, 'Aeropuerto Internacional Tribhuvan',              'KTM', 126, ST_MakePoint(85.3591,    27.6966)::geography),
(143, 'Aeropuerto Internacional Jinnah',                 'KHI', 127, ST_MakePoint(67.1608,    24.9065)::geography),
(144, 'Aeropuerto Internacional Allama Iqbal',           'LHE', 128, ST_MakePoint(74.4036,    31.5216)::geography),
(145, 'Aeropuerto Internacional Islamabad',              'ISB', 129, ST_MakePoint(72.8516,    33.5497)::geography),

-- ASIA CENTRAL / CÁUCASO
(146, 'Aeropuerto Internacional de Almaty',              'ALA', 130, ST_MakePoint(77.0208,    43.3521)::geography),
(147, 'Aeropuerto Internacional de Taskent',             'TAS', 132, ST_MakePoint(69.2812,    41.2579)::geography),
(148, 'Aeropuerto Internacional Heydar Aliyev',          'GYD', 133, ST_MakePoint(50.0466,    40.4675)::geography),
(149, 'Aeropuerto Internacional de Tiflis',              'TBS', 134, ST_MakePoint(44.9547,    41.6692)::geography),
(150, 'Aeropuerto Internacional Zvartnots',              'EVN', 135, ST_MakePoint(44.3959,    40.1473)::geography),

-- ASIA ORIENTAL
(151, 'Aeropuerto Internacional de Taipéi Taoyuan',      'TPE', 137, ST_MakePoint(121.2332,   25.0777)::geography),
(152, 'Aeropuerto Internacional de Hong Kong',           'HKG', 138, ST_MakePoint(113.9145,   22.3080)::geography),
(153, 'Aeropuerto Internacional de Osaka Kansai',        'KIX', 139, ST_MakePoint(135.2440,   34.4272)::geography),
(154, 'Aeropuerto Internacional de Shanghái Pudong',     'PVG', 142, ST_MakePoint(121.8083,   31.1443)::geography),
(155, 'Aeropuerto Internacional de Guangzhou',           'CAN', 143, ST_MakePoint(113.2980,   23.3924)::geography),
(156, 'Aeropuerto Internacional de Chengdu',             'CTU', 145, ST_MakePoint(103.9477,   30.5785)::geography),
(157, 'Aeropuerto Internacional de Ulán Bator',          'ULN', 136, ST_MakePoint(106.7664,   47.8431)::geography),

-- ORIENTE MEDIO
(158, 'Aeropuerto Internacional Rey Khalid',             'RUH', 148, ST_MakePoint(46.6988,    24.9576)::geography),
(159, 'Aeropuerto Internacional Rey Abdulaziz',          'JED', 149, ST_MakePoint(39.1565,    21.6796)::geography),
(160, 'Aeropuerto Internacional Ben Gurión',             'TLV', 151, ST_MakePoint(34.8854,    32.0055)::geography),
(161, 'Aeropuerto Internacional Reina Alia',             'AMM', 152, ST_MakePoint(35.9932,    31.7226)::geography),
(162, 'Aeropuerto Internacional de Beirut',              'BEY', 153, ST_MakePoint(35.4884,    33.8209)::geography),
(163, 'Aeropuerto Internacional de Kuwait',              'KWI', 154, ST_MakePoint(47.9689,    29.2267)::geography),
(164, 'Aeropuerto Internacional de Bahréin',             'BAH', 155, ST_MakePoint(50.6336,    26.2708)::geography),
(165, 'Aeropuerto Internacional de Mascate',             'MCT', 156, ST_MakePoint(58.2844,    23.5933)::geography),
(166, 'Aeropuerto Internacional de Abu Dabi',            'AUH', 159, ST_MakePoint(54.6511,    24.4330)::geography),

-- OCEANÍA / PACÍFICO
(167, 'Aeropuerto Internacional de Port Moresby',        'POM', 160, ST_MakePoint(147.2200,   -9.4433)::geography),
(168, 'Aeropuerto Internacional de Nadi',                'NAN', 161, ST_MakePoint(177.4436,  -17.7554)::geography),
(169, 'Aeropuerto Internacional de Faaa',                'PPT', 162, ST_MakePoint(-149.6067, -17.5534)::geography),
(170, 'Aeropuerto Internacional de Cairns',              'CNS', 163, ST_MakePoint(145.7553,  -16.8858)::geography),
(171, 'Aeropuerto Internacional de Perth',               'PER', 164, ST_MakePoint(115.9672,  -31.9402)::geography),
(172, 'Aeropuerto Internacional de Christchurch',        'CHC', 166, ST_MakePoint(172.5369,  -43.4894)::geography);


-- ============================================================
-- VERIFICACIÓN: distancia entre SCL y EZE (debe ser ~1135 km)
-- ============================================================
-- SELECT
--     a1.Codigo_IATA AS origen,
--     a2.Codigo_IATA AS destino,
--     ROUND(ST_Distance(a1.posicion, a2.posicion)::numeric / 1000, 1) AS distancia_km
-- FROM Aeropuerto a1, Aeropuerto a2
-- WHERE a1.Codigo_IATA = 'SCL' AND a2.Codigo_IATA = 'EZE';



DO $$
DECLARE
rec         RECORD;
    num_puertas INT;
    i           INT;
    codigo_p    VARCHAR(15);
    terminal_name VARCHAR(80);

    -- Clasificación por IATA
    megahubs    TEXT[] := ARRAY['ATL','DXB','LHR','ORD','HND','LAX','CDG','JFK','FRA','AMS',
                                 'PEK','PVG','DFW','CAN','SIN','ICN','DEL','BKK','HKG','MAD'];
    hubs_grandes TEXT[] := ARRAY['GRU','MIA','BCN','FCO','MUC','SYD','YYZ','MEX','LIM','BOG',
                                  'SCL','EZE','PTY','IST','SVO','DOH','KUL','NBO','ADD','JNB',
                                  'CMN','LOS','DAR','SGN','KBP','RUH','TLV','KIX','TPE','CTU'];
    hubs_regionales TEXT[] := ARRAY['MVD','LGW','BER','VIE','ZRH','BRU','CPH','ARN','LIS','HEL',
                                     'OSL','PRG','BUD','ATH','DUB','LUX','RIX','VNO','TLL','GYD',
                                     'TBS','ALA','KTM','CMB','AMM','AUH','MCT','BAH','KWI','JED',
                                     'BSB','CNF','COR','UIO','VVI','HAV','SJO','SDQ','KGL','ACC',
                                     'DKR','ABJ','LAD','EBB','HRE','LUN','CPT','DUR'];
BEGIN
FOR rec IN SELECT ID_AEROPUERTO, Codigo_IATA FROM Aeropuerto LOOP

                                                  -- Determinar número de puertas y tipo de terminales
    IF rec.Codigo_IATA = ANY(megahubs) THEN
            num_puertas := 12;
ELSIF rec.Codigo_IATA = ANY(hubs_grandes) THEN
            num_puertas := 10;
        ELSIF rec.Codigo_IATA = ANY(hubs_regionales) THEN
            num_puertas := 8;
ELSE
            -- Verificar si ya tiene puertas (aeropuertos del bloque original)
            IF EXISTS (SELECT 1 FROM Puerta_Embarque WHERE ID_AEROPUERTO = rec.ID_AEROPUERTO) THEN
                CONTINUE;  -- ya procesado anteriormente, saltar
END IF;
            num_puertas := 4;
END IF;

        -- Saltar si ya tiene puertas insertadas
        IF EXISTS (SELECT 1 FROM Puerta_Embarque WHERE ID_AEROPUERTO = rec.ID_AEROPUERTO) THEN
            CONTINUE;
END IF;

FOR i IN 1..num_puertas LOOP

            -- Asignación de terminal realista según clasificación y número de puerta
            IF rec.Codigo_IATA = ANY(megahubs) THEN
                -- Megahubs: 3 terminales
                IF i <= 4 THEN
                    terminal_name := 'Terminal Internacional';
                ELSIF i <= 8 THEN
                    terminal_name := 'Terminal Doméstica';
ELSE
                    terminal_name := 'Terminal Low Cost';
END IF;

            ELSIF rec.Codigo_IATA = ANY(hubs_grandes) THEN
                -- Hubs grandes: 2 terminales
                IF i <= 5 THEN
                    terminal_name := 'Terminal Internacional';
ELSE
                    terminal_name := 'Terminal Doméstica';
END IF;

            ELSIF rec.Codigo_IATA = ANY(hubs_regionales) THEN
                -- Hubs regionales: 2 terminales
                IF i <= 4 THEN
                    terminal_name := 'Terminal A';
ELSE
                    terminal_name := 'Terminal B';
END IF;

ELSE
                terminal_name := 'Terminal Única';
END IF;

            -- Código de puerta: IATA + letra de terminal + número
            -- Megahubs: INT-A01, DOM-B05, LC-C09
            -- Otros: A01, B05, etc.
            IF rec.Codigo_IATA = ANY(megahubs) THEN
                IF i <= 4 THEN
                    codigo_p := rec.Codigo_IATA || '-A' || LPAD(i::text, 2, '0');
                ELSIF i <= 8 THEN
                    codigo_p := rec.Codigo_IATA || '-B' || LPAD((i-4)::text, 2, '0');
ELSE
                    codigo_p := rec.Codigo_IATA || '-C' || LPAD((i-8)::text, 2, '0');
END IF;

            ELSIF rec.Codigo_IATA = ANY(hubs_grandes) THEN
                IF i <= 5 THEN
                    codigo_p := rec.Codigo_IATA || '-I' || LPAD(i::text, 2, '0');
ELSE
                    codigo_p := rec.Codigo_IATA || '-D' || LPAD((i-5)::text, 2, '0');
END IF;

            ELSIF rec.Codigo_IATA = ANY(hubs_regionales) THEN
                IF i <= 4 THEN
                    codigo_p := rec.Codigo_IATA || '-A' || LPAD(i::text, 2, '0');
ELSE
                    codigo_p := rec.Codigo_IATA || '-B' || LPAD((i-4)::text, 2, '0');
END IF;

ELSE
                codigo_p := rec.Codigo_IATA || '-' || LPAD(i::text, 2, '0');
END IF;

INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO)
VALUES (codigo_p, terminal_name, rec.ID_AEROPUERTO);

END LOOP;
END LOOP;
END $$;




-- ============================================================
-- VERIFICACIONES
-- ============================================================

-- Total de aeropuertos por continente
-- SELECT co.Nombre AS continente, COUNT(*) AS total_aeropuertos
-- FROM Aeropuerto a
-- JOIN Ciudad ci ON a.ID_CIUDAD = ci.ID_CIUDAD
-- JOIN Pais pa ON ci.ID_PAIS = pa.ID_PAIS
-- JOIN Continente co ON pa.ID_CONTINENTE = co.ID_CONTINENTE
-- GROUP BY co.Nombre ORDER BY total_aeropuertos DESC;

-- Total de puertas por aeropuerto
-- SELECT a.Codigo_IATA, COUNT(p.ID_PUERTA) AS puertas
-- FROM Aeropuerto a
-- LEFT JOIN Puerta_Embarque p ON a.ID_AEROPUERTO = p.ID_AEROPUERTO
-- GROUP BY a.Codigo_IATA ORDER BY puertas DESC;

-- Aeropuertos sin coordenadas (no debería haber ninguno)
-- SELECT Codigo_IATA FROM Aeropuerto WHERE posicion IS NULL;







-- 2. Insertamos la aerolínea exclusiva del sistema
INSERT INTO Aerolinea (ID_AEROLINEA, Nombre, Codigo) VALUES
    (1, 'SkyWay Airlines', 'SW');

-- =============================================================================
-- SCRIPT DE POBLADO INTEGRAL Y COHERENTE - SKYWAY ENTERPRISE
-- =============================================================================
INSERT INTO Clase_asiento (Descripcion) VALUES
                                            ('Económica'),
                                            ('Ejecutiva'),
                                            ('Primera Clase');

-- =============================================================================
-- 2. FABRICANTES DE AERONAVES
-- =============================================================================
INSERT INTO Fabricante (Nombre) VALUES
                                    ('Airbus'),
                                    ('Boeing'),
                                    ('Embraer');

-- =============================================================================
-- 3. MODELOS DE AVIÓN
-- =============================================================================
INSERT INTO Modelo_Avion (Nombre, ID_FABRICANTE) VALUES
                                                     ('Airbus A320',  (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Airbus')),
                                                     ('Boeing 747',   (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Boeing')),
                                                     ('Airbus A350',  (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Airbus')),
                                                     ('Boeing 787',   (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Boeing')),
                                                     ('Embraer E195', (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Embraer')),
                                                     ('Boeing 777',   (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Boeing')),
                                                     ('Airbus A380',  (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Airbus')),
                                                     ('Airbus A330',  (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Airbus')),
                                                     ('Boeing 757',   (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Boeing'));

-- =============================================================================
-- 4. CONFIGURACIÓN ESPACIAL DE CABINAS POR MODELO (REALISTA)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- AIRBUS A320 (Fusilaje Estrecho | Total: 180 asientos -> 12 Ejecutiva + 168 Económica)
-- -----------------------------------------------------------------------------
INSERT INTO Configuracion_Cabina (ID_MODELO, ID_CLASE, Fila_Inicio, Fila_Fin, Distribucion_Columnas, Letras_Columnas, Es_Salida_Emergencia) VALUES
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Airbus A320'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Ejecutiva'), 1, 3, '2-2', 'A,C,D,F', FALSE),
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Airbus A320'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'), 4, 11, '3-3', 'A,B,C,D,E,F', FALSE),
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Airbus A320'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'), 12, 13, '3-3', 'A,B,C,D,E,F', TRUE),
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Airbus A320'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'), 14, 31, '3-3', 'A,B,C,D,E,F', FALSE);

-- -----------------------------------------------------------------------------
-- BOEING 747-400 (Fusilaje Ancho | Total: 380 asientos -> 12 Primera + 56 Ejecutiva + 312 Económica)
-- -----------------------------------------------------------------------------
INSERT INTO Configuracion_Cabina (ID_MODELO, ID_CLASE, Fila_Inicio, Fila_Fin, Distribucion_Columnas, Letras_Columnas, Es_Salida_Emergencia) VALUES
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Boeing 747'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Primera Clase'), 1, 3, '1-2-1', 'A,D,G,K', FALSE),
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Boeing 747'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Ejecutiva'),     4, 11, '2-3-2', 'A,B,D,E,F,J,K', FALSE),
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Boeing 747'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'),     12, 12, '3-4-3', 'A,B,C,D,E,F,G,H,J,K', TRUE),
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Boeing 747'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'),     14, 43, '3-4-3', 'A,B,C,D,E,F,G,H,J,K', FALSE);

-- -----------------------------------------------------------------------------
-- AIRBUS A350-900 (Fusilaje Ancho | Total: 310 asientos -> 16 Primera + 48 Ejecutiva + 246 Económica)
-- -----------------------------------------------------------------------------
INSERT INTO Configuracion_Cabina (ID_MODELO, ID_CLASE, Fila_Inicio, Fila_Fin, Distribucion_Columnas, Letras_Columnas, Es_Salida_Emergencia) VALUES
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Airbus A350'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Primera Clase'), 1, 4, '1-2-1', 'A,D,G,K', FALSE),
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Airbus A350'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Ejecutiva'),     5, 12, '2-2-2', 'A,C,D,G,H,K', FALSE),
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Airbus A350'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'),     14, 14, '3-3-3', 'A,B,C,D,E,F,J,K,L', TRUE),
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Airbus A350'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'),     15, 40, '3-3-3', 'A,B,C,D,E,F,J,K,L', FALSE);

-- -----------------------------------------------------------------------------
-- BOEING 787-9 (Fusilaje Ancho | Total: 220 asientos -> 12 Primera + 36 Ejecutiva + 172 Económica)
-- -----------------------------------------------------------------------------
INSERT INTO Configuracion_Cabina (ID_MODELO, ID_CLASE, Fila_Inicio, Fila_Fin, Distribucion_Columnas, Letras_Columnas, Es_Salida_Emergencia) VALUES
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Boeing 787'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Primera Clase'), 1, 3, '1-2-1', 'A,D,G,K', FALSE),
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Boeing 787'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Ejecutiva'),     4, 9, '2-2-2', 'A,C,D,G,H,K', FALSE),
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Boeing 787'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'),     10, 10, '3-3-3', 'A,B,C,D,E,F,J,K,L', TRUE),
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Boeing 787'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'),     11, 28, '3-3-3', 'A,B,C,D,E,F,J,K,L', FALSE);

-- -----------------------------------------------------------------------------
-- EMBRAER E195 (Regional | Total: 120 asientos -> 12 Ejecutiva + 108 Económica)
-- -----------------------------------------------------------------------------
INSERT INTO Configuracion_Cabina (ID_MODELO, ID_CLASE, Fila_Inicio, Fila_Fin, Distribucion_Columnas, Letras_Columnas, Es_Salida_Emergencia) VALUES
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Embraer E195'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Ejecutiva'), 1, 4, '1-2', 'A,C,D', FALSE),
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Embraer E195'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'), 5, 12, '2-2', 'A,B,C,D', FALSE),
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Embraer E195'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'), 13, 13, '2-2', 'A,B,C,D', TRUE),
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Embraer E195'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'), 14, 30, '2-2', 'A,B,C,D', FALSE);

-- -----------------------------------------------------------------------------
-- BOEING 777-300ER (Fusilaje Ancho Grande | Total: 450 asientos -> 16 Primera + 70 Ejecutiva + 364 Económica)
-- -----------------------------------------------------------------------------
INSERT INTO Configuracion_Cabina (ID_MODELO, ID_CLASE, Fila_Inicio, Fila_Fin, Distribucion_Columnas, Letras_Columnas, Es_Salida_Emergencia) VALUES
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Boeing 777'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Primera Clase'), 1, 4, '1-2-1', 'A,D,G,K', FALSE),
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Boeing 777'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Ejecutiva'),     5, 14, '2-3-2', 'A,B,D,E,F,J,K', FALSE),
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Boeing 777'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'),     15, 15, '3-4-3', 'A,B,C,D,E,F,G,H,J,K', TRUE),
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Boeing 777'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'),     16, 50, '3-4-3', 'A,B,C,D,E,F,G,H,J,K', FALSE);

-- -----------------------------------------------------------------------------
-- AIRBUS A380-800 (Superjumbo | Total: 650 asientos -> 20 Primera + 96 Ejecutiva + 534 Económica)
-- -----------------------------------------------------------------------------
INSERT INTO Configuracion_Cabina (ID_MODELO, ID_CLASE, Fila_Inicio, Fila_Fin, Distribucion_Columnas, Letras_Columnas, Es_Salida_Emergencia) VALUES
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Airbus A380'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Primera Clase'), 1, 5, '1-2-1', 'A,E,F,K', FALSE),
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Airbus A380'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Ejecutiva'),     6, 21, '2-2-2', 'A,B,E,F,J,K', FALSE),
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Airbus A380'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'),     22, 22, '3-4-3', 'A,B,C,D,E,F,G,H,J,K', TRUE),
((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Airbus A380'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'),     23, 74, '3-4-3', 'A,B,C,D,E,F,G,H,J,K', FALSE);

-- -----------------------------------------------------------------------------
-- AIRBUS A330-300 (Fusilaje Ancho Medio | Total: 250 asientos -> 8 Primera + 36 Ejecutiva + 206 Económica)
-- -----------------------------------------------------------------------------
INSERT INTO Configuracion_Cabina (ID_MODELO, ID_CLASE, Fila_Inicio, Fila_Fin, Distribucion_Columnas, Letras_Columnas, Es_Salida_Emergencia) VALUES
                                                                                                                                                ((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Airbus A330'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Primera Clase'), 1, 2, '1-2-1', 'A,D,G,K', FALSE),
                                                                                                                                                ((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Airbus A330'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Ejecutiva'),     3, 8, '2-2-2', 'A,B,D,G,J,K', FALSE),
                                                                                                                                                ((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Airbus A330'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'),     9, 9, '2-4-2', 'A,C,D,E,F,G,H,K', TRUE),
                                                                                                                                                ((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Airbus A330'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'),     10, 34, '2-4-2', 'A,C,D,E,F,G,H,K', FALSE);

-- -----------------------------------------------------------------------------
-- BOEING 757-200 (Fusilaje Estrecho Largo | Total: 190 asientos -> 16 Ejecutiva + 174 Económica)
-- -----------------------------------------------------------------------------
INSERT INTO Configuracion_Cabina (ID_MODELO, ID_CLASE, Fila_Inicio, Fila_Fin, Distribucion_Columnas, Letras_Columnas, Es_Salida_Emergencia) VALUES
                                                                                                                                                ((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Boeing 757'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Ejecutiva'), 1, 4, '2-2', 'A,C,D,F', FALSE),
                                                                                                                                                ((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Boeing 757'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'), 5, 14, '3-3', 'A,B,C,D,E,F', FALSE),
                                                                                                                                                ((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Boeing 757'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'), 15, 16, '3-3', 'A,B,C,D,E,F', TRUE),
                                                                                                                                                ((SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Boeing 757'), (SELECT ID_CLASE FROM Clase_asiento WHERE Descripcion = 'Económica'), 17, 33, '3-3', 'A,B,C,D,E,F', FALSE);

-- =============================================================================
-- 5. UNIDADES FÍSICAS DE AVIONES
-- =============================================================================
INSERT INTO Avion (Numero_de_Registro, ID_MODELO, Ano_de_Fabricacion, Capacidad_de_Pasajeros, Capacidad_de_Carga, Estado_de_Mantenimiento, Fecha_Proximo_Mantenimiento) VALUES
                                                                                                                                                                            ('DEF456', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Airbus A320'),  2018, 180, 15000, 'En mantenimiento', NULL),
                                                                                                                                                                            ('GHI789', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Boeing 747'),   2005, 380, 45000, 'Operativo', NULL),
                                                                                                                                                                            ('JKL012', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Airbus A350'),  2019, 310, 40000, 'Operativo', NULL),
                                                                                                                                                                            ('MNO345', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Boeing 787'),   2020, 220, 35000, 'En servicio', NULL),
                                                                                                                                                                            ('PQR678', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Embraer E195'), 2016, 120, 12000, 'Operativo', NULL),
                                                                                                                                                                            ('XYZ123', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Boeing 777'),   2014, 450, 50000, 'En servicio', NULL),
                                                                                                                                                                            ('LMN987', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Airbus A380'),  2018, 650, 75000, 'Operativo', NULL),
                                                                                                                                                                            ('STU456', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Airbus A330'),  2017, 250, 35000, 'En mantenimiento', NULL),
                                                                                                                                                                            ('WXY543', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre = 'Boeing 757'),   2003, 190, 22000, 'Operativo', NULL);

-- =============================================================================
-- 6. CAPACIDAD OPERATIVA POR CLASE
-- =============================================================================
INSERT INTO Capacidad_Clase (ID_AVION, ID_CLASE, Cantidad)
SELECT avion.ID_AVION, clase.ID_CLASE, capacidad
FROM (
         VALUES
             ('DEF456', 'Ejecutiva', 12),     ('DEF456', 'Económica', 168),
             ('GHI789', 'Primera Clase', 12), ('GHI789', 'Ejecutiva', 56),  ('GHI789', 'Económica', 312),
             ('JKL012', 'Primera Clase', 16), ('JKL012', 'Ejecutiva', 48),  ('JKL012', 'Económica', 246),
             ('MNO345', 'Primera Clase', 12), ('MNO345', 'Ejecutiva', 36),  ('MNO345', 'Económica', 172),
             ('PQR678', 'Ejecutiva', 12),     ('PQR678', 'Económica', 108),
             ('XYZ123', 'Primera Clase', 16), ('XYZ123', 'Ejecutiva', 70),  ('XYZ123', 'Económica', 364),
             ('LMN987', 'Primera Clase', 20), ('LMN987', 'Ejecutiva', 96),  ('LMN987', 'Económica', 534),
             ('STU456', 'Primera Clase', 8),  ('STU456', 'Ejecutiva', 36),  ('STU456', 'Económica', 206),
             ('WXY543', 'Ejecutiva', 16),     ('WXY543', 'Económica', 174)
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

INSERT INTO Tarifa (Nombre) VALUES
                                ('Básica'),
                                ('Flexible'),
                                ('Premium');

-- Características
INSERT INTO Caracteristica_Tarifa (Nombre, Descripcion, Tipo_Dato) VALUES
                                                                       ('Permite Cambios Asiento', 'Permite cambiar los asiento luego de la compra', 'boolean'),
                                                                       ('Horas Minimas Cambio Asiento', 'Horas mínimas antes del vuelo para cambiar', 'int'),
                                                                       ('Permite Cancelacion', 'Permite cancelar la reserva', 'boolean');

-- Tarifa Básica
INSERT INTO Tarifa_Caracteristica (ID_TARIFA, ID_CARACTERISTICA, Valor_Bool) VALUES
                                                                                 (1, 1, FALSE),
                                                                                 (1, 3, TRUE);

-- Tarifa Flexible
INSERT INTO Tarifa_Caracteristica (ID_TARIFA, ID_CARACTERISTICA, Valor_Bool) VALUES
                                                                                 (2, 1, TRUE),
                                                                                 (2, 3, TRUE);
INSERT INTO Tarifa_Caracteristica (ID_TARIFA, ID_CARACTERISTICA, Valor_Int) VALUES
    (2, 2, 48);

-- Tarifa Premium
INSERT INTO Tarifa_Caracteristica (ID_TARIFA, ID_CARACTERISTICA, Valor_Bool) VALUES
                                                                                 (3, 1, TRUE),
                                                                                 (3, 3, TRUE);
INSERT INTO Tarifa_Caracteristica (ID_TARIFA, ID_CARACTERISTICA, Valor_Int) VALUES
    (3, 2, 24);

-- Insertar vuelos
TRUNCATE TABLE Asignacion_Puerta, Segmento_Vuelo, Precio_Asiento, Vuelo CASCADE;

-- Insertar vuelos con los RUTs de los pilotos correctos y estados de vuelo
-- =========================================================================
-- POBLADO DE VUELOS OPERADOS EXCLUSIVAMENTE POR SKYWAY AIRLINES (ID_AEROLINEA = 1)
-- =========================================================================
INSERT INTO Vuelo (ID_VUELO, Numero_Vuelo, Fecha_Hora_Salida, Fecha_Hora_Llegada, ID_ESTADO_VUELO, ID_AVION, RUT_PILOTO, ID_AEROLINEA) VALUES
   (nextval('vuelo_seq'), 'SW8117', '2025-08-01 14:30', '2025-08-01 17:45', 1, 1, '12345678-9', 1),
   (nextval('vuelo_seq'), 'SW8118', '2025-08-01 18:55', '2025-08-01 21:30', 1, 1, '12345678-9', 1),
   (nextval('vuelo_seq'), 'SW8180', '2025-08-01 22:50', '2025-08-02 07:35', 1, 2, '98765432-1', 1),
   (nextval('vuelo_seq'), 'SW8989', '2025-08-02 09:55', '2025-08-02 12:50', 1, 3, '98765432-1', 1),
   (nextval('vuelo_seq'), 'SW650',  '2025-08-01 07:50', '2025-08-01 10:40', 1, 1, '12345678-9', 1),
   (nextval('vuelo_seq'), 'SW2482', '2025-08-01 12:00', '2025-08-01 20:15', 1, 3, '12345678-9', 1),
   (nextval('vuelo_seq'), 'SW8954', '2025-08-01 22:30', '2025-08-02 01:06', 1, 1, '98765432-1', 1),
   (nextval('vuelo_seq'), 'SW8120', '2025-08-02 15:00', '2025-08-02 17:30', 1, 2, '12345678-9', 1),
   (nextval('vuelo_seq'), 'SW8130', '2025-08-02 19:00', '2025-08-02 21:45', 1, 1, '98765432-1', 1),
   (nextval('vuelo_seq'), 'SW8140', '2025-08-02 22:30', '2025-08-03 01:30', 1, 3, '12345678-9', 1),
   (nextval('vuelo_seq'), 'SW8150', '2025-08-03 03:00', '2025-08-03 06:00', 1, 2, '98765432-1', 1),
   (nextval('vuelo_seq'), 'SW8160', '2025-08-03 08:00', '2025-08-03 10:30', 1, 1, '12345678-9', 1),
   (nextval('vuelo_seq'), 'SW8170', '2025-08-03 11:30', '2025-08-03 14:00', 1, 3, '98765432-1', 1),
   (nextval('vuelo_seq'), 'SW8185', '2025-08-03 15:00', '2025-08-03 20:30', 1, 2, '12345678-9', 1),
   (nextval('vuelo_seq'), 'SW9000', '2025-08-08 06:00', '2025-08-08 13:00', 1, 1, '12345678-9', 1),
   (nextval('vuelo_seq'), 'SW9001', '2025-08-08 15:00', '2025-08-08 22:00', 1, 3, '98765432-1', 1),
   (nextval('vuelo_seq'), 'SW9002', '2025-08-09 06:00', '2025-08-09 10:00', 1, 2, '12345678-9', 1),
   (nextval('vuelo_seq'), 'SW9100', '2025-08-03 15:00', '2025-08-03 19:30', 1, 1, '12345678-9', 1),
   (nextval('vuelo_seq'), 'SW9200', '2025-08-04 08:00', '2025-08-04 11:30', 1, 1, '12345678-9', 1),
   (nextval('vuelo_seq'), 'SW9201', '2025-08-04 13:00', '2025-08-04 16:00', 1, 2, '98765432-1', 1),
   (nextval('vuelo_seq'), 'SW9202', '2025-08-04 18:00', '2025-08-04 21:00', 1, 3, '12345678-9', 1),
   (nextval('vuelo_seq'), 'SW9900', '2025-08-01 23:55', '2025-08-02 09:30', 1, 1, '12345678-9', 1),
   (nextval('vuelo_seq'), 'SW9901', '2025-08-01 09:00', '2025-08-01 15:00', 1, 2, '98765432-1', 1),
   (nextval('vuelo_seq'), 'SW9902', '2025-08-01 17:00', '2025-08-01 22:00', 1, 2, '12345678-9', 1),
   (nextval('vuelo_seq'), 'SW9400', '2025-08-10 08:00', '2025-08-10 15:00', 1, 2, '98765432-1', 1),
   (nextval('vuelo_seq'), 'SW9500', '2025-08-10 17:00', '2025-08-10 23:30', 1, 2, '98765432-1', 1);


INSERT INTO Precio_Asiento (ID_VUELO, ID_CLASE, PRECIO)
SELECT
    v.ID_VUELO,
    c.ID_CLASE,
    CASE
        WHEN c.Descripcion = 'Económica' THEN 150
        WHEN c.Descripcion = 'Ejecutiva' THEN 350
        ELSE 700
        END as PRECIO
FROM Vuelo v
         CROSS JOIN Clase_asiento c;

-- =========================================================================
-- POBLADO DE SEGMENTOS DE VUELO (ORDEN AUTOMÁTICO POR TRIGGER)
-- =========================================================================

INSERT INTO Segmento_Vuelo (ID_SEGMENTO, ID_VUELO, ID_AEROPUERTO_ORIGEN, ID_AEROPUERTO_DESTINO, HORA_SALIDA, HORA_LLEGADA) VALUES
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW8117'), 1, 2, now() + interval '1 day 14 hours 30 min', now() + interval '1 day 17 hours 45 min'), -- SCL -> MVD
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW8118'), 2, 3, now() + interval '1 day 18 hours 55 min', now() + interval '1 day 21 hours 30 min'), -- MVD -> GRU
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW8180'), 3, 4, now() + interval '1 day 22 hours 50 min', now() + interval '2 day 7 hours 35 min'),  -- GRU -> JFK
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW8989'), 4, 5, now() + interval '2 day 9 hours 55 min', now() + interval '2 day 12 hours 50 min'), -- JFK -> LAX
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW650'),  1, 6, now() + interval '1 day 7 hours 50 min', now() + interval '1 day 10 hours 40 min'), -- SCL -> LIM
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW2482'), 6, 7, now() + interval '1 day 12 hours', now() + interval '1 day 20 hours 15 min'),           -- LIM -> ATL
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW8954'), 7, 4, now() + interval '1 day 22 hours 30 min', now() + interval '2 day 1 hours 6 min'),   -- ATL -> JFK
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW8120'), 2, 1, now() + interval '2 day 15 hours', now() + interval '2 day 17 hours 30 min'),           -- MVD -> SCL
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW8130'), 3, 2, now() + interval '2 day 19 hours', now() + interval '2 day 21 hours 45 min'),           -- GRU -> MVD
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW8140'), 5, 7, now() + interval '2 day 22 hours 30 min', now() + interval '3 day 1 hours 30 min'),-- LAX -> ATL
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW8150'), 7, 2, now() + interval '3 day 3 hours', now() + interval '3 day 6 hours'),                       -- ATL -> MVD
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW8160'), 1, 2, now() + interval '3 day 8 hours', now() + interval '3 day 10 hours 30 min'),           -- SCL -> MVD
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW8170'), 2, 3, now() + interval '3 day 11 hours 30 min', now() + interval '3 day 14 hours'),          -- MVD -> GRU

                                                                                                                               -- Multi-segmento para el vuelo de largo alcance SW8185 (El trigger manejará el orden 1, 2 y 3 correlativamente)
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW8185'), 3, 8, now() + interval '3 day 15 hours', now() + interval '3 day 18 hours 30 min'),          -- Tramo 1: GRU -> BOG
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW8185'), 8, 9, now() + interval '3 day 19 hours 15 min', now() + interval '3 day 21 hours 45 min'),-- Tramo 2: BOG -> MIA
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW8185'), 9, 4, now() + interval '3 day 22 hours 30 min', now() + interval '4 day 1 hours'),           -- Tramo 3: MIA -> JFK

                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW9000'), 5, 7, now() + interval '8 day 6 hours', now() + interval '8 day 13 hours'),                      -- LAX -> ATL
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW9001'), 7, 6, now() + interval '8 day 15 hours', now() + interval '8 day 22 hours'),                     -- ATL -> LIM
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW9002'), 6, 1, now() + interval '9 day 6 hours', now() + interval '9 day 10 hours'),                      -- LIM -> SCL
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW9100'), 9, 1, now() + interval '3 day 15 hours', now() + interval '3 day 19 hours 30 min'),          -- MIA -> SCL
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW9200'), 3, 8, now() + interval '4 day 8 hours', now() + interval '4 day 11 hours 30 min'),           -- GRU -> BOG
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW9201'), 8, 9, now() + interval '4 day 13 hours', now() + interval '4 day 16 hours'),                      -- BOG -> MIA
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW9202'), 9, 4, now() + interval '4 day 18 hours', now() + interval '4 day 21 hours'),                      -- MIA -> JFK
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW9900'), 1, 4, now() + interval '1 day 23 hours 55 min', now() + interval '2 day 9 hours 30 min'),     -- SCL -> JFK
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW9901'), 1, 8, now() + interval '1 day 9 hours', now() + interval '1 day 15 hours'),                  -- SCL -> BOG
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW9902'), 8, 4, now() + interval '1 day 17 hours', now() + interval '1 day 22 hours'),                  -- BOG -> JFK
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW9400'), 5, 9, now() + interval '3 day 8 hours', now() + interval '3 day 15 hours'),                  -- LAX -> MIA
                                                                                                                               (nextval('segmento_vuelo_seq'), (SELECT ID_VUELO FROM Vuelo WHERE Numero_Vuelo = 'SW9500'), 9, 1, now() + interval '3 day 17 hours', now() + interval '3 day 23 hours 30 min');            -- MIA -> SCL


INSERT INTO Asignacion_Puerta (ID_SEGMENTO, ID_PUERTA) VALUES
                                                           -- SW8117 SCL -> MVD usa la puerta SCL-01
                                                           ((SELECT sg.ID_SEGMENTO FROM Segmento_Vuelo sg JOIN Vuelo v ON sg.ID_VUELO = v.ID_VUELO WHERE v.Numero_Vuelo = 'SW8117' AND sg.ORDEN_SEGMENTO = 1),
                                                            (SELECT ID_PUERTA FROM Puerta_Embarque WHERE Codigo_Puerta = 'SCL-01')),

                                                           -- SW8118 MVD -> GRU usa la puerta MVD-02
                                                           ((SELECT sg.ID_SEGMENTO FROM Segmento_Vuelo sg JOIN Vuelo v ON sg.ID_VUELO = v.ID_VUELO WHERE v.Numero_Vuelo = 'SW8118' AND sg.ORDEN_SEGMENTO = 1),
                                                            (SELECT ID_PUERTA FROM Puerta_Embarque WHERE Codigo_Puerta = 'MVD-02')),

                                                           -- SW8180 GRU -> JFK usa la puerta GRU-01
                                                           ((SELECT sg.ID_SEGMENTO FROM Segmento_Vuelo sg JOIN Vuelo v ON sg.ID_VUELO = v.ID_VUELO WHERE v.Numero_Vuelo = 'SW8180' AND sg.ORDEN_SEGMENTO = 1),
                                                            (SELECT ID_PUERTA FROM Puerta_Embarque WHERE Codigo_Puerta = 'GRU-01')),

                                                           -- SW8989 JFK -> LAX usa la puerta JFK-03
                                                           ((SELECT sg.ID_SEGMENTO FROM Segmento_Vuelo sg JOIN Vuelo v ON sg.ID_VUELO = v.ID_VUELO WHERE v.Numero_Vuelo = 'SW8989' AND sg.ORDEN_SEGMENTO = 1),
                                                            (SELECT ID_PUERTA FROM Puerta_Embarque WHERE Codigo_Puerta = 'JFK-03')),

                                                           -- SW650 SCL -> LIM usa la puerta SCL-02
                                                           ((SELECT sg.ID_SEGMENTO FROM Segmento_Vuelo sg JOIN Vuelo v ON sg.ID_VUELO = v.ID_VUELO WHERE v.Numero_Vuelo = 'SW650' AND sg.ORDEN_SEGMENTO = 1),
                                                            (SELECT ID_PUERTA FROM Puerta_Embarque WHERE Codigo_Puerta = 'SCL-02')),

                                                           -- SW2482 LIM -> ATL usa la puerta LIM-02
                                                           ((SELECT sg.ID_SEGMENTO FROM Segmento_Vuelo sg JOIN Vuelo v ON sg.ID_VUELO = v.ID_VUELO WHERE v.Numero_Vuelo = 'SW2482' AND sg.ORDEN_SEGMENTO = 1),
                                                            (SELECT ID_PUERTA FROM Puerta_Embarque WHERE Codigo_Puerta = 'LIM-02')),

                                                           -- SW8954 ATL -> JFK usa la puerta ATL-01
                                                           ((SELECT sg.ID_SEGMENTO FROM Segmento_Vuelo sg JOIN Vuelo v ON sg.ID_VUELO = v.ID_VUELO WHERE v.Numero_Vuelo = 'SW8954' AND sg.ORDEN_SEGMENTO = 1),
                                                            (SELECT ID_PUERTA FROM Puerta_Embarque WHERE Codigo_Puerta = 'ATL-01')),

                                                           -- SW8160 SCL -> MVD usa la puerta SCL-03
                                                           ((SELECT sg.ID_SEGMENTO FROM Segmento_Vuelo sg JOIN Vuelo v ON sg.ID_VUELO = v.ID_VUELO WHERE v.Numero_Vuelo = 'SW8160' AND sg.ORDEN_SEGMENTO = 1),
                                                            (SELECT ID_PUERTA FROM Puerta_Embarque WHERE Codigo_Puerta = 'SCL-03')),

                                                           -- SW8170 MVD -> GRU usa la puerta MVD-01
                                                           ((SELECT sg.ID_SEGMENTO FROM Segmento_Vuelo sg JOIN Vuelo v ON sg.ID_VUELO = v.ID_VUELO WHERE v.Numero_Vuelo = 'SW8170' AND sg.ORDEN_SEGMENTO = 1),
                                                            (SELECT ID_PUERTA FROM Puerta_Embarque WHERE Codigo_Puerta = 'MVD-01')),

                                                           -- SW8185 tramo multi-segmento (BOG -> MIA) usa la puerta BOG-01
                                                           ((SELECT sg.ID_SEGMENTO FROM Segmento_Vuelo sg JOIN Vuelo v ON sg.ID_VUELO = v.ID_VUELO WHERE v.Numero_Vuelo = 'SW8185' AND sg.ORDEN_SEGMENTO = 2),
                                                            (SELECT ID_PUERTA FROM Puerta_Embarque WHERE Codigo_Puerta = 'BOG-01')),

                                                           -- SW9400 LAX -> MIA usa la puerta LAX-04
                                                           ((SELECT sg.ID_SEGMENTO FROM Segmento_Vuelo sg JOIN Vuelo v ON sg.ID_VUELO = v.ID_VUELO WHERE v.Numero_Vuelo = 'SW9400' AND sg.ORDEN_SEGMENTO = 1),
                                                            (SELECT ID_PUERTA FROM Puerta_Embarque WHERE Codigo_Puerta = 'LAX-04')),

                                                           -- SW9500 MIA -> SCL usa la puerta MIA-02
                                                           ((SELECT sg.ID_SEGMENTO FROM Segmento_Vuelo sg JOIN Vuelo v ON sg.ID_VUELO = v.ID_VUELO WHERE v.Numero_Vuelo = 'SW9500' AND sg.ORDEN_SEGMENTO = 1),
                                                            (SELECT ID_PUERTA FROM Puerta_Embarque WHERE Codigo_Puerta = 'MIA-02'));



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


INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (1, CURRENT_DATE, '2025-12-10 08:00:00', '2025-12-10 12:00:00', 1);
INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (2, CURRENT_DATE, '2025-12-11 08:00:00', '2025-12-11 12:00:00', 1);
INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (3, CURRENT_DATE, '2025-12-12 08:00:00', '2025-12-12 12:00:00', 1);
INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (4, CURRENT_DATE, '2025-12-13 08:00:00', '2025-12-13 12:00:00', 1);
INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (5, CURRENT_DATE, '2025-12-14 08:00:00', '2025-12-14 12:00:00', 1);
INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (6, CURRENT_DATE, '2025-12-15 08:00:00', '2025-12-15 12:00:00', 1);
INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (7, CURRENT_DATE, '2025-12-16 08:00:00', '2025-12-16 12:00:00', 1);
INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (8, CURRENT_DATE, '2025-12-17 08:00:00', '2025-12-17 12:00:00', 1);
INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (9, CURRENT_DATE, '2025-12-18 08:00:00', '2025-12-18 12:00:00', 1);
INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (10, CURRENT_DATE, '2025-12-19 08:00:00', '2025-12-19 12:00:00', 1);
INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (11, CURRENT_DATE, '2025-12-20 08:00:00', '2025-12-20 12:00:00', 1);
INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (12, CURRENT_DATE, '2025-12-21 08:00:00', '2025-12-21 12:00:00', 1);
INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (13, CURRENT_DATE, '2025-12-22 08:00:00', '2025-12-22 12:00:00', 1);
INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (14, CURRENT_DATE, '2025-12-23 08:00:00', '2025-12-23 12:00:00', 1);
INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (15, CURRENT_DATE, '2025-12-24 08:00:00', '2025-12-24 12:00:00', 1);
INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (16, CURRENT_DATE, '2025-12-25 08:00:00', '2025-12-25 12:00:00', 1);
INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (17, CURRENT_DATE, '2025-12-26 08:00:00', '2025-12-26 12:00:00', 1);
INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (18, CURRENT_DATE, '2025-12-27 08:00:00', '2025-12-27 12:00:00', 1);
INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (19, CURRENT_DATE, '2025-12-28 08:00:00', '2025-12-28 12:00:00', 1);
INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (20, CURRENT_DATE, '2025-12-29 08:00:00', '2025-12-29 12:00:00', 1);
INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (21, CURRENT_DATE, '2025-12-30 08:00:00', '2025-12-30 12:00:00', 1);
INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (22, CURRENT_DATE, '2025-12-31 08:00:00', '2025-12-31 12:00:00', 1);
INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (23, CURRENT_DATE, '2026-01-01 08:00:00', '2026-01-01 12:00:00', 1);
INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (24, CURRENT_DATE, '2026-01-02 08:00:00', '2026-01-02 12:00:00', 1);
INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (25, CURRENT_DATE, '2026-01-03 08:00:00', '2026-01-03 12:00:00', 1);
INSERT INTO Turno (ID_VUELO, Fecha, Hora_Inicio, Hora_Fin, ID_TIPO_TURNO) VALUES (26, CURRENT_DATE, '2026-01-04 08:00:00', '2026-01-04 12:00:00', 1);





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



/*-- Ejemplo de asignación de asientos (asumiendo IDs de asiento disponibles)
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
(3, 21, 1766);*/




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

SELECT
    v.Numero_Vuelo,
    sg.ID_SEGMENTO,
    sg.ORDEN_SEGMENTO,
    a1.Codigo_IATA as Origen,
    a2.Codigo_IATA as Destino
FROM Segmento_Vuelo sg
         JOIN Vuelo v ON sg.ID_VUELO = v.ID_VUELO
         JOIN Aeropuerto a1 ON sg.ID_AEROPUERTO_ORIGEN = a1.ID_AEROPUERTO
         JOIN Aeropuerto a2 ON sg.ID_AEROPUERTO_DESTINO = a2.ID_AEROPUERTO
ORDER BY v.Numero_Vuelo, sg.ORDEN_SEGMENTO;



SELECT
    c1.nombre || '-' || aprt1.nombre_aeropuerto || ' ' || aprt1.codigo_iata AS origen,
    c2.nombre || '-' || aprt2.nombre_aeropuerto || ' ' || aprt2.codigo_iata AS destino,
    it.hora_salida,
    it.hora_llegada
FROM itinerario it
         JOIN aeropuerto aprt1 ON aprt1.id_aeropuerto = it.origen_aeropuerto
         JOIN aeropuerto aprt2 ON aprt2.id_aeropuerto = it.destino_aeropuerto
         JOIN ciudad c1 ON c1.id_ciudad = aprt1.id_ciudad
         JOIN ciudad c2 ON c2.id_ciudad = aprt2.id_ciudad
WHERE it.hora_salida >= NOW()
  AND it.hora_salida < NOW() + INTERVAL '7 days'
ORDER BY it.hora_salida ASC;