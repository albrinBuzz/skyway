CREATE TABLE reserva_asiento (
    id SERIAL PRIMARY KEY,
    id_vuelo INT NOT NULL,
    id_asiento INT NOT NULL,
    estado VARCHAR(20) NOT NULL, -- 'bloqueado', 'confirmado', 'liberado'
    ts_bloqueo TIMESTAMP NOT NULL,
    pasajero_id INT NOT NULL
);

CREATE UNIQUE INDEX idx_reserva_unica ON reserva_asiento (id_vuelo, id_asiento) WHERE estado IN ('bloqueado', 'confirmado');
