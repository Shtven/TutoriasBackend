-- =====================================================================
-- Migracion (revisada): matricula String -> Integer con limpieza de datos
-- Fecha: 2026-06-03
-- Motor: PostgreSQL (Supabase)
--
-- Sustituye al script 2026-06-02 que no contemplaba datos sucios.
--
-- Esta version:
--   1. Borra en cascada los usuarios con matricula que no se puede
--      convertir a 8 digitos numericos (junto con sus horarios,
--      tutorias, asistencias, comentarios y temas asociados).
--   2. Normaliza los usuarios cuya matricula tenia prefijo zS/s,
--      quitando el prefijo en todas las tablas (usuario, horario,
--      asistencia, comentarios) en una sola transaccion.
--   3. Convierte el tipo de la columna a integer.
--   4. Recrea las foreign keys.
--
-- IMPORTANTE: haz backup antes de ejecutar (Supabase > Database > Backups).
-- =====================================================================

BEGIN;

-- ---------------------------------------------------------------------
-- 1. Eliminar foreign keys que apuntan a usuario.matricula.
-- ---------------------------------------------------------------------
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

-- ---------------------------------------------------------------------
-- 2. Borrar en cascada usuarios con matricula irrecuperable.
--    Lista cerrada para evitar borrar de mas si la tabla tiene otros
--    valores raros que no detectamos ahora mismo.
-- ---------------------------------------------------------------------

-- 2a. Si esos usuarios eran tutores, sus tutorias se llevan por delante
--     temas, asistencias y comentarios anidados.
DELETE FROM temas
WHERE id_tutoria IN (
    SELECT t.id_tutoria FROM tutoria t
    JOIN horario h ON t.id_horario = h.id_horario
    WHERE h.matricula_tutor IN ('1234567', 'zS2320594')
);

DELETE FROM asistencia
WHERE id_tutoria IN (
    SELECT t.id_tutoria FROM tutoria t
    JOIN horario h ON t.id_horario = h.id_horario
    WHERE h.matricula_tutor IN ('1234567', 'zS2320594')
);

DELETE FROM comentarios
WHERE id_tutoria IN (
    SELECT t.id_tutoria FROM tutoria t
    JOIN horario h ON t.id_horario = h.id_horario
    WHERE h.matricula_tutor IN ('1234567', 'zS2320594')
);

DELETE FROM tutoria
WHERE id_horario IN (
    SELECT id_horario FROM horario
    WHERE matricula_tutor IN ('1234567', 'zS2320594')
);

DELETE FROM horario
WHERE matricula_tutor IN ('1234567', 'zS2320594');

-- 2b. Si esos usuarios eran tutorados, borrar sus filas directas.
DELETE FROM asistencia
WHERE matricula IN ('1234567', 'zS2320594');

DELETE FROM comentarios
WHERE matricula_usuario IN ('1234567', 'zS2320594');

-- 2c. Finalmente el usuario.
DELETE FROM usuario
WHERE matricula IN ('1234567', 'zS2320594');

-- ---------------------------------------------------------------------
-- 3. Normalizar matriculas con prefijo zS/s en TODAS las tablas.
--    regexp_replace con flag 'i' = case-insensitive.
-- ---------------------------------------------------------------------
UPDATE usuario
SET matricula = regexp_replace(matricula, '^(zs|s)', '', 'i')
WHERE matricula ~* '^(zs|s)\d+$';

UPDATE horario
SET matricula_tutor = regexp_replace(matricula_tutor, '^(zs|s)', '', 'i')
WHERE matricula_tutor ~* '^(zs|s)\d+$';

UPDATE asistencia
SET matricula = regexp_replace(matricula, '^(zs|s)', '', 'i')
WHERE matricula ~* '^(zs|s)\d+$';

UPDATE comentarios
SET matricula_usuario = regexp_replace(matricula_usuario, '^(zs|s)', '', 'i')
WHERE matricula_usuario ~* '^(zs|s)\d+$';

-- ---------------------------------------------------------------------
-- 4. Sanity check: todo debe ser ahora exactamente 8 digitos numericos.
--    Si quedo algo raro, abortar la transaccion en seco.
-- ---------------------------------------------------------------------
DO $$
DECLARE
    bad_usuario       integer;
    bad_horario       integer;
    bad_asistencia    integer;
    bad_comentarios   integer;
BEGIN
    SELECT COUNT(*) INTO bad_usuario
    FROM usuario WHERE matricula !~ '^\d{8}$';

    SELECT COUNT(*) INTO bad_horario
    FROM horario WHERE matricula_tutor IS NOT NULL
                   AND matricula_tutor !~ '^\d{8}$';

    SELECT COUNT(*) INTO bad_asistencia
    FROM asistencia WHERE matricula IS NOT NULL
                      AND matricula !~ '^\d{8}$';

    SELECT COUNT(*) INTO bad_comentarios
    FROM comentarios WHERE matricula_usuario IS NOT NULL
                       AND matricula_usuario !~ '^\d{8}$';

    IF bad_usuario + bad_horario + bad_asistencia + bad_comentarios > 0 THEN
        RAISE EXCEPTION
            'Quedan matriculas no-8-digitos: usuario=%, horario=%, asistencia=%, comentarios=%',
            bad_usuario, bad_horario, bad_asistencia, bad_comentarios;
    END IF;
END $$;

-- ---------------------------------------------------------------------
-- 5. Convertir el tipo de columna a integer.
-- ---------------------------------------------------------------------
ALTER TABLE usuario
    ALTER COLUMN matricula TYPE integer USING matricula::integer;

ALTER TABLE horario
    ALTER COLUMN matricula_tutor TYPE integer USING matricula_tutor::integer;

ALTER TABLE asistencia
    ALTER COLUMN matricula TYPE integer USING matricula::integer;

ALTER TABLE comentarios
    ALTER COLUMN matricula_usuario TYPE integer USING matricula_usuario::integer;

-- ---------------------------------------------------------------------
-- 6. Recrear foreign keys.
-- ---------------------------------------------------------------------
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
--   SELECT column_name, data_type
--   FROM information_schema.columns
--   WHERE table_name IN ('usuario','horario','asistencia','comentarios')
--     AND column_name IN ('matricula','matricula_tutor','matricula_usuario');
--
-- Esperado: 4 filas con data_type = integer.
