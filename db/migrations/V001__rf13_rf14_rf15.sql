-- =====================================================================
-- Migration: V001 — RF13 (calificacion), RF14 (temas), RF15 (comentarios)
-- Idempotente: puede ejecutarse multiples veces sin efectos secundarios.
-- =====================================================================

-- ----------------------------------------------------------------------
-- RF13: agregar columna calificacion (1-5, nullable) a asistencia
-- ----------------------------------------------------------------------
ALTER TABLE asistencia
    ADD COLUMN IF NOT EXISTS calificacion INTEGER NULL
        CHECK (calificacion IS NULL OR (calificacion BETWEEN 1 AND 5));

-- ----------------------------------------------------------------------
-- RF15: tabla comentarios
-- ----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS comentarios (
    id_comentario     SERIAL       PRIMARY KEY,
    matricula_usuario VARCHAR(50)  NOT NULL,
    id_tutoria        INTEGER      NOT NULL,
    comentario        VARCHAR(255) NOT NULL,
    CONSTRAINT fk_comentarios_usuario
        FOREIGN KEY (matricula_usuario) REFERENCES usuario(matricula)
        ON DELETE CASCADE,
    CONSTRAINT fk_comentarios_tutoria
        FOREIGN KEY (id_tutoria) REFERENCES tutoria(id_tutoria)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_comentarios_tutoria
    ON comentarios(id_tutoria);

CREATE INDEX IF NOT EXISTS idx_comentarios_usuario
    ON comentarios(matricula_usuario);

-- ----------------------------------------------------------------------
-- RF14: tabla temas
-- ----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS temas (
    id_tema    SERIAL       PRIMARY KEY,
    tema       VARCHAR(255) NOT NULL,
    id_tutoria INTEGER      NOT NULL,
    CONSTRAINT fk_temas_tutoria
        FOREIGN KEY (id_tutoria) REFERENCES tutoria(id_tutoria)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_temas_tutoria
    ON temas(id_tutoria);

-- ----------------------------------------------------------------------
-- Seed de roles (no-op si ya existen)
-- ----------------------------------------------------------------------
INSERT INTO rol (rol)
SELECT v
FROM (VALUES ('TUTOR'), ('TUTORADO'), ('ADMIN')) AS r(v)
WHERE NOT EXISTS (
    SELECT 1 FROM rol WHERE rol.rol = r.v
);
