-- =============================================================================
-- 2026-11-normalizar-correos-persona.sql
--
-- PROPÓSITO
--   Normalizar los correos de `persona` a minúsculas y sin espacios, para que
--   coincidan con la identidad verificada en AWS SES y con la regla que aplica
--   la aplicación desde esta versión.
--
-- PRERREQUISITO
--   Respaldo de la tabla `persona` antes de ejecutar (mysqldump o similar).
--   No hay cambios de esquema.
--
-- ORDEN
--   1. Diagnóstico.
--   2. Duplicados que aparecerían al normalizar (debe devolver 0 filas).
--      Si devuelve filas, resolverlas a mano ANTES de ejecutar el paso 3.
--   3. Normalización.
--   4. Verificación (debe devolver por_normalizar = 0).
--
-- CÓMO REVERTIR
--   Restaurar la tabla `persona` desde el respaldo. La normalización no se
--   puede deshacer de forma selectiva sin ese respaldo.
-- =============================================================================

-- 1. Diagnóstico
SELECT COUNT(*) AS total,
       SUM(CASE WHEN BINARY correo <> BINARY LOWER(TRIM(correo)) THEN 1 ELSE 0 END) AS por_normalizar
FROM persona;

-- 2. Duplicados que aparecerían al normalizar
SELECT LOWER(TRIM(correo)) AS correo,
       COUNT(*) AS n,
       GROUP_CONCAT(id ORDER BY id) AS ids
FROM persona
WHERE correo IS NOT NULL
GROUP BY LOWER(TRIM(correo))
HAVING COUNT(*) > 1;

-- 3. Normalizar (idempotente)
UPDATE persona
SET correo = LOWER(TRIM(correo))
WHERE correo IS NOT NULL
  AND BINARY correo <> BINARY LOWER(TRIM(correo));

-- 4. Verificación: debe devolver por_normalizar = 0
SELECT COUNT(*) AS total,
       SUM(CASE WHEN BINARY correo <> BINARY LOWER(TRIM(correo)) THEN 1 ELSE 0 END) AS por_normalizar
FROM persona;
