-- =============================================================================
-- 2026-10-D00-separar-plazos-cohorte.sql
--
-- PROPÓSITO
--   D-00 (UFPS-891 / UFPS-896). Las cohortes creadas antes de D-00 tienen
--   id_plazoinscripcion = id_plazodocumentacion (la misma fila de `plazo`), y
--   algunas incluso id_plazopago apuntando a esa misma fila o a la de
--   inscripción. Resultado: el plazo de inscripción muestra las fechas del de
--   documentación y editar uno cambia el otro.
--
--   Este script da a cada cohorte tres plazos SEPARADOS y con el tipo correcto:
--     1. Crea una fila nueva de `plazo` por cada fila compartida (inscripción y
--        pago) copiando fechas, y reasigna la cohorte al id nuevo.
--     2. Corrige id_tipoplazo de los plazos de documentación y pago a
--        DOCUMENTACION y PAGO.
--
--   El código de D-00 (AspiranteCoreService.updateCohorte) hace esta misma
--   separación de forma diferida, cohorte por cohorte y solo cuando alguien la
--   edita. Este script la aplica a todas de una vez.
--
-- PRERREQUISITO
--   Respaldo completo de la base ANTES de ejecutar (mysqldump o similar). El
--   script modifica `cohorte` e inserta filas en `plazo`. No es reversible con
--   SQL simple; ver "CÓMO REVERTIR".
--
-- VERIFICAR ANTES
--   Ejecutar solo la sección 0 y revisar el diagnóstico. Si el entorno no tiene
--   los tipos INSCRIPCION y PAGO en `tipoplazo`, el procedimiento aborta con un
--   error y no cambia nada.
--
-- CÓMO REVERTIR
--   1. La reversión correcta es restaurar el respaldo previo: los ids nuevos y
--      los ids de `cohorte` que cambian no se pueden reconstruir con certeza a
--      posteriori.
--   2. Reversión manual (solo si se anotaron los ids antes de ejecutar):
--        UPDATE cohorte SET id_plazoinscripcion = id_plazodocumentacion
--          WHERE id IN (<cohortes afectadas>);
--        UPDATE cohorte SET id_plazopago = <id original> WHERE id IN (<...>);
--        DELETE FROM plazo WHERE id IN (<plazos creados por el script>);
--      Los plazos creados son los de id mayor que MAX(id) de `plazo` tomado
--      antes de ejecutar; la sección 0 imprime ese valor.
--
-- MOTOR: MySQL 8+ / 9.
-- =============================================================================

-- ── 0. Diagnóstico previo (no modifica nada) ─────────────────────────────────
SELECT 'cohortes totales' AS metrica, COUNT(*) AS valor FROM cohorte
UNION ALL
SELECT 'cohortes con doc = inscripcion',
       COUNT(*) FROM cohorte
 WHERE id_plazoinscripcion IS NOT NULL
   AND id_plazoinscripcion = id_plazodocumentacion
UNION ALL
SELECT 'cohortes con pago compartido',
       COUNT(*) FROM cohorte
 WHERE id_plazopago IS NOT NULL
   AND (id_plazopago = id_plazodocumentacion OR id_plazopago = id_plazoinscripcion)
UNION ALL
SELECT 'plazos con tipo INCORRECTO en documentacion',
       COUNT(*) FROM cohorte c
  JOIN plazo p ON p.id = c.id_plazodocumentacion
  JOIN tipoplazo t ON t.id = p.id_tipoplazo
 WHERE t.tipo <> 'DOCUMENTACION'
UNION ALL
SELECT 'plazos con tipo INCORRECTO en pago',
       COUNT(*) FROM cohorte c
  JOIN plazo p ON p.id = c.id_plazopago
  JOIN tipoplazo t ON t.id = p.id_tipoplazo
 WHERE t.tipo <> 'PAGO';

-- Máximo id de `plazo` antes de ejecutar (sirve para identificar lo creado).
SELECT MAX(id) AS max_id_plazo_antes FROM plazo;

-- Filas que quedarían en conflicto si un plazo es compartido por dos cohortes
-- con roles distintos. Si esto devuelve filas, revisar a mano antes de seguir.
SELECT p.id AS id_plazo, p.id_tipoplazo,
       GROUP_CONCAT(c.id ORDER BY c.id) AS cohortes,
       GROUP_CONCAT(DISTINCT
              CASE
                WHEN c.id_plazodocumentacion = p.id THEN 'DOCUMENTACION'
                WHEN c.id_plazoinscripcion  = p.id THEN 'INSCRIPCION'
                WHEN c.id_plazopago         = p.id THEN 'PAGO'
              END ORDER BY c.id) AS roles
  FROM plazo p
  JOIN cohorte c
    ON c.id_plazodocumentacion = p.id
    OR c.id_plazoinscripcion  = p.id
    OR c.id_plazopago         = p.id
 GROUP BY p.id, p.id_tipoplazo
HAVING COUNT(DISTINCT
         CASE
           WHEN c.id_plazodocumentacion = p.id THEN 'DOCUMENTACION'
           WHEN c.id_plazoinscripcion  = p.id THEN 'INSCRIPCION'
           WHEN c.id_plazopago         = p.id THEN 'PAGO'
         END) > 1;

-- ── 1. Separación de plazos compartidos ──────────────────────────────────────
DROP PROCEDURE IF EXISTS d00_separar_plazos_cohorte;

DELIMITER $$
CREATE PROCEDURE d00_separar_plazos_cohorte()
BEGIN
  DECLARE done INT DEFAULT 0;
  DECLARE v_id_cohorte INT;
  DECLARE v_id_doc INT;
  DECLARE v_id_insc INT;
  DECLARE v_id_pago INT;
  DECLARE v_ini DATE;
  DECLARE v_fin DATE;
  DECLARE v_nuevo INT;
  DECLARE v_tipo_insc INT;
  DECLARE v_tipo_pago INT;

  -- Se recorren solo las cohortes con alguna fila compartida y con plazo de
  -- documentación existente (de ahí se copian las fechas).
  DECLARE cur CURSOR FOR
    SELECT c.id, c.id_plazodocumentacion, c.id_plazoinscripcion, c.id_plazopago
      FROM cohorte c
     WHERE c.id_plazodocumentacion IS NOT NULL
       AND EXISTS (SELECT 1 FROM plazo p WHERE p.id = c.id_plazodocumentacion)
       AND (c.id_plazoinscripcion = c.id_plazodocumentacion
         OR c.id_plazopago = c.id_plazodocumentacion
         OR c.id_plazopago = c.id_plazoinscripcion);
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

  -- Subconsultas escalares: no disparan el handler NOT FOUND si no hay fila.
  SET v_tipo_insc = (SELECT id FROM tipoplazo WHERE tipo = 'INSCRIPCION' LIMIT 1);
  SET v_tipo_pago = (SELECT id FROM tipoplazo WHERE tipo = 'PAGO'        LIMIT 1);

  IF v_tipo_insc IS NULL OR v_tipo_pago IS NULL THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'tipoplazo no tiene los tipos INSCRIPCION y/o PAGO; no se aplicó ningún cambio';
  END IF;

  OPEN cur;

  read_loop: LOOP
    FETCH cur INTO v_id_cohorte, v_id_doc, v_id_insc, v_id_pago;
    IF done = 1 THEN
      LEAVE read_loop;
    END IF;

    -- 1a. Inscripción compartida con documentación.
    IF v_id_insc = v_id_doc THEN
      SELECT fechainicio, fechafin INTO v_ini, v_fin FROM plazo WHERE id = v_id_doc;
      INSERT INTO plazo (fechainicio, fechafin, id_tipoplazo)
        VALUES (v_ini, v_fin, v_tipo_insc);
      SET v_nuevo = LAST_INSERT_ID();
      UPDATE cohorte SET id_plazoinscripcion = v_nuevo WHERE id = v_id_cohorte;
      SET v_id_insc = v_nuevo;
    END IF;

    -- 1b. Pago compartido con documentación o con inscripción.
    IF v_id_pago = v_id_doc OR v_id_pago = v_id_insc THEN
      SELECT fechainicio, fechafin INTO v_ini, v_fin FROM plazo WHERE id = v_id_pago;
      INSERT INTO plazo (fechainicio, fechafin, id_tipoplazo)
        VALUES (v_ini, v_fin, v_tipo_pago);
      SET v_nuevo = LAST_INSERT_ID();
      UPDATE cohorte SET id_plazopago = v_nuevo WHERE id = v_id_cohorte;
    END IF;
  END LOOP;

  CLOSE cur;
END$$
DELIMITER ;

START TRANSACTION;

CALL d00_separar_plazos_cohorte();

-- ── 2. Tipo correcto en los plazos de documentación y pago ───────────────────
UPDATE plazo p
  JOIN cohorte c  ON c.id_plazodocumentacion = p.id
  JOIN tipoplazo t ON t.tipo = 'DOCUMENTACION'
   SET p.id_tipoplazo = t.id
 WHERE p.id_tipoplazo <> t.id;

UPDATE plazo p
  JOIN cohorte c  ON c.id_plazopago = p.id
  JOIN tipoplazo t ON t.tipo = 'PAGO'
   SET p.id_tipoplazo = t.id
 WHERE p.id_tipoplazo <> t.id;

COMMIT;

DROP PROCEDURE IF EXISTS d00_separar_plazos_cohorte;

-- ── 3. Verificación posterior ────────────────────────────────────────────────
-- Las dos primeras filas deben dar 0.
SELECT 'cohortes con doc = inscripcion' AS metrica, COUNT(*) AS valor
  FROM cohorte
 WHERE id_plazoinscripcion = id_plazodocumentacion
UNION ALL
SELECT 'cohortes con pago compartido',
       COUNT(*) FROM cohorte
 WHERE id_plazopago = id_plazodocumentacion
    OR id_plazopago = id_plazoinscripcion
UNION ALL
SELECT 'plazos de documentacion con tipo distinto de DOCUMENTACION',
       COUNT(*) FROM cohorte c
  JOIN plazo p ON p.id = c.id_plazodocumentacion
  JOIN tipoplazo t ON t.id = p.id_tipoplazo
 WHERE t.tipo <> 'DOCUMENTACION'
UNION ALL
SELECT 'plazos de pago con tipo distinto de PAGO',
       COUNT(*) FROM cohorte c
  JOIN plazo p ON p.id = c.id_plazopago
  JOIN tipoplazo t ON t.id = p.id_tipoplazo
 WHERE t.tipo <> 'PAGO';

SELECT MAX(id) AS max_id_plazo_despues FROM plazo;
