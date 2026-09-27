-- Eliminamos las tablas viejas si existen
DROP TABLE IF EXISTS Tarifa_Caracteristica CASCADE;
DROP TABLE IF EXISTS Caracteristica_Tarifa CASCADE;
DROP TABLE IF EXISTS Tarifa CASCADE;

-- Creamos la nueva tabla con JSONB
CREATE TABLE Tarifa (
    ID_TARIFA INT PRIMARY KEY DEFAULT nextval('tarifa_seq'),
    Nombre VARCHAR(50) NOT NULL UNIQUE,
    Descripcion TEXT,
    -- Aquí vivirá toda la flexibilidad de tu modelo "NoSQL"
    Configuracion JSONB NOT NULL DEFAULT '{}'::jsonb
);