-- Estructura híbrida para SkyWay
CREATE TABLE Tarifa (
                        ID_TARIFA SERIAL PRIMARY KEY,
                        Nombre VARCHAR(50) NOT NULL UNIQUE,
                        Descripcion TEXT,
                        Configuracion JSONB NOT NULL DEFAULT '{}'::jsonb
);

-- 1. Tarifa "Zero" (La más barata, casi sin beneficios)
INSERT INTO Tarifa (Nombre, Descripcion, Configuracion)
VALUES (
           'Zero',
           'Viaja ligero, solo con mochila.',
           '{
             "equipaje": {
               "mochila_personal": true,
               "maleta_cabina": 0,
               "maletas_bodega": 0
             },
             "asientos": {
               "seleccion_gratuita": false,
               "permite_cambio": false
             },
             "penalidades": {
               "reembolsable": false,
               "cambio_vuelo": "no_permitido"
             }
           }'
       );

-- 2. Tarifa "Plus" (Intermedia)
INSERT INTO Tarifa (Nombre, Descripcion, Configuracion)
VALUES (
           'Plus',
           'Ideal para viajes cortos con maleta de mano.',
           '{
             "equipaje": {
               "mochila_personal": true,
               "maleta_cabina": 1,
               "maletas_bodega": 0
             },
             "asientos": {
               "seleccion_gratuita": true,
               "filas_permitidas": ["traseras", "medio"],
               "permite_cambio": true,
               "horas_limite_cambio": 48
             },
             "penalidades": {
               "reembolsable": false,
               "cambio_vuelo": "con_multa",
               "costo_multa_usd": 30.00
             }
           }'
       );

-- 3. Tarifa "Premium Flex" (La más cara)
INSERT INTO Tarifa (Nombre, Descripcion, Configuracion)
VALUES (
           'Premium Flex',
           'Máxima flexibilidad y comodidad total.',
           '{
             "equipaje": {
               "mochila_personal": true,
               "maleta_cabina": 1,
               "maletas_bodega": 2,
               "peso_max_bodega_kg": 23
             },
             "asientos": {
               "seleccion_gratuita": true,
               "filas_permitidas": ["todas", "salida_emergencia", "primera_fila"],
               "permite_cambio": true,
               "horas_limite_cambio": 2
             },
             "penalidades": {
               "reembolsable": true,
               "cambio_vuelo": "gratis"
             },
             "beneficios_extra": {
               "embarque_prioritario": true,
               "acceso_sala_vip": true,
               "snack_premium_abordo": true
             }
           }'
       );