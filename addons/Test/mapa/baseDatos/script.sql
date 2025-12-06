-- Habilitar extensión PostGIS
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE aeropuertos (
    id SERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,       -- Código IATA
    name VARCHAR(255) NOT NULL,
    country VARCHAR(100),
    continent VARCHAR(50),
    timezone VARCHAR(50),
    location GEOGRAPHY(POINT, 4326) NOT NULL
);

-- Ejemplo de inserción
INSERT INTO aeropuertos (code, name, country, continent, timezone, location)
VALUES ('JFK', 'New York - JFK', 'USA', 'North America', 'America/New_York', ST_GeogFromText('POINT(-73.7781 40.6413)'));


CREATE TABLE rutas (
    id SERIAL PRIMARY KEY,
    origin_airport_id INT NOT NULL REFERENCES aeropuertos(id),
    destination_airport_id INT NOT NULL REFERENCES aeropuertos(id),
    distance_km DOUBLE PRECISION,
    flight_time_minutes INT
);


UPDATE rutas
SET distance_km = ST_Distance(
    (SELECT location FROM aeropuertos WHERE id = origin_airport_id),
    (SELECT location FROM aeropuertos WHERE id = destination_airport_id)
)/1000;  -- metros a km


CREATE TABLE waypoints (
    id SERIAL PRIMARY KEY,
    ruta_id INT NOT NULL REFERENCES rutas(id),
    sequence_num INT NOT NULL,               -- orden del waypoint
    location GEOGRAPHY(POINT, 4326) NOT NULL
);


SELECT a1.name AS origen, a2.name AS destino,
       ST_Distance(a1.location, a2.location)/1000 AS distancia_km
FROM aeropuertos a1, aeropuertos a2
WHERE a1.code='JFK' AND a2.code='LAX';


SELECT code, name,
       ST_Distance(location, ST_GeogFromText('POINT(-73.7781 40.6413)'))/1000 AS distancia_km
FROM aeropuertos
WHERE ST_DWithin(location, ST_GeogFromText('POINT(-73.7781 40.6413)'), 500000);
