-- Drop de tablas si existen (en orden dependiente)
DROP TABLE IF EXISTS Reserva_Asiento CASCADE;
DROP TABLE IF EXISTS Pago CASCADE;
DROP TABLE IF EXISTS Reserva CASCADE;
DROP TABLE IF EXISTS Metodo_Pago CASCADE;
DROP TABLE IF EXISTS Estado_reserva CASCADE;
DROP TABLE IF EXISTS Vuelo CASCADE;
DROP TABLE IF EXISTS Estado_Vuelo CASCADE;
DROP TABLE IF EXISTS Asiento CASCADE;
DROP TABLE IF EXISTS Clase_asiento CASCADE;
DROP TABLE IF EXISTS Avion CASCADE;
DROP TABLE IF EXISTS Aeropuerto CASCADE;
DROP TABLE IF EXISTS Ciudad CASCADE;
DROP TABLE IF EXISTS Pais CASCADE;
DROP TABLE IF EXISTS Piloto CASCADE;
DROP TABLE IF EXISTS Pasajero CASCADE;
DROP TABLE IF EXISTS Usuario CASCADE;
DROP TABLE IF EXISTS Equipaje CASCADE;
DROP TABLE IF EXISTS precio_asiento CASCADE;
DROP TABLE IF EXISTS Roles CASCADE;
DROP TABLE IF EXISTS notificacion CASCADE;
DROP TABLE IF EXISTS contenido CASCADE;
DROP TABLE IF EXISTS CategoriaPost CASCADE;
DROP TABLE IF EXISTS Post CASCADE;
DROP TABLE IF EXISTS Comentario CASCADE;

-- Drop de secuencias si existen
DROP SEQUENCE IF EXISTS reserva_asiento_seq;
DROP SEQUENCE IF EXISTS pago_seq;
DROP SEQUENCE IF EXISTS metodo_pago_seq;
DROP SEQUENCE IF EXISTS estado_reserva_seq;
DROP SEQUENCE IF EXISTS reserva_seq;
DROP SEQUENCE IF EXISTS vuelo_seq;
DROP SEQUENCE IF EXISTS estado_vuelo_seq;
DROP SEQUENCE IF EXISTS asiento_seq;
DROP SEQUENCE IF EXISTS clase_asiento_seq;
DROP SEQUENCE IF EXISTS avion_seq;
DROP SEQUENCE IF EXISTS aeropuerto_seq;
DROP SEQUENCE IF EXISTS ciudad_seq;
DROP SEQUENCE IF EXISTS pais_seq;
DROP SEQUENCE IF EXISTS pasajero_seq;
DROP SEQUENCE IF EXISTS usuario_seq;
DROP SEQUENCE IF EXISTS equipaje_seq;
DROP SEQUENCE IF EXISTS piloto_seq;
DROP SEQUENCE IF EXISTS precio_asiento_seq;
DROP SEQUENCE IF EXISTS rol_seq CASCADE;
DROP SEQUENCE IF EXISTS notifica_seq CASCADE;
DROP SEQUENCE IF EXISTS catePost_seq CASCADE;
DROP SEQUENCE IF EXISTS post_seq CASCADE;
DROP SEQUENCE IF EXISTS comentario_seq CASCADE;
DROP SEQUENCE IF EXISTS contenido_seq CASCADE;

-- Crear secuencias

CREATE SEQUENCE aeropuerto_seq START WITH 1;
CREATE SEQUENCE asiento_seq START WITH 1;
CREATE SEQUENCE avion_seq START WITH 1;
CREATE SEQUENCE ciudad_seq START WITH 1;
CREATE SEQUENCE clase_asiento_seq START WITH 1;
CREATE SEQUENCE estado_reserva_seq START WITH 1;
CREATE SEQUENCE estado_vuelo_seq START WITH 1;
CREATE SEQUENCE pago_seq START WITH 1;
CREATE SEQUENCE pais_seq START WITH 1;
CREATE SEQUENCE pasajero_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE piloto_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE reserva_seq START WITH 1;
CREATE SEQUENCE reserva_asiento_seq START WITH 1;
CREATE SEQUENCE usuario_seq START WITH 1;
CREATE SEQUENCE vuelo_seq START WITH 1;
CREATE SEQUENCE precio_asiento_seq START WITH 1;
CREATE SEQUENCE equipaje_seq START WITH 1;
CREATE SEQUENCE metodo_pago_seq START WITH 1;
CREATE SEQUENCE rol_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE contenido_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE notifica_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE catePost_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE post_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE comentario_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE Roles (
    id_rol INT PRIMARY KEY DEFAULT nextval('rol_seq'),        -- ID único para cada rol
    nombre VARCHAR(50) NOT NULL UNIQUE,
	descripcion varchar(100) not null

);


-- Crear la tabla Usuario
-- Crear la tabla Usuario
CREATE TABLE Usuario (
    --ID_USUARIO INT DEFAULT nextval('usuario_seq'),
	--RUT VARCHAR(12) NOT NULL UNIQUE,
    RUT VARCHAR(12) PRIMARY KEY,  -- Usando RUT como clave primaria
    Nombre VARCHAR(255) NOT NULL,
    Apellido VARCHAR(255) NOT NULL,
    Correo_Electronico VARCHAR(100) NOT NULL UNIQUE,
    Telefono VARCHAR(255) NOT NULL,
    Documento_Identidad VARCHAR(20) NOT NULL UNIQUE,
    Fecha_Nacimiento DATE NOT NULL,
    Contrasena VARCHAR(100) NOT NULL,
	id_rol int REFERENCES Roles(id_rol),
    Rol VARCHAR(50) NOT NULL CHECK (Rol IN ('Pasajero', 'Piloto','admin'))
);


-- Crear la tabla Piloto
CREATE TABLE Piloto (
 	--ID_PILOTO INT PRIMARY KEY DEFAULT nextval('piloto_seq'),
    RUT VARCHAR(12) PRIMARY KEY,
	Licencia VARCHAR(20) NOT NULL,
    Experiencia_anos INT NOT NULL
    --CONSTRAINT fk_usuario FOREIGN KEY (ID_PILOTO) REFERENCES Usuario(ID_USUARIO)
) INHERITS (Usuario);

-- Crear la tabla Pasajero
CREATE TABLE Pasajero (
	RUT VARCHAR(12) PRIMARY KEY
	--ID_PASAJERO INT PRIMARY KEY DEFAULT nextval('pasajero_seq')
	--CONSTRAINT fk_usuario FOREIGN KEY (ID_PASAJERO) REFERENCES Usuario(ID_USUARIO)
) INHERITS (Usuario);

-- Crear la tabla Pais
CREATE TABLE Pais (
    ID_PAIS INT PRIMARY KEY DEFAULT nextval('pais_seq'),
    Nombre VARCHAR(100) NOT NULL
);

-- Crear la tabla Ciudad
CREATE TABLE Ciudad (
    ID_CIUDAD INT PRIMARY KEY DEFAULT nextval('ciudad_seq'),
    Nombre VARCHAR(100) NOT NULL,
    ID_PAIS INT REFERENCES Pais(ID_PAIS)
);

-- Crear la tabla Aeropuerto
CREATE TABLE Aeropuerto (
    ID_AEROPUERTO INT PRIMARY KEY DEFAULT nextval('aeropuerto_seq'),
    Nombre_Aeropuerto VARCHAR(100) NOT NULL,
    Ciudad INT REFERENCES Ciudad(ID_CIUDAD),
    Codigo_IATA VARCHAR(10) NOT NULL
);

-- Crear la tabla Avion
CREATE TABLE Avion (
    ID_AVION INT PRIMARY KEY DEFAULT nextval('avion_seq'),
    Numero_de_Registro VARCHAR(20) NOT NULL,
    Modelo VARCHAR(100) NOT NULL,
    Fabricante VARCHAR(100) NOT NULL,
    Ano_de_Fabricacion INT NOT NULL,
    Capacidad_de_Pasajeros INT NOT NULL,
    Capacidad_de_Carga INT NOT NULL,
    Estado_de_Mantenimiento VARCHAR(50) NOT NULL,
	cap_economica INT NOT NULL,
	cap_ejecutiva INT NOT NULL,
	cap_primera INT NOT NULL

);

-- Crear la tabla Clase_asiento
CREATE TABLE Clase_asiento (
    ID_CLASE INT PRIMARY KEY DEFAULT nextval('clase_asiento_seq'),
    Descripcion VARCHAR(50) NOT NULL
);

-- Crear la tabla Asiento
CREATE TABLE Asiento (
    ID_ASIENTO INT PRIMARY KEY DEFAULT nextval('asiento_seq'),
    Numero_Asiento VARCHAR(10) NOT NULL,
    ID_CLASE INT REFERENCES Clase_asiento(ID_CLASE),
    ID_AVION INT REFERENCES Avion(ID_AVION)
);

-- Crear la tabla Estado_Vuelo
CREATE TABLE Estado_Vuelo (
    ID_ESTADO_VUELO INT PRIMARY KEY DEFAULT nextval('estado_vuelo_seq'),
    Descripcion VARCHAR(100) NOT NULL,
	Estado VARCHAR(100) NOT NULL
);

-- Crear la tabla Vuelo
CREATE TABLE Vuelo (
    ID_VUELO INT PRIMARY KEY DEFAULT nextval('vuelo_seq'),
    Numero_Vuelo VARCHAR(220),
    Fecha_Hora_Salida TIMESTAMP NOT NULL,
    Fecha_Hora_Llegada TIMESTAMP NOT NULL,
    ID_AEROPUERTO_SALIDA INT REFERENCES Aeropuerto(ID_AEROPUERTO),
    ID_AEROPUERTO_LLEGADA INT REFERENCES Aeropuerto(ID_AEROPUERTO),
    --Duracion INTERVAL NOT NULL,
	Precio int,
    ID_ESTADO_VUELO INT REFERENCES Estado_Vuelo(ID_ESTADO_VUELO),
    ID_AVION INT REFERENCES Avion(ID_AVION),
    RUT_PILOTO VARCHAR(12)  REFERENCES Piloto(RUT)
);

-- Crear la tabla Estado_reserva
CREATE TABLE Estado_reserva (
    ID_ESTADO_RESERVA INT PRIMARY KEY DEFAULT nextval('estado_reserva_seq'),
    Descripcion VARCHAR(100) NOT NULL
);

-- Crear la tabla Metodo_Pago
CREATE TABLE Metodo_Pago (
    ID_METODO_PAGO INT PRIMARY KEY DEFAULT nextval('metodo_pago_seq'),
    Descripcion VARCHAR(100) NOT NULL
);

-- Crear la tabla Reserva
CREATE TABLE Reserva (
    ID_RESERVA INT PRIMARY KEY DEFAULT nextval('reserva_seq'),
    Fecha_Reserva TIMESTAMP NOT NULL,
    Estado_Reserva INT REFERENCES Estado_reserva(ID_ESTADO_RESERVA),
    ID_VUELO INT REFERENCES Vuelo(ID_VUELO),
    RUT_PASAJERO VARCHAR(12) REFERENCES Pasajero(RUT)
);

-- Crear la tabla Pago
CREATE TABLE Pago (
    ID_PAGO INT PRIMARY KEY DEFAULT nextval('pago_seq'),
    monto_pagado DECIMAL(10, 2) NOT NULL,
    Fecha TIMESTAMP NOT NULL,
    ID_RESERVA INT REFERENCES Reserva(ID_RESERVA),
    ID_METODO_PAGO INT REFERENCES Metodo_Pago(ID_METODO_PAGO)
);

-- Crear la tabla Equipaje
CREATE TABLE Equipaje (
    ID_EQUIPAJE INT PRIMARY KEY DEFAULT nextval('equipaje_seq'),
    Peso DECIMAL(10, 2) NOT NULL,
    Dimensiones VARCHAR(10) NOT NULL,
    Tipo VARCHAR(20) NOT NULL,                 -- Agregado
    ID_RESERVA INT,                            -- Agregado
    RUT_PASAJERO VARCHAR(12) REFERENCES Pasajero(RUT)
);

-- Crear la tabla Reserva_Asiento
CREATE TABLE Reserva_Asiento (
    ID_RESERVA_ASIENTO INT PRIMARY KEY DEFAULT nextval('reserva_asiento_seq'),
    ID_RESERVA INT REFERENCES Reserva(ID_RESERVA),
    ID_ASIENTO INT REFERENCES Asiento(ID_ASIENTO)
);


create table precio_asiento(
	id_precio_asiento INT PRIMARY KEY,
	precio INT NOT NULL,
	id_vuelo INT REFERENCES vuelo(ID_VUELO),
	ID_CLASE INT REFERENCES Clase_asiento(ID_CLASE)

);


create table notificacion(

	id_notificacion int primary key DEFAULT nextval('notifica_seq'),
	RUT VARCHAR(12) ,
	titulo varchar(55) not null,
	mensaje text not null,
	fecha TIMESTAMP not null,
	leida BOOLEAN not null

);

CREATE TABLE contenido (
    id int primary key DEFAULT nextval('contenido_seq'),
    tipo VARCHAR(255),
    titulo VARCHAR(255),
    seccion VARCHAR(255),
    contenido TEXT
);


CREATE TABLE CategoriaPost (
    id_categoria_post int primary key DEFAULT nextval('catePost_seq'),
    nombre VARCHAR(55) not null,
	descripcion VARCHAR(155) not null
);


CREATE TABLE Post (
    id_post int primary key DEFAULT nextval('post_seq'),
    titulo VARCHAR(255) not null,
    fecha date not null,
    contenido TEXT,
    --rut VARCHAR(12)  REFERENCES Usuario(RUT),
	id_categoria_post int REFERENCES CategoriaPost(id_categoria_post)
);



CREATE TABLE Comentario (
    id_comentario int primary key DEFAULT nextval('comentario_seq'),
    texto varchar(255),
	rut VARCHAR(12)  REFERENCES Pasajero(RUT),
	id_post int REFERENCES Post(id_post)
);


/*

ALTER TABLE notificacion
ADD CONSTRAINT fk_rut_usuario
FOREIGN KEY (rut) REFERENCES usuario(rut) ON DELETE CASCADE;
*/

