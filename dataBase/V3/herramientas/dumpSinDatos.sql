

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
DROP SEQUENCE IF EXISTS tarifa_seq CASCADE;
DROP SEQUENCE IF EXISTS itinerario_tarifa_seq CASCADE;
DROP SEQUENCE IF EXISTS caracteristica_tarifa_seq CASCADE;
DROP SEQUENCE IF EXISTS tarifa_caracteristica_seq CASCADE;
DROP SEQUENCE IF EXISTS reserva_pasajero_seq CASCADE;


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
DROP TABLE IF EXISTS Itinerario_Tarifa CASCADE;
DROP TABLE IF EXISTS Tarifa CASCADE;
DROP TABLE IF EXISTS Caracteristica_Tarifa CASCADE;
DROP TABLE IF EXISTS Tarifa_Caracteristica CASCADE;
DROP TABLE IF EXISTS pasajero_reserva CASCADE;


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
CREATE SEQUENCE caracteristica_tarifa_seq START 1 INCREMENT 1;
CREATE SEQUENCE tarifa_caracteristica_seq START 1 INCREMENT 1;
CREATE SEQUENCE reserva_pasajero_seq START 1 INCREMENT 1;


CREATE SEQUENCE tarifa_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO CYCLE;

CREATE SEQUENCE itinerario_tarifa_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO CYCLE;








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
                         Correo_Electronico VARCHAR(100) UNIQUE,
                         Telefono VARCHAR(255) NOT NULL,
                         Documento_Identidad VARCHAR(20) NOT NULL UNIQUE,
                         Fecha_Nacimiento DATE NOT NULL,
                         Contrasena VARCHAR(100),
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

/*CREATE TABLE aeropuertos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    ciudad VARCHAR(255) NOT NULL,
    codigo_iata VARCHAR(3) NOT NULL,
    latitud DOUBLE PRECISION NOT NULL,
    longitud DOUBLE PRECISION NOT NULL
);*/

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


CREATE TABLE Tarifa (
                        ID_TARIFA INT PRIMARY KEY DEFAULT nextval('tarifa_seq'),
                        Nombre VARCHAR(50) NOT NULL UNIQUE
);


CREATE TABLE Caracteristica_Tarifa (
                                       ID_CARACTERISTICA INT PRIMARY KEY DEFAULT nextval('caracteristica_tarifa_seq'),
                                       Nombre VARCHAR(100) NOT NULL UNIQUE,
                                       Descripcion TEXT,
                                       Tipo_Dato VARCHAR(20)  CHECK (Tipo_Dato IN ('boolean', 'int', 'text'))
);

CREATE TABLE Tarifa_Caracteristica (
                                       ID_TARIFA_CARACTERISTICA INT PRIMARY KEY DEFAULT nextval('tarifa_caracteristica_seq'),
                                       ID_TARIFA INT REFERENCES Tarifa(ID_TARIFA) ON DELETE CASCADE,
                                       ID_CARACTERISTICA INT REFERENCES Caracteristica_Tarifa(ID_CARACTERISTICA) ON DELETE CASCADE,
                                       Valor VARCHAR(100),
                                       Valor_Bool BOOLEAN,
                                       Valor_Int INT,
                                       CONSTRAINT unique_tarifa_caracteristica UNIQUE (ID_TARIFA, ID_CARACTERISTICA)
);

-- Tabla Itinerario_Tarifa con ID autoincremental y restricción única para combinación
CREATE TABLE Itinerario_Tarifa (
                                   ID_ITINERARIO_TARIFA INT PRIMARY KEY DEFAULT nextval('itinerario_tarifa_seq'),
                                   ID_ITINERARIO INT NOT NULL REFERENCES Itinerario(ID_ITINERARIO) ON DELETE CASCADE,
                                   ID_TARIFA INT NOT NULL REFERENCES Tarifa(ID_TARIFA) ON DELETE CASCADE,
                                   Precio DECIMAL(10,2) NOT NULL,
                                   CONSTRAINT unique_itinerario_tarifa UNIQUE (ID_ITINERARIO, ID_TARIFA)
);

CREATE TABLE Turno (
                       ID_TURNO INT PRIMARY KEY DEFAULT nextval('turno_seq'), -- Secuencia añadida
                       ID_VUELO INT NOT NULL REFERENCES Vuelo(ID_VUELO),
                       Fecha DATE ,
                       Hora_Inicio TIMESTAMP ,
                       Hora_Fin TIMESTAMP ,
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
                                    ID_RESERVA INT NOT NULL REFERENCES Reserva(ID_RESERVA),
                                    ID_ITINERARIO INT NOT NULL REFERENCES Itinerario(ID_ITINERARIO),
                                    ID_ITINERARIO_TARIFA INT NOT NULL REFERENCES Itinerario_Tarifa(ID_ITINERARIO_TARIFA)
    --ID_TARIFA INT NOT NULL REFERENCES Tarifa(ID_TARIFA)
);


CREATE TABLE pasajero_reserva (
                                  id_pasajero_reserva INT PRIMARY KEY DEFAULT nextval('reserva_pasajero_seq'),
                                  id_reserva          INTEGER NOT NULL,
                                  rut                 VARCHAR(12) NOT NULL,
                                  CONSTRAINT fk_pasajero_reserva_reserva
                                      FOREIGN KEY (id_reserva)
                                          REFERENCES reserva(id_reserva),

                                  CONSTRAINT fk_pasajero_reserva_pasajero
                                      FOREIGN KEY (rut)
                                          REFERENCES pasajero(rut),

    -- Un pasajero no puede repetirse en la misma reserva
                                  CONSTRAINT uq_reserva_pasajero
                                      UNIQUE (id_reserva, rut)
);


-- Crear la tabla Reserva_Asiento
-- Crear la tabla Reserva_Asiento
CREATE TABLE Reserva_Asiento (
                                 ID_RESERVA_ASIENTO INT PRIMARY KEY DEFAULT nextval('reserva_asiento_seq'),
                                 ID_RESERVA INT REFERENCES Reserva(ID_RESERVA),
                                 ID_VUELO INT REFERENCES Vuelo(ID_VUELO),
                                 ID_ASIENTO INT REFERENCES Asiento(ID_ASIENTO),
                                 rut  VARCHAR(12) NOT NULL,

                                 CONSTRAINT fk_pasajero_reservaAsiento
                                     FOREIGN KEY (rut)
                                         REFERENCES pasajero(rut),
    --id_pasajero_reserva  INTEGER NOT NULL,

                                 CONSTRAINT unique_reserva_asiento UNIQUE(ID_VUELO, ID_ASIENTO)

    /*CONSTRAINT fk_reserva_asiento_pasajero_reserva
        FOREIGN KEY (id_pasajero_reserva)
        REFERENCES pasajero_reserva(id_pasajero_reserva),

    -- Un pasajero solo puede tener un asiento por vuelo
   CONSTRAINT uq_pasajero_vuelo
        UNIQUE (id_pasajero_reserva, id_vuelo)*/

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

/*CREATE TABLE Seguimiento_Vuelo (
    ID_SEGUIMIENTO SERIAL PRIMARY KEY,
    ID_VUELO INT REFERENCES Vuelo(ID_VUELO),
    Latitud DECIMAL(9,6),
    Longitud DECIMAL(9,6),
    Altitud INT,
    Velocidad INT,
    Timestamp TIMESTAMP DEFAULT now()
);*/


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



DO $$
DECLARE

cursoFunciones  cursor for
SELECT routine_name AS function_name,
       routine_type AS function_type
FROM information_schema.routines
WHERE   routine_schema = 'public'
order by routine_type;
BEGIN

for cols in cursoFunciones
	loop
     RAISE NOTICE 'ID: %, Name: %', cols.function_name, cols.function_type;

execute ' drop  '|| cols.function_type ||' '||cols.function_name||' cascade';

end loop;

END $$;


-- Crear la función del trigger
CREATE OR REPLACE FUNCTION fn_insertarAsientos()
RETURNS TRIGGER AS $$
DECLARE
indice INTEGER;
    letra CHAR;
    asiento VARCHAR;
BEGIN
FOR indice IN 0 .. NEW.cantidad - 1 LOOP
        letra := chr(65 + (indice % 6));  -- A-F
        asiento := (indice + 1) || letra;

INSERT INTO Asiento (Numero_Asiento, ID_CLASE, ID_AVION)
VALUES (asiento, NEW.ID_CLASE, NEW.ID_AVION);
END LOOP;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;




-- Crear el trigger que llama a la función cuando se inserta un avión
CREATE TRIGGER trigger_insertar_asientos
    AFTER INSERT ON Capacidad_Clase
    FOR EACH ROW
    EXECUTE FUNCTION fn_insertarAsientos();


/*ALTER TABLE Segmento_Vuelo DISABLE TRIGGER trg_set_orden_segmento_vuelo;

ALTER TABLE Segmento_Vuelo DISABLE TRIGGER trg_set_numero_vuelo;

ALTER TABLE Segmento_Vuelo DISABLE TRIGGER trg_set_fecha_vuelo;

ALTER TABLE Itinerario_Vuelo DISABLE TRIGGER trg_set_fecha_itinerario;

ALTER TABLE Itinerario_Vuelo DISABLE TRIGGER trg_set_orden_itinerario_vuelo;*/




DO $$
DECLARE

cursoFunciones  cursor for
SELECT routine_name AS function_name,
       routine_type AS function_type
FROM information_schema.routines
WHERE   routine_schema = 'public'
order by routine_type;
BEGIN

for cols in cursoFunciones
	loop
     RAISE NOTICE 'ID: %, Name: %', cols.function_name, cols.function_type;

execute ' drop  '|| cols.function_type ||' '||cols.function_name||' cascade';

end loop;

END $$;





-- Crear la función del trigger
CREATE OR REPLACE FUNCTION fn_insertarAsientos()
RETURNS TRIGGER AS $$
DECLARE
indice INTEGER;
    letra CHAR;
    asiento VARCHAR;
BEGIN
FOR indice IN 0 .. NEW.cantidad - 1 LOOP
        letra := chr(65 + (indice % 6));  -- A-F
        asiento := (indice + 1) || letra;

INSERT INTO Asiento (Numero_Asiento, ID_CLASE, ID_AVION)
VALUES (asiento, NEW.ID_CLASE, NEW.ID_AVION);
END LOOP;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;




-- Crear el trigger que llama a la función cuando se inserta un avión
CREATE TRIGGER trigger_insertar_asientos
    AFTER INSERT ON Capacidad_Clase
    FOR EACH ROW
    EXECUTE FUNCTION fn_insertarAsientos();



CREATE OR REPLACE FUNCTION fn_set_orden_itinerario_vuelo()
RETURNS TRIGGER AS $$
DECLARE
ultimo_orden INT;
BEGIN
    -- Si es UPDATE y viene NULL, rescatamos el anterior
    IF (TG_OP = 'UPDATE' AND NEW.ORDEN IS NULL) THEN
        NEW.ORDEN := OLD.ORDEN;
END IF;

    -- Si sigue siendo NULL (en INSERT), calculamos el siguiente
    IF NEW.ORDEN IS NULL THEN
SELECT COALESCE(MAX(ORDEN), 0) INTO ultimo_orden
FROM Itinerario_Vuelo
WHERE ID_ITINERARIO = NEW.ID_ITINERARIO;

NEW.ORDEN := ultimo_orden + 1;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_set_orden_itinerario_vuelo ON Itinerario_Vuelo;
CREATE TRIGGER trg_set_orden_itinerario_vuelo
    BEFORE INSERT OR UPDATE ON Itinerario_Vuelo
                         FOR EACH ROW
                         EXECUTE FUNCTION fn_set_orden_itinerario_vuelo();






CREATE OR REPLACE FUNCTION fn_set_orden_segmento_vuelo()
RETURNS TRIGGER AS $$
DECLARE
v_ultimo_orden INT;
BEGIN
    -- 1. Manejo para UPDATE
    IF (TG_OP = 'UPDATE') THEN
        -- Si JPA envió un NULL en el orden, rescatamos el valor que ya tenía el registro
        IF NEW.ORDEN_SEGMENTO IS NULL THEN
            NEW.ORDEN_SEGMENTO := OLD.ORDEN_SEGMENTO;
END IF;

        -- Si el vuelo NO cambió, ya no hay nada más que hacer, retornamos el NEW corregido
        IF (OLD.ID_VUELO = NEW.ID_VUELO) THEN
            RETURN NEW;
END IF;

        -- Si cambió el ID_VUELO y el orden seguía siendo el viejo,
        -- quizás quieras recalcularlo para el nuevo vuelo.
        -- En ese caso, podrías ponerlo en NULL aquí para que siga a la lógica de abajo.
        IF (OLD.ID_VUELO <> NEW.ID_VUELO AND NEW.ORDEN_SEGMENTO = OLD.ORDEN_SEGMENTO) THEN
             NEW.ORDEN_SEGMENTO := NULL;
END IF;
END IF;

    -- 2. Lógica de Asignación Automática (Solo si es NULL después de las validaciones de arriba)
    IF NEW.ORDEN_SEGMENTO IS NULL THEN
SELECT COALESCE(MAX(ORDEN_SEGMENTO), 0)
INTO v_ultimo_orden
FROM Segmento_Vuelo
WHERE ID_VUELO = NEW.ID_VUELO;

NEW.ORDEN_SEGMENTO := v_ultimo_orden + 1;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;



-- Trigger configurado para actuar antes de insertar o actualizar
CREATE TRIGGER trg_set_orden_segmento_vuelo
    BEFORE INSERT OR UPDATE ON Segmento_Vuelo
                         FOR EACH ROW
                         EXECUTE FUNCTION fn_set_orden_segmento_vuelo();





CREATE OR REPLACE FUNCTION fn_set_numero_vuelo()
RETURNS TRIGGER AS $$
DECLARE
codigoArp1 varchar;
    codigoArp2 varchar;
    numeroVueloN varchar;
BEGIN
    -- Obtenemos los códigos IATA de origen y destino
SELECT Codigo_IATA INTO codigoArp1 FROM aeropuerto WHERE id_aeropuerto = NEW.ID_AEROPUERTO_ORIGEN;
SELECT Codigo_IATA INTO codigoArp2 FROM aeropuerto WHERE id_aeropuerto = NEW.ID_AEROPUERTO_DESTINO;

-- Construimos el número basado en el ID real del segmento
numeroVueloN := codigoArp1 || '-' || codigoArp2 || NEW.ID_SEGMENTO::TEXT;

UPDATE vuelo
SET numero_vuelo = numeroVueloN
WHERE id_vuelo = NEW.ID_VUELO;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Lo activamos también en UPDATE por si cambian los aeropuertos
DROP TRIGGER IF EXISTS trg_set_numero_vuelo ON Segmento_Vuelo;
CREATE TRIGGER trg_set_numero_vuelo
    AFTER INSERT OR UPDATE OF ID_AEROPUERTO_ORIGEN, ID_AEROPUERTO_DESTINO ON Segmento_Vuelo
    FOR EACH ROW
    EXECUTE FUNCTION fn_set_numero_vuelo();



CREATE OR REPLACE FUNCTION fn_set_fecha_itinerario()
RETURNS TRIGGER AS $$
DECLARE
fechaSalida TIMESTAMP;
    fechaLlegada TIMESTAMP;
BEGIN
    -- Si es el primer vuelo del itinerario, establecemos hora de salida y llegada
    IF NEW.ORDEN = 1 THEN
SELECT sgv.hora_salida
INTO fechaSalida
FROM Segmento_Vuelo sgv
WHERE sgv.id_vuelo = NEW.ID_VUELO
  AND sgv.ORDEN_SEGMENTO = 1;

SELECT sgv.hora_llegada
INTO fechaLlegada
FROM Segmento_Vuelo sgv
WHERE sgv.id_vuelo = NEW.ID_VUELO
  AND sgv.ORDEN_SEGMENTO = (
    SELECT MAX(sgv2.ORDEN_SEGMENTO)
    FROM Segmento_Vuelo sgv2
    WHERE sgv2.id_vuelo = NEW.ID_VUELO
);

UPDATE Itinerario
SET HORA_SALIDA = fechaSalida,
    HORA_LLEGADA = fechaLlegada
WHERE ID_ITINERARIO = NEW.ID_ITINERARIO;

ELSIF NEW.ORDEN > 1 THEN
        -- En vuelos posteriores (conexiones), actualizamos solo la hora de llegada
SELECT sgv.hora_llegada
INTO fechaLlegada
FROM Segmento_Vuelo sgv
WHERE sgv.id_vuelo = NEW.ID_VUELO
  AND sgv.ORDEN_SEGMENTO = (
    SELECT MAX(sgv2.ORDEN_SEGMENTO)
    FROM Segmento_Vuelo sgv2
    WHERE sgv2.id_vuelo = NEW.ID_VUELO
);

UPDATE Itinerario
SET HORA_LLEGADA = fechaLlegada
WHERE ID_ITINERARIO = NEW.ID_ITINERARIO;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;




CREATE TRIGGER trg_set_fecha_itinerario
    AFTER INSERT ON Itinerario_Vuelo
    FOR EACH ROW
    EXECUTE FUNCTION fn_set_fecha_itinerario();





CREATE OR REPLACE FUNCTION fn_update_itinerario_desde_vuelo()
RETURNS TRIGGER AS $$
BEGIN
    -- 1. Actualizar HORA_SALIDA del Itinerario si este vuelo es el PRIMERO (ORDEN 1)
UPDATE Itinerario i
SET HORA_SALIDA = NEW.Fecha_Hora_Salida
    FROM Itinerario_Vuelo iv
WHERE iv.ID_ITINERARIO = i.ID_ITINERARIO
  AND iv.ID_VUELO = NEW.ID_VUELO
  AND iv.ORDEN = 1;

-- 2. Actualizar HORA_LLEGADA del Itinerario si este vuelo es el ÚLTIMO
UPDATE Itinerario i
SET HORA_LLEGADA = NEW.Fecha_Hora_Llegada
    FROM Itinerario_Vuelo iv
WHERE iv.ID_ITINERARIO = i.ID_ITINERARIO
  AND iv.ID_VUELO = NEW.ID_VUELO
  AND iv.ORDEN = (
    SELECT MAX(iv2.ORDEN)
    FROM Itinerario_Vuelo iv2
    WHERE iv2.ID_ITINERARIO = i.ID_ITINERARIO
    );

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_vuelo_hacia_itinerario
    AFTER UPDATE OF Fecha_Hora_Salida, Fecha_Hora_Llegada ON Vuelo
    FOR EACH ROW
    EXECUTE FUNCTION fn_update_itinerario_desde_vuelo();



CREATE OR REPLACE FUNCTION fn_set_fecha_vuelo()
RETURNS TRIGGER AS $$
DECLARE
v_id_vuelo INT;
    v_fecha_salida TIMESTAMP;
    v_fecha_llegada TIMESTAMP;
BEGIN
    -- 1. Determinar el ID del vuelo afectado (Maneja INSERT, UPDATE y DELETE)
    -- Si es un UPDATE y cambió el ID_VUELO, debemos actualizar el Vuelo antiguo también.
    -- Para simplificar, primero identificamos qué vuelo procesar en este hilo.
    IF (TG_OP = 'DELETE') THEN
        v_id_vuelo := OLD.ID_VUELO;
ELSE
        v_id_vuelo := NEW.ID_VUELO;
END IF;

    -- 2. Obtener la salida del primer segmento y la llegada del último en una sola consulta
    -- Esto es más eficiente que hacer dos SELECT por separado.
SELECT
    (SELECT hora_salida FROM Segmento_Vuelo WHERE id_vuelo = v_id_vuelo ORDER BY ORDEN_SEGMENTO ASC LIMIT 1),
        (SELECT hora_llegada FROM Segmento_Vuelo WHERE id_vuelo = v_id_vuelo ORDER BY ORDEN_SEGMENTO DESC LIMIT 1)
INTO v_fecha_salida, v_fecha_llegada;

-- 3. Actualizar la tabla Vuelo
UPDATE Vuelo
SET Fecha_Hora_Salida = v_fecha_salida,
    Fecha_Hora_Llegada = v_fecha_llegada
WHERE ID_VUELO = v_id_vuelo;

-- 4. Caso Especial: Si hubo un UPDATE y se cambió el ID_VUELO de un segmento,
-- debemos recalcular también el Vuelo que perdió el segmento.
IF (TG_OP = 'UPDATE' AND OLD.ID_VUELO <> NEW.ID_VUELO) THEN
UPDATE Vuelo
SET Fecha_Hora_Salida = (SELECT hora_salida FROM Segmento_Vuelo WHERE id_vuelo = OLD.ID_VUELO ORDER BY ORDEN_SEGMENTO ASC LIMIT 1),
    Fecha_Hora_Llegada = (SELECT hora_llegada FROM Segmento_Vuelo WHERE id_vuelo = OLD.ID_VUELO ORDER BY ORDEN_SEGMENTO DESC LIMIT 1)
WHERE ID_VUELO = OLD.ID_VUELO;
END IF;

RETURN NULL; -- En triggers AFTER el valor de retorno no afecta al registro
END;
$$ LANGUAGE plpgsql;

-- Definición del Trigger incluyendo DELETE
CREATE TRIGGER trg_set_fecha_vuelo
    AFTER INSERT OR UPDATE OR DELETE ON Segmento_Vuelo
    FOR EACH ROW
    EXECUTE FUNCTION fn_set_fecha_vuelo();



CREATE OR REPLACE FUNCTION fn_set_fecha_turno_automatico()
RETURNS TRIGGER AS $$
DECLARE
v_hora_inicio TIMESTAMP;
    v_hora_fin TIMESTAMP;
BEGIN
    -- 1. Obtener la hora de salida del primer segmento (Orden 1)
SELECT hora_salida
INTO v_hora_inicio
FROM Segmento_Vuelo
WHERE id_vuelo = NEW.ID_VUELO
  AND ORDEN_SEGMENTO = 1;

-- 2. Obtener la hora de llegada del último segmento (El de mayor orden)
SELECT hora_llegada
INTO v_hora_fin
FROM Segmento_Vuelo
WHERE id_vuelo = NEW.ID_VUELO
  AND ORDEN_SEGMENTO = (
    SELECT MAX(sgv2.ORDEN_SEGMENTO)
    FROM Segmento_Vuelo sgv2
    WHERE sgv2.id_vuelo = NEW.ID_VUELO
);

-- 3. Actualizar los turnos asociados a este vuelo
-- Solo se actualiza si se encontraron ambos extremos (inicio y fin)
IF v_hora_inicio IS NOT NULL AND v_hora_fin IS NOT NULL THEN
UPDATE Turno
SET Hora_Inicio = v_hora_inicio,
    Hora_Fin = v_hora_fin,
    Fecha = CAST(v_hora_inicio AS DATE) -- Se asume que la fecha del turno es el día de salida
WHERE ID_VUELO = NEW.ID_VUELO;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER trg_actualizar_fechas_turno
    AFTER INSERT OR UPDATE ON Segmento_Vuelo
                        FOR EACH ROW
                        EXECUTE FUNCTION fn_set_fecha_turno_automatico();


/*ALTER TABLE Segmento_Vuelo DISABLE TRIGGER trg_set_orden_segmento_vuelo;

ALTER TABLE Segmento_Vuelo DISABLE TRIGGER trg_set_numero_vuelo;

ALTER TABLE Segmento_Vuelo DISABLE TRIGGER trg_set_fecha_vuelo;

ALTER TABLE Itinerario_Vuelo DISABLE TRIGGER trg_set_fecha_itinerario;

ALTER TABLE Itinerario_Vuelo DISABLE TRIGGER trg_set_orden_itinerario_vuelo;*/




ALTER TABLE notificacion
    ADD COLUMN IF NOT EXISTS enviada BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS canal VARCHAR(20) DEFAULT 'Email';








UPDATE Usuario
SET Correo_Electronico = 'cr.romanz@duocuc.cl'
WHERE Correo_Electronico = 'juan.perez@piloto.com';


CREATE OR REPLACE FUNCTION fn_notificacionVueloEstado()
RETURNS TRIGGER
LANGUAGE PLPGSQL
AS
$$
DECLARE
pasajero RECORD;
    mensaje TEXT;
BEGIN
    -- Iterar sobre cada pasajero asociado al vuelo actualizado
FOR pasajero IN
SELECT rsv.rut_pasajero, vl.numero_vuelo
FROM reserva_itinerario rsvi
         join itinerario_vuelo itv
              on itv.id_itinerario = rsvi.id_itinerario
         join reserva rsv on rsv.id_reserva = rsvi.id_reserva
         join vuelo vl on vl.id_vuelo = itv.id_vuelo
WHERE itv.id_vuelo = NEW.id_vuelo

    LOOP
        -- Construir el mensaje de notificación con detalles específicos
        mensaje := 'Estimado/a pasajero/a, su vuelo número ' || pasajero.numero_vuelo ||
                   ' ha sido actualizado. ';

-- Incluir información sobre la nueva hora de salida

IF OLD.hora_salida IS DISTINCT FROM NEW.hora_salida THEN
			  mensaje := mensaje || 'La nueva hora de salida es: ' || TO_CHAR(NEW.hora_salida, 'DD/MM/YYYY HH24:MI') || '. ';

        /*IF NEW.fecha_hora_salida IS NOT NULL THEN
            mensaje := mensaje || 'La nueva hora de salida es: ' || TO_CHAR(NEW.fecha_hora_salida, 'DD/MM/YYYY HH24:MI') || '. ';
        ELSE
            mensaje := mensaje || 'La hora de salida no ha sido modificada. ';*/
END IF;

        -- Incluir información sobre la nueva hora de llegada
        IF OLD.hora_llegada IS DISTINCT FROM NEW.hora_llegada THEN
            mensaje := mensaje || 'La nueva hora de llegada es: ' || TO_CHAR(NEW.hora_llegada, 'DD/MM/YYYY HH24:MI') || '. ';
ELSE
            --mensaje := mensaje || 'La hora de llegada no ha sido modificada. ';
END IF;

        -- Añadir información adicional si está disponible
        /*IF NEW.id_aeropuerto_salida IS NOT NULL THEN
            mensaje := mensaje || 'Aeropuerto de salida: ' || NEW.id_aeropuerto_salida || '. ';
        END IF;
        IF NEW.id_aeropuerto_llegada IS NOT NULL THEN
            mensaje := mensaje || 'Aeropuerto de llegada: ' || NEW.id_aeropuerto_llegada || '. ';
        END IF;
        IF NEW.precio IS NOT NULL THEN
            mensaje := mensaje || 'Precio del boleto: $' || NEW.precio || '. ';
        END IF;
        IF NEW.rut_piloto IS NOT NULL THEN


            mensaje := mensaje || 'Piloto a cargo: ' || NEW.rut_piloto || '. ';
        END IF;*/

        -- Insertar la notificación en la tabla correspondiente
INSERT INTO notificacion(rut_destinatario, titulo, mensaje, fecha, leido)
VALUES (
           pasajero.rut_pasajero,
           'Actualización de Vuelo: ' || pasajero.numero_vuelo,
           mensaje,
           NOW(),
           FALSE
       );
END LOOP;
RETURN NEW;
END;
$$;



-- Crear el Trigger para enviar notificaciones después de actualizar un vuelo
CREATE OR REPLACE TRIGGER tr_notificacionVueloEstado
AFTER UPDATE ON Segmento_Vuelo
                 FOR EACH ROW
                 EXECUTE FUNCTION fn_notificacionVueloEstado();




CREATE OR REPLACE FUNCTION fn_notify_new_notification()
RETURNS TRIGGER AS
$$
BEGIN
    PERFORM pg_notify('nuevo_correo', NEW.id_notificacion::TEXT);
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS tr_notify_new_notificacion ON notificacion;

CREATE TRIGGER tr_notify_new_notificacion
    AFTER INSERT ON notificacion
    FOR EACH ROW
    EXECUTE FUNCTION fn_notify_new_notification();


-- ==========================================
-- 1. PROCEDIMIENTO: CONFIRMAR RESERVA
-- ==========================================
CREATE OR REPLACE PROCEDURE spConfirmar_reserva(
    IN p_idVuelo INT,
    IN p_idReserva INT,
    IN p_asientos INT[],
    IN p_rutPasajero TEXT,
    OUT p_resultado TEXT
)
LANGUAGE plpgsql
AS $confirmar_reserva$
DECLARE
reserva_id INT;
    estado_reserva_id INT := 1;
    i INT;
    id_avion INT;
    id_asientoP INT;
    asiento_en_reserva INT;
    numero_asiento TEXT;
    asientos_reservados TEXT := '';
BEGIN
    -- Obtener el avión asignado al vuelo
SELECT vl.id_avion INTO id_avion
FROM vuelo vl
WHERE vl.id_vuelo = p_idVuelo;

FOR i IN 1..array_length(p_asientos, 1)
    LOOP
        id_asientoP := p_asientos[i];
        asiento_en_reserva := 0;

        -- Verificar si el asiento ya está reservado (Sin FOR UPDATE para evitar conflicto con EXCEPTION)
SELECT 1 INTO asiento_en_reserva
FROM reserva_asiento ra
WHERE ra.ID_VUELO = p_idVuelo
  AND ra.ID_ASIENTO = id_asientoP;

IF asiento_en_reserva IS NOT NULL AND asiento_en_reserva > 0 THEN
SELECT ast.numero_asiento INTO numero_asiento
FROM asiento ast
WHERE ast.id_asiento = id_asientoP;

asientos_reservados := asientos_reservados || numero_asiento || ', ';
ELSE
            -- Insertar en reserva_asiento
            INSERT INTO reserva_asiento (id_reserva, id_asiento, ID_VUELO, rut)
            VALUES (p_idReserva, id_asientoP, p_idVuelo, p_rutPasajero);
END IF;
END LOOP;

    IF asientos_reservados <> '' THEN
        p_resultado := 'ERROR: Asientos ya reservados: ' || LEFT(asientos_reservados, LENGTH(asientos_reservados) - 2);
DELETE FROM reserva WHERE id_reserva = p_idReserva;
ELSE
        p_resultado := 'OK: Reserva realizada correctamente.';
END IF;
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'Ocurrió un error: %', SQLERRM;
DELETE FROM reserva WHERE id_reserva = p_idReserva;
p_resultado := 'ERROR: No se pudo completar la reserva. ' || SQLERRM;
END;
$confirmar_reserva$;


-- ==========================================
-- 2. PROCEDIMIENTO: VERIFICAR DISPONIBILIDAD
-- ==========================================
CREATE OR REPLACE PROCEDURE spVerificarDisponinibilidadAsientos(
    IN p_idVuelo INT,
    IN p_asientos INT[],
    OUT p_resultado TEXT
)
LANGUAGE plpgsql
AS $verificar_asientos$
DECLARE
id_asientoP INT;
    asiento_en_reserva INT;
    v_numero_asiento TEXT;
    v_asientos_erroneos TEXT := '';
BEGIN
    p_resultado := 'OK';

FOR i IN 1..array_length(p_asientos, 1)
    LOOP
        id_asientoP := p_asientos[i];
        asiento_en_reserva := NULL;

SELECT a.numero_asiento,
       (SELECT 1 FROM reserva_asiento ra
        WHERE ra.id_vuelo = p_idVuelo
          AND ra.id_asiento = id_asientoP LIMIT 1)
INTO v_numero_asiento, asiento_en_reserva
FROM asiento a
WHERE a.id_asiento = id_asientoP;

IF asiento_en_reserva IS NOT NULL THEN
            v_asientos_erroneos := v_asientos_erroneos || v_numero_asiento || ', ';
END IF;
END LOOP;

    IF v_asientos_erroneos <> '' THEN
        p_resultado := 'ERROR: Asientos ya reservados: ' || LEFT(v_asientos_erroneos, LENGTH(v_asientos_erroneos) - 2);
END IF;
EXCEPTION
    WHEN OTHERS THEN
        p_resultado := 'ERROR: Error interno: ' || SQLERRM;
END;
$verificar_asientos$;


-- ==========================================
-- 3. PROCEDIMIENTO: CAMBIAR ASIENTO
-- ==========================================
CREATE OR REPLACE PROCEDURE sp_cambiarAsiento(
   IN p_id_asiento INT,
   IN p_id_reserva INT,
   IN p_id_asiento_org INT
)
LANGUAGE plpgsql
AS $cambiar_asiento$
BEGIN
UPDATE reserva_asiento
SET id_asiento = p_id_asiento
WHERE id_reserva = p_id_reserva
  AND id_asiento = p_id_asiento_org;
END;
$cambiar_asiento$;


-- ==========================================
-- 4. PROCEDIMIENTO: UPSERT PASAJERO
-- ==========================================
CREATE OR REPLACE PROCEDURE sp_upsertPasajero(
    p_rut VARCHAR,
    p_nombre VARCHAR,
    p_apellido VARCHAR,
    p_correo VARCHAR,
    p_telefono VARCHAR,
    p_documento VARCHAR,
    p_fecha_nacimiento DATE,
    p_contrasena VARCHAR
)
LANGUAGE plpgsql
AS $upsert_pasajero$
DECLARE
v_id_rol INT;
BEGIN
    -- 1. Insertar o actualizar usuario
INSERT INTO Usuario(RUT, Nombre, Apellido, Correo_Electronico, Telefono, Documento_Identidad, Fecha_Nacimiento, Contrasena, Fecha_Registro)
VALUES (p_rut, p_nombre, p_apellido, p_correo, p_telefono, p_documento, p_fecha_nacimiento, p_contrasena, NOW())
    ON CONFLICT (RUT)
    DO UPDATE SET
    Nombre = EXCLUDED.Nombre,
               Apellido = EXCLUDED.Apellido,
               Correo_Electronico = EXCLUDED.Correo_Electronico,
               Telefono = EXCLUDED.Telefono,
               Documento_Identidad = EXCLUDED.Documento_Identidad,
               Fecha_Nacimiento = EXCLUDED.Fecha_Nacimiento,
               Contrasena = EXCLUDED.Contrasena,
               Fecha_Registro = NOW();

-- 2. Insertar o actualizar Pasajero
INSERT INTO Pasajero(RUT, Tipo_Documento, Numero_Documento, Fecha_Nacimiento, Nacionalidad)
VALUES (p_rut, 'DNI', p_documento, p_fecha_nacimiento, 'Desconocida')
    ON CONFLICT (RUT)
    DO UPDATE SET
    Tipo_Documento = EXCLUDED.Tipo_Documento,
               Numero_Documento = EXCLUDED.Numero_Documento,
               Fecha_Nacimiento = EXCLUDED.Fecha_Nacimiento,
               Nacionalidad = EXCLUDED.Nacionalidad;

-- 3. Obtener id del rol "Pasajero"
SELECT id_rol INTO v_id_rol FROM Roles WHERE nombre = 'Pasajero';

-- 4. Insertar rol si no existe
INSERT INTO RolUsuario(id_rol, rut_usuario)
VALUES (v_id_rol, p_rut)
    ON CONFLICT (id_rol, rut_usuario) DO NOTHING;
END;
$upsert_pasajero$;


CREATE OR REPLACE FUNCTION fnBuscarVuelo(
    p_codigo_origen VARCHAR,
    p_codigo_destino VARCHAR,
    p_fecha_inicio DATE
)
RETURNS TABLE (
    itinerario INT,
    origen VARCHAR,
    destino VARCHAR,
    ciudad_salida TEXT,
    ciudad_llegada TEXT,
    cant_paradas TEXT,
    precio INT,
    duracion TEXT,
    hora_salida_24h TEXT,
    hora_llegada_24h TEXT
) AS $$
BEGIN
RETURN QUERY
SELECT
    it.id_itinerario,
    arp1.codigo_iata,
    arp2.codigo_iata,
    ci1.nombre || ' - ' || arp1.nombre_aeropuerto || ' (' || arp1.codigo_iata || ')' AS ciudad_salida,
    ci2.nombre || ' - ' || arp2.nombre_aeropuerto || ' (' || arp2.codigo_iata || ')' AS ciudad_llegada,
    CASE
        WHEN it.numero_escalas = 0 THEN 'sin paradas'
        WHEN it.numero_escalas = 1 THEN '1 parada'
        ELSE it.numero_escalas::TEXT || ' paradas'
        END AS cant_paradas,
    it.precio_base,
    (EXTRACT(epoch FROM (it.hora_llegada - it.hora_salida)) / 3600)::INT || ' h ' ||
        ((EXTRACT(epoch FROM (it.hora_llegada - it.hora_salida)) % 3600) / 60)::INT || ' min' AS duracion,
    TO_CHAR(it.hora_salida, 'HH24:MI'),
    TO_CHAR(it.hora_llegada, 'HH24:MI')
FROM itinerario it
         JOIN aeropuerto arp1 ON arp1.id_aeropuerto = it.origen_aeropuerto
         JOIN aeropuerto arp2 ON arp2.id_aeropuerto = it.destino_aeropuerto
         JOIN ciudad ci1 ON ci1.id_ciudad = arp1.id_ciudad
         JOIN ciudad ci2 ON ci2.id_ciudad = arp2.id_ciudad
WHERE arp1.codigo_iata = p_codigo_origen
  AND arp2.codigo_iata = p_codigo_destino
  AND it.hora_salida >= p_fecha_inicio
  AND it.hora_salida < p_fecha_inicio + INTERVAL '1 day';
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION FnbuscarVuelos(
    p_codigo_origen  VARCHAR,
    p_codigo_destino VARCHAR,
    p_fecha_inicio DATE DEFAULT NULL
)
RETURNS SETOF vuelo
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT
    v.*
FROM vuelo v

         -- Primer segmento (origen)
         LEFT JOIN segmento_vuelo sgmv1
                   ON sgmv1.id_vuelo = v.id_vuelo
                       AND sgmv1.orden_segmento = 1

    -- Último segmento (destino final)
         JOIN (
    SELECT DISTINCT ON (id_vuelo)
        id_vuelo,
        id_aeropuerto_destino
    FROM segmento_vuelo
    ORDER BY id_vuelo, orden_segmento DESC
) sgmv2
              ON sgmv2.id_vuelo = v.id_vuelo

         JOIN aeropuerto arp1
              ON arp1.id_aeropuerto = sgmv1.id_aeropuerto_origen

         JOIN aeropuerto arp2
              ON arp2.id_aeropuerto = sgmv2.id_aeropuerto_destino

WHERE arp1.codigo_iata = p_codigo_origen
  AND arp2.codigo_iata = p_codigo_destino
  AND (
    p_fecha_inicio IS NULL
        OR (
        sgmv1.hora_salida >= p_fecha_inicio
            AND sgmv1.hora_salida < p_fecha_inicio + INTERVAL '1 day'
        )
    );
END;
$$;

SELECT * FROM FnbuscarVuelos('SCL', 'JFK', '2025-12-05');

SELECT * FROM FnbuscarVuelos('SCL', 'JFK', NULL);


select
    *
from vuelo v
         join segmento_vuelo sg ON sg.id_vuelo = v.id_vuelo
order by v.id_vuelo;




CREATE OR REPLACE FUNCTION fnDTinitinerario(p_id_itinerario INT)
RETURNS TABLE(
  ID_ITINERARIO INT,
  Aeropuerto_Origen VARCHAR(100),
  Aeropuerto_Destino VARCHAR(100),
  DURACION_TOTAL TEXT,              -- Convertir INTERVAL a TEXT
  NUMERO_ESCALAS INT,
  ORDEN INT,
  Salida TEXT,
  Aeropuerto_Salida VARCHAR(100),
  Ciudad_Salida VARCHAR(100),
  Llegada TEXT,
  Aeropuerto_Llegada VARCHAR(100),
  Ciudad_Llegada VARCHAR(100),
  Duracion TEXT,
  Tiempo_Espera TEXT,              -- Convertir INTERVAL a TEXT
  Modelo_Avion VARCHAR(100),
  Aerolinea VARCHAR(100),
  Vuelo VARCHAR(50),
  Descripcion_Vuelo TEXT
)
AS $$
BEGIN
RETURN QUERY
SELECT
    it.ID_ITINERARIO,
    a3.Nombre_Aeropuerto AS Aeropuerto_Origen,
    a4.Nombre_Aeropuerto AS Aeropuerto_Destino,
    -- Convertir INTERVAL a texto
    TO_CHAR(it.DURACION_TOTAL, 'HH24 "h" MI "min"') AS DURACION_TOTAL,  -- Aquí se formatea el INTERVAL
    it.NUMERO_ESCALAS,
    iv.ORDEN,
    a1.Codigo_IATA || ' ' || TO_CHAR(sv.HORA_SALIDA, 'DD/MM/YYYY') || ' ' || TO_CHAR(sv.HORA_SALIDA, 'HH:MI AM') AS Salida,
    a1.Nombre_Aeropuerto AS Aeropuerto_Salida,
    c1.nombre AS Ciudad_Salida,
    a2.Codigo_IATA || ' ' || TO_CHAR(sv.HORA_LLEGADA, 'DD/MM/YYYY') || ' ' || TO_CHAR(sv.HORA_LLEGADA, 'HH:MI AM') AS Llegada,
    a2.Nombre_Aeropuerto AS Aeropuerto_Llegada,
    c2.nombre AS Ciudad_Llegada,

    -- Convertir la duración a formato de texto
    EXTRACT(HOUR FROM (sv.HORA_LLEGADA - sv.HORA_SALIDA)) || ' h ' ||
    EXTRACT(MINUTE FROM (sv.HORA_LLEGADA - sv.HORA_SALIDA)) || ' min' AS Duracion,

    -- Convertir el tiempo de espera a texto
    CASE
        WHEN LAG(sv.HORA_LLEGADA) OVER (PARTITION BY it.ID_ITINERARIO ORDER BY iv.ORDEN) IS NOT NULL THEN
        EXTRACT(HOUR FROM (sv.HORA_SALIDA - LAG(sv.HORA_LLEGADA) OVER (PARTITION BY it.ID_ITINERARIO ORDER BY iv.ORDEN))) || ' h ' ||
        EXTRACT(MINUTE FROM (sv.HORA_SALIDA - LAG(sv.HORA_LLEGADA) OVER (PARTITION BY it.ID_ITINERARIO ORDER BY iv.ORDEN))) || ' min'
      ELSE
        '0 h 0 min'
END AS Tiempo_Espera,

    mdv.Nombre AS Modelo_Avion,
    al.Nombre AS Aerolinea,
    v.Numero_Vuelo AS Vuelo,
    'Vuelo ' || v.Numero_Vuelo || ', ' || mdv.Nombre || ', Operado por ' || al.Nombre AS Descripcion_Vuelo

  FROM Itinerario it
  JOIN Itinerario_Vuelo iv ON iv.ID_ITINERARIO = it.ID_ITINERARIO
  JOIN Vuelo v ON v.ID_VUELO = iv.ID_VUELO
  JOIN Avion av ON av.ID_AVION = v.ID_AVION
  JOIN modelo_avion mdv on mdv.id_modelo = av.id_modelo
  JOIN Aerolinea al ON al.ID_AEROLINEA = v.ID_AEROLINEA
  JOIN Piloto p ON p.RUT = v.RUT_PILOTO
  JOIN Segmento_Vuelo sv ON sv.ID_VUELO = v.ID_VUELO
  JOIN Aeropuerto a1 ON a1.ID_AEROPUERTO = sv.ID_AEROPUERTO_ORIGEN
  JOIN Aeropuerto a2 ON a2.ID_AEROPUERTO = sv.ID_AEROPUERTO_DESTINO
  JOIN ciudad c1 on c1.ID_CIUDAD = a1.ID_CIUDAD
  JOIN ciudad c2 on c2.ID_CIUDAD = a2.ID_CIUDAD
  JOIN Aeropuerto a3 ON a3.ID_AEROPUERTO = it.ORIGEN_AEROPUERTO
  JOIN Aeropuerto a4 ON a4.ID_AEROPUERTO = it.DESTINO_AEROPUERTO

  WHERE it.ID_ITINERARIO = p_id_itinerario
  ORDER BY iv.ORDEN;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION fn_getAsientosAvion(
    IN p_idVuelo INT,
    IN p_idReserva INT DEFAULT NULL
)
RETURNS TABLE (
    id_asiento INT,
    numero_asiento VARCHAR,
    estado TEXT,
    precio INT,
    clase VARCHAR
) AS $$
BEGIN
RETURN QUERY
SELECT
    a.id_asiento,
    a.numero_asiento,
    CASE
        WHEN p_idReserva IS NOT NULL AND rsv.id_reserva = p_idReserva THEN 'seleccionado'
        WHEN rsv.id_reserva IS NOT NULL THEN 'ocupado'
        ELSE 'libre'
        END AS estado,
    psa.precio,
    cls.descripcion AS clase
FROM vuelo vl
         JOIN avion av ON av.id_avion = vl.id_avion
         JOIN asiento a ON a.id_avion = av.id_avion AND a.id_avion = vl.id_avion
         LEFT JOIN reserva_asiento rsv ON rsv.id_asiento = a.id_asiento AND rsv.id_vuelo = vl.id_vuelo
         JOIN precio_asiento psa ON psa.id_clase = a.id_clase AND psa.id_vuelo = vl.id_vuelo
         LEFT JOIN clase_asiento cls ON cls.id_clase = a.id_clase
WHERE vl.id_vuelo = p_idVuelo
ORDER BY a.numero_asiento;
END;
$$ LANGUAGE plpgsql;

SELECT * FROM fn_getAsientosAvion(25, 5);

select * from reserva;



CREATE OR REPLACE FUNCTION fn_getItinerariosRut(
    prut_pasajero TEXT,
    limite INT,
    desplazamiento INT
)
RETURNS TABLE (
    id_itinerario int,
    codigo_iata_origen varchar(255),
    codigo_iata_destino varchar(255),
    hora_salida TIMESTAMP
)
AS
$$
BEGIN
RETURN QUERY
SELECT
    i.id_itinerario,
    arp2.codigo_iata,
    arp1.codigo_iata,
    i.hora_salida
FROM reserva r
         JOIN reserva_itinerario ri ON ri.id_reserva = r.id_reserva
         JOIN itinerario i ON i.id_itinerario = ri.id_itinerario
         JOIN aeropuerto arp1 ON arp1.id_aeropuerto = i.destino_aeropuerto
         JOIN aeropuerto arp2 ON arp2.id_aeropuerto = i.origen_aeropuerto
WHERE r.rut_pasajero = prut_pasajero
ORDER BY r.fecha_reserva ASC
    LIMIT limite OFFSET desplazamiento;
END;
$$ LANGUAGE plpgsql STABLE;




CREATE OR REPLACE FUNCTION fn_getItinerariosPorRutYFechas(
    prut_pasajero TEXT,
    limite INT,
    desplazamiento INT,
    pfechaIni TIMESTAMP DEFAULT NULL,
    pfechaFin TIMESTAMP DEFAULT NULL
)
RETURNS TABLE (
    id_itinerario INT,
    codigo_iata_origen VARCHAR(255),
    codigo_iata_destino VARCHAR(255),
    hora_salida TIMESTAMP,
	id_reserva INT
)
AS
$$
BEGIN
RETURN QUERY
SELECT
    i.id_itinerario,
    arp2.codigo_iata,
    arp1.codigo_iata,
    i.hora_salida,
    r.id_reserva
FROM reserva r
         JOIN reserva_itinerario ri ON ri.id_reserva = r.id_reserva
         JOIN itinerario i ON i.id_itinerario = ri.id_itinerario
         JOIN aeropuerto arp1 ON arp1.id_aeropuerto = i.destino_aeropuerto
         JOIN aeropuerto arp2 ON arp2.id_aeropuerto = i.origen_aeropuerto
WHERE r.rut_pasajero = prut_pasajero
  AND (pfechaIni IS NULL OR i.hora_salida >= pfechaIni)
  AND (pfechaFin IS NULL OR i.hora_salida <= pfechaFin)
ORDER BY r.fecha_reserva ASC
    LIMIT limite OFFSET desplazamiento;
END;
$$ LANGUAGE plpgsql STABLE;



CREATE OR REPLACE FUNCTION fn_getAsientosPorItinerarioYReserva(
    pid_itinerario INT,
    pid_reserva INT
)
RETURNS TABLE (
    id_asiento INT,
    numero_asiento VARCHAR,
    clase_asiento VARCHAR,
    id_vuelo INT,
    id_reserva INT
)
AS
$$
BEGIN
RETURN QUERY
SELECT
    a.id_asiento,
    a.numero_asiento,
    ca.descripcion,
    ra.id_vuelo,
    ra.id_reserva
FROM reserva_itinerario ri
         JOIN itinerario_vuelo iv ON iv.id_itinerario = ri.id_itinerario
         JOIN reserva_asiento ra ON ra.id_reserva = ri.id_reserva AND ra.id_vuelo = iv.id_vuelo
         JOIN asiento a ON a.id_asiento = ra.id_asiento
         JOIN clase_asiento ca ON ca.id_clase = a.id_clase
WHERE ri.id_itinerario = pid_itinerario
  AND ri.id_reserva = pid_reserva;
END;
$$ LANGUAGE plpgsql STABLE;





CREATE OR REPLACE FUNCTION fn_getTicket(p_rut_pasajero VARCHAR,  p_id_reserva integer, p_idItinerario integer)
RETURNS TABLE (
    numero_vuelo VARCHAR,
    hora_salida TIMESTAMP,
    hora_llegada TIMESTAMP,
    codigo_puerta VARCHAR,
    terminal VARCHAR,
    numero_asiento VARCHAR,
    clase_asiento VARCHAR,
	nombre text
)
AS $$
BEGIN
RETURN QUERY
SELECT
    vl.numero_vuelo,
    sgm.hora_salida,
    sgm.hora_llegada,
    prta.codigo_puerta,
    prta.terminal,
    ast.numero_asiento,
    clas.descripcion,
    us.nombre || ' ' || us.apellido
FROM reserva_asiento rsva
         left JOIN reserva rsv ON rsv.id_reserva = rsva.id_reserva
         left join itinerario_vuelo itv on itv.id_vuelo = rsva.id_vuelo
         join pasajero p on p.rut = rsva.rut
         join usuario us on us.rut = p.rut
         left JOIN vuelo vl ON vl.id_vuelo = rsva.id_vuelo
         left JOIN segmento_vuelo sgm ON sgm.id_vuelo = rsva.id_vuelo
         left JOIN asignacion_puerta asgp ON asgp.id_segmento = sgm.id_segmento
         left JOIN puerta_embarque prta ON prta.id_puerta = asgp.id_puerta
         left JOIN asiento ast ON ast.id_asiento = rsva.id_asiento
         left JOIN clase_asiento clas ON clas.id_clase = ast.id_clase
WHERE rsv.rut_pasajero = p_rut_pasajero and rsv.id_reserva=p_id_reserva
  and itv.id_itinerario=p_idItinerario
order by  vl.fecha_hora_salida;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION fn_getVueloInfo(p_idVuelo integer)
RETURNS TABLE (
	destino varchar,
    id_vuelo int,
    numero_vuelo text,
    ciudad_salida text,
    ciudad_llegada text,
    fecha_hora_salida TIMESTAMP,
    fecha_hora_llegada TIMESTAMP,
	duracion text

) AS $$
BEGIN
RETURN QUERY
SELECT
    p1.nombre,
    v.ID_VUELO,
    v.Numero_Vuelo::text,
    ci1.nombre||' - '||a1.nombre_aeropuerto||' ('||a1.codigo_iata||')' ,
    ci2.nombre||' - '||a2.nombre_aeropuerto||' ('||a2.codigo_iata||')'  ,
    v.Fecha_Hora_Salida,
    v.Fecha_Hora_Llegada,
    --(EXTRACT(EPOCH FROM (v.Fecha_Hora_Llegada - v.Fecha_Hora_Salida)) / 3600),
    CAST(EXTRACT(HOUR FROM (v.Fecha_Hora_Llegada - v.Fecha_Hora_Salida)) AS VARCHAR) || 'h ' ||
    CAST(EXTRACT(MINUTE FROM (v.Fecha_Hora_Llegada - v.Fecha_Hora_Salida)) AS VARCHAR) || 'm'
FROM
    Vuelo v

        JOIN segmento_vuelo sgm on sgm.id_vuelo = v.id_vuelo

        JOIN
    Aeropuerto a1 ON a1.id_aeropuerto = sgm.id_aeropuerto_origen
        JOIN
    Aeropuerto a2 ON a2.id_aeropuerto = sgm.id_aeropuerto_destino
        JOIN
    Ciudad ci1 ON ci1.id_ciudad = a1.id_ciudad
        JOIN
    Ciudad ci2 ON ci2.id_ciudad = a2.id_ciudad
        Join pais p1 on p1.id_pais = ci2.id_pais
where v.ID_VUELO=p_idVuelo;
--where extract(day from AGE(v.Fecha_Hora_Salida, CURRENT_TIMESTAMP))<=10;

END;
$$ LANGUAGE plpgsql;

select * from vuelo;

select * from fn_getVueloInfo(23);

-- Ejemplo:
SELECT * FROM fn_getAsientosPorItinerarioYReserva(320, 10);

--SELECT * FROM fn_getTicket('12345678-9',4);

SELECT * FROM fn_getItinerariosRut('12345678-9', 100, 0);

SELECT * FROM fn_getItinerariosPorRutYFechas('12345678-9', 1000, 0, '2025-09-01', '2025-09-2');

SELECT * FROM fn_getItinerariosPorRutYFechas('12345678-9', 10, 0, NULL, NULL);

SELECT * FROM fn_getItinerariosPorRutYFechas('12345678-9', 10, 0, '2025-09-01', NULL);

SELECT * FROM fnBuscarVuelo('BOG', 'PEK', '2025-09-02');

SELECT * FROM fn_getItinerariosPorRutYFechas('12345678-9', 10, 0, '2025-09-01', '2025-09-30');

SELECT * FROM fn_getItinerariosPorRutYFechas('12345678-9', 10, 0, NULL, NULL);

SELECT * FROM fn_getItinerariosPorRutYFechas('12345678-9', 10, 0, '2025-09-01', NULL);


INSERT INTO Continente (ID_CONTINENTE, Nombre) VALUES
                                                   (1, 'América del Sur'),
                                                   (2, 'América del Norte'),
                                                   (3, 'Europa'),
                                                   (4, 'Asia'),
                                                   (5, 'Oceanía'),
                                                   (6, 'África'),
                                                   (7, 'Oriente Medio');

-- Insertar datos en la tabla Pais de forma correlativa (1 al 50)
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
                                                      (50, 'Singapur', 4); -- Añadido para cerrar en 50 si gustas, o puedes dejarlo hasta 49

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
                                                    (62, 'Wellington', 25);

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
(34, 'Cairo Intl.',                                              'CAI',  33, ST_MakePoint(31.4056,    30.1219)::geography);





-- Suponiendo que ya existen Aeropuertos con ID 1 al 5
-- Los aeropuertos 1 al 30 se mantienen igual ya que coinciden con tu lista
DO $$
DECLARE
rec RECORD;
    num_puertas INT;
    i INT;
    codigo_p VARCHAR(10);
    terminal_name VARCHAR(50);
BEGIN
    -- Recorremos todos los aeropuertos que ya insertaste en la base de datos
FOR rec IN SELECT ID_AEROPUERTO, Codigo_IATA FROM Aeropuerto LOOP

                                                  -- ASIGNACIÓN DE CAPACIDAD: Definimos cuántas puertas tendrá según su importancia
    IF rec.Codigo_IATA IN ('JFK', 'LHR', 'CDG', 'MIA', 'LAX', 'GRU', 'DXB', 'FRA', 'ATL') THEN
            num_puertas := 10; -- Megahubs internacionales
ELSIF rec.Codigo_IATA IN ('SCL', 'LIM', 'BOG', 'MAD', 'BCN', 'EZE', 'PTY') THEN
            num_puertas := 6;  -- Hubs regionales importantes
ELSE
            num_puertas := 4;  -- Aeropuertos estándar (cumple con tener más de 3)
END IF;

        -- Bucle para insertar la cantidad de puertas asignada a este aeropuerto
FOR i IN 1..num_puertas LOOP

            -- Lógica para alternar terminales (ej: Terminal 1 para impares, Terminal 2 para pares)
            IF i % 2 = 0 THEN
                terminal_name := 'Terminal Internacional';
ELSE
                terminal_name := 'Terminal Nacional';
END IF;

            -- Generamos un código de puerta dinámico (Ej: SCL-01, JFK-05)
            codigo_p := rec.Codigo_IATA || '-' || LPAD(i::text, 2, '0');

            -- Inserción en la base de datos
INSERT INTO Puerta_Embarque (Codigo_Puerta, Terminal, ID_AEROPUERTO)
VALUES (codigo_p, terminal_name, rec.ID_AEROPUERTO);

END LOOP;
END LOOP;
END $$;





-- 2. Insertamos la aerolínea exclusiva del sistema
INSERT INTO Aerolinea (ID_AEROLINEA, Nombre, Codigo) VALUES
    (1, 'SkyWay Airlines', 'SW');


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
