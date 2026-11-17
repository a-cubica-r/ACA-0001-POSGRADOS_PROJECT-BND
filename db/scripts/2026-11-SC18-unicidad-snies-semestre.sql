-- ============================================================================
-- SC-18 — Unicidad de programas.registrosnies y de semestre
-- ----------------------------------------------------------------------------
-- Propósito:
--   1. Dejar en NULL los registros SNIES vacíos.
--   2. Crear los índices únicos que respaldan las validaciones de la aplicación:
--      - programas.registrosnies
--      - semestre.nombre
--      - semestre (año de fecha_inicio, periodo 1/2)
-- Prerrequisito: respaldo completo de la base.
-- Motor: MySQL 8.0.13+ (Northflank usa 9.7). Índices funcionales.
-- Reversión: ver la sección 6.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 0. DIAGNÓSTICO (solo SELECT)
-- Ejecutar primero. Si alguna consulta devuelve filas, DETENERSE y resolver los
-- duplicados a mano antes de crear los índices.
-- ----------------------------------------------------------------------------

-- 0.1 SNIES duplicados (ignorando vacíos)
SELECT TRIM(registrosnies) AS snies, COUNT(*) AS n, GROUP_CONCAT(id ORDER BY id) AS ids
FROM programas
WHERE registrosnies IS NOT NULL AND TRIM(registrosnies) <> ''
GROUP BY TRIM(registrosnies)
HAVING COUNT(*) > 1;

-- 0.2 Semestres con el mismo año y periodo (enero-junio = 1, julio-diciembre = 2)
SELECT YEAR(fecha_inicio) AS anio,
       IF(MONTH(fecha_inicio) <= 6, 1, 2) AS periodo,
       COUNT(*) AS n,
       GROUP_CONCAT(id ORDER BY id) AS ids
FROM semestre
GROUP BY YEAR(fecha_inicio), IF(MONTH(fecha_inicio) <= 6, 1, 2)
HAVING COUNT(*) > 1;

-- 0.3 Semestres con el mismo nombre (sin distinguir mayúsculas ni tildes)
SELECT TRIM(nombre) AS nombre, COUNT(*) AS n, GROUP_CONCAT(id ORDER BY id) AS ids
FROM semestre
GROUP BY TRIM(nombre)
HAVING COUNT(*) > 1;

-- ----------------------------------------------------------------------------
-- 1. Normalizar SNIES vacíos
-- ----------------------------------------------------------------------------
UPDATE programas
SET registrosnies = NULL
WHERE registrosnies IS NOT NULL AND TRIM(registrosnies) = '';

-- ----------------------------------------------------------------------------
-- 2-4. Índices únicos
-- ----------------------------------------------------------------------------
CREATE UNIQUE INDEX uq_programas_registrosnies
    ON programas (registrosnies);

CREATE UNIQUE INDEX uq_semestre_nombre
    ON semestre (nombre);

CREATE UNIQUE INDEX uq_semestre_anio_periodo
    ON semestre ((YEAR(fecha_inicio)), (IF(MONTH(fecha_inicio) <= 6, 1, 2)));

-- ----------------------------------------------------------------------------
-- 5. Verificación
-- ----------------------------------------------------------------------------
SHOW INDEX FROM programas WHERE Key_name LIKE 'uq_%';
SHOW INDEX FROM semestre WHERE Key_name LIKE 'uq_%';

-- Comprobación funcional (debe fallar con 1062 si se intenta duplicar):
-- INSERT INTO programas (codigo, nombre, registrosnies, ...) VALUES (...);
-- INSERT INTO semestre (nombre, fecha_inicio, fecha_fin, id_estado) VALUES (...);

-- ----------------------------------------------------------------------------
-- 6. Reversión
-- ----------------------------------------------------------------------------
-- DROP INDEX uq_semestre_anio_periodo ON semestre;
-- DROP INDEX uq_semestre_nombre ON semestre;
-- DROP INDEX uq_programas_registrosnies ON programas;
