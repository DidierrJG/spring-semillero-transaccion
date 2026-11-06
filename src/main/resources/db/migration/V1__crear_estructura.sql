CREATE TABLE directores (
    id     BIGSERIAL    PRIMARY KEY,
    nombre VARCHAR(160) NOT NULL,
    correo VARCHAR(180) NOT NULL UNIQUE
);

CREATE TABLE semilleros (
    id                  BIGSERIAL    PRIMARY KEY,
    nombre              VARCHAR(180) NOT NULL,
    sigla               VARCHAR(30)  NOT NULL UNIQUE,
    grupo_investigacion VARCHAR(180) NOT NULL,
    mision              TEXT,
    vision              TEXT,
    objetivo_general    TEXT,
    director_id         BIGINT       NOT NULL REFERENCES directores(id)
);

CREATE TABLE lineas (
    id     BIGSERIAL    PRIMARY KEY,
    nombre VARCHAR(180) NOT NULL UNIQUE
);

CREATE TABLE semillero_lineas (
    semillero_id BIGINT NOT NULL REFERENCES semilleros(id) ON DELETE CASCADE,
    linea_id     BIGINT NOT NULL REFERENCES lineas(id) ON DELETE RESTRICT,
    PRIMARY KEY (semillero_id, linea_id)
);

CREATE TABLE integrantes (
    id           BIGSERIAL    PRIMARY KEY,
    semillero_id BIGINT       NOT NULL REFERENCES semilleros(id) ON DELETE CASCADE,
    codigo       VARCHAR(50)  NOT NULL,
    nombre       VARCHAR(160) NOT NULL,
    programa     VARCHAR(160) NOT NULL,
    es_lider     BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_integrante_semillero_codigo UNIQUE (semillero_id, codigo)
);

-- Indice parcial PostgreSQL: exactamente maximo un lider por semillero a nivel BD
CREATE UNIQUE INDEX uq_semillero_un_lider ON integrantes (semillero_id) WHERE es_lider = true;