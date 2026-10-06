-- ==============================================================================
-- SEED DE RENDIMIENTO — posgrados_perf
-- Ejecutar DESPUÉS de que el backend arranque con application-perf.properties
-- (Hibernate crea el esquema vacío con ddl-auto=create)
--
-- Genera:
--   • Catálogos completos
--   • 1 país, 2 departamentos, 5 municipios
--   • 5 facultades, 5 programas, 10 cohortes (5 abiertas)
--   • Usuarios de prueba: superadmin, director1, posgrados1, aspirante_test1..10
--   • 2 000 aspirantes con usuario, vía procedimiento almacenado
-- ==============================================================================

SET FOREIGN_KEY_CHECKS = 0;
SET autocommit = 0;

-- ── 1. Roles ──────────────────────────────────────────────────────────────────
INSERT INTO roles (nombre) VALUES ('ASPIRANTE');               -- id 1
INSERT INTO roles (nombre) VALUES ('SUPER_ADMINISTRADOR');     -- id 2
INSERT INTO roles (nombre) VALUES ('DIRECTOR_DE_PROGRAMA');    -- id 3
INSERT INTO roles (nombre) VALUES ('POSGRADOS');               -- id 4

-- ── 2. Estados ────────────────────────────────────────────────────────────────
INSERT INTO estado (entidad, tipo) VALUES ('ASPIRANTE',   'NO CONFIRMADO');  -- 1
INSERT INTO estado (entidad, tipo) VALUES ('ASPIRANTE',   'CONFIRMADO');     -- 2
INSERT INTO estado (entidad, tipo) VALUES ('ASPIRANTE',   'ADMITIDO');       -- 3
INSERT INTO estado (entidad, tipo) VALUES ('ASPIRANTE',   'NO ADMITIDO');    -- 4
INSERT INTO estado (entidad, tipo) VALUES ('COHORTE',     'ABIERTA');        -- 5
INSERT INTO estado (entidad, tipo) VALUES ('COHORTE',     'CERRADA');        -- 6
INSERT INTO estado (entidad, tipo) VALUES ('PAGO',        'PENDIENTE');      -- 7
INSERT INTO estado (entidad, tipo) VALUES ('PAGO',        'APROBADO');       -- 8
INSERT INTO estado (entidad, tipo) VALUES ('SEMESTRE',    'ACTIVO');         -- 9
INSERT INTO estado (entidad, tipo) VALUES ('ENTREVISTA',  'PENDIENTE');      -- 10
INSERT INTO estado (entidad, tipo) VALUES ('ADMINISTRATIVO', 'ACTIVO');      -- 11

-- ── 3. Catálogos sin FK ───────────────────────────────────────────────────────
INSERT INTO tipodocumentopersona (tipo) VALUES ('Cédula de ciudadanía');  -- 1
INSERT INTO tipodocumentopersona (tipo) VALUES ('Pasaporte');             -- 2
INSERT INTO tipodocumentopersona (tipo) VALUES ('Cédula de extranjería'); -- 3

INSERT INTO estadocivil (estado) VALUES ('Soltero/a');   -- 1
INSERT INTO estadocivil (estado) VALUES ('Casado/a');    -- 2
INSERT INTO estadocivil (estado) VALUES ('Divorciado/a'); -- 3

INSERT INTO genero (nombre) VALUES ('Masculino');  -- 1
INSERT INTO genero (nombre) VALUES ('Femenino');   -- 2
INSERT INTO genero (nombre) VALUES ('Otro');       -- 3

INSERT INTO grupoetnico (grupo) VALUES ('Ninguno');           -- 1
INSERT INTO grupoetnico (grupo) VALUES ('Afrodescendiente');  -- 2
INSERT INTO grupoetnico (grupo) VALUES ('Indígena');          -- 3

INSERT INTO poblacionindigena (poblacion) VALUES ('Ninguna'); -- 1
INSERT INTO poblacionindigena (poblacion) VALUES ('Barí');    -- 2

INSERT INTO capacidadexepcional (tipocapacidad) VALUES ('Ninguna');          -- 1
INSERT INTO capacidadexepcional (tipocapacidad) VALUES ('Altas capacidades'); -- 2

INSERT INTO discapacidad (tipodiscapacidad) VALUES ('Ninguna');  -- 1
INSERT INTO discapacidad (tipodiscapacidad) VALUES ('Visual');   -- 2
INSERT INTO discapacidad (tipodiscapacidad) VALUES ('Auditiva'); -- 3

INSERT INTO tipovinculacion (tipo) VALUES ('Externo');       -- 1
INSERT INTO tipovinculacion (tipo) VALUES ('Docente UFPS'); -- 2

INSERT INTO modalidad (nombre) VALUES ('Presencial'); -- 1
INSERT INTO modalidad (nombre) VALUES ('Virtual');    -- 2

INSERT INTO tiporegistro (tipo) VALUES ('Posgrado'); -- 1

INSERT INTO tipoplazo (tipo) VALUES ('Inscripción');    -- 1
INSERT INTO tipoplazo (tipo) VALUES ('Pago');           -- 2
INSERT INTO tipoplazo (tipo) VALUES ('Documentación');  -- 3

INSERT INTO pagoconcepto (tipo) VALUES ('INSCRIPCION'); -- 1
INSERT INTO pagoconcepto (tipo) VALUES ('MATRICULA');   -- 2

-- ── 4. Geografía: País → Departamentos → Municipios ──────────────────────────
INSERT INTO pais (nombre) VALUES ('Colombia'); -- id 1

INSERT INTO departamento (nombre, id_pais) VALUES ('Norte de Santander', 1); -- 1
INSERT INTO departamento (nombre, id_pais) VALUES ('Santander',          1); -- 2

INSERT INTO municipio (nombre, id_departamento) VALUES ('Cúcuta',          1); -- 1
INSERT INTO municipio (nombre, id_departamento) VALUES ('Villa del Rosario',1); -- 2
INSERT INTO municipio (nombre, id_departamento) VALUES ('Los Patios',       1); -- 3
INSERT INTO municipio (nombre, id_departamento) VALUES ('Bucaramanga',      2); -- 4
INSERT INTO municipio (nombre, id_departamento) VALUES ('Floridablanca',    2); -- 5

-- ── 5. Sedes (requieren una ubicación) ───────────────────────────────────────
INSERT INTO ubicacion (direccion, zonaurbana, id_municipio) VALUES ('Av. Gran Colombia 1', true, 1); -- 1
INSERT INTO ubicacion (direccion, zonaurbana, id_municipio) VALUES ('Calle 5 # 2-35',      true, 2); -- 2

INSERT INTO sede (nombre, id_ubicacion) VALUES ('Sede Principal', 1); -- 1
INSERT INTO sede (nombre, id_ubicacion) VALUES ('Sede Norte',     2); -- 2

-- ── 6. Facultades y Programas ─────────────────────────────────────────────────
INSERT INTO facultad (nombre, correo) VALUES ('Facultad de Ingeniería',        'ing@ufps.edu.co');    -- 1
INSERT INTO facultad (nombre, correo) VALUES ('Facultad de Ciencias Básicas',  'ciencias@ufps.edu.co'); -- 2
INSERT INTO facultad (nombre, correo) VALUES ('Facultad de Educación',         'edu@ufps.edu.co');    -- 3
INSERT INTO facultad (nombre, correo) VALUES ('Facultad de Salud',             'salud@ufps.edu.co'); -- 4
INSERT INTO facultad (nombre, correo) VALUES ('Facultad de Economía',          'eco@ufps.edu.co');   -- 5

-- programas: (nombre, duracion, id_facultad, id_sede, id_tiporegistro, id_modalidad)
INSERT INTO programas (nombre, duracion, id_facultad, id_sede, id_tiporegistro, id_modalidad)
  VALUES ('Maestría en Ingeniería de Sistemas',      4, 1, 1, 1, 1); -- 1
INSERT INTO programas (nombre, duracion, id_facultad, id_sede, id_tiporegistro, id_modalidad)
  VALUES ('Maestría en Matemáticas Aplicadas',       4, 2, 1, 1, 1); -- 2
INSERT INTO programas (nombre, duracion, id_facultad, id_sede, id_tiporegistro, id_modalidad)
  VALUES ('Especialización en Docencia Universitaria',2, 3, 2, 1, 2); -- 3
INSERT INTO programas (nombre, duracion, id_facultad, id_sede, id_tiporegistro, id_modalidad)
  VALUES ('Maestría en Salud Pública',               4, 4, 1, 1, 1); -- 4
INSERT INTO programas (nombre, duracion, id_facultad, id_sede, id_tiporegistro, id_modalidad)
  VALUES ('Especialización en Finanzas',             2, 5, 2, 1, 2); -- 5

-- ── 7. Cargos para directores (uno por programa) ──────────────────────────────
-- CargoEntity: (nombre, descripcion, id_facultad, id_programa)
INSERT INTO cargo (nombre, descripcion, id_facultad, id_programa)
  VALUES ('DIRECTOR DE PROGRAMA', 'Director Maestría Ing. Sistemas', 1, 1); -- 1
INSERT INTO cargo (nombre, descripcion, id_facultad, id_programa)
  VALUES ('DIRECTOR DE PROGRAMA', 'Director Maestría Mat. Aplicadas', 2, 2); -- 2
INSERT INTO cargo (nombre, descripcion, id_facultad, id_programa)
  VALUES ('DIRECTOR DE PROGRAMA', 'Director Esp. Docencia', 3, 3); -- 3

-- ── 8. Semestre ───────────────────────────────────────────────────────────────
INSERT INTO semestre (nombre, fecha_inicio, fecha_fin, id_estado)
  VALUES ('2026-I', '2026-01-01', '2026-06-30', 9);  -- 1
INSERT INTO semestre (nombre, fecha_inicio, fecha_fin, id_estado)
  VALUES ('2026-II', '2026-07-01', '2026-12-31', 9); -- 2

-- ── 9. Plazos (3 por cohorte × 10 cohortes = 30 plazos) ──────────────────────
-- Reutilizamos 3 plazos globales abiertos
INSERT INTO plazo (fechainicio, fechafin, id_tipoplazo) VALUES ('2026-01-01','2026-12-31', 1); -- 1 inscripcion
INSERT INTO plazo (fechainicio, fechafin, id_tipoplazo) VALUES ('2026-01-01','2026-12-31', 2); -- 2 pago
INSERT INTO plazo (fechainicio, fechafin, id_tipoplazo) VALUES ('2026-01-01','2026-12-31', 3); -- 3 documentacion

-- ── 10. Cohortes ──────────────────────────────────────────────────────────────
-- 5 cohortes ABIERTAS (id_estado=5) para distintos programas
-- 5 cohortes CERRADAS (id_estado=6) para datos históricos
-- cohorte: (nombre, cupos, id_estado, id_semestre, id_modalidad, id_plazodocumentacion, id_plazoinscripcion, id_plazopago, id_programa)
INSERT INTO cohorte (nombre,cupos,id_estado,id_semestre,id_modalidad,id_plazodocumentacion,id_plazoinscripcion,id_plazopago,id_programa)
  VALUES ('Cohorte 2026-1 Ing.Sistemas',   50, 5, 1, 1, 3, 1, 2, 1); -- 1 (ABIERTA)
INSERT INTO cohorte (nombre,cupos,id_estado,id_semestre,id_modalidad,id_plazodocumentacion,id_plazoinscripcion,id_plazopago,id_programa)
  VALUES ('Cohorte 2026-1 Mat.Aplicadas',  30, 5, 1, 1, 3, 1, 2, 2); -- 2 (ABIERTA)
INSERT INTO cohorte (nombre,cupos,id_estado,id_semestre,id_modalidad,id_plazodocumentacion,id_plazoinscripcion,id_plazopago,id_programa)
  VALUES ('Cohorte 2026-1 Docencia',       40, 5, 1, 2, 3, 1, 2, 3); -- 3 (ABIERTA)
INSERT INTO cohorte (nombre,cupos,id_estado,id_semestre,id_modalidad,id_plazodocumentacion,id_plazoinscripcion,id_plazopago,id_programa)
  VALUES ('Cohorte 2026-1 Salud Publica',  25, 5, 1, 1, 3, 1, 2, 4); -- 4 (ABIERTA)
INSERT INTO cohorte (nombre,cupos,id_estado,id_semestre,id_modalidad,id_plazodocumentacion,id_plazoinscripcion,id_plazopago,id_programa)
  VALUES ('Cohorte 2026-1 Finanzas',       35, 5, 1, 2, 3, 1, 2, 5); -- 5 (ABIERTA)
INSERT INTO cohorte (nombre,cupos,id_estado,id_semestre,id_modalidad,id_plazodocumentacion,id_plazoinscripcion,id_plazopago,id_programa)
  VALUES ('Cohorte 2025-2 Ing.Sistemas',   50, 6, 2, 1, 3, 1, 2, 1); -- 6 (CERRADA)
INSERT INTO cohorte (nombre,cupos,id_estado,id_semestre,id_modalidad,id_plazodocumentacion,id_plazoinscripcion,id_plazopago,id_programa)
  VALUES ('Cohorte 2025-2 Mat.Aplicadas',  30, 6, 2, 1, 3, 1, 2, 2); -- 7 (CERRADA)
INSERT INTO cohorte (nombre,cupos,id_estado,id_semestre,id_modalidad,id_plazodocumentacion,id_plazoinscripcion,id_plazopago,id_programa)
  VALUES ('Cohorte 2025-2 Docencia',       40, 6, 2, 2, 3, 1, 2, 3); -- 8 (CERRADA)
INSERT INTO cohorte (nombre,cupos,id_estado,id_semestre,id_modalidad,id_plazodocumentacion,id_plazoinscripcion,id_plazopago,id_programa)
  VALUES ('Cohorte 2025-2 Salud Publica',  25, 6, 2, 1, 3, 1, 2, 4); -- 9 (CERRADA)
INSERT INTO cohorte (nombre,cupos,id_estado,id_semestre,id_modalidad,id_plazodocumentacion,id_plazoinscripcion,id_plazopago,id_programa)
  VALUES ('Cohorte 2025-2 Finanzas',       35, 6, 2, 2, 3, 1, 2, 5); -- 10 (CERRADA)

-- ── 11. Ubicaciones compartidas para personas de prueba ───────────────────────
INSERT INTO ubicacion (direccion, zonaurbana, id_municipio) VALUES ('Carrera 10 # 5-60', true, 1); -- 3 (expedicion)
INSERT INTO ubicacion (direccion, zonaurbana, id_municipio) VALUES ('Barrio Centro',      true, 1); -- 4 (nacimiento)
INSERT INTO ubicacion (direccion, zonaurbana, id_municipio) VALUES ('Calle 12 # 3-45',   true, 1); -- 5 (vivienda)

-- ── 12. Usuarios de prueba: superadmin ────────────────────────────────────────
INSERT INTO documentopersona (numerodocumento, id_tipodocumento, id_lugarexpedicion)
  VALUES ('10000000001', 1, 3); -- 1
INSERT INTO persona (nombres, apellidos, correo, celular,
                     id_genero, id_estadocivil, id_documentopersona,
                     id_ubicacionnacimiento, id_ubicacionvivienda)
  VALUES ('Admin', 'Sistema', 'superadmin@perf.local', '3000000001', 1, 1, 1, 4, 5); -- 1
INSERT INTO clave (valor) VALUES ('admin123'); -- 1
INSERT INTO usuarios (nombreusuario, id_persona, id_clave, id_rol)
  VALUES ('superadmin', 1, 1, 2); -- 1

-- ── 13. Usuarios de prueba: director1 (enlazado a programa 1 via cargo 1) ─────
INSERT INTO documentopersona (numerodocumento, id_tipodocumento, id_lugarexpedicion)
  VALUES ('10000000002', 1, 3); -- 2
INSERT INTO persona (nombres, apellidos, correo, celular,
                     id_genero, id_estadocivil, id_documentopersona,
                     id_ubicacionnacimiento, id_ubicacionvivienda)
  VALUES ('Director', 'Programa Uno', 'director1@perf.local', '3000000002', 1, 1, 2, 4, 5); -- 2
INSERT INTO clave (valor) VALUES ('director123'); -- 2
INSERT INTO usuarios (nombreusuario, id_persona, id_clave, id_rol)
  VALUES ('director1', 2, 2, 3); -- 2
-- administrativo vincula persona 2 al cargo 1 (DIRECTOR DE PROGRAMA, programa 1)
INSERT INTO administrativo (fechainicio, id_cargo, id_estado, id_persona)
  VALUES ('2026-01-01', 1, 11, 2); -- 1

-- ── 14. Usuario: posgrados1 ───────────────────────────────────────────────────
INSERT INTO documentopersona (numerodocumento, id_tipodocumento, id_lugarexpedicion)
  VALUES ('10000000003', 1, 3); -- 3
INSERT INTO persona (nombres, apellidos, correo, celular,
                     id_genero, id_estadocivil, id_documentopersona,
                     id_ubicacionnacimiento, id_ubicacionvivienda)
  VALUES ('Coord', 'Posgrados', 'posgrados1@perf.local', '3000000003', 2, 1, 3, 4, 5); -- 3
INSERT INTO clave (valor) VALUES ('posgrados123'); -- 3
INSERT INTO usuarios (nombreusuario, id_persona, id_clave, id_rol)
  VALUES ('posgrados1', 3, 3, 4); -- 3

-- ── 15. 10 aspirantes de prueba (para scripts de autenticación) ───────────────
INSERT INTO documentopersona (numerodocumento, id_tipodocumento, id_lugarexpedicion) VALUES ('20000000001', 1, 3); -- 4
INSERT INTO persona (nombres,apellidos,correo,celular,id_genero,id_estadocivil,id_documentopersona,id_ubicacionnacimiento,id_ubicacionvivienda)
  VALUES ('Aspirante','Test01','aspirante01@perf.local','3100000001',1,1,4,4,5); -- 4
INSERT INTO clave (valor) VALUES ('aspirante123'); -- 4
INSERT INTO aspirante (id_persona,id_estado,id_cohorte,id_tipovinculacion) VALUES (4,2,1,1); -- 1
INSERT INTO usuarios (nombreusuario,id_persona,id_clave,id_rol) VALUES ('aspirante01',4,4,1); -- 4

INSERT INTO documentopersona (numerodocumento, id_tipodocumento, id_lugarexpedicion) VALUES ('20000000002', 1, 3); -- 5
INSERT INTO persona (nombres,apellidos,correo,celular,id_genero,id_estadocivil,id_documentopersona,id_ubicacionnacimiento,id_ubicacionvivienda)
  VALUES ('Aspirante','Test02','aspirante02@perf.local','3100000002',2,1,5,4,5); -- 5
INSERT INTO clave (valor) VALUES ('aspirante123'); -- 5
INSERT INTO aspirante (id_persona,id_estado,id_cohorte,id_tipovinculacion) VALUES (5,2,1,1); -- 2
INSERT INTO usuarios (nombreusuario,id_persona,id_clave,id_rol) VALUES ('aspirante02',5,5,1); -- 5

INSERT INTO documentopersona (numerodocumento, id_tipodocumento, id_lugarexpedicion) VALUES ('20000000003', 1, 3); -- 6
INSERT INTO persona (nombres,apellidos,correo,celular,id_genero,id_estadocivil,id_documentopersona,id_ubicacionnacimiento,id_ubicacionvivienda)
  VALUES ('Aspirante','Test03','aspirante03@perf.local','3100000003',1,2,6,4,5); -- 6
INSERT INTO clave (valor) VALUES ('aspirante123'); -- 6
INSERT INTO aspirante (id_persona,id_estado,id_cohorte,id_tipovinculacion) VALUES (6,2,2,1); -- 3
INSERT INTO usuarios (nombreusuario,id_persona,id_clave,id_rol) VALUES ('aspirante03',6,6,1); -- 6

INSERT INTO documentopersona (numerodocumento, id_tipodocumento, id_lugarexpedicion) VALUES ('20000000004', 1, 3); -- 7
INSERT INTO persona (nombres,apellidos,correo,celular,id_genero,id_estadocivil,id_documentopersona,id_ubicacionnacimiento,id_ubicacionvivienda)
  VALUES ('Aspirante','Test04','aspirante04@perf.local','3100000004',2,1,7,4,5); -- 7
INSERT INTO clave (valor) VALUES ('aspirante123'); -- 7
INSERT INTO aspirante (id_persona,id_estado,id_cohorte,id_tipovinculacion) VALUES (7,2,2,2); -- 4
INSERT INTO usuarios (nombreusuario,id_persona,id_clave,id_rol) VALUES ('aspirante04',7,7,1); -- 7

INSERT INTO documentopersona (numerodocumento, id_tipodocumento, id_lugarexpedicion) VALUES ('20000000005', 1, 3); -- 8
INSERT INTO persona (nombres,apellidos,correo,celular,id_genero,id_estadocivil,id_documentopersona,id_ubicacionnacimiento,id_ubicacionvivienda)
  VALUES ('Aspirante','Test05','aspirante05@perf.local','3100000005',1,1,8,4,5); -- 8
INSERT INTO clave (valor) VALUES ('aspirante123'); -- 8
INSERT INTO aspirante (id_persona,id_estado,id_cohorte,id_tipovinculacion) VALUES (8,2,3,1); -- 5
INSERT INTO usuarios (nombreusuario,id_persona,id_clave,id_rol) VALUES ('aspirante05',8,8,1); -- 8

INSERT INTO documentopersona (numerodocumento, id_tipodocumento, id_lugarexpedicion) VALUES ('20000000006', 1, 3); -- 9
INSERT INTO persona (nombres,apellidos,correo,celular,id_genero,id_estadocivil,id_documentopersona,id_ubicacionnacimiento,id_ubicacionvivienda)
  VALUES ('Aspirante','Test06','aspirante06@perf.local','3100000006',2,2,9,4,5); -- 9
INSERT INTO clave (valor) VALUES ('aspirante123'); -- 9
INSERT INTO aspirante (id_persona,id_estado,id_cohorte,id_tipovinculacion) VALUES (9,2,3,2); -- 6
INSERT INTO usuarios (nombreusuario,id_persona,id_clave,id_rol) VALUES ('aspirante06',9,9,1); -- 9

INSERT INTO documentopersona (numerodocumento, id_tipodocumento, id_lugarexpedicion) VALUES ('20000000007', 1, 3); -- 10
INSERT INTO persona (nombres,apellidos,correo,celular,id_genero,id_estadocivil,id_documentopersona,id_ubicacionnacimiento,id_ubicacionvivienda)
  VALUES ('Aspirante','Test07','aspirante07@perf.local','3100000007',1,1,10,4,5); -- 10
INSERT INTO clave (valor) VALUES ('aspirante123'); -- 10
INSERT INTO aspirante (id_persona,id_estado,id_cohorte,id_tipovinculacion) VALUES (10,2,4,1); -- 7
INSERT INTO usuarios (nombreusuario,id_persona,id_clave,id_rol) VALUES ('aspirante07',10,10,1); -- 10

INSERT INTO documentopersona (numerodocumento, id_tipodocumento, id_lugarexpedicion) VALUES ('20000000008', 1, 3); -- 11
INSERT INTO persona (nombres,apellidos,correo,celular,id_genero,id_estadocivil,id_documentopersona,id_ubicacionnacimiento,id_ubicacionvivienda)
  VALUES ('Aspirante','Test08','aspirante08@perf.local','3100000008',2,1,11,4,5); -- 11
INSERT INTO clave (valor) VALUES ('aspirante123'); -- 11
INSERT INTO aspirante (id_persona,id_estado,id_cohorte,id_tipovinculacion) VALUES (11,2,4,2); -- 8
INSERT INTO usuarios (nombreusuario,id_persona,id_clave,id_rol) VALUES ('aspirante08',11,11,1); -- 11

INSERT INTO documentopersona (numerodocumento, id_tipodocumento, id_lugarexpedicion) VALUES ('20000000009', 1, 3); -- 12
INSERT INTO persona (nombres,apellidos,correo,celular,id_genero,id_estadocivil,id_documentopersona,id_ubicacionnacimiento,id_ubicacionvivienda)
  VALUES ('Aspirante','Test09','aspirante09@perf.local','3100000009',1,1,12,4,5); -- 12
INSERT INTO clave (valor) VALUES ('aspirante123'); -- 12
INSERT INTO aspirante (id_persona,id_estado,id_cohorte,id_tipovinculacion) VALUES (12,2,5,1); -- 9
INSERT INTO usuarios (nombreusuario,id_persona,id_clave,id_rol) VALUES ('aspirante09',12,12,1); -- 12

INSERT INTO documentopersona (numerodocumento, id_tipodocumento, id_lugarexpedicion) VALUES ('20000000010', 1, 3); -- 13
INSERT INTO persona (nombres,apellidos,correo,celular,id_genero,id_estadocivil,id_documentopersona,id_ubicacionnacimiento,id_ubicacionvivienda)
  VALUES ('Aspirante','Test10','aspirante10@perf.local','3100000010',2,2,13,4,5); -- 13
INSERT INTO clave (valor) VALUES ('aspirante123'); -- 13
INSERT INTO aspirante (id_persona,id_estado,id_cohorte,id_tipovinculacion) VALUES (13,2,5,2); -- 10
INSERT INTO usuarios (nombreusuario,id_persona,id_clave,id_rol) VALUES ('aspirante10',13,13,1); -- 13

COMMIT;

-- ── 16. Procedimiento para generar 2 000 aspirantes de carga ──────────────────
DROP PROCEDURE IF EXISTS seed_aspirantes_carga;

DELIMITER $$
CREATE PROCEDURE seed_aspirantes_carga()
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE v_id_doc   INT;
  DECLARE v_id_ubic_exp INT DEFAULT 3;
  DECLARE v_id_ubic_nac INT DEFAULT 4;
  DECLARE v_id_ubic_viv INT DEFAULT 5;
  DECLARE v_id_persona INT;
  DECLARE v_id_clave   INT;
  DECLARE v_id_aspirante INT;
  DECLARE v_cohorte INT;
  DECLARE v_estado  INT;

  WHILE i <= 2000 DO
    -- Distribuir aspirantes entre las 5 cohortes abiertas (ids 1..5)
    SET v_cohorte = (i MOD 5) + 1;
    -- 70 % NO CONFIRMADO (id=1), 30 % CONFIRMADO (id=2)
    SET v_estado = IF(i MOD 10 < 7, 1, 2);

    INSERT INTO documentopersona (numerodocumento, id_tipodocumento, id_lugarexpedicion)
      VALUES (CONCAT('9', LPAD(i, 9, '0')), 1, v_id_ubic_exp);
    SET v_id_doc = LAST_INSERT_ID();

    INSERT INTO persona (nombres, apellidos, correo, celular,
                         id_genero, id_estadocivil, id_documentopersona,
                         id_ubicacionnacimiento, id_ubicacionvivienda)
      VALUES (CONCAT('Nombre', i), CONCAT('Apellido', i),
              CONCAT('perf', i, '@carga.local'),
              CONCAT('31', LPAD(i, 8, '0')),
              (i MOD 2) + 1, 1,
              v_id_doc, v_id_ubic_nac, v_id_ubic_viv);
    SET v_id_persona = LAST_INSERT_ID();

    INSERT INTO clave (valor) VALUES ('perf123');
    SET v_id_clave = LAST_INSERT_ID();

    INSERT INTO aspirante (id_persona, id_estado, id_cohorte, id_tipovinculacion, puntuacion)
      VALUES (v_id_persona, v_estado, v_cohorte, 1,
              ROUND(3.0 + (RAND() * 2.0), 2));
    SET v_id_aspirante = LAST_INSERT_ID();

    INSERT INTO usuarios (nombreusuario, id_persona, id_clave, id_rol)
      VALUES (CONCAT('perf_asp_', i), v_id_persona, v_id_clave, 1);

    SET i = i + 1;
  END WHILE;
END$$
DELIMITER ;

CALL seed_aspirantes_carga();
COMMIT;
DROP PROCEDURE IF EXISTS seed_aspirantes_carga;

SET FOREIGN_KEY_CHECKS = 1;
SET autocommit = 1;

-- ── Verificación rápida ───────────────────────────────────────────────────────
SELECT 'roles'       AS tabla, COUNT(*) AS filas FROM roles
UNION ALL SELECT 'cohorte',    COUNT(*) FROM cohorte
UNION ALL SELECT 'aspirante',  COUNT(*) FROM aspirante
UNION ALL SELECT 'usuarios',   COUNT(*) FROM usuarios
UNION ALL SELECT 'persona',    COUNT(*) FROM persona;
