INSERT INTO Pais (ID_PAIS, Nombre) VALUES
(1, 'Chile'),
(2, 'Uruguay'),
(3, 'Brasil'),
(4, 'USA'),
(5, 'Perú'),
(6, 'Colombia');

-- Insertar datos en la tabla Ciudad
INSERT INTO Ciudad (ID_CIUDAD, Nombre, ID_PAIS) VALUES
(1, 'Santiago', 1),
(2, 'Montevideo', 2),
(3, 'Sao_Paulo', 3), -- Eliminando el acento
(4, 'NuevaYork', 4), -- Reemplazando el espacio por un guion bajo
(5, 'LosAngeles', 4), -- Reemplazando el espacio por un guion bajo
(6, 'Lima', 5),
(7, 'Atlanta', 4),
(8, 'Bogota', 6), -- Eliminando el acento
(9, 'Miami', 4);

INSERT INTO Aerolinea (ID_AEROLINEA, Nombre, Codigo) VALUES
  (1, 'LATAM Airlines Brasil', 'JJ'),
  (2, 'Delta Air Lines', 'DL');

INSERT INTO Aeropuerto (ID_AEROPUERTO, Nombre_Aeropuerto, Codigo_IATA, ID_CIUDAD) VALUES
(1, 'Aeropuerto Internacional Comodoro Arturo Merino Benítez', 'SCL', 1),
(2, 'Carrasco Intl.', 'MVD', 2),
(3, 'Guarulhos Intl.', 'GRU', 3),
(4, 'John F Kennedy', 'JFK', 4),
(5, 'Los Angeles Intl.', 'LAX', 5),
(6, 'J Chavez Intl.', 'LIM', 6),
(7, 'Hartsfield Jackson Atlanta Int.', 'ATL', 7),
(8, 'El Dorado International Airport', 'BOG', 8),
(9, 'Miami International Airport', 'MIA', 9);


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

--CALL insertar_avion_y_asientos('ABC123', 'Boeing 737', 'Boeing', 2000, 240, 20000, 'En servicio');
/*CALL insertar_avion_y_asientos('DEF456', 'Airbus A320', 'Airbus', 2018, 185, 15000, 'En mantenimiento', 150, 30, 5);
CALL insertar_avion_y_asientos('GHI789', 'Boeing 747', 'Boeing', 2005, 380, 45000, 'Operativo', 300, 50, 30);
CALL insertar_avion_y_asientos('JKL012', 'Airbus A350', 'Airbus', 2019, 310, 40000, 'Operativo', 250, 40, 20);
CALL insertar_avion_y_asientos('MNO345', 'Boeing 787', 'Boeing', 2020, 220, 35000, 'En servicio', 180, 40, 15);
CALL insertar_avion_y_asientos('PQR678', 'Embraer E195', 'Embraer', 2016, 120, 12000, 'Operativo', 100, 10, 5);
CALL insertar_avion_y_asientos('XYZ123', 'Boeing 777', 'Boeing', 2014, 450, 50000, 'En servicio', 350, 70, 30);
CALL insertar_avion_y_asientos('LMN987', 'Airbus A380', 'Airbus', 2018, 650, 75000, 'Operativo', 500, 100, 50);
CALL insertar_avion_y_asientos('STU456', 'Airbus A330', 'Airbus', 2017, 250, 35000, 'En mantenimiento', 200, 40, 10);
CALL insertar_avion_y_asientos('WXY543', 'Boeing 757', 'Boeing', 2003, 190, 22000, 'Operativo', 150, 30, 10);*/


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

/*CALL spAsignarCapacidadPorClase(1, 160, 25, 0);
CALL spAsignarCapacidadPorClase(2, 300, 60, 20);
CALL spAsignarCapacidadPorClase(3, 240, 50, 20);
CALL spAsignarCapacidadPorClase(4, 180, 30, 10);
CALL spAsignarCapacidadPorClase(5, 110, 10, 0);
CALL spAsignarCapacidadPorClase(6, 350, 70, 30);
CALL spAsignarCapacidadPorClase(7, 500, 100, 50);
CALL spAsignarCapacidadPorClase(8, 200, 40, 10);
CALL spAsignarCapacidadPorClase(9, 160, 30, 0);*/


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



-- Insertar datos en la tabla Asiento
-- Insertar datos en la tabla Asiento


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

	select * from vuelo;


-- 7. Segmentos de vuelo (IDs hardcoded) - principales y adicionales juntos
INSERT INTO Segmento_Vuelo (ID_SEGMENTO, ID_VUELO, ORDEN_SEGMENTO, ID_AEROPUERTO_ORIGEN, ID_AEROPUERTO_DESTINO, HORA_SALIDA, HORA_LLEGADA) VALUES
  (nextval('segmento_vuelo_seq'), 1, 1, 1, 2, '2025-08-01 14:30', '2025-08-01 17:45'), -- SCL -> MVD (Itinerario 1)
  (nextval('segmento_vuelo_seq'), 1, 2, 2, 3, '2025-08-01 18:55', '2025-08-01 21:30'), -- MVD -> GRU (Itinerario 1)
  (nextval('segmento_vuelo_seq'), 3, 1, 3, 4, '2025-08-01 22:50', '2025-08-02 07:35'), -- GRU -> JFK (Itinerario 1)
  (nextval('segmento_vuelo_seq'), 4, 1, 4, 5, '2025-08-02 09:55', '2025-08-02 12:50'), -- JFK -> LAX (Itinerario 1)
  (nextval('segmento_vuelo_seq'), 5, 1, 1, 6, '2025-08-01 07:50', '2025-08-01 10:40'), -- SCL -> LIM (Itinerarios 2, 3, 5)
  (nextval('segmento_vuelo_seq'), 6, 1, 6, 7, '2025-08-01 12:00', '2025-08-01 20:15'), -- LIM -> ATL (Itinerarios 3, 5)
  (nextval('segmento_vuelo_seq'), 7, 1, 7, 4, '2025-08-01 22:30', '2025-08-02 01:06'), -- ATL -> JFK (Itinerario 3)
  (nextval('segmento_vuelo_seq'), 8, 1, 2, 1, '2025-08-02 15:00', '2025-08-02 17:30'), -- MVD -> SCL (No asignado)
  (nextval('segmento_vuelo_seq'), 9, 1, 3, 2, '2025-08-02 19:00', '2025-08-02 21:45'), -- GRU -> MVD (No asignado)
  (nextval('segmento_vuelo_seq'), 10, 1, 5, 7, '2025-08-02 22:30', '2025-08-03 01:30'), -- LAX -> ATL (No asignado)
  (nextval('segmento_vuelo_seq'), 11, 1, 7, 2, '2025-08-03 03:00', '2025-08-03 06:00'), -- ATL -> MVD (No asignado)
  (nextval('segmento_vuelo_seq'), 12, 1, 1, 2, '2025-08-03 08:00', '2025-08-03 10:30'), -- SCL -> MVD (Itinerario 4)
  (nextval('segmento_vuelo_seq'), 13, 1, 2, 3, '2025-08-03 11:30', '2025-08-03 14:00'), -- MVD -> GRU (Itinerario 4)
  (nextval('segmento_vuelo_seq'), 14, 1, 3, 8, '2025-08-03 15:00', '2025-08-03 18:30'), -- GRU -> BOG
  (nextval('segmento_vuelo_seq'), 14, 2, 8, 9, '2025-08-03 19:15', '2025-08-03 21:45'), -- BOG -> MIA
  (nextval('segmento_vuelo_seq'), 14, 3, 9, 4, '2025-08-03 22:30', '2025-08-04 01:00'), -- MIA -> JFK
  (nextval('segmento_vuelo_seq'), 15, 1, 5, 7, '2025-08-08 06:00', '2025-08-08 13:00'), -- LAX -> ATL
  (nextval('segmento_vuelo_seq'), 16, 1, 7, 6, '2025-08-08 15:00', '2025-08-08 22:00'), -- ATL -> LIM
  (nextval('segmento_vuelo_seq'), 17, 1, 6, 1, '2025-08-09 06:00', '2025-08-09 10:00'), -- LIM -> SCL
  (nextval('segmento_vuelo_seq'), 18, 1, 9, 1, '2025-08-08 06:00', '2025-08-08 13:00'), -- MIA -> SCL (directo)
  (nextval('segmento_vuelo_seq'), 19, 1, 3, 8, '2025-08-04 08:00', '2025-08-04 11:30'), -- GRU -> BOG
  (nextval('segmento_vuelo_seq'), 20, 1, 8, 9, '2025-08-04 13:00', '2025-08-04 16:00'), -- BOG -> MIA
  (nextval('segmento_vuelo_seq'), 21, 1, 9, 4, '2025-08-04 18:00', '2025-08-04 21:00'); -- MIA -> JFK
	-- Segmento directo SCL -> JFK
INSERT INTO Segmento_Vuelo VALUES
  (nextval('segmento_vuelo_seq'), 22, 1, 1, 4, '2025-08-01 23:55', '2025-08-02 09:30');

-- Segmento SCL -> BOG
INSERT INTO Segmento_Vuelo VALUES
  (nextval('segmento_vuelo_seq'), 23, 1, 1, 8, '2025-08-01 09:00', '2025-08-01 15:00');

-- Segmento BOG -> JFK
INSERT INTO Segmento_Vuelo VALUES
  (nextval('segmento_vuelo_seq'), 24, 1, 8, 4, '2025-08-01 17:00', '2025-08-01 22:00');

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




-- Crear reservas para tres pasajeros en el Itinerario 10
INSERT INTO Reserva (Fecha_Reserva, Estado_Reserva, RUT_PASAJERO, Total) VALUES
('2025-08-04 10:00', 1, '87654321-2', 530000.00), -- Lucía
('2025-08-04 10:05', 1, '76543210-3', 530000.00), -- Juan
('2025-08-04 10:10', 1, '65432109-4', 530000.00); -- Ana

-- Asociar reservas con el Itinerario 10 (GRU → JFK)
INSERT INTO Reserva_Itinerario (ID_RESERVA, ID_ITINERARIO) VALUES
(1, 10),
(2, 10),
(3, 10);


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


select * from asiento;

select * from reserva_asiento;

select * from itinerario;


select * from vuelo;



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

SELECT MAX(id_vuelo) FROM vuelo;

select * from asiento;

select * from precio_asiento;

select * from segmento_vuelo;

select * from itinerario;

select * from asignacion_puerta;



