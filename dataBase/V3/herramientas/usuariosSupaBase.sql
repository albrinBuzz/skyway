-- Crear el rol de inicio de sesión con una contraseña segura
CREATE ROLE aerolinea_user WITH 
    LOGIN 
    NOSUPERUSER 
    NOCREATEDB 
    NOCREATEROLE 
    INHERIT 
    NOREPLICATION 
    CONNECTION LIMIT -1 
    PASSWORD 'aerolinea_user';

-- Añadir un comentario profesional en el diccionario de datos (Buena práctica)
COMMENT ON ROLE aerolinea_user IS 'Usuario administrador y dueño de la base de datos operativa de SkyWay.';


-- Crear la base de datos parametrizada profesionalmente
CREATE DATABASE aerolinea_db
    WITH 
    ENCODING = 'UTF8'
    CONNECTION LIMIT = -1;


-- 1. Darle todos los privilegios de conexión y creación en esta base de datos
GRANT ALL PRIVILEGES ON DATABASE aerolinea_db TO aerolinea_user;

-- 2. Asegurar que pueda hacer lo que quiera en el esquema público de esta base de datos

-- 2. Darle superpoderes sobre el esquema público (llave maestra para crear y borrar lo que sea)
GRANT ALL ON SCHEMA public TO aerolinea_user;

-- 3. Asegurar control total sobre todas las tablas, secuencias y funciones actuales
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO aerolinea_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO aerolinea_user;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO aerolinea_user;

-- 4. LA CLAVE: Hacer que controle automáticamente todo lo que se cree en el futuro.
-- Como el usuario "postgres" va a ser el que cree las tablas inicialmente si usas scripts,
-- le decimos a Postgres que todo lo que "postgres" cree, le dé control total a "aerolinea_user".
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON TABLES TO aerolinea_user;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON SEQUENCES TO aerolinea_user;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT ALL ON FUNCTIONS TO aerolinea_user;