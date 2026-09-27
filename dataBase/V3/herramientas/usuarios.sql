-- Usuario DBA (solo administración)
CREATE USER airline_dba WITH PASSWORD 'DBA_STRONG_PASSWORD';

-- Usuario aplicación
CREATE USER airline_app WITH PASSWORD 'APP_STRONG_PASSWORD';

-- Base de datos
CREATE DATABASE airline_prod OWNER airline_dba;

-- Conexión
GRANT CONNECT ON DATABASE airline_prod TO airline_app;



\c airline_prod;

-- Uso del esquema
GRANT USAGE ON SCHEMA public TO airline_app;

-- Permisos sobre tablas
GRANT SELECT, INSERT, UPDATE, DELETE
ON ALL TABLES IN SCHEMA public
TO airline_app;

-- Permisos sobre secuencias
GRANT USAGE, SELECT
ON ALL SEQUENCES IN SCHEMA public
TO airline_app;

-- Permisos automáticos para futuras tablas
ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO airline_app;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT USAGE, SELECT ON SEQUENCES TO airline_app;
