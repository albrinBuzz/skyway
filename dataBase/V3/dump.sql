
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

	IF NEW.ORDEN IS NOT NULL THEN
    RETURN NEW;
	END IF;


    SELECT COALESCE(MAX(ORDEN), 0)
    INTO ultimo_orden
    FROM Itinerario_Vuelo
    WHERE ID_ITINERARIO = NEW.ID_ITINERARIO;

    IF ultimo_orden = 0 THEN
        NEW.ORDEN := 1;
    ELSE
        NEW.ORDEN := ultimo_orden + 1;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;



CREATE TRIGGER trg_set_orden_itinerario_vuelo
BEFORE INSERT ON Itinerario_Vuelo
FOR EACH ROW
EXECUTE FUNCTION fn_set_orden_itinerario_vuelo();






CREATE OR REPLACE FUNCTION fn_set_orden_segmento_vuelo()
RETURNS TRIGGER AS $$
DECLARE
    ultimo_orden INT;
BEGIN
    -- Si ORDEN_SEGMENTO fue especificado manualmente, respetarlo
    IF NEW.ORDEN_SEGMENTO IS NOT NULL THEN
        RETURN NEW;
    END IF;

    -- Obtener el último ORDEN_SEGMENTO para el mismo ID_VUELO
    SELECT COALESCE(MAX(ORDEN_SEGMENTO), 0)
    INTO ultimo_orden
    FROM Segmento_Vuelo
    WHERE ID_VUELO = NEW.ID_VUELO;

    -- Asignar nuevo orden_segmento
    NEW.ORDEN_SEGMENTO := ultimo_orden + 1;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_set_orden_segmento_vuelo
BEFORE INSERT ON Segmento_Vuelo
FOR EACH ROW
EXECUTE FUNCTION fn_set_orden_segmento_vuelo();




CREATE OR REPLACE FUNCTION fn_set_numero_vuelo()
RETURNS TRIGGER AS $$
DECLARE
   	secuencia_vuelo INT;
	codigoArp1 varchar;
	codigoArp2 varchar;
	numeroVueloN varchar;
BEGIN

		SELECT currval('segmento_vuelo_seq') INTO secuencia_vuelo;

	    IF NEW.ORDEN_SEGMENTO = 1 THEN

			 SELECT apr1.Codigo_IATA INTO codigoArp1
		    FROM aeropuerto apr1
		    WHERE apr1.id_aeropuerto = NEW.ID_AEROPUERTO_ORIGEN;

		    SELECT apr2.Codigo_IATA INTO codigoArp2
		    FROM aeropuerto apr2
		    WHERE apr2.id_aeropuerto = NEW.ID_AEROPUERTO_DESTINO;

	    	numeroVueloN := codigoArp1 || '-' || codigoArp2 || secuencia_vuelo::TEXT;

	        UPDATE vuelo
	        SET numero_vuelo = numeroVueloN
	        WHERE id_vuelo = NEW.ID_VUELO;

	    ELSIF NEW.ORDEN_SEGMENTO > 1 THEN

					 SELECT apr1.Codigo_IATA INTO codigoArp1
		    FROM aeropuerto apr1
		    WHERE apr1.id_aeropuerto = NEW.ID_AEROPUERTO_ORIGEN;

		    SELECT apr2.Codigo_IATA INTO codigoArp2
		    FROM aeropuerto apr2
		    WHERE apr2.id_aeropuerto = NEW.ID_AEROPUERTO_DESTINO;

	 		numeroVueloN := codigoArp1 || '-' || codigoArp2 || secuencia_vuelo::TEXT;

	        UPDATE vuelo
	        SET numero_vuelo = numeroVueloN
	        WHERE id_vuelo = NEW.ID_VUELO;


	    END IF;


    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER trg_set_numero_vuelo
AFTER INSERT ON Segmento_Vuelo
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





CREATE OR REPLACE FUNCTION fn_set_fecha_vuelo()
RETURNS TRIGGER AS $$
DECLARE
    fechaSalida TIMESTAMP;
    fechaLlegada TIMESTAMP;
BEGIN

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

        UPDATE vuelo
        SET Fecha_Hora_Salida = fechaSalida,
            Fecha_Hora_Llegada = fechaLlegada
        WHERE ID_VUELO = NEW.ID_VUELO;



    RETURN NEW;
END;
$$ LANGUAGE plpgsql;



CREATE TRIGGER trg_set_fecha_vuelo
AFTER INSERT ON Segmento_Vuelo
FOR EACH ROW
EXECUTE FUNCTION fn_set_fecha_vuelo();


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



CREATE OR REPLACE PROCEDURE spConfirmar_reserva(
    IN p_idVuelo INT,
	IN p_idReserva INT,
    IN p_asientos INT[],
    IN p_rutPasajero TEXT,
    OUT p_resultado TEXT
)
LANGUAGE plpgsql
AS $$
DECLARE
    reserva_id INT;
    estado_reserva_id INT := 1;  -- Suponemos 1 = pendiente o confirmada
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
    WHERE id_vuelo = p_idVuelo;

    -- Iniciar transacción (implícita en SP)
    -- Crear la reserva
    /*INSERT INTO reserva (rut_pasajero, fecha_reserva, estado_reserva, total)
    VALUES (p_rutPasajero, CURRENT_TIMESTAMP, estado_reserva_id, 0)
    RETURNING id_reserva INTO reserva_id;*/

    FOR i IN 1..array_length(p_asientos, 1)
    LOOP
        id_asientoP := p_asientos[i];

        -- Verificar si el asiento ya está reservado en este vuelo
        SELECT 1 INTO asiento_en_reserva
		FROM reserva_asiento ra
		where ra.ID_VUELO=p_idVuelo
		and ra.ID_ASIENTO=id_asientoP
		FOR UPDATE;



        IF asiento_en_reserva > 0 THEN
            SELECT ast.numero_asiento INTO numero_asiento
            FROM asiento ast
            WHERE ast.id_asiento = id_asientoP;

            asientos_reservados := asientos_reservados || numero_asiento || ', ';
        ELSE
            -- Insertar en reserva_asiento
            INSERT INTO reserva_asiento (id_reserva, id_asiento,ID_VUELO,rut)
            VALUES (p_idReserva, id_asientoP,p_idVuelo,p_rutPasajero);
        END IF;
    END LOOP;

    IF asientos_reservados <> '' THEN
        p_resultado := 'ERROR: Asientos ya reservados: ' || LEFT(asientos_reservados, LENGTH(asientos_reservados) - 2);
        -- Puedes eliminar la reserva si quedó sin asientos
        DELETE FROM reserva WHERE id_reserva = p_idReserva;
    ELSE
        -- Asociar la reserva con el vuelo
        --INSERT INTO id_asientoP (id_reserva, id_vuelo)
        --VALUES (reserva_id, p_idVuelo);

        p_resultado := 'OK: Reserva realizada correctamente.';
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        -- Rollback seguro en caso de error
        RAISE NOTICE 'Ocurrió un error: %', SQLERRM;
        DELETE FROM reserva WHERE id_reserva = p_idReserva;
        p_resultado := 'ERROR: No se pudo completar la reserva.'||SQLERRM;
END;
$$;



CREATE OR REPLACE PROCEDURE spVerificarDisponinibilidadAsientos(
    IN p_idVuelo INT,
    IN p_asientos INT[],
    OUT p_resultado TEXT
)
LANGUAGE plpgsql
AS $$
DECLARE
    reserva_id INT;
    estado_reserva_id INT := 1;  -- Suponemos 1 = pendiente o confirmada
    i INT;
    id_avion INT;
    id_asientoP INT;
    asiento_en_reserva INT;
    numero_asiento TEXT;
    asientos_reservados TEXT := '-';
BEGIN
    -- Obtener el avión asignado al vuelo
    SELECT vl.id_avion INTO id_avion
    FROM vuelo vl
    WHERE id_vuelo = p_idVuelo;


    FOR i IN 1..array_length(p_asientos, 1)
    LOOP
        id_asientoP := p_asientos[i];

        -- Verificar si el asiento ya está reservado en este vuelo
        SELECT 1 INTO asiento_en_reserva
		FROM reserva_asiento ra
		where ra.ID_VUELO=p_idVuelo
		and ra.ID_ASIENTO=id_asientoP
		FOR UPDATE;



        IF asiento_en_reserva > 0 THEN
            asientos_reservados := asientos_reservados || numero_asiento || ', ';
        END IF;
    END LOOP;

    IF asientos_reservados <> '-' THEN
        p_resultado := 'ERROR: Asientos ya reservados: ' || LEFT(asientos_reservados, LENGTH(asientos_reservados) - 2);
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        -- Rollback seguro en caso de error
        RAISE NOTICE 'Ocurrió un error: %', SQLERRM;
        p_resultado := 'ERROR: No se pudo completar la reserva.'||SQLERRM;
END;
$$;


CREATE or replace PROCEDURE sp_cambiarAsiento(
   in p_id_asiento int ,
   in p_id_reserva int,
   in p_id_asiento_org int
)
LANGUAGE plpgsql
AS $$
BEGIN

  	update reserva_asiento
	  set id_asiento=p_id_asiento
	  where id_reserva=p_id_reserva
	  and id_asiento=p_id_asiento_org;




END;
$$;



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
  (nextval('vuelo_seq'),'LA8118', '2025-08-01 18:55', '2025-08-01 21:30', 1, 1, '12345678-9', 1), -- Vuelo 2: MVD -> GRU (Parte del Itinerario 1)
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



/*INSERT INTO aeropuertos (nombre, ciudad, codigo_iata, latitud, longitud, ubicacion)
VALUES
('Aeropuerto Internacional de la Ciudad de México', 'Ciudad de México', 'MMMX', 19.4361, -99.0721, ST_SetSRID(ST_MakePoint(-99.0721, 19.4361), 4326)),
('Aeropuerto Internacional de Madrid-Barajas', 'Madrid', 'LEMD', 40.4531, -3.5772, ST_SetSRID(ST_MakePoint(-3.5772, 40.4531), 4326)),
('Aeropuerto de Barcelona-El Prat', 'Barcelona', 'LEBL', 41.2973, 2.0833, ST_SetSRID(ST_MakePoint(2.0833, 41.2973), 4326)),
('Aeropuerto de Los Ángeles', 'Los Ángeles', 'KLAX', 33.9416, -118.4085, ST_SetSRID(ST_MakePoint(-118.4085, 33.9416), 4326));*/
