-- DROP TABLE IF EXISTS notificacion; -- Solo si necesitas recrear

CREATE TABLE Notificacion (
    ID_NOTIFICACION INT PRIMARY KEY DEFAULT nextval('notificacion_seq'),

    RUT_DESTINATARIO VARCHAR(12) NOT NULL REFERENCES Usuario(RUT), -- Destinatario de la notificación

    Titulo VARCHAR(100) NOT NULL,
    Mensaje TEXT NOT NULL,

    Tipo VARCHAR(50),               -- Ej: 'Vuelo', 'Reserva', 'Checkin', 'Pago', etc.
    Canal VARCHAR(20) DEFAULT 'App',-- Ej: 'App', 'Email', 'SMS'

    Leido BOOLEAN DEFAULT FALSE,    -- Marcada como leída por el usuario
    Enviada BOOLEAN DEFAULT FALSE,  -- Marcada como enviada por backend (correo, push, etc.)

    Prioridad VARCHAR(20) DEFAULT 'Normal', -- Opcional: 'Alta', 'Media', 'Baja'

    Fecha TIMESTAMP DEFAULT NOW()
);



ALTER TABLE notificacion
ADD COLUMN IF NOT EXISTS enviada BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS canal VARCHAR(20) DEFAULT 'Email';


UPDATE Segmento_Vuelo
SET HORA_SALIDA = '2025-10-01 08:00:00'
WHERE ID_VUELO = 1 AND ORDEN_SEGMENTO = 1;





UPDATE Usuario
SET Correo_Electronico = 'cr.romanz@duocuc.cl'
WHERE Correo_Electronico = 'juan.perez@piloto.com';