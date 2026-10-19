# SC-06: Endpoints convertidos de POST a GET

## Contexto

- **Rama:** SC-06
- **Fecha:** 2026-10-18
- **Prefijo de servidor:** `${APP_PREFIX}` (p. ej. `/posgrados-project`) se antepone a **todas** las rutas del sistema; no se repite en las columnas siguientes para mantener las tablas legibles.
- **Prefijo API REST:** `/api/dev/endpoint` aplicado automáticamente a todos los controladores del paquete `ufps.edu.co.controllers.rest` mediante `ApiRestConfiguration.configurePathMatch()`.
- **Ruta completa en uso por el frontend:** `<APP_PREFIX>/api/dev/endpoint/<recurso>/<ruta>` (p. ej. `/posgrados-project/api/dev/endpoint/administrativo/5`).
- **Total de endpoints convertidos:** 42, distribuidos en 37 controladores.
- **Pruebas tras el cambio:** 167 tests — 0 fallos, 0 errores, 0 omitidos (idéntico al baseline).

---

## A. Tabla principal — rutas convertidas

> Las rutas no incluyen el prefijo de servidor `<APP_PREFIX>`. Antepónerlo según el entorno.

| N°  | Endpoint anterior (POST)                                                 | Endpoint nuevo (GET)                                                      |
|-----|--------------------------------------------------------------------------|---------------------------------------------------------------------------|
| 1   | POST /api/dev/endpoint/administrativo/list                               | GET /api/dev/endpoint/administrativo/{id}                                 |
| 2   | POST /api/dev/endpoint/aspirante/list                                    | GET /api/dev/endpoint/aspirante/{id}                                      |
| 3   | POST /api/dev/endpoint/aspirante/estado                                  | GET /api/dev/endpoint/aspirante/{id}/estado                               |
| 4   | POST /api/dev/endpoint/calificacioncriterio/list                         | GET /api/dev/endpoint/calificacioncriterio/{id}                           |
| 5   | POST /api/dev/endpoint/calificacioncriterio/aspirante                    | GET /api/dev/endpoint/calificacioncriterio/aspirante/{idAspirante}        |
| 6   | POST /api/dev/endpoint/calificacioncriterio/criterio                     | GET /api/dev/endpoint/calificacioncriterio/criterio/{idCriterio}          |
| 7   | POST /api/dev/endpoint/cargo/list                                        | GET /api/dev/endpoint/cargo/{id}                                          |
| 8   | POST /api/dev/endpoint/cambiodocumento/list                              | GET /api/dev/endpoint/cambiodocumento/{id}                                |
| 9   | POST /api/dev/endpoint/clave/list                                        | GET /api/dev/endpoint/clave/{id}                                          |
| 10  | POST /api/dev/endpoint/cohortes/list                                     | GET /api/dev/endpoint/cohortes/{id}                                       |
| 11  | POST /api/dev/endpoint/criterioevaluacion/list                           | GET /api/dev/endpoint/criterioevaluacion/{id}                             |
| 12  | POST /api/dev/endpoint/departamento/list                                 | GET /api/dev/endpoint/departamento/{id}                                   |
| 13  | POST /api/dev/endpoint/documento/list                                    | GET /api/dev/endpoint/documento/{id}                                      |
| 14  | POST /api/dev/endpoint/documentopersona/list                             | GET /api/dev/endpoint/documentopersona/{id}                               |
| 15  | POST /api/dev/endpoint/documentosrequisitoconsejo/list                   | GET /api/dev/endpoint/documentosrequisitoconsejo/{id}                     |
| 16  | POST /api/dev/endpoint/documentosrequisitoconsejocohorte/list            | GET /api/dev/endpoint/documentosrequisitoconsejocohorte/{id}              |
| 17  | POST /api/dev/endpoint/documentosrequisitoprograma/list                  | GET /api/dev/endpoint/documentosrequisitoprograma/{id}                    |
| 18  | POST /api/dev/endpoint/documentosrequisitoprogramacohorte/list           | GET /api/dev/endpoint/documentosrequisitoprogramacohorte/{id}             |
| 19  | POST /api/dev/endpoint/entrevista/list                                   | GET /api/dev/endpoint/entrevista/{id}                                     |
| 20  | POST /api/dev/endpoint/estado/list                                       | GET /api/dev/endpoint/estado/{id}                                         |
| 21  | POST /api/dev/endpoint/estadodocumento/list                              | GET /api/dev/endpoint/estadodocumento/{id}                                |
| 22  | POST /api/dev/endpoint/facultad/list                                     | GET /api/dev/endpoint/facultad/{id}                                       |
| 23  | POST /api/dev/endpoint/genero/list                                       | GET /api/dev/endpoint/genero/{id}                                         |
| 24  | POST /api/dev/endpoint/modalidad/list                                    | GET /api/dev/endpoint/modalidad/{id}                                      |
| 25  | POST /api/dev/endpoint/municipio/list                                    | GET /api/dev/endpoint/municipio/{id}                                      |
| 26  | POST /api/dev/endpoint/otrosvalores/list                                 | GET /api/dev/endpoint/otrosvalores/{id}                                   |
| 27  | POST /api/dev/endpoint/pais/list                                         | GET /api/dev/endpoint/pais/{id}                                           |
| 28  | POST /api/dev/endpoint/persona/list                                      | GET /api/dev/endpoint/persona/{id}                                        |
| 29  | POST /api/dev/endpoint/plazo/list                                        | GET /api/dev/endpoint/plazo/{id}                                          |
| 30  | POST /api/dev/endpoint/programa/list                                     | GET /api/dev/endpoint/programa/{id}                                       |
| 31  | POST /api/dev/endpoint/programa/listbyfacultad                           | GET /api/dev/endpoint/programa/byfacultad/{id}                            |
| 32  | POST /api/dev/endpoint/programa/count-en-proceso                         | GET /api/dev/endpoint/programa/count-en-proceso/{id}                      |
| 33  | POST /api/dev/endpoint/prueba/list                                       | GET /api/dev/endpoint/prueba/{id}                                         |
| 34  | POST /api/dev/endpoint/rol/list                                          | GET /api/dev/endpoint/rol/{id}                                            |
| 35  | POST /api/dev/endpoint/sedes/list                                        | GET /api/dev/endpoint/sedes/{id}                                          |
| 36  | POST /api/dev/endpoint/semestre/list                                     | GET /api/dev/endpoint/semestre/{id}                                       |
| 37  | POST /api/dev/endpoint/tipodocumentopersona/list                         | GET /api/dev/endpoint/tipodocumentopersona/{id}                           |
| 38  | POST /api/dev/endpoint/tipoentrevista/list                               | GET /api/dev/endpoint/tipoentrevista/{id}                                 |
| 39  | POST /api/dev/endpoint/tipoplazo/list                                    | GET /api/dev/endpoint/tipoplazo/{id}                                      |
| 40  | POST /api/dev/endpoint/tipoprueba/list                                   | GET /api/dev/endpoint/tipoprueba/{id}                                     |
| 41  | POST /api/dev/endpoint/ubicacion/list                                    | GET /api/dev/endpoint/ubicacion/{id}                                      |
| 42  | POST /api/dev/endpoint/usuario/list                                      | GET /api/dev/endpoint/usuario/{id}                                        |

---

## B. Tabla de detalle

| N°  | Controlador                                        | Body JSON enviado antes                      | Cómo viaja el id ahora              | Roles autorizados                        | Notas |
|-----|----------------------------------------------------|----------------------------------------------|-------------------------------------|------------------------------------------|-------|
| 1   | AdministrativoRestController                       | `{"id": <idAdministrativo>}`                 | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 2   | AspiranteRestController                            | `{"id": <idAspirante>}`                      | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | Se eliminó `@Valid` del parámetro (no aplica en GET) |
| 3   | AspiranteRestController                            | `{"id": <idAspirante>}`                      | Path variable `/{id}` en `/{id}/estado` | SUPER_ADMINISTRADOR                 | Devuelve `EstadoOutput`, no `AspiranteOutput` |
| 4   | CalificacioncriterioRestController                 | `{"id": <idCalificacion>}`                   | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 5   | CalificacioncriterioRestController                 | `{"idAspirante": <valor>}`                   | Path variable `/aspirante/{idAspirante}` | SUPER_ADMINISTRADOR               | Campo del body era `idAspirante`, no `id` |
| 6   | CalificacioncriterioRestController                 | `{"idCriterio": <valor>}`                    | Path variable `/criterio/{idCriterio}` | SUPER_ADMINISTRADOR                | Campo del body era `idCriterio`, no `id` |
| 7   | CargoRestController                                | `{"id": <idCargo>}`                          | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 8   | CambiodocumentoRestController                      | `{"id": <idCambio>}`                         | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 9   | ClaveRestController                                | `{"id": <idClave>}`                          | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 10  | CohorteRestController                              | `{"id": <idCohorte>}`                        | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 11  | CriterioevaluacionRestController                   | `{"id": <idCriterio>}`                       | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 12  | DepartamentoRestController                         | `{"id": <idDepartamento>}`                   | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 13  | DocumentoRestController                            | `{"id": <idDocumento>}`                      | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 14  | DocumentopersonaRestController                     | `{"id": <idDocpersona>}`                     | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 15  | DocumentosrequisitoconsejoRestController           | `{"id": <idRequisito>}`                      | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 16  | DocumentosrequisitoconsejocohorteRestController    | `{"id": <idRequisito>}`                      | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 17  | DocumentosrequisitoprogramaRestController          | `{"id": <idRequisito>}`                      | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 18  | DocumentosrequisitoprogramacohorteRestController   | `{"id": <idRequisito>}`                      | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 19  | EntrevistaRestController                           | `{"id": <idEntrevista>}`                     | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 20  | EstadoRestController                               | `{"id": <idEstado>}`                         | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 21  | EstadodocumentoRestController                      | `{"id": <idEstadoDoc>}`                      | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 22  | FacultadRestController                             | `{"id": <idFacultad>}`                       | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 23  | GeneroRestController                               | `{"id": <idGenero>}`                         | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | Se añadió import `@GetMapping` (no existía) |
| 24  | ModalidadRestController                            | `{"id": <idModalidad>}`                      | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 25  | MunicipioRestController                            | `{"id": <idMunicipio>}`                      | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 26  | OtrosvaloresRestController                         | `{"id": <idOtrosvalores>}`                   | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 27  | PaisRestController                                 | `{"id": <idPais>}`                           | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | Se añadió import `@GetMapping` (no existía) |
| 28  | PersonaRestController                              | `{"id": <idPersona>}`                        | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | Se eliminó `@Valid` del parámetro (no aplica en GET) |
| 29  | PlazoRestController                                | `{"id": <idPlazo>}`                          | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 30  | ProgramaRestController                             | `{"id": <idPrograma>}`                       | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 31  | ProgramaRestController                             | `{"id": <idFacultad>}`                       | Path variable `/byfacultad/{id}`    | SUPER_ADMINISTRADOR, POSGRADOS           | SecurityConfig actualizado: `/programa/listbyfacultad` → `/programa/byfacultad/**` |
| 32  | ProgramaRestController                             | `{"id": <idCohorte>}`                        | Path variable `/count-en-proceso/{id}` | SUPER_ADMINISTRADOR                  | Devuelve `Long`, no una entidad |
| 33  | PruebaRestController                               | `{"id": <idPrueba>}`                         | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 34  | RolRestController                                  | `{"id": <idRol>}`                            | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 35  | SedeController                                     | `{"id": <idSede>}`                           | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 36  | SemestreRestController                             | `{"id": <idSemestre>}`                       | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 37  | TipodocumentopersonaRestController                 | `{"id": <idTipoDoc>}`                        | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | Se añadió import `@GetMapping` (no existía) |
| 38  | TipoentrevistaRestController                       | `{"id": <idTipoEntrevista>}`                 | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 39  | TipoplazoRestController                            | `{"id": <idTipoPlazo>}`                      | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 40  | TipopruebaRestController                           | `{"id": <idTipoPrueba>}`                     | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 41  | UbicacionRestController                            | `{"id": <idUbicacion>}`                      | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |
| 42  | UsuarioRestController                              | `{"id": <idUsuario>}`                        | Path variable `/{id}`               | SUPER_ADMINISTRADOR                      | — |

---

## C. Ejemplos antes/después

### Patrón estándar (aplicado a 34 controladores — filas 1 y 7–42)

**Antes:**
```http
POST <APP_PREFIX>/api/dev/endpoint/estado/list
Authorization: Bearer <token>
Content-Type: application/json

{"id": 5}
```

**Después:**
```http
GET <APP_PREFIX>/api/dev/endpoint/estado/5
Authorization: Bearer <token>
```

La respuesta HTTP (código 200 + cuerpo JSON) y los roles autorizados son idénticos.

---

### Aspirante — buscar por id (fila 2)

**Antes:**
```http
POST <APP_PREFIX>/api/dev/endpoint/aspirante/list
Authorization: Bearer <token>
Content-Type: application/json

{"id": 12}
```

**Después:**
```http
GET <APP_PREFIX>/api/dev/endpoint/aspirante/12
Authorization: Bearer <token>
```

---

### Aspirante — obtener estado (fila 3)

**Antes:**
```http
POST <APP_PREFIX>/api/dev/endpoint/aspirante/estado
Authorization: Bearer <token>
Content-Type: application/json

{"id": 12}
```

**Después:**
```http
GET <APP_PREFIX>/api/dev/endpoint/aspirante/12/estado
Authorization: Bearer <token>
```

---

### CalificacionCriterio — por aspirante (fila 5)

**Antes:**
```http
POST <APP_PREFIX>/api/dev/endpoint/calificacioncriterio/aspirante
Authorization: Bearer <token>
Content-Type: application/json

{"idAspirante": 7}
```

**Después:**
```http
GET <APP_PREFIX>/api/dev/endpoint/calificacioncriterio/aspirante/7
Authorization: Bearer <token>
```

> El campo del body era `idAspirante` (no `id`). En la URL también se llama `{idAspirante}`.

---

### CalificacionCriterio — por criterio (fila 6)

**Antes:**
```http
POST <APP_PREFIX>/api/dev/endpoint/calificacioncriterio/criterio
Authorization: Bearer <token>
Content-Type: application/json

{"idCriterio": 3}
```

**Después:**
```http
GET <APP_PREFIX>/api/dev/endpoint/calificacioncriterio/criterio/3
Authorization: Bearer <token>
```

> El campo del body era `idCriterio` (no `id`). En la URL también se llama `{idCriterio}`.

---

### Programa — lista por facultad (fila 31)

**Antes:**
```http
POST <APP_PREFIX>/api/dev/endpoint/programa/listbyfacultad
Authorization: Bearer <token>
Content-Type: application/json

{"id": 2}
```

**Después:**
```http
GET <APP_PREFIX>/api/dev/endpoint/programa/byfacultad/2
Authorization: Bearer <token>
```

> Esta fue la única ruta que también requirió actualizar `SecurityConfig.java`: la entrada `/api/dev/endpoint/programa/listbyfacultad` en `POSGRADOS_PATHS` se reemplazó por `/api/dev/endpoint/programa/byfacultad/**`. Los roles (SUPER_ADMINISTRADOR y POSGRADOS) no cambian.

---

### Programa — count aspirantes en proceso (fila 32)

**Antes:**
```http
POST <APP_PREFIX>/api/dev/endpoint/programa/count-en-proceso
Authorization: Bearer <token>
Content-Type: application/json

{"id": 4}
```

**Después:**
```http
GET <APP_PREFIX>/api/dev/endpoint/programa/count-en-proceso/4
Authorization: Bearer <token>
```

> Devuelve un `Long` (número), no una entidad.

---

## D. Notas para el frontend

1. **Eliminar `Content-Type: application/json` y el cuerpo de la petición** en todas las llamadas convertidas.
2. **Cambiar el método HTTP** de `POST` a `GET`.
3. **Patrón estándar (filas 1, 2, 4, 7–30, 33–42):** el body era `{"id": N}` → mover `N` al final de la URL: `.../recurso/N`.
4. **Aspirante/estado (fila 3):** el body era `{"id": N}` → la URL queda `.../aspirante/N/estado`.
5. **CalificacionCriterio/aspirante (fila 5):** el campo del body era `idAspirante`, no `id`. La URL queda `.../calificacioncriterio/aspirante/{idAspirante}`.
6. **CalificacionCriterio/criterio (fila 6):** el campo del body era `idCriterio`, no `id`. La URL queda `.../calificacioncriterio/criterio/{idCriterio}`.
7. **Programa/byfacultad (fila 31):** el campo del body era `id` (idFacultad). La URL queda `.../programa/byfacultad/{id}`.
8. **Programa/count-en-proceso (fila 32):** el campo del body era `id`. La URL queda `.../programa/count-en-proceso/{id}`.
9. Los **códigos de respuesta HTTP** y los **cuerpos JSON** son idénticos a los anteriores; solo cambia cómo se llama al endpoint.
10. El header `Authorization: Bearer <token>` sigue siendo obligatorio en todos estos endpoints.
11. El prefijo de servidor `<APP_PREFIX>` (definido en `application.properties` como `server.servlet.context-path=${APP_PREFIX}`) no cambia.
12. Los endpoints `GET /listall` de cada recurso **no se modificaron**; siguen funcionando exactamente igual.

---

## E. Endpoints POST que NO se convirtieron

Los siguientes grupos de endpoints POST se mantienen **sin cambios** porque tienen efectos secundarios (escritura, autenticación, envío de correo, pago, subida de archivos) o no cumplen el criterio de conversión (cuerpo complejo con múltiples filtros, identificador personal que no debe ir en la URL):

- `POST /auth/login` y `POST /auth/refresh` — autenticación, generan tokens.
- `POST /api/application/case/recuperarContrasena/solicitar` — envía correo electrónico.
- `POST /api/application/case/recuperarContrasena/cambiar` — modifica contraseña en base de datos.
- `POST /api/application/case/login/recoveryPassword` — recuperación de contraseña.
- `POST /api/application/case/aspirantes/confirmar-correo` — confirma dirección de correo.
- `POST /api/application/case/wompi/**` — webhooks de pasarela de pago (efecto externo).
- `POST /api/application/case/inscripciones/**` — crea y gestiona inscripciones de aspirantes.
- `POST .../create` — creación de entidades en todos los controladores (efecto de escritura).
- `POST` en rutas de los paquetes `cases` y `command` (`/api/application/case/**`, `/api/application/command/**`) que no son consultas puras por identificador único.
- `POST /api/dev/endpoint/documentosrequisitoconsejo/superadmin/create` — creación con subida de archivo adjunto.
- `POST /api/dev/endpoint/valoresglobalesadmin/**` — creación de valores globales del sistema.
- `POST /api/dev/endpoint/superadmin/**` — operaciones de super administrador con efectos en base de datos.
