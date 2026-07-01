
-- Eliminar Secuencias con CASCADE
DROP SEQUENCE IF EXISTS rol_seq CASCADE;
DROP SEQUENCE IF EXISTS usuario_seq CASCADE;
DROP SEQUENCE IF EXISTS pais_seq CASCADE;
DROP SEQUENCE IF EXISTS ciudad_seq CASCADE;
DROP SEQUENCE IF EXISTS aeropuerto_seq CASCADE;
DROP SEQUENCE IF EXISTS avion_seq CASCADE;
DROP SEQUENCE IF EXISTS clase_asiento_seq CASCADE;
DROP SEQUENCE IF EXISTS asiento_seq CASCADE;
DROP SEQUENCE IF EXISTS vuelo_seq CASCADE;
DROP SEQUENCE IF EXISTS estado_reserva_seq CASCADE;
DROP SEQUENCE IF EXISTS metodo_pago_seq CASCADE;
DROP SEQUENCE IF EXISTS reserva_seq CASCADE;
DROP SEQUENCE IF EXISTS pago_seq CASCADE;
DROP SEQUENCE IF EXISTS equipaje_seq CASCADE;
DROP SEQUENCE IF EXISTS reserva_asiento_seq CASCADE;
DROP SEQUENCE IF EXISTS precio_asiento_seq CASCADE;
DROP SEQUENCE IF EXISTS escala_seq CASCADE;
DROP SEQUENCE IF EXISTS puerta_seq CASCADE;
DROP SEQUENCE IF EXISTS checkin_seq CASCADE;
DROP SEQUENCE IF EXISTS estado_vuelo_seq CASCADE;
DROP SEQUENCE IF EXISTS habilidad_tripulacion_seq CASCADE;
DROP SEQUENCE IF EXISTS turno_tripulacion_seq CASCADE;
DROP SEQUENCE IF EXISTS aerolinea_seq CASCADE;
DROP SEQUENCE IF EXISTS fabricante_seq CASCADE;
DROP SEQUENCE IF EXISTS modelo_avion_seq CASCADE;
DROP SEQUENCE IF EXISTS segmento_vuelo_seq CASCADE;
DROP SEQUENCE IF EXISTS asignacion_puerta_seq CASCADE;
DROP SEQUENCE IF EXISTS itinerario_seq CASCADE;
DROP SEQUENCE IF EXISTS notificacion_seq CASCADE;
DROP SEQUENCE IF EXISTS capacidad_clase_seq CASCADE;
DROP SEQUENCE IF EXISTS tipo_equipaje_seq CASCADE;
DROP SEQUENCE IF EXISTS turno_seq CASCADE;
DROP SEQUENCE IF EXISTS reserva_itinerario_seq CASCADE;
DROP SEQUENCE IF EXISTS itinerario_vuelo_seq CASCADE;
DROP SEQUENCE IF EXISTS rolusuario_id_seq CASCADE;
DROP SEQUENCE IF EXISTS tipo_turno_seq CASCADE;

-- Eliminar Tablas con CASCADE
DROP TABLE IF EXISTS estado_vuelo CASCADE;
DROP TABLE IF EXISTS Vuelo CASCADE;
DROP TABLE IF EXISTS Reserva CASCADE;
DROP TABLE IF EXISTS Pago CASCADE;
DROP TABLE IF EXISTS Equipaje CASCADE;
DROP TABLE IF EXISTS Checkin CASCADE;
DROP TABLE IF EXISTS Estado_reserva CASCADE;
DROP TABLE IF EXISTS Metodo_Pago CASCADE;
DROP TABLE IF EXISTS Pasajero CASCADE;
DROP TABLE IF EXISTS Tripulacion CASCADE;
DROP TABLE IF EXISTS Tipo_Metodo_Pago CASCADE;
DROP TABLE IF EXISTS Tipo_Equipaje CASCADE;
DROP TABLE IF EXISTS Tipo_Turno CASCADE;
DROP TABLE IF EXISTS Personal_Operativo CASCADE;
DROP TABLE IF EXISTS Capacidad_Clase CASCADE;
DROP TABLE IF EXISTS Asignacion_Puerta CASCADE;
DROP TABLE IF EXISTS Turno_Tripulacion CASCADE;
DROP TABLE IF EXISTS Segmento_Vuelo CASCADE;
DROP TABLE IF EXISTS Precio_Asiento CASCADE;
DROP TABLE IF EXISTS Notificacion CASCADE;
DROP TABLE IF EXISTS Piloto CASCADE;
DROP TABLE IF EXISTS Aeropuerto CASCADE;
DROP TABLE IF EXISTS Puerta_Embarque CASCADE;
DROP TABLE IF EXISTS Ciudad CASCADE;
DROP TABLE IF EXISTS Pais CASCADE;
DROP TABLE IF EXISTS Fabricante CASCADE;
DROP TABLE IF EXISTS Modelo_Avion CASCADE;
DROP TABLE IF EXISTS Avion CASCADE;
DROP TABLE IF EXISTS Clase_asiento CASCADE;
DROP TABLE IF EXISTS Asiento CASCADE;
DROP TABLE IF EXISTS Reserva_Asiento CASCADE;
DROP TABLE IF EXISTS Roles CASCADE;
DROP TABLE IF EXISTS Usuario CASCADE;
DROP TABLE IF EXISTS RolUsuario CASCADE;
DROP TABLE IF EXISTS Personal_Administrativo CASCADE;
DROP TABLE IF EXISTS Aerolinea CASCADE;
DROP TABLE IF EXISTS Itinerario_Vuelo CASCADE;
DROP TABLE IF EXISTS Itinerario CASCADE;
DROP TABLE IF EXISTS Reserva_Itinerario CASCADE;
DROP TABLE IF EXISTS Turno CASCADE;
DROP TABLE IF EXISTS Continente CASCADE;
DROP TABLE IF EXISTS seguimiento_vuelo CASCADE;




-- SECUENCIAS INICIALES
CREATE SEQUENCE rol_seq;
CREATE SEQUENCE usuario_seq;
CREATE SEQUENCE pais_seq;
CREATE SEQUENCE ciudad_seq;
CREATE SEQUENCE aeropuerto_seq;
CREATE SEQUENCE avion_seq;
CREATE SEQUENCE clase_asiento_seq;
CREATE SEQUENCE asiento_seq;
CREATE SEQUENCE vuelo_seq;
CREATE SEQUENCE estado_reserva_seq;
CREATE SEQUENCE metodo_pago_seq;
CREATE SEQUENCE reserva_seq;
CREATE SEQUENCE pago_seq;
CREATE SEQUENCE equipaje_seq;
CREATE SEQUENCE reserva_asiento_seq;
CREATE SEQUENCE precio_asiento_seq;
CREATE SEQUENCE escala_seq;
CREATE SEQUENCE puerta_seq;
CREATE SEQUENCE checkin_seq;
CREATE SEQUENCE estado_vuelo_seq START WITH 1;

CREATE SEQUENCE habilidad_tripulacion_seq;

CREATE SEQUENCE turno_tripulacion_seq;
CREATE SEQUENCE tipo_equipaje_seq START 1;
CREATE SEQUENCE turno_seq START 1;
CREATE SEQUENCE itinerario_vuelo_seq START 1;
CREATE SEQUENCE reserva_itinerario_seq START 1;
CREATE SEQUENCE aerolinea_seq START 1;
CREATE SEQUENCE fabricante_seq START 1;
CREATE SEQUENCE modelo_avion_seq START 1;
CREATE SEQUENCE segmento_vuelo_seq START 1;
CREATE SEQUENCE asignacion_puerta_seq START 1;
CREATE SEQUENCE itinerario_seq START 1;
CREATE SEQUENCE notificacion_seq START 1;
CREATE SEQUENCE tipo_turno_seq START 1;
CREATE SEQUENCE capacidad_clase_seq START 1;
CREATE SEQUENCE rolusuario_id_seq START 1 INCREMENT 1;





-- TABLAS Usuarios

CREATE TABLE Roles (
    id_rol INT PRIMARY KEY DEFAULT nextval('rol_seq'),
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(100) NOT NULL
);

CREATE TABLE Usuario (
    RUT VARCHAR(12) PRIMARY KEY,
    Nombre VARCHAR(255) NOT NULL,
    Apellido VARCHAR(255) NOT NULL,
    Correo_Electronico VARCHAR(100) NOT NULL UNIQUE,
    Telefono VARCHAR(255) NOT NULL,
    Documento_Identidad VARCHAR(20) NOT NULL UNIQUE,
    Fecha_Nacimiento DATE NOT NULL,
    Contrasena VARCHAR(100) NOT NULL,
    Fecha_Registro TIMESTAMP DEFAULT NOW()
);

-- 1. Crear secuencia


-- 2. Crear tabla RolUsuario con ID como PK
CREATE TABLE RolUsuario (
    id_rol_usuario INT PRIMARY KEY DEFAULT nextval('rolusuario_id_seq'),  -- Nueva PK autoincremental
    id_rol INTEGER NOT NULL,
    rut_usuario VARCHAR(15) NOT NULL,

     CONSTRAINT unique_rol_usuario UNIQUE (id_rol, rut_usuario),
    FOREIGN KEY (id_rol) REFERENCES Roles (id_rol),
    FOREIGN KEY (rut_usuario) REFERENCES Usuario (RUT)

);


-- PASAJERO Y PROGRAMA DE FIDELIDAD
CREATE TABLE Pasajero (
    RUT VARCHAR(12) PRIMARY KEY REFERENCES Usuario(RUT),
	Tipo_Documento VARCHAR(20), -- Ejemplo: Pasaporte, DNI, etc.
    Numero_Documento VARCHAR(50),
    Fecha_Nacimiento DATE,
    Nacionalidad VARCHAR(50)
);



-- TRIPULACIÓN
CREATE TABLE Piloto (
    RUT VARCHAR(12) PRIMARY KEY REFERENCES Usuario(RUT),
    Licencia VARCHAR(20) NOT NULL,
    Experiencia_anos INT NOT NULL,
    Especializaciones TEXT
);


CREATE TABLE Tripulacion (
    RUT VARCHAR(12) PRIMARY KEY REFERENCES Usuario(RUT),
    Cargo VARCHAR(50) NOT NULL, -- Ej: Copiloto, Azafata, Jefe de Cabina
    Fecha_Ingreso TIMESTAMP DEFAULT NOW()
);

/*CREATE TABLE Personal_Operativo (
    RUT VARCHAR(12) PRIMARY KEY REFERENCES Usuario(RUT),
    Cargo VARCHAR(50) NOT NULL, -- Piloto, Copiloto, Azafata, Jefe de Cabina
    Fecha_Ingreso TIMESTAMP DEFAULT NOW(),
    Licencia VARCHAR(50), -- Solo aplica a pilotos
    Experiencia_Anios INT,
    Especializaciones TEXT
);*/

CREATE TABLE Personal_Administrativo (
    RUT VARCHAR(12) PRIMARY KEY REFERENCES Usuario(RUT),
    Departamento VARCHAR(100) NOT NULL, -- Ej: Recursos Humanos, Finanzas, IT, Marketing
    Cargo VARCHAR(100) NOT NULL,        -- Ej: Analista, Gerente, Coordinador
    Fecha_Contratacion TIMESTAMP DEFAULT NOW(),
    Nivel_Acceso VARCHAR(50),           -- Opcional: Bajo, Medio, Alto
    Titulo_Profesional VARCHAR(100),    -- Ej: Ingeniero Comercial, Contador, etc.
    Experiencia_Anios INT
);



CREATE TABLE Tipo_Equipaje (
    ID_TIPO INT PRIMARY KEY DEFAULT nextval('tipo_equipaje_seq'), -- Secuencia añadida
    Nombre VARCHAR(100)
);



CREATE TABLE Tipo_Turno (
    ID_TIPO INT PRIMARY KEY DEFAULT nextval('tipo_turno_seq'), -- Secuencia añadida,
    Nombre VARCHAR(100)
);


CREATE TABLE Continente (
    ID_CONTINENTE INT PRIMARY KEY,
    Nombre VARCHAR(100) NOT NULL
);

-- LOCALIZACIÓN
CREATE TABLE Pais (
    ID_PAIS INT PRIMARY KEY DEFAULT nextval('pais_seq'),
    Nombre VARCHAR(100) NOT NULL,
    ID_CONTINENTE INT,
    CONSTRAINT fk_pais_continente FOREIGN KEY (ID_CONTINENTE) REFERENCES Continente(ID_CONTINENTE)
);

--ALTER TABLE Pais ADD COLUMN ID_CONTINENTE INT;

--ALTER TABLE Pais ADD CONSTRAINT fk_pais_continente FOREIGN KEY (ID_CONTINENTE) REFERENCES Continente(ID_CONTINENTE);

CREATE TABLE Ciudad (
    ID_CIUDAD INT PRIMARY KEY DEFAULT nextval('ciudad_seq'),
    Nombre VARCHAR(100) NOT NULL,
    ID_PAIS INT REFERENCES Pais(ID_PAIS)
);

CREATE TABLE Aeropuerto (
    ID_AEROPUERTO INT PRIMARY KEY DEFAULT nextval('aeropuerto_seq'),
    Nombre_Aeropuerto VARCHAR(100) NOT NULL,
    ID_CIUDAD INT REFERENCES Ciudad(ID_CIUDAD),
    Codigo_IATA VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE Aerolinea (
    ID_AEROLINEA INT PRIMARY KEY DEFAULT nextval('aerolinea_seq'),
    Nombre VARCHAR(255),
    Codigo VARCHAR(10)
);


CREATE TABLE Puerta_Embarque (
    ID_PUERTA INT PRIMARY KEY DEFAULT nextval('puerta_seq'),
    Codigo_Puerta VARCHAR(10) NOT NULL,
    Terminal VARCHAR(50),
    ID_AEROPUERTO INT REFERENCES Aeropuerto(ID_AEROPUERTO)
);


-- AVIONES Y ASIENTOS

CREATE TABLE Clase_asiento (
    ID_CLASE INT PRIMARY KEY DEFAULT nextval('clase_asiento_seq'),
    Descripcion VARCHAR(50) NOT NULL
);

CREATE TABLE Fabricante (
    ID_FABRICANTE INT PRIMARY KEY DEFAULT nextval('fabricante_seq'),
    Nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE Modelo_Avion (
    ID_MODELO INT PRIMARY KEY DEFAULT nextval('modelo_avion_seq'),
    Nombre VARCHAR(100) NOT NULL UNIQUE,
    ID_FABRICANTE INT REFERENCES Fabricante(ID_FABRICANTE) ON DELETE CASCADE
);


CREATE TABLE Avion (
    ID_AVION INT PRIMARY KEY DEFAULT nextval('avion_seq'),
    Numero_de_Registro VARCHAR(20) NOT NULL UNIQUE,
    ID_MODELO INT REFERENCES Modelo_Avion(ID_MODELO) ON DELETE SET NULL,
    Ano_de_Fabricacion INT NOT NULL,
    Capacidad_de_Pasajeros INT NOT NULL,
    Capacidad_de_Carga INT NOT NULL,
    Estado_de_Mantenimiento VARCHAR(50) NOT NULL,
    Fecha_Proximo_Mantenimiento TIMESTAMP
);


CREATE TABLE Capacidad_Clase (
    ID_CAPACIDAD_CLASE INT PRIMARY KEY DEFAULT nextval('capacidad_clase_seq'), -- Usar la secuencia
    ID_AVION INT,                                 -- Referencia al avión
    ID_CLASE INT ,                         -- Referencia a la clase de asiento
    Cantidad INT NOT NULL,                                                    -- Cantidad de asientos disponibles
    CONSTRAINT fk_avion FOREIGN KEY (ID_AVION) REFERENCES Avion(ID_AVION) ON DELETE CASCADE,
    CONSTRAINT fk_clase FOREIGN KEY (ID_CLASE) REFERENCES Clase_asiento(ID_CLASE) ON DELETE CASCADE
    --CONSTRAINT unique_avion_clase UNIQUE (ID_AVION, ID_CLASE)                 -- Se mantiene la unicidad de la combinación
);

CREATE TABLE Asiento (
    ID_ASIENTO INT PRIMARY KEY DEFAULT nextval('asiento_seq'),
    Numero_Asiento VARCHAR(10) NOT NULL,
    ID_CLASE INT REFERENCES Clase_asiento(ID_CLASE),
    ID_AVION INT REFERENCES Avion(ID_AVION)
);

--ALTER TABLE Asiento
--ADD CONSTRAINT unique_asiento_avion UNIQUE (Numero_Asiento, ID_AVION);


-- VUELO Y CONEXIONES
CREATE TABLE Estado_Vuelo (
    ID_ESTADO_VUELO INT PRIMARY KEY DEFAULT nextval('estado_vuelo_seq'),
    Descripcion VARCHAR(100) NOT NULL,
	Estado VARCHAR(100) NOT NULL
);




CREATE TABLE Vuelo (
    ID_VUELO INT PRIMARY KEY DEFAULT nextval('vuelo_seq'),
    Numero_Vuelo VARCHAR(255) ,  -- Número de vuelo (más corto)
    Fecha_Hora_Salida TIMESTAMP,  -- Fecha y hora de salida
    Fecha_Hora_Llegada TIMESTAMP,  -- Fecha y hora de llegada
    ID_ESTADO_VUELO INT REFERENCES Estado_Vuelo(ID_ESTADO_VUELO),  -- Estado del vuelo
    ID_AVION INT REFERENCES Avion(ID_AVION),  -- Relación con el avión
    RUT_PILOTO VARCHAR(12) REFERENCES Piloto(RUT),  -- Relación con piloto
    ID_AEROLINEA INT REFERENCES Aerolinea(ID_AEROLINEA)  -- Relación con aerolínea
);

-- Crear la tabla Precio_Asiento
CREATE TABLE Precio_Asiento (
    ID_PRECIO_ASIENTO INT PRIMARY KEY DEFAULT nextval('precio_asiento_seq'),
    PRECIO INT NOT NULL,
    ID_VUELO INT REFERENCES Vuelo(ID_VUELO), -- Relación con Vuelo
    ID_CLASE INT REFERENCES Clase_asiento(ID_CLASE), -- Relación con Clase de Asiento
    -- Relación de clave única por vuelo y clase de asiento
    CONSTRAINT unique_precio_vuelo_clase UNIQUE(ID_VUELO, ID_CLASE)
);



CREATE TABLE Segmento_Vuelo (
    ID_SEGMENTO INT PRIMARY KEY DEFAULT nextval('segmento_vuelo_seq'),
    ID_VUELO INT REFERENCES Vuelo(ID_VUELO),       -- A qué vuelo pertenece este segmento
    ORDEN_SEGMENTO INT ,                   -- Para saber qué tramo es (1º, 2º, etc.)
    ID_AEROPUERTO_ORIGEN INT ,
    ID_AEROPUERTO_DESTINO INT ,
    HORA_SALIDA TIMESTAMP,
    HORA_LLEGADA TIMESTAMP,
    DURACION_ESTIMADA INTERVAL DEFAULT '0',
    CONSTRAINT fk_aeropuerto_origen FOREIGN KEY (ID_AEROPUERTO_ORIGEN) REFERENCES Aeropuerto(ID_AEROPUERTO),
    CONSTRAINT fk_aeropuerto_destino FOREIGN KEY (ID_AEROPUERTO_DESTINO) REFERENCES Aeropuerto(ID_AEROPUERTO)
);

CREATE TABLE Asignacion_Puerta (
    ID_ASIGNACION INT PRIMARY KEY DEFAULT nextval('asignacion_puerta_seq'),
    ID_SEGMENTO INT REFERENCES Segmento_Vuelo(ID_SEGMENTO),
    ID_PUERTA INT REFERENCES Puerta_Embarque(ID_PUERTA),
    Hora_Asignacion TIMESTAMP DEFAULT NOW(),
    CONSTRAINT unique_asignacion_puerta UNIQUE(ID_SEGMENTO)

);

CREATE TABLE Itinerario (
    ID_ITINERARIO INT PRIMARY KEY DEFAULT nextval('itinerario_seq'),
    FECHA_CREACION TIMESTAMP DEFAULT NOW(),
    ORIGEN_AEROPUERTO INT ,
    DESTINO_AEROPUERTO INT ,
    HORA_SALIDA TIMESTAMP,
    HORA_LLEGADA TIMESTAMP,
    DURACION_TOTAL INTERVAL,
    NUMERO_ESCALAS INT,
    Precio_Base INT,
    CONSTRAINT fk_origen FOREIGN KEY (ORIGEN_AEROPUERTO) REFERENCES Aeropuerto(ID_AEROPUERTO),
    CONSTRAINT fk_destino FOREIGN KEY (DESTINO_AEROPUERTO) REFERENCES Aeropuerto(ID_AEROPUERTO)
);

CREATE TABLE Itinerario_Vuelo (
    ID_ITINERARIO_VUELO INT PRIMARY KEY DEFAULT nextval('itinerario_vuelo_seq'),
    ID_ITINERARIO INT REFERENCES Itinerario(ID_ITINERARIO),
    ID_VUELO INT REFERENCES Vuelo(ID_VUELO),
    ORDEN INT DEFAULT 1,
    TIEMPO_ESPERA INTERVAL DEFAULT '0',
    TIPO_CONEXION VARCHAR(255) -- Esta columna estaba faltando
);



CREATE TABLE Turno (
    ID_TURNO INT PRIMARY KEY DEFAULT nextval('turno_seq'), -- Secuencia añadida
    ID_VUELO INT NOT NULL REFERENCES Vuelo(ID_VUELO),
    Fecha DATE NOT NULL,
    Hora_Inicio TIMESTAMP NOT NULL,
    Hora_Fin TIMESTAMP NOT NULL,
    ID_TIPO_TURNO INT REFERENCES Tipo_Turno(ID_TIPO)
);



CREATE TABLE Turno_Tripulacion (
    ID_TURNO_Tripulacion INT PRIMARY KEY DEFAULT nextval('turno_tripulacion_seq'),
    RUT_TRIPULACION VARCHAR(12) NOT NULL ,  -- Relacionado al tripulante
    ID_TURNO INT NOT NULL,  -- Relacionado al turno específico
    CONSTRAINT fk_tripulacion FOREIGN KEY (RUT_TRIPULACION) REFERENCES Tripulacion(RUT),  -- Relación con tripulante
    CONSTRAINT fk_turno FOREIGN KEY (ID_TURNO) REFERENCES Turno(ID_TURNO)  -- Relación con turno
);





-- RESERVAS Y PAGOS
CREATE TABLE Estado_reserva (
    ID_ESTADO_RESERVA INT PRIMARY KEY DEFAULT nextval('estado_reserva_seq'),
    Descripcion VARCHAR(100) NOT NULL
);

CREATE TABLE Metodo_Pago (
    ID_METODO_PAGO INT PRIMARY KEY DEFAULT nextval('metodo_pago_seq'),
    Descripcion VARCHAR(100) NOT NULL
);

CREATE TABLE Reserva (
    ID_RESERVA INT PRIMARY KEY DEFAULT nextval('reserva_seq'),
    Fecha_Reserva TIMESTAMP NOT NULL,
    Estado_Reserva INT REFERENCES Estado_reserva(ID_ESTADO_RESERVA),
    RUT_PASAJERO VARCHAR(12) REFERENCES Pasajero(RUT),
    Total DECIMAL(10,2)
    --Tipo_Boleto VARCHAR(50) CHECK (Tipo_Boleto IN ('Económica', 'Ejecutiva', 'Primera Clase')),
    --Codigo_Reserva VARCHAR(50) UNIQUE
);



CREATE TABLE Reserva_Itinerario (
    ID_RESERVA_ITINERARIO INT PRIMARY KEY DEFAULT nextval('reserva_itinerario_seq'),
    ID_RESERVA INT NOT NULL REFERENCES Reserva(ID_RESERVA),  -- Relacionado con Reserva
    ID_ITINERARIO INT NOT NULL REFERENCES Itinerario(ID_ITINERARIO)  -- Relacionado con Itinerario
);



-- Crear la tabla Reserva_Asiento
-- Crear la tabla Reserva_Asiento
CREATE TABLE Reserva_Asiento (
    ID_RESERVA_ASIENTO INT PRIMARY KEY DEFAULT nextval('reserva_asiento_seq'),
    ID_RESERVA INT REFERENCES Reserva(ID_RESERVA),
    ID_VUELO INT REFERENCES Vuelo(ID_VUELO),
    ID_ASIENTO INT REFERENCES Asiento(ID_ASIENTO),
    CONSTRAINT unique_reserva_asiento UNIQUE(ID_VUELO, ID_ASIENTO)
);





CREATE TABLE Pago (
    ID_PAGO INT PRIMARY KEY DEFAULT nextval('pago_seq'),
    monto_pagado DECIMAL(10, 2) NOT NULL,
    Fecha TIMESTAMP NOT NULL,
    ID_RESERVA INT REFERENCES Reserva(ID_RESERVA),
    ID_METODO_PAGO INT REFERENCES Metodo_Pago(ID_METODO_PAGO)
);

/*CREATE TABLE Cancelacion (
    ID_CANCELACION INT PRIMARY KEY DEFAULT nextval('cancelacion_seq'),
    ID_RESERVA INT REFERENCES Reserva(ID_RESERVA),
    Fecha TIMESTAMP NOT NULL,
    Motivo TEXT,
    Estado VARCHAR(50) CHECK (Estado IN ('Pendiente', 'Procesado', 'Reembolsado')),
    Monto_Reembolso DECIMAL(10, 2) -- Puede ser igual al monto total o un porcentaje
);*/


CREATE TABLE Equipaje (
    ID_EQUIPAJE INT PRIMARY KEY DEFAULT nextval('equipaje_seq'),
    Peso DECIMAL(10, 2) NOT NULL,
    Dimensiones VARCHAR(20) NOT NULL,
    Tipo VARCHAR(20) NOT NULL,
    ID_RESERVA INT REFERENCES Reserva(ID_RESERVA),
    RUT_PASAJERO VARCHAR(12) REFERENCES Pasajero(RUT),
    ID_TIPO INT REFERENCES Tipo_Equipaje(ID_TIPO)
);



-- CHECK-IN
CREATE TABLE Checkin (
    ID_CHECKIN INT PRIMARY KEY DEFAULT nextval('checkin_seq'),
    ID_RESERVA INT REFERENCES Reserva(ID_RESERVA),
    Fecha_Hora TIMESTAMP,
    Metodo VARCHAR(50) CHECK (Metodo IN ('Web', 'App', 'Mostrador'))
);


CREATE TABLE Notificacion (
    ID_NOTIFICACION INT PRIMARY KEY DEFAULT nextval('notificacion_seq'),
    RUT_DESTINATARIO VARCHAR(12) REFERENCES Usuario(RUT),
    Titulo VARCHAR(100),
    Mensaje TEXT,
    Leido BOOLEAN DEFAULT FALSE,
    Fecha TIMESTAMP DEFAULT NOW()
);

CREATE TABLE Seguimiento_Vuelo (
    ID_SEGUIMIENTO SERIAL PRIMARY KEY,
    ID_VUELO INT REFERENCES Vuelo(ID_VUELO),
    Latitud DECIMAL(9,6),
    Longitud DECIMAL(9,6),
    Altitud INT,
    Velocidad INT,
    Timestamp TIMESTAMP DEFAULT now()
);


CREATE INDEX idx_idItinerario ON itinerario_vuelo(id_itinerario);

CREATE INDEX idx_idAsientoAvion ON asiento(id_avion);

/*ALTER TABLE Precio_Asiento DROP CONSTRAINT IF EXISTS precio_asiento_id_vuelo_fkey;
ALTER TABLE Segmento_Vuelo DROP CONSTRAINT IF EXISTS segmento_vuelo_id_vuelo_fkey;
ALTER TABLE Turno DROP CONSTRAINT IF EXISTS turno_id_vuelo_fkey;
ALTER TABLE Itinerario_Vuelo DROP CONSTRAINT IF EXISTS itinerario_vuelo_id_vuelo_fkey;
ALTER TABLE Reserva_Asiento DROP CONSTRAINT IF EXISTS reserva_asiento_id_vuelo_fkey;

ALTER TABLE Precio_Asiento
ADD CONSTRAINT precio_asiento_id_vuelo_fkey
FOREIGN KEY (ID_VUELO) REFERENCES Vuelo(ID_VUELO) ON DELETE CASCADE;

ALTER TABLE Segmento_Vuelo
ADD CONSTRAINT segmento_vuelo_id_vuelo_fkey
FOREIGN KEY (ID_VUELO) REFERENCES Vuelo(ID_VUELO) ON DELETE CASCADE;

ALTER TABLE Turno
ADD CONSTRAINT turno_id_vuelo_fkey
FOREIGN KEY (ID_VUELO) REFERENCES Vuelo(ID_VUELO) ON DELETE CASCADE;

ALTER TABLE Itinerario_Vuelo
ADD CONSTRAINT itinerario_vuelo_id_vuelo_fkey
FOREIGN KEY (ID_VUELO) REFERENCES Vuelo(ID_VUELO) ON DELETE CASCADE;

ALTER TABLE Reserva_Asiento
ADD CONSTRAINT reserva_asiento_id_vuelo_fkey
FOREIGN KEY (ID_VUELO) REFERENCES Vuelo(ID_VUELO) ON DELETE CASCADE;*/