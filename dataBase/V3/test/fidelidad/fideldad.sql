-- 1. Niveles del Programa de Fidelidad (Ej: Clásica, Plata, Oro, Platino)
CREATE TABLE Nivel_Fidelidad (
                                 ID_NIVEL INT PRIMARY KEY DEFAULT nextval('nivel_fidelidad_seq'),
                                 Nombre_Nivel VARCHAR(50) NOT NULL UNIQUE, -- Ej: 'Member', 'Silver', 'Gold', 'Black'
                                 Millas_Minimas_Anuales INT NOT NULL,     -- Millas necesarias para subir/mantener el nivel
                                 Multiplicador_Millas DECIMAL(3,2) DEFAULT 1.00, -- Bonificación (Ej: Gold acumula x1.5)
                                 Prioridad_Embarque INT DEFAULT 0,
                                 Equipaje_Extra_Kg INT DEFAULT 0
);

-- 2. Cuenta de Fidelización asociada al Pasajero
CREATE TABLE Cuenta_Fidelidad (
                                  ID_CUENTA INT PRIMARY KEY DEFAULT nextval('cuenta_fidelidad_seq'),
                                  Codigo_Socio VARCHAR(20) NOT NULL UNIQUE,  -- Número único de miembro
                                  RUT_PASAJERO VARCHAR(12) NOT NULL UNIQUE REFERENCES Pasajero(RUT) ON DELETE CASCADE,
                                  ID_NIVEL INT NOT NULL REFERENCES Nivel_Fidelidad(ID_NIVEL),
                                  Saldo_Millas_Disponibles INT DEFAULT 0 CHECK (Saldo_Millas_Disponibles >= 0),
                                  Millas_Calificables_Anio_Actual INT DEFAULT 0, -- Millas que cuentan para subir de nivel
                                  Fecha_Inscripcion DATE DEFAULT CURRENT_DATE
);

-- 3. Historial/Bitácora de Transacciones de Millas (Abonos y Canjes)
CREATE TABLE Transaccion_Millas (
                                    ID_TRANSACCION INT PRIMARY KEY DEFAULT nextval('transaccion_millas_seq'),
                                    ID_CUENTA INT NOT NULL REFERENCES Cuenta_Fidelidad(ID_CUENTA) ON DELETE CASCADE,
                                    ID_RESERVA INT REFERENCES Reserva(ID_RESERVA), -- Opcional, si la transacción proviene de un vuelo
                                    Tipo_Transaccion VARCHAR(20) NOT NULL CHECK (Tipo_Transaccion IN ('ABONO_VUELO', 'ABONO_ALIADO', 'CANJE_VUELO', 'CANJE_UPGRADE', 'VENCIMIENTO')),
                                    Millas INT NOT NULL,                          -- Positivo para abonos, negativo para canjes
                                    Fecha_Transaccion TIMESTAMP DEFAULT NOW(),
                                    Fecha_Vencimiento DATE,                       -- Aplica para abonos (Ej: vencen en 2 años)
                                    Descripcion VARCHAR(255)
);

-- 4. Catálogo de Alianzas Comerciales (Tarjetas de Crédito, Hoteles)
CREATE TABLE Socio_Comercial (
                                 ID_SOCIO INT PRIMARY KEY DEFAULT nextval('socio_comercial_seq'),
                                 Nombre VARCHAR(100) NOT NULL,
                                 Categoria VARCHAR(50) CHECK (Categoria IN ('Banco', 'Hotel', 'Rent_a_Car', 'Retail'))
);

-- 5. Canje de Millas por Servicios Adicionales o Vuelos
CREATE TABLE Canje_Millas (
                              ID_CANJE INT PRIMARY KEY DEFAULT nextval('canje_millas_seq'),
                              ID_TRANSACCION INT NOT NULL REFERENCES Transaccion_Millas(ID_TRANSACCION),
                              Tipo_Premio VARCHAR(50) CHECK (Tipo_Premio IN ('Boleto_Gratis', 'Upgrade_Clase', 'Equipaje_Adicional', 'Acceso_Lounge')),
                              Millas_Utilizadas INT NOT NULL
);