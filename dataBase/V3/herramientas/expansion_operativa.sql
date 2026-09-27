-- ============================================================
-- EXPANSIÓN OPERATIVA MASIVA DE LA AEROLÍNEA
-- Nuevos fabricantes, modelos, flota, personal y pasajeros
-- ============================================================


-- ============================================================
-- 1. FABRICANTES ADICIONALES
-- ============================================================
INSERT INTO Fabricante (Nombre) VALUES
                                    ('Bombardier'),
                                    ('ATR'),
                                    ('Cessna'),
                                    ('McDonnell Douglas'),
                                    ('Sukhoi'),
                                    ('COMAC'),
                                    ('Mitsubishi');


-- ============================================================
-- 2. MODELOS DE AVIÓN ADICIONALES
-- ============================================================
INSERT INTO Modelo_Avion (Nombre, ID_FABRICANTE) VALUES
-- Airbus
('Airbus A220',      (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Airbus')),
('Airbus A319',      (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Airbus')),
('Airbus A321',      (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Airbus')),
('Airbus A321XLR',   (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Airbus')),
('Airbus A340',      (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Airbus')),
('Airbus A350-1000', (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Airbus')),
-- Boeing
('Boeing 737-800',   (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Boeing')),
('Boeing 737 MAX 8', (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Boeing')),
('Boeing 737 MAX 10',(SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Boeing')),
('Boeing 767',       (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Boeing')),
('Boeing 777X',      (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Boeing')),
('Boeing 787-10',    (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Boeing')),
-- Embraer
('Embraer E175',     (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Embraer')),
('Embraer E190',     (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Embraer')),
-- Bombardier
('Bombardier CRJ900',(SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Bombardier')),
('Bombardier Q400',  (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Bombardier')),
-- ATR
('ATR 72-600',       (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'ATR')),
('ATR 42-600',       (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'ATR')),
-- COMAC (China)
('COMAC C919',       (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'COMAC')),
-- Sukhoi
('Sukhoi Superjet 100', (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Sukhoi')),
-- Mitsubishi
('Mitsubishi SpaceJet M90', (SELECT ID_FABRICANTE FROM Fabricante WHERE Nombre = 'Mitsubishi'));


-- ============================================================
-- 3. FLOTA: 40 AVIONES NUEVOS
-- ============================================================
-- Capacidades de pasajeros por modelo (configuración típica real):
--   A220:        130 pax   A319: 140   A321: 220   A321XLR: 220
--   A340:        295 pax   A350-1000: 369
--   737-800:     189 pax   737 MAX8: 178  737 MAX10: 204
--   767:         218 pax   777X: 426   787-10: 330
--   E175:         76 pax   E190: 100
--   CRJ900:       90 pax   Q400: 78
--   ATR 72:       70 pax   ATR 42: 50
--   C919:        168 pax   SSJ100: 98   SpaceJet: 88
-- ============================================================
INSERT INTO Avion (Numero_de_Registro, ID_MODELO, Ano_de_Fabricacion,
                   Capacidad_de_Pasajeros, Capacidad_de_Carga,
                   Estado_de_Mantenimiento, Fecha_Proximo_Mantenimiento)
VALUES
-- Airbus A220 (flota regional intercontinental)
('CC-BAA', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Airbus A220'),      2021, 130, 13000, 'Operativo',        '2026-03-15'),
('CC-BAB', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Airbus A220'),      2021, 130, 13000, 'Operativo',        '2026-04-20'),
('CC-BAC', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Airbus A220'),      2022, 130, 13000, 'Operativo',        '2026-06-10'),

-- Airbus A319 (rutas cortas)
('CC-ANA', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Airbus A319'),      2016, 140, 14000, 'Operativo',        '2026-02-28'),
('CC-ANB', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Airbus A319'),      2017, 140, 14000, 'En mantenimiento', NULL),
('CC-ANC', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Airbus A319'),      2018, 140, 14000, 'Operativo',        '2026-05-05'),

-- Airbus A321 (rutas medias alta densidad)
('CC-AUA', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Airbus A321'),      2019, 220, 22000, 'Operativo',        '2026-07-12'),
('CC-AUB', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Airbus A321'),      2020, 220, 22000, 'En servicio',      '2026-08-01'),
('CC-AUC', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Airbus A321'),      2021, 220, 22000, 'Operativo',        '2026-09-18'),

-- Airbus A321XLR (largo alcance eficiente)
('CC-XLA', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Airbus A321XLR'),   2023, 220, 22000, 'Operativo',        '2026-11-22'),
('CC-XLB', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Airbus A321XLR'),   2023, 220, 22000, 'Operativo',        '2027-01-10'),

-- Airbus A340 (largo alcance cuatrimotor)
('CC-ADA', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Airbus A340'),      2010, 295, 38000, 'Operativo',        '2026-03-01'),
('CC-ADB', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Airbus A340'),      2011, 295, 38000, 'En mantenimiento', NULL),

-- Airbus A350-1000 (ultra largo alcance)
('CC-AEA', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Airbus A350-1000'), 2022, 369, 45000, 'Operativo',        '2026-12-05'),
('CC-AEB', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Airbus A350-1000'), 2023, 369, 45000, 'Operativo',        '2027-02-14'),

-- Boeing 737-800 (workhorse regional)
('CC-BHA', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Boeing 737-800'),   2013, 189, 20000, 'Operativo',        '2026-02-15'),
('CC-BHB', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Boeing 737-800'),   2014, 189, 20000, 'Operativo',        '2026-04-01'),
('CC-BHC', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Boeing 737-800'),   2015, 189, 20000, 'En mantenimiento', NULL),
('CC-BHD', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Boeing 737-800'),   2015, 189, 20000, 'Operativo',        '2026-06-20'),

-- Boeing 737 MAX 8
('CC-MXA', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Boeing 737 MAX 8'), 2022, 178, 19000, 'Operativo',        '2026-09-30'),
('CC-MXB', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Boeing 737 MAX 8'), 2022, 178, 19000, 'Operativo',        '2026-10-15'),
('CC-MXC', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Boeing 737 MAX 8'), 2023, 178, 19000, 'Operativo',        '2027-01-20'),

-- Boeing 737 MAX 10
('CC-MXD', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Boeing 737 MAX 10'),2023, 204, 21000, 'Operativo',        '2027-03-01'),
('CC-MXE', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Boeing 737 MAX 10'),2024, 204, 21000, 'Operativo',        '2027-05-10'),

-- Boeing 767 (rutas medias-largas)
('CC-B67', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Boeing 767'),       2008, 218, 28000, 'Operativo',        '2026-03-20'),

-- Boeing 777X (flagship largo alcance)
('CC-VXA', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Boeing 777X'),      2023, 426, 52000, 'Operativo',        '2027-01-15'),
('CC-VXB', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Boeing 777X'),      2024, 426, 52000, 'Operativo',        '2027-06-01'),

-- Boeing 787-10
('CC-B8A', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Boeing 787-10'),    2021, 330, 42000, 'Operativo',        '2026-11-01'),
('CC-B8B', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Boeing 787-10'),    2022, 330, 42000, 'Operativo',        '2027-02-01'),

-- Embraer E175 (rutas regionales cortas)
('CC-E7A', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Embraer E175'),     2019,  76,  7000, 'Operativo',        '2026-05-20'),
('CC-E7B', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Embraer E175'),     2020,  76,  7000, 'Operativo',        '2026-07-08'),

-- Embraer E190
('CC-E9A', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Embraer E190'),     2018, 100,  9000, 'Operativo',        '2026-04-15'),
('CC-E9B', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Embraer E190'),     2019, 100,  9000, 'En mantenimiento', NULL),

-- Bombardier CRJ900 (rutas cortas regionales)
('CC-CRA', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Bombardier CRJ900'),2017,  90,  8000, 'Operativo',        '2026-03-10'),
('CC-CRB', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='Bombardier CRJ900'),2018,  90,  8000, 'Operativo',        '2026-06-05'),

-- ATR 72-600 (rutas domésticas / isla)
('CC-ATA', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='ATR 72-600'),       2020,  70,  6500, 'Operativo',        '2026-02-20'),
('CC-ATB', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='ATR 72-600'),       2021,  70,  6500, 'Operativo',        '2026-05-15'),
('CC-ATC', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='ATR 72-600'),       2022,  70,  6500, 'Operativo',        '2026-08-30'),

-- ATR 42-600
('CC-ATD', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='ATR 42-600'),       2021,  50,  5000, 'Operativo',        '2026-06-01'),

-- COMAC C919
('CC-C19', (SELECT ID_MODELO FROM Modelo_Avion WHERE Nombre='COMAC C919'),       2023, 168, 18000, 'Operativo',        '2026-12-01');


-- ============================================================
-- 4. CAPACIDAD POR CLASE PARA LA FLOTA NUEVA
-- ============================================================
INSERT INTO Capacidad_Clase (ID_AVION, ID_CLASE, Cantidad)
SELECT av.ID_AVION, cl.ID_CLASE, datos.cantidad
FROM (VALUES
          -- A220
          ('CC-BAA','Económica',110), ('CC-BAA','Ejecutiva',15), ('CC-BAA','Primera Clase',5),
          ('CC-BAB','Económica',110), ('CC-BAB','Ejecutiva',15), ('CC-BAB','Primera Clase',5),
          ('CC-BAC','Económica',110), ('CC-BAC','Ejecutiva',15), ('CC-BAC','Primera Clase',5),
          -- A319
          ('CC-ANA','Económica',120), ('CC-ANA','Ejecutiva',16), ('CC-ANA','Primera Clase',4),
          ('CC-ANB','Económica',120), ('CC-ANB','Ejecutiva',16), ('CC-ANB','Primera Clase',4),
          ('CC-ANC','Económica',120), ('CC-ANC','Ejecutiva',16), ('CC-ANC','Primera Clase',4),
          -- A321
          ('CC-AUA','Económica',185), ('CC-AUA','Ejecutiva',28), ('CC-AUA','Primera Clase',7),
          ('CC-AUB','Económica',185), ('CC-AUB','Ejecutiva',28), ('CC-AUB','Primera Clase',7),
          ('CC-AUC','Económica',185), ('CC-AUC','Ejecutiva',28), ('CC-AUC','Primera Clase',7),
          -- A321XLR
          ('CC-XLA','Económica',182), ('CC-XLA','Ejecutiva',30), ('CC-XLA','Primera Clase',8),
          ('CC-XLB','Económica',182), ('CC-XLB','Ejecutiva',30), ('CC-XLB','Primera Clase',8),
          -- A340
          ('CC-ADA','Económica',240), ('CC-ADA','Ejecutiva',40), ('CC-ADA','Primera Clase',15),
          ('CC-ADB','Económica',240), ('CC-ADB','Ejecutiva',40), ('CC-ADB','Primera Clase',15),
          -- A350-1000
          ('CC-AEA','Económica',300), ('CC-AEA','Ejecutiva',50), ('CC-AEA','Primera Clase',19),
          ('CC-AEB','Económica',300), ('CC-AEB','Ejecutiva',50), ('CC-AEB','Primera Clase',19),
          -- 737-800
          ('CC-BHA','Económica',162), ('CC-BHA','Ejecutiva',20), ('CC-BHA','Primera Clase',7),
          ('CC-BHB','Económica',162), ('CC-BHB','Ejecutiva',20), ('CC-BHB','Primera Clase',7),
          ('CC-BHC','Económica',162), ('CC-BHC','Ejecutiva',20), ('CC-BHC','Primera Clase',7),
          ('CC-BHD','Económica',162), ('CC-BHD','Ejecutiva',20), ('CC-BHD','Primera Clase',7),
          -- 737 MAX 8
          ('CC-MXA','Económica',150), ('CC-MXA','Ejecutiva',22), ('CC-MXA','Primera Clase',6),
          ('CC-MXB','Económica',150), ('CC-MXB','Ejecutiva',22), ('CC-MXB','Primera Clase',6),
          ('CC-MXC','Económica',150), ('CC-MXC','Ejecutiva',22), ('CC-MXC','Primera Clase',6),
          -- 737 MAX 10
          ('CC-MXD','Económica',174), ('CC-MXD','Ejecutiva',24), ('CC-MXD','Primera Clase',6),
          ('CC-MXE','Económica',174), ('CC-MXE','Ejecutiva',24), ('CC-MXE','Primera Clase',6),
          -- 767
          ('CC-B67','Económica',180), ('CC-B67','Ejecutiva',28), ('CC-B67','Primera Clase',10),
          -- 777X
          ('CC-VXA','Económica',350), ('CC-VXA','Ejecutiva',55), ('CC-VXA','Primera Clase',21),
          ('CC-VXB','Económica',350), ('CC-VXB','Ejecutiva',55), ('CC-VXB','Primera Clase',21),
          -- 787-10
          ('CC-B8A','Económica',271), ('CC-B8A','Ejecutiva',44), ('CC-B8A','Primera Clase',15),
          ('CC-B8B','Económica',271), ('CC-B8B','Ejecutiva',44), ('CC-B8B','Primera Clase',15),
          -- E175
          ('CC-E7A','Económica',64),  ('CC-E7A','Ejecutiva',10), ('CC-E7A','Primera Clase',2),
          ('CC-E7B','Económica',64),  ('CC-E7B','Ejecutiva',10), ('CC-E7B','Primera Clase',2),
          -- E190
          ('CC-E9A','Económica',84),  ('CC-E9A','Ejecutiva',12), ('CC-E9A','Primera Clase',4),
          ('CC-E9B','Económica',84),  ('CC-E9B','Ejecutiva',12), ('CC-E9B','Primera Clase',4),
          -- CRJ900
          ('CC-CRA','Económica',78),  ('CC-CRA','Ejecutiva',10), ('CC-CRA','Primera Clase',2),
          ('CC-CRB','Económica',78),  ('CC-CRB','Ejecutiva',10), ('CC-CRB','Primera Clase',2),
          -- ATR 72
          ('CC-ATA','Económica',64),  ('CC-ATA','Ejecutiva',6),  ('CC-ATA','Primera Clase',0),
          ('CC-ATB','Económica',64),  ('CC-ATB','Ejecutiva',6),  ('CC-ATB','Primera Clase',0),
          ('CC-ATC','Económica',64),  ('CC-ATC','Ejecutiva',6),  ('CC-ATC','Primera Clase',0),
          -- ATR 42
          ('CC-ATD','Económica',44),  ('CC-ATD','Ejecutiva',6),  ('CC-ATD','Primera Clase',0),
          -- C919
          ('CC-C19','Económica',140), ('CC-C19','Ejecutiva',22), ('CC-C19','Primera Clase',6)
     ) AS datos(registro, clase, cantidad)
         JOIN Avion av ON av.Numero_de_Registro = datos.registro
         JOIN Clase_asiento cl ON cl.Descripcion = datos.clase
WHERE datos.cantidad > 0;


-- ============================================================
-- 5. PILOTOS ADICIONALES (25 pilotos nuevos, internacionales)
-- ============================================================
INSERT INTO Usuario (RUT, Nombre, Apellido, Correo_Electronico, Telefono,
                     Documento_Identidad, Fecha_Nacimiento, Contrasena, Fecha_Registro)
VALUES
    ('30100001-1','Alejandro','Vidal',       'a.vidal@piloto.aerolinea.com',      '+56911000001','PIL-CL-001','1977-03-12','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30100002-2','Valentina','Núñez',       'v.nunez@piloto.aerolinea.com',      '+56911000002','PIL-CL-002','1982-07-08','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30100003-3','Rodrigo','Espinoza',      'r.espinoza@piloto.aerolinea.com',   '+56911000003','PIL-CL-003','1979-11-25','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30100004-4','Claudia','Herrera',       'c.herrera@piloto.aerolinea.com',    '+56911000004','PIL-CL-004','1984-04-18','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30100005-5','Felipe','Araya',          'f.araya@piloto.aerolinea.com',      '+56911000005','PIL-CL-005','1973-09-02','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30100006-6','Gonzalo','Ibáñez',        'g.ibanez@piloto.aerolinea.com',     '+56911000006','PIL-CL-006','1986-01-30','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30100007-7','Isidora','Tapia',         'i.tapia@piloto.aerolinea.com',      '+56911000007','PIL-CL-007','1990-06-14','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30100008-8','Sebastián','Morales',     's.morales@piloto.aerolinea.com',    '+56911000008','PIL-CL-008','1981-10-07','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30100009-9','Pilar','Castillo',        'p.castillo@piloto.aerolinea.com',   '+56911000009','PIL-CL-009','1975-02-20','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30100010-K','Cristóbal','Lagos',       'c.lagos@piloto.aerolinea.com',      '+56911000010','PIL-CL-010','1988-08-16','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Pilotos internacionales
    ('30100011-1','Marco','Rossi',           'm.rossi@piloto.aerolinea.com',      '+39322000011','PIL-IT-001','1976-05-03','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30100012-2','Claire','Dubois',         'c.dubois@piloto.aerolinea.com',     '+33622000012','PIL-FR-001','1983-12-19','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30100013-3','James','Thompson',        'j.thompson@piloto.aerolinea.com',   '+44722000013','PIL-UK-001','1978-07-11','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30100014-4','Hans','Müller',           'h.muller@piloto.aerolinea.com',     '+49522000014','PIL-DE-001','1974-03-28','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30100015-5','Sofia','Andersen',        's.andersen@piloto.aerolinea.com',   '+47922000015','PIL-NO-001','1987-09-05','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30100016-6','Hiroshi','Tanaka',        'h.tanaka@piloto.aerolinea.com',     '+81322000016','PIL-JP-001','1980-11-22','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30100017-7','Ahmed','Al-Rashid',       'a.alrashid@piloto.aerolinea.com',   '+97122000017','PIL-AE-001','1985-04-14','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30100018-8','Priya','Sharma',          'p.sharma@piloto.aerolinea.com',     '+91922000018','PIL-IN-001','1989-08-30','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30100019-9','Lucas','Oliveira',        'l.oliveira@piloto.aerolinea.com',   '+55922000019','PIL-BR-001','1977-01-17','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30100020-K','Emma','van der Berg',     'e.vanderberg@piloto.aerolinea.com', '+31622000020','PIL-NL-001','1982-06-08','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30100021-1','Dimitri','Papadopoulos',  'd.papadopoulos@piloto.aerolinea.com','+30622000021','PIL-GR-001','1971-10-01','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30100022-2','Amara','Diallo',          'a.diallo@piloto.aerolinea.com',     '+22122000022','PIL-SN-001','1986-03-25','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30100023-3','Wei','Zhang',             'w.zhang@piloto.aerolinea.com',      '+86222000023','PIL-CN-001','1979-07-19','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30100024-4','Fatima','Al-Sayed',       'f.alsayed@piloto.aerolinea.com',    '+96522000024','PIL-KW-001','1984-12-03','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30100025-5','Ivan','Petrov',           'i.petrov@piloto.aerolinea.com',     '+79222000025','PIL-RU-001','1975-05-28','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW());

-- Rol Piloto (id_rol = 3) a todos
INSERT INTO RolUsuario (rut_usuario, id_rol)
SELECT RUT, 3 FROM Usuario
WHERE RUT LIKE '30100%';

-- Detalles en tabla Piloto
INSERT INTO Piloto (RUT, Licencia, Experiencia_anos, Especializaciones) VALUES
                                                                            ('30100001-1','ATPL-CL-001',22,'Largo alcance, A350, B787'),
                                                                            ('30100002-2','ATPL-CL-002',17,'Rutas internacionales, A321, A320'),
                                                                            ('30100003-3','ATPL-CL-003',20,'Transoceánico, B777, B747'),
                                                                            ('30100004-4','ATPL-CL-004',15,'Rutas regionales, A319, A220'),
                                                                            ('30100005-5','ATPL-CL-005',26,'Largo alcance, B777X, A350-1000'),
                                                                            ('30100006-6','ATPL-CL-006',13,'Rutas continentales, B737 MAX'),
                                                                            ('30100007-7','ATPL-CL-007',9, 'Rutas cortas, E195, CRJ900'),
                                                                            ('30100008-8','ATPL-CL-008',18,'Cargo y pasajeros, B767, A330'),
                                                                            ('30100009-9','ATPL-CL-009',24,'Transoceánico, A380, B747'),
                                                                            ('30100010-K','ATPL-CL-010',11,'Rutas nacionales, ATR 72, E175'),
                                                                            ('30100011-1','ATPL-IT-001',23,'Largo alcance, A340, A350'),
                                                                            ('30100012-2','ATPL-FR-001',16,'Rutas europeas, A320, A321XLR'),
                                                                            ('30100013-3','ATPL-UK-001',21,'Transoceánico, B777, B787-10'),
                                                                            ('30100014-4','ATPL-DE-001',25,'Largo alcance, A380, B747'),
                                                                            ('30100015-5','ATPL-NO-001',12,'Rutas nórdicas, B737-800, A220'),
                                                                            ('30100016-6','ATPL-JP-001',19,'Rutas asiáticas, B777X, B787'),
                                                                            ('30100017-7','ATPL-AE-001',14,'Rutas Oriente Medio, A350, B777'),
                                                                            ('30100018-8','ATPL-IN-001',10,'Rutas subcontinente, A321, B737 MAX'),
                                                                            ('30100019-9','ATPL-BR-001',22,'Rutas Sudamérica, A320, E190'),
                                                                            ('30100020-K','ATPL-NL-001',17,'Rutas Europa-Asia, B787-10, A350'),
                                                                            ('30100021-1','ATPL-GR-001',28,'Veterano largo alcance, B747, A340'),
                                                                            ('30100022-2','ATPL-SN-001',13,'Rutas África, A321, B737-800'),
                                                                            ('30100023-3','ATPL-CN-001',20,'Rutas Asia-Pacífico, C919, B787'),
                                                                            ('30100024-4','ATPL-KW-001',15,'Rutas Golfo Pérsico, A321XLR, B777'),
                                                                            ('30100025-5','ATPL-RU-001',24,'Largo alcance, B777X, A350-1000');


-- ============================================================
-- 6. TRIPULACIÓN ADICIONAL (30 miembros)
-- ============================================================
INSERT INTO Usuario (RUT, Nombre, Apellido, Correo_Electronico, Telefono,
                     Documento_Identidad, Fecha_Nacimiento, Contrasena, Fecha_Registro)
VALUES
    ('30200001-1','Sofía','Navarrete',   's.navarrete@tripulacion.aerolinea.com', '+56911200001','TRP-CL-001','1995-02-10','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200002-2','Ignacio','Bravo',     'i.bravo@tripulacion.aerolinea.com',     '+56911200002','TRP-CL-002','1993-07-22','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200003-3','Catalina','Soto',     'c.soto@tripulacion.aerolinea.com',      '+56911200003','TRP-CL-003','1997-11-05','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200004-4','Diego','Pizarro',     'd.pizarro@tripulacion.aerolinea.com',   '+56911200004','TRP-CL-004','1990-04-17','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200005-5','Constanza','Leiva',   'c.leiva@tripulacion.aerolinea.com',     '+56911200005','TRP-CL-005','1996-09-30','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200006-6','Matías','Rojas',      'm.rojas@tripulacion.aerolinea.com',     '+56911200006','TRP-CL-006','1994-01-14','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200007-7','Fernanda','Urrutia',  'f.urrutia@tripulacion.aerolinea.com',   '+56911200007','TRP-CL-007','1998-06-28','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200008-8','Cristóbal','Meza',    'c.meza@tripulacion.aerolinea.com',      '+56911200008','TRP-CL-008','1992-12-03','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200009-9','Javiera','Cornejo',   'j.cornejo@tripulacion.aerolinea.com',   '+56911200009','TRP-CL-009','1999-03-19','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200010-K','Tomás','Guzmán',      't.guzman@tripulacion.aerolinea.com',    '+56911200010','TRP-CL-010','1991-08-07','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Tripulación internacional
    ('30200011-1','Luciana','Ferreira',  'l.ferreira@tripulacion.aerolinea.com',  '+55922200011','TRP-BR-001','1996-05-25','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200012-2','Pablo','Rodríguez',   'p.rodriguez@tripulacion.aerolinea.com', '+34622200012','TRP-ES-001','1994-10-11','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200013-3','Yuki','Yamamoto',     'y.yamamoto@tripulacion.aerolinea.com',  '+81322200013','TRP-JP-001','1998-02-14','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200014-4','Aisha','Mohammed',    'a.mohammed@tripulacion.aerolinea.com',  '+97122200014','TRP-AE-001','1997-07-03','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200015-5','Pierre','Lefevre',    'p.lefevre@tripulacion.aerolinea.com',   '+33622200015','TRP-FR-001','1993-12-20','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200016-6','Anna','Kowalski',     'a.kowalski@tripulacion.aerolinea.com',  '+48622200016','TRP-PL-001','1995-04-08','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200017-7','Carlos','Mendoza',    'c.mendoza@tripulacion.aerolinea.com',   '+52122200017','TRP-MX-001','1990-09-16','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200018-8','Zanele','Dlamini',    'z.dlamini@tripulacion.aerolinea.com',   '+27822200018','TRP-ZA-001','1996-01-30','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200019-9','Mei','Chen',          'm.chen@tripulacion.aerolinea.com',      '+86222200019','TRP-CN-001','1999-06-12','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200020-K','Elif','Yilmaz',       'e.yilmaz@tripulacion.aerolinea.com',   '+90522200020','TRP-TR-001','1997-11-04','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200021-1','Kevin','O''Brien',    'k.obrien@tripulacion.aerolinea.com',    '+35322200021','TRP-IE-001','1994-03-23','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200022-2','Nadia','Benali',      'n.benali@tripulacion.aerolinea.com',    '+21222200022','TRP-MA-001','1998-08-17','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200023-3','Viktor','Kovalenko',  'v.kovalenko@tripulacion.aerolinea.com', '+38022200023','TRP-UA-001','1992-05-06','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200024-4','Sunita','Patel',      's.patel@tripulacion.aerolinea.com',     '+91922200024','TRP-IN-001','1996-10-29','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200025-5','Grace','Okonkwo',     'g.okonkwo@tripulacion.aerolinea.com',   '+23422200025','TRP-NG-001','1995-02-18','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200026-6','Rafael','Herrera',    'r.herrera@tripulacion.aerolinea.com',   '+57322200026','TRP-CO-001','1993-07-14','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200027-7','Hana','Novakova',     'h.novakova@tripulacion.aerolinea.com',  '+42022200027','TRP-CZ-001','1997-12-01','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200028-8','Tariq','Hassan',      't.hassan@tripulacion.aerolinea.com',    '+96222200028','TRP-SA-001','1991-04-20','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200029-9','Ingrid','Larsson',    'i.larsson@tripulacion.aerolinea.com',   '+46722200029','TRP-SE-001','1998-09-09','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30200030-K','Bianca','Costa',      'b.costa@tripulacion.aerolinea.com',     '+55922200030','TRP-BR-002','1994-01-27','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW());

-- Rol Tripulación (id_rol = 4)
INSERT INTO RolUsuario (rut_usuario, id_rol)
SELECT RUT, 4 FROM Usuario WHERE RUT LIKE '30200%';

-- Detalles en tabla Tripulacion
INSERT INTO Tripulacion (RUT, Cargo, Fecha_Ingreso) VALUES
                                                        ('30200001-1','Jefe de Cabina',  NOW()),
                                                        ('30200002-2','Copiloto',        NOW()),
                                                        ('30200003-3','Azafata',         NOW()),
                                                        ('30200004-4','Azafato',         NOW()),
                                                        ('30200005-5','Azafata',         NOW()),
                                                        ('30200006-6','Copiloto',        NOW()),
                                                        ('30200007-7','Azafata',         NOW()),
                                                        ('30200008-8','Jefe de Cabina',  NOW()),
                                                        ('30200009-9','Azafata',         NOW()),
                                                        ('30200010-K','Copiloto',        NOW()),
                                                        ('30200011-1','Azafata',         NOW()),
                                                        ('30200012-2','Copiloto',        NOW()),
                                                        ('30200013-3','Azafata',         NOW()),
                                                        ('30200014-4','Azafata',         NOW()),
                                                        ('30200015-5','Azafato',         NOW()),
                                                        ('30200016-6','Azafata',         NOW()),
                                                        ('30200017-7','Jefe de Cabina',  NOW()),
                                                        ('30200018-8','Azafata',         NOW()),
                                                        ('30200019-9','Azafata',         NOW()),
                                                        ('30200020-K','Azafata',         NOW()),
                                                        ('30200021-1','Azafato',         NOW()),
                                                        ('30200022-2','Azafata',         NOW()),
                                                        ('30200023-3','Copiloto',        NOW()),
                                                        ('30200024-4','Azafata',         NOW()),
                                                        ('30200025-5','Jefe de Cabina',  NOW()),
                                                        ('30200026-6','Azafato',         NOW()),
                                                        ('30200027-7','Azafata',         NOW()),
                                                        ('30200028-8','Copiloto',        NOW()),
                                                        ('30200029-9','Azafata',         NOW()),
                                                        ('30200030-K','Jefe de Cabina',  NOW());


-- ============================================================
-- 7. PASAJEROS ADICIONALES (50 pasajeros internacionales)
-- ============================================================
INSERT INTO Usuario (RUT, Nombre, Apellido, Correo_Electronico, Telefono,
                     Documento_Identidad, Fecha_Nacimiento, Contrasena, Fecha_Registro)
VALUES
    ('30300001-1','Andrés','Fuentes',     'a.fuentes@mail.com',    '+56911300001','DOC-CL-001','1990-05-12','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30300002-2','Bárbara','Molina',     'b.molina@mail.com',     '+56911300002','DOC-CL-002','1985-09-28','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30300003-3','Camilo','Riquelme',    'c.riquelme@mail.com',   '+56911300003','DOC-CL-003','1998-03-07','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30300004-4','Daniela','Fuentealba', 'd.fuentealba@mail.com', '+56911300004','DOC-CL-004','1993-11-15','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30300005-5','Esteban','Quiroga',    'e.quiroga@mail.com',    '+56911300005','DOC-CL-005','1987-07-02','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30300006-6','Florencia','Aguilera', 'f.aguilera@mail.com',   '+56911300006','DOC-CL-006','2000-01-19','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30300007-7','Gustavo','Salinas',    'g.salinas@mail.com',    '+56911300007','DOC-CL-007','1982-06-25','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30300008-8','Helena','Arenas',      'h.arenas@mail.com',     '+56911300008','DOC-CL-008','1975-10-31','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30300009-9','Iván','Carvajal',      'i.carvajal@mail.com',   '+56911300009','DOC-CL-009','1996-04-14','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30300010-K','Josefina','Paredes',   'j.paredes@mail.com',    '+56911300010','DOC-CL-010','1991-08-08','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Internacional - Argentina
    ('30300011-1','Santiago','Benitez',   's.benitez@mail.com',    '+54911300011','DOC-AR-001','1988-02-23','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30300012-2','Valentina','Ibarra',   'v.ibarra@mail.com',     '+54911300012','DOC-AR-002','1994-07-10','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Brasil
    ('30300013-3','Fernando','Alves',     'f.alves@mail.com',      '+55911300013','DOC-BR-001','1980-12-18','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30300014-4','Juliana','Nascimento', 'j.nascimento@mail.com', '+55911300014','DOC-BR-002','1997-05-04','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Perú
    ('30300015-5','Miguel','Quispe',      'm.quispe@mail.com',     '+51911300015','DOC-PE-001','1986-09-21','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Colombia
    ('30300016-6','Alejandra','Restrepo', 'a.restrepo@mail.com',   '+57911300016','DOC-CO-001','1992-03-15','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- México
    ('30300017-7','Roberto','Gutiérrez',  'r.gutierrez@mail.com',  '+52911300017','DOC-MX-001','1983-11-07','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- España
    ('30300018-8','Carmen','Vázquez',     'c.vazquez@mail.com',    '+34611300018','DOC-ES-001','1979-06-30','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30300019-9','Javier','Moreno',      'j.moreno@mail.com',     '+34611300019','DOC-ES-002','1995-01-13','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Francia
    ('30300020-K','Céline','Martin',      'c.martin@mail.com',     '+33611300020','DOC-FR-001','1988-08-26','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Alemania
    ('30300021-1','Klaus','Wagner',       'k.wagner@mail.com',     '+49711300021','DOC-DE-001','1974-04-09','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Reino Unido
    ('30300022-2','Emily','Clarke',       'e.clarke@mail.com',     '+44711300022','DOC-UK-001','1991-10-22','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Italia
    ('30300023-3','Lorenzo','Ferrari',    'l.ferrari@mail.com',    '+39311300023','DOC-IT-001','1985-02-05','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- USA
    ('30300024-4','Michael','Johnson',    'm.johnson@mail.com',    '+12125300024','DOC-US-001','1977-07-19','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
    ('30300025-5','Ashley','Williams',    'a.williams@mail.com',   '+12125300025','DOC-US-002','1999-12-01','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Canadá
    ('30300026-6','Liam','MacDonald',     'l.macdonald@mail.com',  '+14165300026','DOC-CA-001','1989-05-16','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Japón
    ('30300027-7','Kenji','Nakamura',     'k.nakamura@mail.com',   '+81335300027','DOC-JP-001','1984-09-08','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- China
    ('30300028-8','Li','Wang',            'l.wang@mail.com',       '+86215300028','DOC-CN-001','1992-03-27','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- India
    ('30300029-9','Raj','Patel',          'r.patel@mail.com',      '+91225300029','DOC-IN-001','1986-11-14','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Australia
    ('30300030-K','Olivia','Smith',       'o.smith@mail.com',      '+61425300030','DOC-AU-001','1993-06-03','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Corea del Sur
    ('30300031-1','Jisoo','Kim',          'j.kim@mail.com',        '+82225300031','DOC-KR-001','1997-01-20','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- UAE
    ('30300032-2','Omar','Al-Farsi',      'o.alfarsi@mail.com',    '+97125300032','DOC-AE-001','1988-07-07','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Sudáfrica
    ('30300033-3','Thembi','Nkosi',       't.nkosi@mail.com',      '+27825300033','DOC-ZA-001','1983-04-30','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Kenia
    ('30300034-4','Amina','Kamau',        'a.kamau@mail.com',      '+25425300034','DOC-KE-001','1995-10-13','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Marruecos
    ('30300035-5','Fatima','Benali',      'f.benali@mail.com',     '+21225300035','DOC-MA-001','1990-02-07','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Turquía
    ('30300036-6','Mehmet','Yilmaz',      'm.yilmaz@mail.com',     '+90525300036','DOC-TR-001','1978-08-22','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Rusia
    ('30300037-7','Natasha','Ivanova',    'n.ivanova@mail.com',    '+79225300037','DOC-RU-001','1987-12-15','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Polonia
    ('30300038-8','Piotr','Kowalski',     'p.kowalski@mail.com',   '+48625300038','DOC-PL-001','1981-05-28','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Grecia
    ('30300039-9','Eleni','Papadaki',     'e.papadaki@mail.com',   '+30625300039','DOC-GR-001','1994-09-11','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Tailandia
    ('30300040-K','Araya','Sombat',       'a.sombat@mail.com',     '+66625300040','DOC-TH-001','1991-03-04','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Singapur
    ('30300041-1','Cheng','Lim',          'c.lim@mail.com',        '+65625300041','DOC-SG-001','1996-11-28','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Vietnam
    ('30300042-2','Lan','Nguyen',         'l.nguyen@mail.com',     '+84925300042','DOC-VN-001','1989-06-17','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Ecuador
    ('30300043-3','María','Alvarado',     'm.alvarado@mail.com',   '+59325300043','DOC-EC-001','1993-01-10','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Bolivia
    ('30300044-4','Pablo','Mamani',       'p.mamani@mail.com',     '+59125300044','DOC-BO-001','1984-07-23','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Portugal
    ('30300045-5','Ana','Ferreira',       'an.ferreira@mail.com',  '+35125300045','DOC-PT-001','1990-04-06','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Países Bajos
    ('30300046-6','Lars','de Vries',      'l.devries@mail.com',    '+31625300046','DOC-NL-001','1977-10-19','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Suecia
    ('30300047-7','Maja','Lindqvist',     'm.lindqvist@mail.com',  '+46725300047','DOC-SE-001','1995-02-12','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Israel
    ('30300048-8','Tal','Cohen',          't.cohen@mail.com',      '+97225300048','DOC-IL-001','1988-08-05','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Nigeria
    ('30300049-9','Emeka','Okafor',       'e.okafor@mail.com',     '+23425300049','DOC-NG-001','1982-12-28','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW()),
-- Nueva Zelanda
    ('30300050-K','Aroha','Te Rau',       'a.terau@mail.com',      '+64925300050','DOC-NZ-001','1997-05-21','$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m',NOW());

-- Rol Pasajero (id_rol = 2)
INSERT INTO RolUsuario (rut_usuario, id_rol)
SELECT RUT, 2 FROM Usuario WHERE RUT LIKE '30300%';

-- Detalles en tabla Pasajero
INSERT INTO Pasajero (RUT, Tipo_Documento, Numero_Documento, Fecha_Nacimiento, Nacionalidad)
SELECT
    u.RUT,
    CASE
        WHEN u.RUT LIKE '%-CL-%' OR u.RUT LIKE '30300001%' OR u.RUT LIKE '30300002%'
            OR u.RUT LIKE '30300003%' OR u.RUT LIKE '30300004%' OR u.RUT LIKE '30300005%'
            OR u.RUT LIKE '30300006%' OR u.RUT LIKE '30300007%' OR u.RUT LIKE '30300008%'
            OR u.RUT LIKE '30300009%' OR u.RUT LIKE '30300010%'
            THEN 'Cédula de Identidad'
        ELSE 'Pasaporte'
        END AS Tipo_Documento,
    u.Documento_Identidad,
    u.Fecha_Nacimiento,
    CASE u.RUT
        WHEN '30300001-1' THEN 'Chilena'      WHEN '30300002-2' THEN 'Chilena'
        WHEN '30300003-3' THEN 'Chilena'      WHEN '30300004-4' THEN 'Chilena'
        WHEN '30300005-5' THEN 'Chilena'      WHEN '30300006-6' THEN 'Chilena'
        WHEN '30300007-7' THEN 'Chilena'      WHEN '30300008-8' THEN 'Chilena'
        WHEN '30300009-9' THEN 'Chilena'      WHEN '30300010-K' THEN 'Chilena'
        WHEN '30300011-1' THEN 'Argentina'    WHEN '30300012-2' THEN 'Argentina'
        WHEN '30300013-3' THEN 'Brasileña'    WHEN '30300014-4' THEN 'Brasileña'
        WHEN '30300015-5' THEN 'Peruana'      WHEN '30300016-6' THEN 'Colombiana'
        WHEN '30300017-7' THEN 'Mexicana'     WHEN '30300018-8' THEN 'Española'
        WHEN '30300019-9' THEN 'Española'     WHEN '30300020-K' THEN 'Francesa'
        WHEN '30300021-1' THEN 'Alemana'      WHEN '30300022-2' THEN 'Británica'
        WHEN '30300023-3' THEN 'Italiana'     WHEN '30300024-4' THEN 'Estadounidense'
        WHEN '30300025-5' THEN 'Estadounidense' WHEN '30300026-6' THEN 'Canadiense'
        WHEN '30300027-7' THEN 'Japonesa'     WHEN '30300028-8' THEN 'China'
        WHEN '30300029-9' THEN 'India'        WHEN '30300030-K' THEN 'Australiana'
        WHEN '30300031-1' THEN 'Surcoreana'   WHEN '30300032-2' THEN 'Emiratí'
        WHEN '30300033-3' THEN 'Sudafricana'  WHEN '30300034-4' THEN 'Keniana'
        WHEN '30300035-5' THEN 'Marroquí'     WHEN '30300036-6' THEN 'Turca'
        WHEN '30300037-7' THEN 'Rusa'         WHEN '30300038-8' THEN 'Polaca'
        WHEN '30300039-9' THEN 'Griega'       WHEN '30300040-K' THEN 'Tailandesa'
        WHEN '30300041-1' THEN 'Singapurense' WHEN '30300042-2' THEN 'Vietnamita'
        WHEN '30300043-3' THEN 'Ecuatoriana'  WHEN '30300044-4' THEN 'Boliviana'
        WHEN '30300045-5' THEN 'Portuguesa'   WHEN '30300046-6' THEN 'Neerlandesa'
        WHEN '30300047-7' THEN 'Sueca'        WHEN '30300048-8' THEN 'Israelí'
        WHEN '30300049-9' THEN 'Nigeriana'    WHEN '30300050-K' THEN 'Neozelandesa'
        ELSE 'No especificada'
        END AS Nacionalidad
FROM Usuario u
WHERE u.RUT LIKE '30300%';


-- ============================================================
-- RESUMEN OPERATIVO (ejecutar para verificar)
-- ============================================================
-- SELECT 'Aviones'      AS entidad, COUNT(*) AS total FROM Avion
-- UNION ALL
-- SELECT 'Pilotos',     COUNT(*) FROM Piloto
-- UNION ALL
-- SELECT 'Tripulación', COUNT(*) FROM Tripulacion
-- UNION ALL
-- SELECT 'Pasajeros',   COUNT(*) FROM Pasajero
-- UNION ALL
-- SELECT 'Usuarios',    COUNT(*) FROM Usuario;