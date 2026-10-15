-- =====================================================================
-- Migración 004 — Login con contraseña (POST /auth/signin y /auth/signup)
--
-- Para una BD creada con 001_esquema.sql ANTES de este cambio. No borra
-- datos. Una BD nueva no la necesita: 001_esquema.sql ya trae lo mismo.
--
-- ORDEN DE DESPLIEGUE: aplicar esto ANTES de desplegar el backend nuevo.
-- Hibernate valida el esquema al arrancar (ddl-auto=validate) y no inicia
-- si falta usuario.pwd_hash. El backend anterior sigue funcionando con el
-- esquema ya migrado.
--
-- Como aplicar en Supabase:
--   Dashboard > SQL Editor > New query > pegar este archivo > Run.
--   (o)  psql "<connection string>" -f db/supabase/004_contrasenas.sql
--
-- Estados válidos de una fila de usuario (ck_usuario_credencial):
--   proveedor NULL                    pre-registro: sin credencial; el
--                                     signup lo reclama si el correo coincide
--   proveedor 'LOCAL'                 contraseña en pwd_hash (BCrypt)
--   proveedor 'MICROSOFT' | 'GOOGLE'  cuenta del login anterior; ya no
--                                     puede entrar y el signup la reclama
--                                     igual que a un pre-registro
-- =====================================================================

BEGIN;

ALTER TABLE usuario ADD COLUMN IF NOT EXISTS pwd_hash VARCHAR(100);

ALTER TABLE usuario DROP CONSTRAINT IF EXISTS ck_usuario_proveedor;
ALTER TABLE usuario DROP CONSTRAINT IF EXISTS ck_usuario_proveedor_id;
ALTER TABLE usuario DROP CONSTRAINT IF EXISTS ck_usuario_credencial;

ALTER TABLE usuario ADD CONSTRAINT ck_usuario_proveedor
    CHECK (proveedor IN ('LOCAL', 'MICROSOFT', 'GOOGLE'));

ALTER TABLE usuario ADD CONSTRAINT ck_usuario_credencial CHECK (
    (proveedor IS NULL     AND id_externo IS NULL     AND pwd_hash IS NULL)
 OR (proveedor = 'LOCAL'   AND id_externo IS NULL     AND pwd_hash IS NOT NULL)
 OR (proveedor IN ('MICROSOFT', 'GOOGLE') AND id_externo IS NOT NULL AND pwd_hash IS NULL)
);

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
--       pwd_hash = extensions.crypt('<contraseña>', extensions.gen_salt('bf', 10))
--   WHERE matricula = '25480';
--
-- Usa una contraseña de al menos 8 caracteres y no la dejes en el
-- historial del SQL Editor si la BD es compartida.
-- ---------------------------------------------------------------------
