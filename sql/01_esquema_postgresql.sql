-- ejecutar conectado a la base aerolinea; postgres 12 o superior
CREATE TABLE IF NOT EXISTS pasajero (
    id BIGSERIAL PRIMARY KEY,
    nombres VARCHAR(80) NOT NULL,
    apellidos VARCHAR(80) NOT NULL,
    tipo_documento VARCHAR(3) NOT NULL,
    numero_documento VARCHAR(25) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    correo VARCHAR(120),
    telefono VARCHAR(25),
    nacionalidad VARCHAR(60),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_pasajero_documento UNIQUE (tipo_documento, numero_documento),
    CONSTRAINT ck_pasajero_tipo CHECK (tipo_documento IN ('CC','CE','PAS','TI')),
    CONSTRAINT ck_pasajero_documento CHECK (char_length(numero_documento) BETWEEN 4 AND 25)
);
CREATE INDEX IF NOT EXISTS idx_pasajero_busqueda ON pasajero (apellidos, nombres);
CREATE INDEX IF NOT EXISTS idx_pasajero_activo ON pasajero (activo);
