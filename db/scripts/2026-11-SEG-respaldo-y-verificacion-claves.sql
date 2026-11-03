-- =============================================================================
-- 2026-11-SEG-respaldo-y-verificacion-claves.sql
--
-- PROPÓSITO
--   Migrar una base a claves BCrypt con MigracionClavesRunner (perfil
--   `migrar-claves`) guardando antes un respaldo y verificando después que no
--   quede ninguna clave en texto plano.
--
--   Ejecutar los pasos en este orden:
--     1. Diagnóstico (antes).
--     2. Respaldo.
--     3. Ejecutar MigracionClavesRunner contra esta base.
--     4. Verificación (después): `texto_plano` debe ser 0.
--     5. Reversión, solo si algo salió mal.
--     6. Limpieza, después de verificar el login de todos los roles.
--
-- PRERREQUISITO
--   Respaldo completo de la base (mysqldump o similar) antes de empezar.
--   El respaldo `clave_respaldo_20261102` contiene claves en claro: BORRARLO
--   en cuanto se verifique el login de todos los roles.
--
--   La columna `clave.valor` es TEXT: un hash BCrypt (60 caracteres) cabe sin
--   cambios de esquema.
--
-- CÓMO REVERTIR
--   1. Vuelve a desplegar la versión anterior del backend o deja
--      AUTH_ALLOW_PLAIN_TEXT_LEGACY=true mientras investigas.
--   2. Restaura los valores con el paso 5.
--   3. No borres el respaldo hasta confirmar que la migración fue correcta.
--
-- MOTOR: MySQL 8+ / 9.
-- =============================================================================

-- ── 1. Diagnóstico (antes) ───────────────────────────────────────────────────
SELECT COUNT(*) AS total,
       SUM(valor REGEXP '^\\$2[aby]\\$[0-9]{2}\\$.{53}$') AS bcrypt,
       SUM(NOT (valor REGEXP '^\\$2[aby]\\$[0-9]{2}\\$.{53}$')) AS texto_plano
  FROM clave;

-- ── 2. Respaldo (contiene claves en claro: BORRARLO tras verificar) ──────────
CREATE TABLE clave_respaldo_20261102 AS SELECT * FROM clave;
SELECT COUNT(*) AS filas_respaldo FROM clave_respaldo_20261102;

-- ── 3. Ejecutar la migración ─────────────────────────────────────────────────
--   IMPORTANTE: detener la aplicación (o impedir cambios de clave) mientras
--   corre la migración. Si un usuario cambia su contraseña entre la lectura y
--   el guardado, la migración podría pisar el hash nuevo.
--
--   Desde la raíz del repositorio, con las variables de conexión de la base:
--     $env:DB_URL="jdbc:mysql://..."; $env:DB_USER="..."; $env:DB_PASSWORD="..."
--     .\mvnw.cmd -pl platform/application -am spring-boot:run `
--       "-Dspring-boot.run.profiles=migrar-claves"
--
--   Alternativa con el jar empaquetado:
--     java -jar application.jar --spring.profiles.active=migrar-claves
--
--   El proceso debe terminar con código 0. Si termina con 1, revisar en el log
--   los ids con clave vacía o que no quedaron en BCrypt.

-- ── 4. Verificación (después): texto_plano debe ser 0 ────────────────────────
SELECT COUNT(*) AS total,
       SUM(valor REGEXP '^\\$2[aby]\\$[0-9]{2}\\$.{53}$') AS bcrypt,
       SUM(NOT (valor REGEXP '^\\$2[aby]\\$[0-9]{2}\\$.{53}$')) AS texto_plano
  FROM clave;

-- ── 5. Reversión (solo si algo salió mal) ────────────────────────────────────
-- UPDATE clave c
--   JOIN clave_respaldo_20261102 r ON r.id = c.id
--    SET c.valor = r.valor;

-- ── 6. Limpieza (solo tras verificar el login de todos los roles) ────────────
-- DROP TABLE clave_respaldo_20261102;
