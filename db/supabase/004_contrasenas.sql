-- =====================================================================
-- Migración 004 — Login con contraseña (POST /auth/signin y /auth/signup)
--
-- Para una BD creada con 001_esquema.sql ANTES de este cambio. No borra
-- datos y se puede re-ejecutar. Una BD nueva no la necesita: 001_esquema.sql
-- ya trae lo mismo.
--
-- Sirve también si ya se aplicó la versión anterior de esta migración
-- (004_registro_con_contrasena.sql, 12-oct-2026): conserva las contraseñas
-- que existan y marca esas cuentas como LOCAL.
--
-- ORDEN DE DESPLIEGUE: aplicar esto ANTES de desplegar el backend nuevo.
-- Hibernate valida el esquema al arrancar (ddl-auto=validate) y no inicia
-- si falta usuario.password_hash. El backend anterior sigue funcionando con el
-- esquema ya migrado.
--
-- Como aplicar en Supabase:
--   Dashboard > SQL Editor > New query > pegar este archivo > Run.
--   (o)  psql "<connection string>" -f db/supabase/004_contrasenas.sql
--
-- Estados válidos de una fila de usuario (ck_usuario_credencial):
--   proveedor NULL                    pre-registro: sin credencial; el
--                                     signup lo reclama si el correo coincide
--   proveedor 'LOCAL'                 contraseña en password_hash (BCrypt)
--   proveedor 'MICROSOFT' | 'GOOGLE'  cuenta del login anterior; ya no
--                                     puede entrar y el signup la reclama
--                                     igual que a un pre-registro
-- =====================================================================

BEGIN;

ALTER TABLE usuario ADD COLUMN IF NOT EXISTS password_hash VARCHAR(100);

ALTER TABLE usuario DROP CONSTRAINT IF EXISTS ck_usuario_proveedor;
ALTER TABLE usuario DROP CONSTRAINT IF EXISTS ck_usuario_proveedor_id;
ALTER TABLE usuario DROP CONSTRAINT IF EXISTS ck_usuario_credencial;
ALTER TABLE usuario DROP CONSTRAINT IF EXISTS ck_usuario_password_bcrypt;

-- Cuentas que ya tienen contraseña (versión anterior): pasan a LOCAL.
UPDATE usuario
SET proveedor = 'LOCAL', id_externo = NULL
WHERE password_hash IS NOT NULL AND proveedor IS DISTINCT FROM 'LOCAL';

ALTER TABLE usuario ADD CONSTRAINT ck_usuario_proveedor
    CHECK (proveedor IN ('LOCAL', 'MICROSOFT', 'GOOGLE'));

ALTER TABLE usuario ADD CONSTRAINT ck_usuario_credencial CHECK (
    (proveedor IS NULL     AND id_externo IS NULL     AND password_hash IS NULL)
 OR (proveedor = 'LOCAL'   AND id_externo IS NULL     AND password_hash IS NOT NULL)
 OR (proveedor IN ('MICROSOFT', 'GOOGLE') AND id_externo IS NOT NULL AND password_hash IS NULL)
);

-- Solo hashes BCrypt: evita guardar una contraseña en texto plano al editar a mano.
ALTER TABLE usuario ADD CONSTRAINT ck_usuario_password_bcrypt
    CHECK (password_hash ~ '^\$2[aby]\$[0-9]{2}\$[./A-Za-z0-9]{53}$');

COMMIT;

-- ---------------------------------------------------------------------
-- Contraseña de un ADMIN (no pasan por el signup: su matrícula no tiene
-- el formato S + 8 dígitos y el signup no asigna ADMIN).
--
-- Supabase trae pgcrypto en el esquema "extensions". crypt(..., gen_salt('bf'))
-- genera un hash BCrypt ($2a$) que el backend acepta tal cual.
--
--   UPDATE usuario
--   SET proveedor = 'LOCAL', id_externo = NULL,
--       password_hash = extensions.crypt('<contraseña>', extensions.gen_salt('bf', 10))
--   WHERE matricula = '25480';
--
-- Usa una contraseña de al menos 8 caracteres y no la dejes en el
-- historial del SQL Editor si la BD es compartida.
-- ---------------------------------------------------------------------
