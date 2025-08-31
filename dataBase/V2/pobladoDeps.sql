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
(20, 'Emiratos Arabes Unidos');

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
(29, 'Houston', 4);


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
(28, 'Chicago O\'Hare', 'ORD', 28),
(29, 'George Bush Intercontinental', 'IAH', 29),
(30, 'Gatwick Airport', 'LGW', 13);


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