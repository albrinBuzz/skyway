CREATE TABLE Tarifa (
    ID_TARIFA INT PRIMARY KEY DEFAULT nextval('tarifa_seq'),
    Nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE Caracteristica_Tarifa (
    ID_CARACTERISTICA INT PRIMARY KEY DEFAULT nextval('caracteristica_tarifa_seq'),
    Nombre VARCHAR(100) NOT NULL UNIQUE,
    Descripcion TEXT
);

CREATE TABLE Tarifa_Caracteristica (
    ID_TARIFA INT REFERENCES Tarifa(ID_TARIFA) ON DELETE CASCADE,
    ID_CARACTERISTICA INT REFERENCES Caracteristica_Tarifa(ID_CARACTERISTICA) ON DELETE CASCADE,
    Valor VARCHAR(100),
    PRIMARY KEY (ID_TARIFA, ID_CARACTERISTICA)
);







-- Crear tipo de dato ENUM para Tipo_Dato
CREATE TYPE tipo_dato_enum AS ENUM ('boolean', 'int', 'text');

-- Crear la tabla Tarifa
CREATE TABLE Tarifa (
    ID_TARIFA INT PRIMARY KEY DEFAULT nextval('tarifa_seq'),
    Nombre VARCHAR(50) NOT NULL UNIQUE
);

-- Crear la tabla Caracteristica_Tarifa
CREATE TABLE Caracteristica_Tarifa (
    ID_CARACTERISTICA INT PRIMARY KEY DEFAULT nextval('caracteristica_tarifa_seq'),
    Nombre VARCHAR(100) NOT NULL UNIQUE,
    Descripcion TEXT,
    Tipo_Dato tipo_dato_enum
);

-- Crear la tabla Tarifa_Caracteristica
CREATE TABLE Tarifa_Caracteristica (
    ID_TARIFA_CARACTERISTICA INT PRIMARY KEY DEFAULT nextval('tarifa_caracteristica_seq'),
    ID_TARIFA INT REFERENCES Tarifa(ID_TARIFA) ON DELETE CASCADE,
    ID_CARACTERISTICA INT REFERENCES Caracteristica_Tarifa(ID_CARACTERISTICA) ON DELETE CASCADE,
    Valor JSONB, -- Usando JSONB para almacenar valores de distintos tipos
    CONSTRAINT unique_tarifa_caracteristica UNIQUE (ID_TARIFA, ID_CARACTERISTICA)
);


-- Crear la tabla Itinerario_Tarifa
CREATE TABLE Itinerario_Tarifa (
    ID_ITINERARIO_TARIFA INT PRIMARY KEY DEFAULT nextval('itinerario_tarifa_seq'),
    ID_ITINERARIO INT NOT NULL REFERENCES Itinerario(ID_ITINERARIO) ON DELETE CASCADE,
    ID_TARIFA INT NOT NULL REFERENCES Tarifa(ID_TARIFA) ON DELETE CASCADE,
    Precio DECIMAL(10,2) NOT NULL CHECK (Precio >= 0), -- Restricción para precios positivos
    CONSTRAINT unique_itinerario_tarifa UNIQUE (ID_ITINERARIO, ID_TARIFA)
);

-- Crear índices
CREATE INDEX idx_tarifa_caracteristica ON Tarifa_Caracteristica (ID_TARIFA, ID_CARACTERISTICA);
CREATE INDEX idx_itinerario_tarifa ON Itinerario_Tarifa (ID_ITINERARIO, ID_TARIFA);




-- Insertar una tarifa
INSERT INTO Tarifa (Nombre)
VALUES
  ('Tarifa A'),
  ('Tarifa B');


-- Insertar características de tarifa
INSERT INTO Caracteristica_Tarifa (Nombre, Descripcion, Tipo_Dato)
VALUES
  ('Descuento', 'Porcentaje de descuento en la tarifa', 'int'),
  ('Incluye comida', 'Indica si la tarifa incluye comida', 'boolean'),
  ('Comentarios', 'Texto descriptivo adicional sobre la tarifa', 'text');


-- Insertar en Tarifa_Caracteristica con JSONB para los valores
INSERT INTO Tarifa_Caracteristica (ID_TARIFA, ID_CARACTERISTICA, Valor)
VALUES
  (1, 1, '{"int": 20}'),                -- Tarifa A, 20% de descuento (Valor de tipo int)
  (1, 2, '{"boolean": true}'),           -- Tarifa A, incluye comida (Valor de tipo booleano)
  (2, 1, '{"int": 10}'),                -- Tarifa B, 10% de descuento (Valor de tipo int)
  (2, 3, '{"text": "Tarifa flexible"}'); -- Tarifa B, comentario adicional (Valor de tipo texto)


-- Insertar en Itinerario_Tarifa
INSERT INTO Itinerario_Tarifa (ID_ITINERARIO, ID_TARIFA, Precio)
VALUES
  (1, 1, 100.00),  -- Itinerario 1, Tarifa A, Precio 100.00
  (2, 1, 120.00),  -- Itinerario 2, Tarifa A, Precio 120.00
  (1, 2, 90.00),   -- Itinerario 1, Tarifa B, Precio 90.00
  (2, 2, 110.00);  -- Itinerario 2, Tarifa B, Precio 110.00
