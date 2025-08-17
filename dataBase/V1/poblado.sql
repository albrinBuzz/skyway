INSERT INTO Pais (Nombre) VALUES
('Argentina'),
('Chile'),
('Colombia'),
('España'),
('Estados Unidos'),
('Brasil'),
('México'),
('Perú'),
('Francia'),
('Italia');


-- Insertar datos en la tabla Ciudad
INSERT INTO Ciudad (Nombre, ID_PAIS) VALUES
('Buenos Aires', 1),
('Santiago', 2),
('Bogotá', 3),
('Madrid', 4),
('Nueva York', 5),
('São Paulo', 6),
('Río de Janeiro', 6),
('Ciudad de México', 7),
('Lima', 8),
('París', 9),
('Roma', 10);

-- Insertar datos en la tabla Aeropuerto
INSERT INTO Aeropuerto (Nombre_Aeropuerto, Ciudad, Codigo_IATA) VALUES
('Aeropuerto Internacional de Ezeiza', 1, 'EZE'),
('Aeropuerto Internacional Arturo Merino Benítez', 2, 'SCL'),
('Aeropuerto El Dorado', 3, 'BOG'),
('Aeropuerto Adolfo Suárez Madrid-Barajas', 4, 'MAD'),
('Aeropuerto John F. Kennedy', 5, 'JFK'),
('Aeroporto de São Paulo/Guarulhos', 6, 'GRU'),
('Aeroporto Internacional do Galeão', 6, 'GIG'),
('Aeropuerto Internacional de la Ciudad de México', 7, 'MEX'),
('Aeropuerto Internacional Jorge Chávez', 8, 'LIM'),
('Aéroport de Paris-Charles de Gaulle', 9, 'CDG'),
('Aeroporto di Roma-Fiumicino', 10, 'FCO');


INSERT INTO Clase_asiento (Descripcion) VALUES
('Económica'),
('Ejecutiva'),
('Primera Clase');



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
    Modelo,
    Fabricante,
    Ano_de_Fabricacion,
    Capacidad_de_Pasajeros,
    Capacidad_de_Carga,
    Estado_de_Mantenimiento,
    cap_economica,
    cap_ejecutiva,
    cap_primera
)
VALUES
    ('DEF456', 'Airbus A320', 'Airbus', 2018, 185, 15000, 'En mantenimiento', 150, 30, 5), -- Avión 1
    ('GHI789', 'Boeing 747', 'Boeing', 2005, 380, 45000, 'Operativo', 300, 50, 30),     -- Avión 2
    ('JKL012', 'Airbus A350', 'Airbus', 2019, 310, 40000, 'Operativo', 250, 40, 20),     -- Avión 3
    ('MNO345', 'Boeing 787', 'Boeing', 2020, 220, 35000, 'En servicio', 180, 40, 15),     -- Avión 4
    ('PQR678', 'Embraer E195', 'Embraer', 2016, 120, 12000, 'Operativo', 100, 10, 5),     -- Avión 5
    ('XYZ123', 'Boeing 777', 'Boeing', 2014, 450, 50000, 'En servicio', 350, 70, 30),     -- Avión 6
    ('LMN987', 'Airbus A380', 'Airbus', 2018, 650, 75000, 'Operativo', 500, 100, 50),    -- Avión 7
    ('STU456', 'Airbus A330', 'Airbus', 2017, 250, 35000, 'En mantenimiento', 200, 40, 10), -- Avión 8
    ('WXY543', 'Boeing 757', 'Boeing', 2003, 190, 22000, 'Operativo', 150, 30, 10);     -- Avión 9


-- Insertar datos en la tabla Asiento
-- Insertar datos en la tabla Asiento


-- Insertar datos en la tabla Estado_Vuelo
INSERT INTO Estado_Vuelo (Descripcion, Estado) VALUES
('Vuelo Programado', 'Programado'),
('Vuelo Cancelado', 'Cancelado'),
('Vuelo Retrasado', 'Retrasado');

INSERT INTO roles (nombre, descripcion)
VALUES
    ('admin', 'Rol de administrador con permisos completos'),
    ('Piloto', 'Rol de piloto'),
    ('Pasajero', 'Rol pasajero');

-- Insertar estados de reserva
INSERT INTO Estado_reserva (Descripcion) VALUES
('Confirmada'),
('Cancelada'),
('Pendiente');

-- Insertar métodos de pago
INSERT INTO Metodo_Pago (Descripcion) VALUES
('Tarjeta de Crédito'),
('PayPal'),
('Débito');



-- Insertar contenido de tipo "informacion" para la sección "Quiénes Somos"
INSERT INTO contenido (tipo, titulo, seccion, contenido)
VALUES
(
    'informacion',           -- Tipo: 'informacion' es adecuado para secciones informativas
    'Quiénes Somos',         -- Título de la sección
    'quienes_somos',         -- Slug o identificador único para la sección
    'Somos una aerolínea de clase mundial, comprometida con ofrecer el mejor servicio aéreo a nuestros pasajeros. Con más de 20 años de experiencia, ofrecemos vuelos a destinos nacionales e internacionales, siempre con un enfoque en la seguridad, puntualidad y comodidad.'
    -- Contenido que describe la empresa y su enfoque
);

-- Insertar contenido de tipo "informacion" para la sección "Historia"
INSERT INTO contenido (tipo, titulo, seccion, contenido)
VALUES
(
    'informacion',           -- Tipo: 'informacion'
    'Historia',              -- Título de la sección
    'historia',              -- Slug único para la sección
    'Fundada en 1998, nuestra aerolínea ha crecido rápidamente para convertirse en uno de los principales actores en la industria aérea, ofreciendo vuelos a más de 30 destinos internacionales.'
    -- Contenido explicando la historia de la aerolínea
);

-- Insertar contenido de tipo "informacion" para la sección "Misión"
INSERT INTO contenido (tipo, titulo, seccion, contenido)
VALUES
(
    'informacion',           -- Tipo: 'informacion'
    'Misión',                -- Título
    'mision',                -- Slug único para la sección
    'Nuestra misión es proporcionar experiencias de vuelo seguras, cómodas y accesibles, mientras seguimos innovando y superando las expectativas de nuestros pasajeros.'
    -- Contenido que describe la misión de la aerolínea
);

-- Insertar contenido de tipo "informacion" para la sección "Visión"
INSERT INTO contenido (tipo, titulo, seccion, contenido)
VALUES
(
    'informacion',           -- Tipo: 'informacion'
    'Visión',                -- Título
    'vision',                -- Slug único para la sección
    'Ser la aerolínea líder a nivel mundial en servicio al cliente, innovación tecnológica y sostenibilidad en la industria aérea.'
    -- Contenido sobre la visión de la aerolínea
);

-- Insertar contenido de tipo "faq" para la pregunta "¿Cómo puedo cambiar mi vuelo?"
INSERT INTO contenido (tipo, titulo, seccion, contenido)
VALUES
(
    'faq',                   -- Tipo: 'faq' para preguntas frecuentes
    '¿Cómo puedo cambiar mi vuelo?', -- Título de la pregunta
    'faq_cambio_vuelo',      -- Slug único para la pregunta
    'Puedes cambiar tu vuelo a través de nuestra página web en la sección de "Gestión de Reservas" o llamando a nuestro centro de atención al cliente.'
    -- Respuesta a la pregunta frecuente sobre cambios de vuelo
);

-- Insertar contenido de tipo "faq" para la pregunta "¿Cómo puedo cancelar mi reserva?"
INSERT INTO contenido (tipo, titulo, seccion, contenido)
VALUES
(
    'faq',                   -- Tipo: 'faq'
    '¿Cómo puedo cancelar mi reserva?', -- Título de la pregunta
    'faq_cancelacion_reserva', -- Slug único para la pregunta
    'Puedes cancelar tu reserva a través de la página web, en la sección "Mis Reservas". Dependiendo de la tarifa de tu billete, se puede aplicar una penalización.'
    -- Respuesta a la pregunta frecuente sobre cancelaciones
);

-- Insertar contenido de tipo "Estático" para la sección "Política de Privacidad"
INSERT INTO contenido (tipo, titulo, seccion, contenido)
VALUES
('Estático', 'Política de Privacidad', 'politicas_privacidad', 'En nuestra aerolínea, respetamos tu privacidad. Recopilamos solo la información necesaria para brindarte un mejor servicio y garantizamos que tus datos no serán compartidos sin tu consentimiento.');

-- Insertar contenido de tipo "Estático" para la sección "Términos y Condiciones"
INSERT INTO contenido (tipo, titulo, seccion, contenido)
VALUES
('Estático', 'Términos y Condiciones', 'politicas_terminos', 'Al usar nuestros servicios, aceptas los términos y condiciones establecidos. Te recomendamos leerlos cuidadosamente antes de hacer tu compra.');






INSERT INTO Usuario (RUT, Nombre, Apellido, Correo_Electronico, Telefono, Documento_Identidad, Fecha_Nacimiento, Contrasena, id_rol, Rol)
VALUES ('123456789-0', 'Juan', 'Pérez', 'juan.perez@correo.com', '123456789', '987654321', '1985-05-15', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 1, 'admin');


-- Insertar datos en la tabla Piloto
INSERT INTO Piloto (RUT, Nombre, Apellido, Correo_Electronico, Telefono, Documento_Identidad, Fecha_Nacimiento, Contrasena, Rol, Experiencia_anos, Licencia, id_rol) VALUES
('12345678-9', 'Juan', 'Pérez', 'juan.perez@piloto.com', '123456789', '12345678A', '1980-05-20', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Piloto', 9, 'Licencia A', 2),
('87654321-0', 'María', 'Gómez', 'maria.gomez@piloto.com', '987654321', '87654321B', '1985-08-15', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Piloto', 8, 'Licencia A', 2),
('11223344-1', 'Diego', 'Fernández', 'diego.fernandez@piloto.com', '456456456', '11223344E', '1975-03-10', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Piloto', 7, 'Licencia A', 2),
('55667788-2', 'Sofía', 'Romero', 'sofia.romero@piloto.com', '654654654', '55667788F', '1988-12-25', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Piloto', 12, 'Licencia A', 2),
('99887766-3', 'Andrés', 'Martinez', 'andres.martinez@piloto.com', '987987987', '99887766G', '1992-07-19', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Piloto', 10, 'Licencia A', 2);







-- Insertar datos en la tabla Pasajero
INSERT INTO Pasajero (rut, Nombre, Apellido, Correo_Electronico, Telefono, Documento_Identidad, Fecha_Nacimiento, contrasena, rol, id_rol) VALUES
('12345678-9', 'Carlos', 'López', 'carlos.lopez@email.com', '123123123', '12345678C', '1990-01-01', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3),
('87654321-0', 'Laura', 'Martínez', 'laura.martinez@email.com', '321321321', '87654321D', '1995-02-02', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3),
('11223344-1', 'Ana', 'Sánchez', 'ana.sanchez@email.com', '222222222', '12312312H', '1993-04-01', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3),
('55667788-2', 'Luis', 'Hernández', 'luis.hernandez@email.com', '333333333', '45645645I', '1986-11-11', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3),
('99887766-3', 'Clara', 'Mendoza', 'clara.mendoza@email.com', '444444444', '78978978J', '1999-06-30', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3),
('55601234-5', 'Roberto', 'García', 'roberto.garcia@email.com', '555555555', '32132132K', '1982-09-20', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3),
('12345678-0', 'Juan', 'Pérez', 'juan.perez@example.com', '5551234567', '12345678', '1990-01-15', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3),
('23456789-1', 'María', 'García', 'maria.garcia@example.com', '5552345678', '23456789', '1985-02-20', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3),
('34567890-2', 'Pedro', 'Martínez', 'pedro.martinez@example.com', '5553456789', '34567890', '1992-03-10', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3),
('45678901-3', 'Ana', 'Ramírez', 'ana.ramirez@email.com', '5554567890', '45678901', '1993-03-12', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3),
('56789012-4', 'Carlos', 'López', 'carlos.lopez@example.com', '5555678901', '56789012', '1988-07-22', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3),
('67890123-5', 'Sofía', 'Torres', 'sofia.torres@email.com', '5556789012', '67890123', '1995-12-05', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3),
('78901234-6', 'Luis', 'Fernández', 'luis.fernandez@email.com', '5557890123', '78901234', '1991-04-18', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3),
('89012345-7', 'Elena', 'Jiménez', 'elena.jimenez@email.com', '5558901234', '89012345', '1994-05-25', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3),
('90123456-8', 'Ricardo', 'Morales', 'ricardo.morales@email.com', '5559012345', '90123456', '1987-08-30', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3),
('01234567-9', 'Gabriela', 'Salazar', 'gabriela.salazar@email.com', '5560123456', '01234567', '1996-09-12', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3),
('10123456-0', 'Diego', 'Cruz', 'diego.cruz@email.com', '5551234501', '10123456', '1989-01-05', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3),
('20123457-1', 'Valentina', 'Hernández', 'valentina.hernandez@email.com', '5552345602', '20123457', '1990-02-18', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3),
('30123458-2', 'Andrés', 'Vázquez', 'andres.vazquez@email.com', '5553456703', '30123458', '1988-03-12', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3),
('40123459-3', 'Mariana', 'Núñez', 'mariana.nunez@email.com', '5554567804', '40123459', '1994-04-21', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3),
('50123450-4', 'Jorge', 'Ríos', 'jorge.rios@email.com', '5555678905', '50123450', '1986-05-30', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3),
('60123461-5', 'Natalia', 'Castillo', 'natalia.castillo@email.com', '5556789016', '60123461', '1992-06-25', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3),
('70123472-6', 'Felipe', 'Ponce', 'felipe.ponce@email.com', '5557890127', '70123472', '1991-07-12', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3),
('80123483-7', 'Lina', 'Moreno', 'lina.moreno@email.com', '5558901238', '80123483', '1995-08-15', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3),
('90123494-8', 'Santiago', 'Bermúdez', 'santiago.bermudez@email.com', '5559012349', '90123494', '1987-09-10', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3),
('10234506-9', 'Carmen', 'Santos', 'carmen.santos@email.com', '5560123450', '10234506', '1993-10-03', '$2a$10$hsjnyiws1X0PpAZYrNNbYuFacX53JUO9jfasNhL.WhHa6JpkO4O/m', 'Pasajero', 3);

INSERT INTO CategoriaPost (nombre, descripcion) VALUES
('Vuelos', 'Información sobre vuelos, horarios, rutas y más.'),
('Destinos', 'Guías y recomendaciones de destinos que puedes visitar con nuestra aerolínea.'),
('Consejos para Viajeros', 'Consejos prácticos para hacer más cómodos tus viajes.');

-- Insertar datos en la tabla Vuelo
-- Insertar datos en la tabla Vuelo con los RUTs de los pilotos
INSERT INTO Vuelo (Numero_Vuelo, Fecha_Hora_Salida, Fecha_Hora_Llegada, ID_AEROPUERTO_SALIDA, ID_AEROPUERTO_LLEGADA, ID_ESTADO_VUELO, ID_AVION, RUT_PILOTO) VALUES
('AR357', TO_TIMESTAMP('2024-10-21 14:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2024-10-21 16:30:00', 'YYYY-MM-DD HH24:MI:SS'), 1, 2, 1, 1, '12345678-9'),
('AR456', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '2 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '2 days' + INTERVAL '2 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 2, 4, 1, 2, '12345678-9'),
('BR456', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '3 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '3 days' + INTERVAL '2 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 6, 7, 1, 1, '12345678-9'),
('MX789', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '4 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '4 days' + INTERVAL '4 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 7, 8, 1, 2, '11223344-1'),
('FR101', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '5 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '5 days' + INTERVAL '3 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 9, 10, 2, 3, '99887766-3'),
('BR202', TO_TIMESTAMP(TO_CHAR(NOW(), 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '2.5 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 6, 1, 1, 1, '55667788-2'), -- Sofía Romero
('MX303', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '1 day', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '1 day' + INTERVAL '2.5 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 7, 4, 1, 2, '11223344-1'),
('AR404', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '2 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '2 days' + INTERVAL '2.5 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 1, 8, 1, 3, '11223344-1'),
('CL505', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '3 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '3 days' + INTERVAL '3 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 2, 5, 1, 4, '87654321-0'),
('ES606', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '4 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '4 days' + INTERVAL '2.5 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 4, 3, 1, 1, '87654321-0'),
('CO707', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '5 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '5 days' + INTERVAL '3 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 3, 10, 1, 2, '12345678-9'),
('FR808', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '6 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '6 days' + INTERVAL '3 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 9, 6, 1, 3, '99887766-3'),
('AR909', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '7 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '7 days' + INTERVAL '3 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 1, 2, 1, 4, '12345678-9'),
('BR1010', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '8 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '8 days' + INTERVAL '2.5 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 6, 7, 1, 5, '99887766-3'),
('MX1111', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '9 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '9 days' + INTERVAL '3 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 7, 9, 1, 1, '87654321-0'),
('MX2222', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '9 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '9 days' + INTERVAL '3 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 7, 9, 1, 1, '12345678-9'),
('AR1212', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '10 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '10 days' + INTERVAL '2.5 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 1, 3, 1, 1, '11223344-1'),
('BR1313', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '11 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '11 days' + INTERVAL '2.5 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 6, 5, 1, 2,  '12345678-9'),
('MX1414', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '12 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '12 days' + INTERVAL '2.5 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 7, 8, 1, 3, '12345678-9'),
('FR1515', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '13 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '13 days' + INTERVAL '3 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 9, 4, 1, 4, '11223344-1'),
('CO1616', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '14 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '14 days' + INTERVAL '3 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 3, 2, 1, 5, '11223344-1');
-- Vuelos adicionales
INSERT INTO Vuelo (Numero_Vuelo, Fecha_Hora_Salida, Fecha_Hora_Llegada, ID_AEROPUERTO_SALIDA, ID_AEROPUERTO_LLEGADA, ID_ESTADO_VUELO, ID_AVION, RUT_PILOTO) VALUES
('AR1717', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '15 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '15 days' + INTERVAL '2.5 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 1, 5, 1, 1, '12345678-9'),
('BR1818', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '16 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '16 days' + INTERVAL '3 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 6, 9, 1, 2, '99887766-3'),
('MX1919', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '17 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '17 days' + INTERVAL '4 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 7, 10, 1, 3, '11223344-1'),
('FR2020', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '18 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '18 days' + INTERVAL '2.5 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 9, 3, 1, 1, '87654321-0'),
('CO2121', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '19 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '19 days' + INTERVAL '3.5 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 3, 8, 1, 2, '99887766-3'),
('AR2222', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '20 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '20 days' + INTERVAL '3 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 1, 1, 1, 3, '11223344-1'),
('BR2323', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '21 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '21 days' + INTERVAL '4 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 6, 2, 1, 1, '87654321-0'),
('MX2424', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '22 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '22 days' + INTERVAL '2 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 7, 4, 1, 2, '12345678-9'),
('FR2525', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '23 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '23 days' + INTERVAL '3 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 9, 5, 1, 3, '99887766-3'),
('CO2626', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '24 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '24 days' + INTERVAL '2.5 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 3, 6, 1, 1, '11223344-1'),
('AR2727', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '25 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '25 days' + INTERVAL '4 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 1, 7, 1, 2, '87654321-0'),
('BR2828', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '26 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '26 days' + INTERVAL '3.5 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 6, 8, 1, 3, '99887766-3'),
('MX2929', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '27 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '27 days' + INTERVAL '2 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 7, 9, 1, 1, '11223344-1');

-- Vuelos adicionales
-- Inserción de pilotos a los vuelos adicionales
INSERT INTO Vuelo (Numero_Vuelo, Fecha_Hora_Salida, Fecha_Hora_Llegada, ID_AEROPUERTO_SALIDA, ID_AEROPUERTO_LLEGADA, ID_ESTADO_VUELO, ID_AVION, RUT_PILOTO) VALUES
('AR3030', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '28 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '28 days' + INTERVAL '3 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 1, 10, 1, 1, '12345678-9'),
('BR3131', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '29 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '29 days' + INTERVAL '4 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 6, 1, 1, 2, '99887766-3'),
('MX3232', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '30 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '30 days' + INTERVAL '2.5 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 7, 2, 1, 3, '11223344-1'),
('FR3333', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '31 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '31 days' + INTERVAL '3 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 9, 3, 1, 1, '87654321-0'),
('CO3434', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '32 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '32 days' + INTERVAL '2 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 3, 4, 1, 2, '99887766-3'),
('AR3535', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '33 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '33 days' + INTERVAL '3.5 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 1, 5, 1, 3, '11223344-1'),
('BR3636', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '34 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '34 days' + INTERVAL '2 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 6, 6, 1, 1, '87654321-0'),
('MX3737', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '35 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '35 days' + INTERVAL '4 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 7, 7, 1, 2, '99887766-3'),
('FR3838', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '36 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '36 days' + INTERVAL '3 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 9, 8, 1, 3, '11223344-1'),
('CO3939', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '37 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '37 days' + INTERVAL '2.5 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 3, 9, 1, 1, '87654321-0'),
('CO2938', TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '37 days', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP(TO_CHAR(NOW() + INTERVAL '37 days' + INTERVAL '2.5 hours', 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'), 6, 7, 1, 1, '12345678-9');


select fn_insertarPreciosAsientos();


UPDATE Vuelo SET Precio = 630.00;

-- Insertar datos en la tabla Reserva con RUTs de los pasajeros
INSERT INTO Reserva (Fecha_Reserva, Estado_Reserva, ID_VUELO, rut_pasajero) VALUES
('2024-09-20 14:00:00', 1, 1, '12345678-0'),  -- Reserva confirmada para Juan
('2024-09-20 14:00:00', 1, 2, '12345678-0'),  -- Reserva confirmada para Juan
('2024-09-21 09:30:00', 2, 2, '23456789-1'),  -- Reserva cancelada para María
('2024-09-22 10:15:00', 3, 1, '34567890-2'),  -- Reserva pendiente para Pedro
('2024-09-25 16:00:00', 1, 1, '45678901-3'),  -- Reserva confirmada para Ana
('2024-09-26 11:00:00', 2, 2, '56789012-4'),  -- Reserva cancelada para Carlos
('2024-09-27 13:30:00', 3, 1, '67890123-5'),  -- Reserva pendiente para Sofía
('2024-09-28 15:00:00', 1, 1, '78901234-6'),  -- Reserva confirmada para Luis
('2024-09-29 08:30:00', 3, 1, '89012345-7'),  -- Reserva pendiente para Elena
('2024-09-30 14:00:00', 1, 2, '90123456-8'),  -- Reserva confirmada para Ricardo
('2024-10-01 12:30:00', 2, 2, '01234567-9'),  -- Reserva cancelada para Gabriela
('2024-10-02 10:00:00', 1, 3, '10123456-0'),  -- Reserva confirmada para Diego
('2024-10-03 12:00:00', 2, 4, '20123457-1'),  -- Reserva cancelada para Valentina
('2024-10-04 14:30:00', 3, 3, '30123458-2'),  -- Reserva pendiente para Andrés
('2024-10-05 16:45:00', 1, 4, '40123459-3'),  -- Reserva confirmada para Mariana
('2024-10-06 08:00:00', 2, 3, '50123450-4'),  -- Reserva cancelada para Jorge
('2024-10-07 11:30:00', 3, 4, '60123461-5'),  -- Reserva pendiente para Natalia
('2024-10-08 13:00:00', 1, 3, '70123472-6'),  -- Reserva confirmada para Felipe
('2024-10-09 15:15:00', 3, 4, '80123483-7'),  -- Reserva pendiente para Lina
('2024-10-10 17:30:00', 1, 4, '90123494-8'),  -- Reserva confirmada para Santiago
('2024-10-11 19:00:00', 2, 3, '10234506-9');  -- Reserva cancelada para Carmen



-- Insertar pagos
INSERT INTO Pago (fecha, monto_pagado, ID_METODO_PAGO, ID_RESERVA) VALUES
('2024-09-20', 150.00, 1, 1),  -- Pago de Juan
('2024-09-21', 0.00, 2, 2),    -- Pago de María (cancelada)
('2024-09-22', 100.00, 1, 3),  -- Pago de Pedro
('2024-09-25', 200.00, 1, 4),  -- Pago de Ana
('2024-09-26', 0.00, 2, 5),    -- Pago de Carlos (cancelada)
('2024-09-27', 80.00, 1, 6),   -- Pago de Sofía
('2024-09-28', 120.00, 1, 7),  -- Pago de Luis
('2024-09-29', 200.00, 1, 8),  -- Pago de Elena
('2024-09-30', 150.00, 1, 9),  -- Pago de Ricardo
('2024-10-01', 0.00, 2, 10),   -- Pago de Gabriela (cancelada)
('2024-10-02', 120.00, 1, 11),  -- Pago de Diego
('2024-10-03', 0.00, 2, 12),   -- Pago de Valentina (cancelada)
('2024-10-04', 90.00, 1, 13),   -- Pago de Andrés
('2024-10-05', 150.00, 1, 14),  -- Pago de Mariana
('2024-10-06', 0.00, 2, 15),     -- Pago de Jorge (cancelada)
('2024-10-07', 80.00, 1, 16),    -- Pago de Natalia
('2024-10-08', 130.00, 1, 17),   -- Pago de Felipe
('2024-10-09', 200.00, 1, 18),   -- Pago de Lina
('2024-10-10', 140.00, 1, 19),   -- Pago de Santiago
('2024-10-11', 0.00, 2, 20);     -- Pago de Carmen (cancelada)

-- Insertar equipaje
-- Insertar datos en la tabla Equipaje con RUTs de los pasajeros
INSERT INTO Equipaje (Peso, Dimensiones, Tipo, ID_RESERVA, rut_pasajero) VALUES
(20.00, '55x35x25', 'Facturado', 1, '12345678-0'),  -- Equipaje de Juan
(10.00, '40x30x15', 'Mano', 2, '23456789-1'),       -- Equipaje de María (cancelada)
(8.00, '45x35x20', 'Mano', 3, '34567890-2'),        -- Equipaje de Pedro
(15.00, '60x40x25', 'Facturado', 4, '45678901-3'),  -- Equipaje de Ana
(8.00, '45x35x20', 'Mano', 5, '56789012-4'),        -- Equipaje de Carlos (cancelada)
(7.50, '50x30x15', 'Mano', 6, '67890123-5'),        -- Equipaje de Sofía
(25.00, '70x50x30', 'Facturado', 7, '78901234-6'),  -- Equipaje de Luis
(12.00, '60x40x20', 'Mano', 8, '89012345-7'),        -- Equipaje de Elena
(30.00, '80x60x40', 'Facturado', 9, '90123456-8'),  -- Equipaje de Ricardo
(5.00, '45x30x15', 'Mano', 10, '01234567-9'),      -- Equipaje de Gabriela (cancelada)
(25.00, '70x40x30', 'Facturado', 11, '10123456-0'),  -- Equipaje de Diego
(12.00, '55x35x25', 'Mano', 12, '20123457-1'),       -- Equipaje de Valentina (cancelada)
(10.00, '45x35x20', 'Mano', 13, '30123458-2'),        -- Equipaje de Andrés
(20.00, '60x40x25', 'Facturado', 14, '40123459-3'),  -- Equipaje de Mariana
(8.00, '40x30x15', 'Mano', 15, '50123450-4'),         -- Equipaje de Jorge (cancelada)
(7.50, '50x30x15', 'Mano', 16, '60123461-5'),         -- Equipaje de Natalia
(30.00, '80x60x40', 'Facturado', 17, '70123472-6'),  -- Equipaje de Felipe
(9.00, '45x35x20', 'Mano', 18, '80123483-7'),         -- Equipaje de Lina
(15.00, '60x40x20', 'Facturado', 19, '90123494-8'),  -- Equipaje de Santiago
(5.00, '55x35x25', 'Mano', 20, '10234506-9');         -- Equipaje de Carmen (cancelada)

-- Insertar reservas de asiento
INSERT INTO RESERVA_ASIENTO (ID_ASIENTO, ID_RESERVA) VALUES
(1, 1),  -- Asiento 1A reservado para Juan
(2, 2),  -- Asiento 1B reservado para María (cancelada)
(3, 3),  -- Asiento 2A reservado para Pedro
(4, 4),  -- Asiento 2B reservado para Ana
(5, 5),  -- Asiento 2C reservado para Carlos (cancelada)
(6, 6),  -- Asiento 3A reservado para Sofía
(7, 7),  -- Asiento 3B reservado para Luis
(8, 8),  -- Asiento 3C reservado para Elena
(9, 9),  -- Asiento 3D reservado para Ricardo
(10, 10), -- Asiento 3E reservado para Gabriela (cancelada)
(11, 11),  -- Asiento 1A reservado para Diego
(12, 12),  -- Asiento 1B reservado para Valentina (cancelada)
(13, 13),  -- Asiento 2A reservado para Andrés
(14, 14),  -- Asiento 2B reservado para Mariana
(15, 15),  -- Asiento 2C reservado para Jorge (cancelada)
(16, 16),  -- Asiento 3A reservado para Natalia
(17, 17),  -- Asiento 3B reservado para Felipe
(18, 18),  -- Asiento 3C reservado para Lina
(19, 19),  -- Asiento 3D reservado para Santiago
(20, 20);  -- Asiento 3E reservado para Carmen (cancelada)



SELECT
    r.ID_RESERVA,
    v.ID_VUELO,
    p.Nombre AS Pasajero_Nombre,
    p.Apellido AS Pasajero_Apellido,
    v.Fecha_Hora_Salida,
    v.Fecha_Hora_Llegada,
    v.Precio,
    pi.Nombre AS Piloto_Nombre,
    pi.Apellido AS Piloto_Apellido,
    estr.Descripcion
FROM
    Reserva r
JOIN
    Vuelo v ON r.ID_VUELO = v.ID_VUELO
JOIN
    Pasajero p on p.rut = r.rut_pasajero
JOIN
    Piloto pi on pi.rut = v.rut_piloto
JOIN estado_reserva estr on estr.id_estado_reserva = r.estado_reserva;   -- Asegúrate de que este campo sea correcto


select * from asiento;

select * from usuario u
  join roles rl
on rl.id_rol = u.id_rol;


