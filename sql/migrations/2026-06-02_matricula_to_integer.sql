-- =====================================================================
-- Migracion: matricula String -> Integer
-- Fecha: 2026-06-02
-- Motor: PostgreSQL (Supabase)
--
-- IMPORTANTE: hacer backup antes de ejecutar.
-- Pre-requisito: todas las matriculas existentes deben ser numericas.
-- Verifica con:
--   SELECT matricula FROM usuario WHERE matricula !~ '^[0-9]+$';
-- Si esa consulta devuelve filas, NO ejecutes esta migracion sin antes
-- decidir que hacer con esos usuarios.
-- =====================================================================

BEGIN;

-- 1. Borrar las foreign keys que apuntan a usuario.matricula.
--    Los nombres de constraint los crea Hibernate con prefijo fk*; los
--    descubrimos dinamicamente para no depender del hash.
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT tc.constraint_name, tc.table_name
        FROM information_schema.table_constraints tc
        JOIN information_schema.constraint_column_usage ccu
          ON tc.constraint_name = ccu.constraint_name
        WHERE tc.constraint_type = 'FOREIGN KEY'
          AND ccu.table_name = 'usuario'
          AND ccu.column_name = 'matricula'
    LOOP
        EXECUTE format('ALTER TABLE %I DROP CONSTRAINT %I',
                       r.table_name, r.constraint_name);
    END LOOP;
END $$;

-- 2. Convertir la PK y las columnas FK al tipo integer.
ALTER TABLE usuario
    ALTER COLUMN matricula TYPE integer USING matricula::integer;

ALTER TABLE horario
    ALTER COLUMN matricula_tutor TYPE integer USING matricula_tutor::integer;

ALTER TABLE asistencia
    ALTER COLUMN matricula TYPE integer USING matricula::integer;

ALTER TABLE comentarios
    ALTER COLUMN matricula_usuario TYPE integer USING matricula_usuario::integer;

-- 3. Recrear las foreign keys.
ALTER TABLE horario
    ADD CONSTRAINT fk_horario_tutor
    FOREIGN KEY (matricula_tutor) REFERENCES usuario(matricula);

ALTER TABLE asistencia
    ADD CONSTRAINT fk_asistencia_usuario
    FOREIGN KEY (matricula) REFERENCES usuario(matricula);

ALTER TABLE comentarios
    ADD CONSTRAINT fk_comentarios_usuario
    FOREIGN KEY (matricula_usuario) REFERENCES usuario(matricula);

COMMIT;

-- Verificacion post-migracion:
--   \d usuario
--   \d horario
--   \d asistencia
--   \d comentarios
