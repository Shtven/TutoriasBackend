-- =====================================================================
-- 004 — Registro y login con contraseña
-- =====================================================================
-- Agrega usuario.password_hash a una BD creada con 001_esquema.sql antes
-- de este cambio. Lo usan POST /auth/signup y POST /auth/signin
-- (docs/AUTENTICACION.md § 9).
--
-- NO es destructivo: no borra ni modifica datos. Es seguro re-ejecutarlo.
-- Una BD nueva creada con la versión actual de 001_esquema.sql ya trae
-- la columna; ahí este script no hace nada.
--
-- Como aplicar en Supabase:
--   Dashboard > SQL Editor > New query > pegar este archivo > Run.
--   (o)  psql "<connection string>" -f db/supabase/004_registro_con_contrasena.sql
-- =====================================================================

BEGIN;

ALTER TABLE usuario ADD COLUMN IF NOT EXISTS password_hash VARCHAR(100);

-- Solo hashes BCrypt ($2a$, $2b$ o $2y$): evita guardar una contraseña en
-- texto plano por error al editar la tabla a mano.
ALTER TABLE usuario DROP CONSTRAINT IF EXISTS ck_usuario_password_bcrypt;
ALTER TABLE usuario ADD CONSTRAINT ck_usuario_password_bcrypt
    CHECK (password_hash ~ '^\$2[aby]\$[0-9]{2}\$[./A-Za-z0-9]{53}$');

COMMIT;

-- ---------------------------------------------------------------------
-- Opcional: contraseña para un usuario que ya existe
-- ---------------------------------------------------------------------
-- El ADMIN no puede registrarse por la API (su rol se asigna en BD y su
-- matrícula no tiene formato de alumno). Para que entre con
-- POST /auth/signin, ponle una contraseña con pgcrypto, que en Supabase
-- está instalada en el esquema "extensions":
--
-- UPDATE usuario
--    SET password_hash = extensions.crypt('Cambia-esta-contraseña', extensions.gen_salt('bf', 10))
--  WHERE correo = 'asalas@uv.mx';
--
-- Entra con {"correo": "asalas@uv.mx", "password": "..."} o con
-- {"matricula": "25480", "password": "..."}.
