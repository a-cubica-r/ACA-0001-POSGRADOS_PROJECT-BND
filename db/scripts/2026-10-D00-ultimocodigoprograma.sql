-- =============================================================================
-- 2026-10-D00-ultimocodigoprograma.sql
--
-- PROPÓSITO
--   D-00 (UFPS-891). Crear la tabla `ultimocodigoprograma`, que guarda el
--   consecutivo con el que se genera el código de cada estudiante:
--
--     código de estudiante = programas.codigo + codigo (4 dígitos)
--
--   `codigo` es el PRÓXIMO secuencial a asignar (arranca en 0). Después de
--   generar un código, el consecutivo avanza en 1.
--
--   El script crea la tabla y deja UNA fila por cada programa que ya exista.
--   Si un programa ya tiene estudiantes, el contador arranca después del último
--   código registrado; si no tiene, arranca en 0.
--
-- PRERREQUISITO
--   Respaldo completo de la base ANTES de ejecutar (mysqldump o similar).
--   Debe ejecutarse ANTES de desplegar el código de D-00: sin la tabla, crear
--   un programa y aprobar un aspirante fallan (el esquema no lo crea Hibernate,
--   `ddl-auto = none`).
--
-- VERIFICAR ANTES
--   Ejecutar solo la sección 0 y revisar el diagnóstico. El script es
--   idempotente: si la tabla existe, no se vuelve a crear; los programas que ya
--   tienen fila no se tocan.
--
-- CÓMO REVERTIR
--   1. El código desplegado deja de funcionar sin la tabla; revierte primero el
--      despliegue y después:
--        DROP TABLE IF EXISTS ultimocodigoprograma;
--   2. No hace falta ningún otro cambio: la tabla no modifica ni borra datos
--      existentes (solo inserta filas nuevas).
--
-- MOTOR: MySQL 8+ / 9.
-- =============================================================================

-- ── 0. Diagnóstico previo (no modifica nada) ─────────────────────────────────
SELECT 'tabla ultimocodigoprograma existe' AS metrica,
       COUNT(*) AS valor
  FROM information_schema.tables
 WHERE table_schema = DATABASE()
   AND table_name = 'ultimocodigoprograma'
UNION ALL
SELECT 'programas', COUNT(*) FROM programas
UNION ALL
SELECT 'estudiantes', COUNT(*) FROM estudiantes
UNION ALL
SELECT 'estudiantes con codigo', COUNT(*) FROM estudiantes WHERE codigo IS NOT NULL;

-- ── 1. Tabla ─────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS ultimocodigoprograma (
    id          INT NOT NULL AUTO_INCREMENT,
    id_programa INT NOT NULL,
    codigo      INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uq_ultimocodigoprograma_programa (id_programa),
    CONSTRAINT fk_ultimocodigoprograma_programa
        FOREIGN KEY (id_programa) REFERENCES programas (id) ON DELETE CASCADE
) ENGINE = InnoDB;

-- Filas de consecutivo que ya existían antes de la sección 2 (0 la primera vez).
SELECT COUNT(*) AS programas_con_fila FROM ultimocodigoprograma;

-- ── 2. Una fila por programa ─────────────────────────────────────────────────
-- Si el programa ya tiene estudiantes, arranca después del último secuencial
-- (misma regla que SecuencialCodigoEstudiante: últimos 4 dígitos + 1). Si el
-- último terminó en 9999, el contador queda en 10000, que marca el programa
-- como agotado: la generación falla en vez de repetir códigos.
INSERT INTO ultimocodigoprograma (id_programa, codigo)
SELECT p.id,
       COALESCE((SELECT MAX(CAST(RIGHT(e.codigo, 4) AS UNSIGNED)) + 1
                   FROM estudiantes e
                  WHERE e.programa_id = p.id
                    AND e.codigo REGEXP '^[0-9]{4,}$'), 0)
  FROM programas p
 WHERE NOT EXISTS (SELECT 1
                     FROM ultimocodigoprograma u
                    WHERE u.id_programa = p.id);

-- ── 3. Verificación (no modifica nada) ───────────────────────────────────────
-- Una fila por programa.
SELECT COUNT(*) AS filas_ultimocodigoprograma FROM ultimocodigoprograma;

-- Programas sin fila: debe devolver 0 filas.
SELECT p.id AS id_programa, p.nombre
  FROM programas p
 WHERE NOT EXISTS (SELECT 1
                     FROM ultimocodigoprograma u
                    WHERE u.id_programa = p.id);

-- Valores resultantes, en el orden en que los verá la pantalla.
SELECT u.id_programa, p.nombre, u.codigo
  FROM ultimocodigoprograma u
  JOIN programas p ON p.id = u.id_programa
 ORDER BY p.nombre;

-- Programas agotados (codigo = 10000): no podrán generar más códigos.
SELECT u.id_programa, p.nombre, u.codigo
  FROM ultimocodigoprograma u
  JOIN programas p ON p.id = u.id_programa
 WHERE u.codigo > 9999;
