-- =============================================================================
-- 2026-11-SC03-rol-servicio-tramites.sql
--
-- PROPÓSITO
--   SC-03 (integración con Trámites). Crear el rol `Servicio Tramites`, que usa
--   la cuenta técnica del sistema de Trámites para consultar
--   GET /api/application/case/integracion/v1/estudiantes/{cedula}.
--   En el código corresponde a Rol.SERVICIO_TRAMITES; el login lo normaliza a
--   SERVICIO_TRAMITES (RoleNameNormalizer).
--
--   El usuario técnico NO se crea aquí, para no versionar ninguna contraseña:
--   se crea desde /superadmin/usuarios con este rol.
--
-- PRERREQUISITO
--   Respaldo de la tabla `roles` antes de ejecutar (mysqldump o similar).
--   Puede ejecutarse antes o después de desplegar el código: sin la fila, la
--   cuenta técnica no puede existir y el endpoint simplemente responde 403.
--
-- VERIFICAR ANTES
--   Ejecutar solo la sección 0. El script es idempotente: si el rol ya existe,
--   no se vuelve a insertar.
--
-- CÓMO REVERTIR
--   1. Quitar el rol a la cuenta técnica (o borrarla) desde /superadmin/usuarios.
--   2. DELETE FROM roles WHERE nombre = 'Servicio Tramites';
--
-- MOTOR: MySQL 8+ / 9.
-- =============================================================================

-- ── 0. Diagnóstico previo (no modifica nada) ─────────────────────────────────
SELECT 'rol Servicio Tramites existe' AS metrica,
       COUNT(*) AS valor
FROM roles
WHERE nombre = 'Servicio Tramites';

-- ── 1. Crear el rol ──────────────────────────────────────────────────────────
INSERT INTO roles (nombre)
SELECT 'Servicio Tramites'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nombre = 'Servicio Tramites');

-- ── 2. Verificación ──────────────────────────────────────────────────────────
SELECT id, nombre FROM roles WHERE nombre = 'Servicio Tramites';
