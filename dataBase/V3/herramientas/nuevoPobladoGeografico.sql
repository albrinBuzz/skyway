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
