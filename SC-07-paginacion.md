1# SC-07 — Paginación de endpoints críticos

> **Rama:** `SC-07`  
> **Prioridad:** los 5 endpoints están sin paginación y sin `Pageable` en toda la cadena (repositorio, servicio, controlador).  
> **Requisito previo para #1 y #2:** eliminar el N+1 _antes_ de paginar (ver sección de cada endpoint).

---

## Convención de respuesta paginada

Todos los endpoints paginados devuelven el objeto estándar de Spring Data:

```json
{
  "content": [...],
  "pageable": { "pageNumber": 0, "pageSize": 20 },
  "totalElements": 400,
  "totalPages": 20,
  "last": false,
  "first": true
}
```

El tipo de retorno en el controlador cambia de `List<XOutput>` → `Page<XOutput>`.  
Los parámetros de paginación son query params opcionales con defaults seguros.

---

## #1 — `GET .../director-programa/cohorte/{idCohorte}/aspirantes-a-validar`

**Archivo controlador:** `api-rest/.../controllers/cases/administrativo/DirectorProgramaCase.java`  
**Archivo servicio:** `api-core/.../services/core/crud/AspiranteCoreService.java` (método `findAValidarByCohorte`, línea ~1026)  
**Archivo repositorio:** `api-base/.../persistence/repositories/AspiranteRepository.java`

### Paso 0 — Eliminar el N+1 (obligatorio primero)

El método `findAValidarByCohorte` actualmente hace esto:
```java
// PROBLEMA: 1 query por aspirante = 401 queries con 400 aspirantes
for (AspiranteEntity aspirante : aspirantes) {
    List<DocumentoDTO> docs = documentoService.findByIdAspirante(aspirante.getId());
}
```

**Fix en `AspiranteCoreService.java`** — reemplazar el loop por una carga en batch:

```java
// ANTES (N+1)
public List<AspiranteCohorteOutput> findAValidarByCohorte(Integer cohorteId) {
    return service.findAValidarByCohorte(cohorteId).stream().map(aspirante -> {
        List<DocumentoDTO> docs = documentoService.findByIdAspirante(aspirante.getId());
        // ...
    }).toList();
}

// DESPUÉS (2 queries totales)
public List<AspiranteCohorteOutput> findAValidarByCohorte(Integer cohorteId) {
    List<AspiranteEntity> aspirantes = service.findAValidarByCohorte(cohorteId);

    List<Integer> ids = aspirantes.stream().map(AspiranteEntity::getId).toList();
    Map<Integer, List<DocumentoDTO>> docsPorAspirante = documentoService
            .findByIdAspiranteIn(ids)                          // nueva query batch
            .stream()
            .collect(Collectors.groupingBy(DocumentoDTO::idAspirante));

    return aspirantes.stream().map(aspirante -> {
        List<DocumentoDTO> docs = docsPorAspirante.getOrDefault(aspirante.getId(), List.of());
        long validados = docs.stream().filter(d -> /* condición validado */).count();
        // ... resto del mapeo igual que antes
    }).toList();
}
```

> Para implementar `findByIdAspiranteIn`: añadir en `DocumentoRepository`:
> ```java
> List<DocumentoEntity> findByIdAspiranteIn(List<Integer> ids);
> ```
> y el método correspondiente en `DocumentoCoreService`.

### Paso 1 — Repositorio

```java
// AspiranteRepository.java — añadir:
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

Page<AspiranteEntity> findByIdCohorteAndEstadoTipoIn(
        int idCohorte, List<String> estadoTipos, Pageable pageable);
```

> El método escalar `findAValidarByCohorte` en el servicio llama internamente a `findByIdCohorteAndEstadoTipoIn` con los tipos de estado correspondientes.

### Paso 2 — Servicio

```java
// AspiranteCoreService.java — añadir imports:
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// Nuevo método paginado (conservar el existente sin paginación si algo más lo usa):
public Page<AspiranteCohorteOutput> findAValidarByCohorte(Integer cohorteId, Pageable pageable) {
    Page<AspiranteEntity> pageResult = aspiranteRepository
            .findByIdCohorteAndEstadoTipoIn(cohorteId, ESTADOS_A_VALIDAR, pageable);

    // batch de documentos solo para los ids de esta página
    List<Integer> ids = pageResult.getContent().stream().map(AspiranteEntity::getId).toList();
    Map<Integer, List<DocumentoDTO>> docsPorAspirante = documentoService
            .findByIdAspiranteIn(ids).stream()
            .collect(Collectors.groupingBy(DocumentoDTO::idAspirante));

    return pageResult.map(aspirante -> {
        List<DocumentoDTO> docs = docsPorAspirante.getOrDefault(aspirante.getId(), List.of());
        // ... mismo mapeo que antes
        return AspiranteCohorteOutput.builder()/* ... */.build();
    });
}
```

### Paso 3 — Controlador

```java
// ANTES:
@GetMapping(value = "/cohorte/{idCohorte}/aspirantes-a-validar")
public ResponseEntity<List<AspiranteCohorteOutput>> getAspirantesAValidar(
        @PathVariable Integer idCohorte) {
    try {
        return ResponseEntity.ok(aspiranteProcessor.findAValidarByCohorte(idCohorte));
    } catch (Exception e) {
        return ResponseEntity.internalServerError().build();
    }
}

// DESPUÉS:
@GetMapping(value = "/cohorte/{idCohorte}/aspirantes-a-validar")
public ResponseEntity<Page<AspiranteCohorteOutput>> getAspirantesAValidar(
        @PathVariable Integer idCohorte,
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "20") int size) {
    try {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(aspiranteProcessor.findAValidarByCohorte(idCohorte, pageable));
    } catch (Exception e) {
        return ResponseEntity.internalServerError().build();
    }
}
```

**Imports nuevos en el controlador:**
```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
```

**Llamada nueva desde el front:**
```
// ANTES
GET .../director-programa/cohorte/3/aspirantes-a-validar

// DESPUÉS
GET .../director-programa/cohorte/3/aspirantes-a-validar?page=0&size=20
```

---

## #2 — `GET .../cohortes/{idCohorte}/aspirantes`

**Archivo controlador:** `api-rest/.../controllers/cases/aspirante/CohorteCase.java`  
**Archivo servicio:** `api-core/.../services/core/crud/AspiranteCoreService.java` (método `findByCohorteConResumen`, línea ~995)  
**Archivo repositorio:** `api-base/.../persistence/repositories/AspiranteRepository.java`

### Paso 0 — Eliminar el N+1 (obligatorio primero)

Mismo patrón que #1. El método `findByCohorteConResumen` tiene el mismo loop con `documentoService.findByIdAspirante(aspirante.getId())`.  
Aplicar el mismo fix de batch con `findByIdAspiranteIn` descrito en #1-Paso 0.

### Paso 1 — Repositorio

```java
// AspiranteRepository.java — añadir (si no se añadió en #1):
Page<AspiranteEntity> findByIdCohorte(int idCohorte, Pageable pageable);
```

### Paso 2 — Servicio

```java
// AspiranteCoreService.java — nuevo método paginado:
public Page<AspiranteCohorteOutput> findByCohorteConResumen(Integer cohorteId, Pageable pageable) {
    Page<AspiranteEntity> pageResult = aspiranteRepository.findByIdCohorte(cohorteId, pageable);

    List<Integer> ids = pageResult.getContent().stream().map(AspiranteEntity::getId).toList();
    Map<Integer, List<DocumentoDTO>> docsPorAspirante = documentoService
            .findByIdAspiranteIn(ids).stream()
            .collect(Collectors.groupingBy(DocumentoDTO::idAspirante));

    return pageResult.map(aspirante -> {
        List<DocumentoDTO> docs = docsPorAspirante.getOrDefault(aspirante.getId(), List.of());
        // ... mismo mapeo que antes
        return AspiranteCohorteOutput.builder()/* ... */.build();
    });
}
```

### Paso 3 — Controlador

```java
// ANTES (CohorteCase.java):
@GetMapping("/{idCohorte}/aspirantes")
public ResponseEntity<List<AspiranteCohorteOutput>> getAspirantesByCohorte(
        @PathVariable Integer idCohorte) {
    return ResponseEntity.ok(aspiranteProcessor.findByCohorteConResumen(idCohorte));
}

// DESPUÉS:
@GetMapping("/{idCohorte}/aspirantes")
public ResponseEntity<Page<AspiranteCohorteOutput>> getAspirantesByCohorte(
        @PathVariable Integer idCohorte,
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "20") int size) {
    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(aspiranteProcessor.findByCohorteConResumen(idCohorte, pageable));
}
```

**Llamada nueva desde el front:**
```
// ANTES
GET .../cohortes/3/aspirantes

// DESPUÉS
GET .../cohortes/3/aspirantes?page=0&size=20
```

---

## #3 — `GET .../aspirante/listall`

**Archivo controlador:** `api-rest/.../controllers/rest/AspiranteRestController.java`  
**Archivo servicio:** `api-core/.../services/core/crud/AspiranteCoreService.java`  
**Archivo repositorio:** `api-base/.../persistence/repositories/AspiranteRepository.java`

No hay N+1 en este endpoint. Cambio limpio de `findAll()` → `findAll(Pageable)`.

### Paso 1 — Repositorio

`JpaRepository` ya hereda `Page<T> findAll(Pageable pageable)` — **no hay que añadir nada** en el repositorio.

### Paso 2 — Servicio

```java
// AspiranteCoreService.java — añadir imports:
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// Nuevo método (conservar findAll() si algo más lo usa):
public Page<AspiranteOutput> findAll(Pageable pageable) {
    return aspiranteRepository.findAll(pageable)
            .map(/* mapper existente, mismo que en findAll() */);
}
```

### Paso 3 — Controlador

```java
// ANTES (AspiranteRestController.java):
@GetMapping("/listall")
public ResponseEntity<List<AspiranteOutput>> findAll() {
    List<AspiranteOutput> list = processor.findAll();
    return ResponseEntity.ok(list);
}

// DESPUÉS:
@GetMapping("/listall")
public ResponseEntity<Page<AspiranteOutput>> findAll(
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "20") int size) {
    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(processor.findAll(pageable));
}
```

**Imports nuevos en el controlador:**
```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
```

**Llamada nueva desde el front:**
```
// ANTES
GET .../aspirante/listall

// DESPUÉS
GET .../aspirante/listall?page=0&size=20
```

---

## #4 — `GET .../documento/listall`

**Archivo controlador:** `api-rest/.../controllers/rest/DocumentoRestController.java`  
**Archivo servicio:** `api-core/.../services/core/crud/DocumentoCoreService.java`  
**Archivo repositorio:** `api-base/.../persistence/repositories/DocumentoRepository.java`

### Paso 1 — Repositorio

`JpaRepository` ya hereda `Page<T> findAll(Pageable pageable)` — **no hay que añadir nada**.

> **Nota:** El repositorio tiene `@EntityGraph` en `findById` pero no en `findAll`. Con paginación el lazy-loading por fila se distribuye en páginas pequeñas y deja de ser crítico. Si en producción se detecta lazy-loading por elemento, añadir la anotación también al `findAll(Pageable)` sobreescrito.

### Paso 2 — Servicio

```java
// DocumentoCoreService.java — añadir imports:
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public Page<DocumentoOutput> findAll(Pageable pageable) {
    return documentoRepository.findAll(pageable)
            .map(/* mapper existente */);
}
```

### Paso 3 — Controlador

```java
// ANTES (DocumentoRestController.java):
@GetMapping("/listall")
public ResponseEntity<List<DocumentoOutput>> findAll() {
    List<DocumentoOutput> list = processor.findAll();
    return ResponseEntity.ok(list);
}

// DESPUÉS:
@GetMapping("/listall")
public ResponseEntity<Page<DocumentoOutput>> findAll(
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "20") int size) {
    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(processor.findAll(pageable));
}
```

**Imports nuevos en el controlador:**
```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
```

**Llamada nueva desde el front:**
```
// ANTES
GET .../documento/listall

// DESPUÉS
GET .../documento/listall?page=0&size=20
```

---

## #5 — `GET .../calificacioncriterio/listall`

**Archivo controlador:** `api-rest/.../controllers/rest/CalificacioncriterioRestController.java`  
**Archivo servicio:** `api-core/.../services/core/crud/CalificacioncriterioCoreService.java`  
**Archivo repositorio:** `api-base/.../persistence/repositories/CalificacioncriterioRepository.java`

### Paso 1 — Repositorio

`JpaRepository` ya hereda `Page<T> findAll(Pageable pageable)` — **no hay que añadir nada**.

### Paso 2 — Servicio

```java
// CalificacioncriterioCoreService.java — añadir imports:
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public Page<CalificacioncriterioOutput> findAll(Pageable pageable) {
    return calificacioncriterioRepository.findAll(pageable)
            .map(/* mapper existente */);
}
```

### Paso 3 — Controlador

```java
// ANTES (CalificacioncriterioRestController.java):
@GetMapping("/listall")
public ResponseEntity<List<CalificacioncriterioOutput>> findAll() {
    return ResponseEntity.ok(processor.findAll());
}

// DESPUÉS:
@GetMapping("/listall")
public ResponseEntity<Page<CalificacioncriterioOutput>> findAll(
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "20") int size) {
    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(processor.findAll(pageable));
}
```

**Imports nuevos en el controlador:**
```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
```

**Llamada nueva desde el front:**
```
// ANTES
GET .../calificacioncriterio/listall

// DESPUÉS
GET .../calificacioncriterio/listall?page=0&size=20
```

---

## Resumen de archivos a modificar

| # | Controlador | Servicio | Repositorio | N+1 fix |
|---|---|---|---|---|
| 1 | `DirectorProgramaCase.java` | `AspiranteCoreService.java` | `AspiranteRepository.java` | Sí — batch docs |
| 2 | `CohorteCase.java` | `AspiranteCoreService.java` | `AspiranteRepository.java` | Sí — batch docs |
| 3 | `AspiranteRestController.java` | `AspiranteCoreService.java` | heredado de JpaRepository | No |
| 4 | `DocumentoRestController.java` | `DocumentoCoreService.java` | heredado de JpaRepository | No |
| 5 | `CalificacioncriterioRestController.java` | `CalificacioncriterioCoreService.java` | heredado de JpaRepository | No |

**Nuevo archivo requerido para el N+1 fix (#1 y #2):**
- `DocumentoRepository.java` → añadir `findByIdAspiranteIn(List<Integer> ids)`
- `DocumentoCoreService.java` → añadir `findByIdAspiranteIn(List<Integer> ids)`

---

## Orden de implementación recomendado

1. `DocumentoRepository` + `DocumentoCoreService` — `findByIdAspiranteIn` (prerequisito del N+1 fix)
2. `AspiranteCoreService` — fix N+1 en `findByCohorteConResumen` y `findAValidarByCohorte`
3. `AspiranteRepository` — métodos paginados `findByIdCohorte(Pageable)` y `findByIdCohorteAndEstadoTipoIn(..., Pageable)`
4. `AspiranteCoreService` — sobrecargas paginadas de los dos métodos
5. `CohorteCase` + `DirectorProgramaCase` — controladores #1 y #2
6. `CalificacioncriterioCoreService` + controlador #5 (tabla más grande, impacto inmediato)
7. `AspiranteCoreService` + `AspiranteRestController` — #3
8. `DocumentoCoreService` + `DocumentoRestController` — #4
