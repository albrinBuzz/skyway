-- Registro de capturas de pasarela: anti-replay y reconciliacion (ver PagoRetornoService).
CREATE SEQUENCE IF NOT EXISTS pago_pasarela_seq;

CREATE TABLE IF NOT EXISTS pago_pasarela (
    id_pago_pasarela    INT PRIMARY KEY DEFAULT nextval('pago_pasarela_seq'),
    proveedor           VARCHAR(20)  NOT NULL,
    id_externo          VARCHAR(200) NOT NULL,
    orden_compra        VARCHAR(64),
    monto               NUMERIC(12,2) NOT NULL CHECK (monto > 0),
    moneda              VARCHAR(3),
    estado              VARCHAR(24)  NOT NULL
        CHECK (estado IN ('CAPTURADO', 'PAGADA', 'REEMBOLSADO', 'REEMBOLSO_PENDIENTE')),
    id_reserva          INT REFERENCES reserva(id_reserva),
    fecha_creacion      TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP NOT NULL,
    CONSTRAINT uq_pago_pasarela_externo UNIQUE (proveedor, id_externo)
);

-- Metodo de pago usado por MercadoPagoController (id 3).
INSERT INTO Metodo_Pago (Descripcion)
SELECT 'MercadoPago' WHERE NOT EXISTS (SELECT 1 FROM Metodo_Pago WHERE Descripcion = 'MercadoPago');
