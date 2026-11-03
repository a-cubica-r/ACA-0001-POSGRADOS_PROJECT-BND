# Posgrados API

Backend del sistema de posgrados de la UFPS. Es un **monolito modular Maven** con un único
artefacto ejecutable (`platform/application`): gestiona autenticación, aspirantes, inscripciones,
documentos, pagos (Wompi), entrevistas, pruebas, criterios, lista de admitidos y legalización de
estudiantes. La rama desplegada actualmente es `render`.

## Stack

| Pieza | Versión / herramienta |
|---|---|
| Java | 21 |
| Framework | Spring Boot 4.0.6 (Spring Framework 7, Spring Security 7) |
| Build | Maven Wrapper (`mvnw`, `mvnw.cmd`) |
| Base de datos | MySQL, Spring Data JPA / Hibernate (`ddl-auto=none`) |
| Mapeo Entity ↔ DTO | MapStruct 1.6.3 |
| JWT | JJWT 0.12.6 |
| Documentación HTTP | springdoc-openapi 3.0.2 (`/api-docs`, `/swagger-ui.html`) |
| Almacenamiento | AWS S3 |
| Correo | AWS SES (Resend queda como configuración heredada) |
| Pagos | Wompi (gateway real o simulado) |
| PDF / recibos | Thymeleaf + Flying Saucer + ZXing + OpenPDF |
| Pruebas | JUnit 5, Mockito, MockMvc, H2 para pruebas de persistencia |

## Módulos

| Módulo | Responsabilidad |
|---|---|
| `backend/api-base` | Entidades JPA, repositorios, DTOs, servicios CRUD (`rest/services`) y mappers MapStruct (`mapping`). No contiene casos de uso. |
| `backend/api-core` | Casos de uso en `services/core` (`crud`, `cases`, `integracion`), records de entrada/salida, `maps` de input→DTO, excepciones de dominio, validadores, PDF de admitidos, eventos y listeners. |
| `backend/api-auth` | Login, refresh, contraseñas BCrypt (con rehash y transición desde texto plano), emisión/validación de JWT y normalización de roles. |
| `backend/api-rest` | Controladores HTTP (`controllers/rest`, `controllers/cases`, `controllers/auth`), manejo global de errores y configuración de prefijos. |
| `backend/api-wompi` | Gateway de pagos Wompi (real/simulado) y modelos de la integración. |
| `backend/mail-module` | Correo transaccional por AWS SES y tokens de confirmación/reset. |
| `backend/s3-storage` | Subida y descarga de documentos en S3. |
| `backend/check-builder` | Recibo de inscripción en PDF (Thymeleaf + Flying Saucer + ZXing) y su carga a S3. |
| `backend/api-test` | Módulo auxiliar de pruebas de mappers. |
| `platform/security` | `SecurityFilterChain`, CORS, filtro JWT y adaptador de principal. No valida contraseñas ni emite tokens. |
| `platform/application` | Punto de entrada (`ufps.edu.co.Application`), configuración central y ensamblaje de todos los módulos. |

Dependencias principales:

```text
api-base ─┬─ api-wompi ─┐
          ├─ mail-module ┤
          ├─ api-auth     │
          └─ api-core ◄───┘   (api-core → mail-module, api-wompi)
                ├─ s3-storage ── check-builder
                ├─ api-rest  (api-core, api-auth, mail-module, s3-storage)
                └─ api-test
security ── api-auth
application ── security, api-auth, api-base, api-core, api-rest, s3-storage, check-builder
```

## Requisitos

- JDK 21 (`java -version`). Con JDK 24 el build funciona, pero JaCoCo 0.8.12 avisa que no puede
  instrumentar clases de esa versión; para evitarlo usa JDK 21.
- MySQL 8/9 accesible. El esquema **no** lo crea Hibernate (`ddl-auto=none`): debe existir antes de
  arrancar. El repositorio **no** incluye un dump completo; pide el esquema/dump al equipo. Los
  scripts de cambios puntuales viven en `db/scripts/`.
- Opcional: Docker, para construir la imagen; k6, para las pruebas de rendimiento.

## Configuración

`platform/application/src/main/resources/application.properties` carga, si existe, un archivo
`.env.properties` en la raíz del repositorio:

```properties
spring.config.import=optional:file:.env.properties,optional:file:../../.env.properties
```

Puedes usar ese archivo (está en `.gitignore`) o definir las variables en el entorno. **Nunca
commitees valores reales.** Estas son solo las variables necesarias:

> En `.env.properties` escribe los valores **sin comillas** (`ENABLE_API_DOCS=true`, no
> `"true"`). Java Properties no elimina las comillas y Spring las interpreta como parte del valor.

| Grupo | Variables |
|---|---|
| Base de datos | `DB_URL`, `DB_USER`, `DB_PASSWORD`, `DB_SCHEMA`, `DB_DRIVER`, `DB_DIALECT` |
| Aplicación | `APP_NAME`, `APP_PREFIX`, `APP_BASE_URL`, `APP_FRONTEND_LOGIN_URL`, `APP_FRONTEND_RESET_PASSWORD_URL` |
| Seguridad básica | `SPRING_SECURITY_USER_NAME`, `SPRING_SECURITY_USER_PASSWORD` |
| JWT | `JWT_SECRET` (obligatoria; sin valor por defecto seguro en producción) |
| Contraseñas | `AUTH_ALLOW_PLAIN_TEXT_LEGACY` (opcional; por defecto `true` durante la transición a BCrypt) |
| Swagger | `ENABLE_API_DOCS`, `ENABLE_SWAGGER_UI`, `SWAGGER_API_DOCS_PATH`, `SWAGGER_UI_PATH` |
| Marcadores de módulo | `APP_MODULE_NAME_API_BASE`, `APP_MODULE_NAME_API_CORE`, `APP_MODULE_NAME_API_REST`, `APP_MODULE_NAME_API_AUTH`, `APP_MODULE_NAME_API_TEST`, `APP_MODULE_NAME_API_WOMPI`, `APP_MODULE_NAME_CHECK_BUILDER`, `APP_MODULE_NAME_APPLICATION`, `APP_MODULE_NAME_SECURITY`, `APP_MODULE_NAME_MAIL_MODULE`, `APP_MODULE_NAME_S3_STORAGE` |
| S3 | `AWS_ACCESS_KEY`, `AWS_SECRET_KEY`, `AWS_REGION`, `AWS_BUCKET_NAME` |
| SES | `AWS_ACCESS_KEY_ID`, `AWS_SECRET_KEY_SES`, `AWS_SES_FROM_EMAIL`, `AWS_SES_REGION` |
| Wompi | `WOMPI_SIMULATED`, `WOMPI_API_BASE_URL`, `WOMPI_CHECKOUT_BASE_URL`, `WOMPI_WIDGET_SCRIPT_URL`, `WOMPI_PUBLIC_KEY`, `WOMPI_PRIVATE_KEY`, `WOMPI_INTEGRITY_SECRET`, `WOMPI_CURRENCY`, `WOMPI_RETURN_URL`, `WOMPI_WEBHOOK_URL` |
| Resend (heredado) | `RESEND_API_KEY`, `MAIL_FROM` |
| Docker / Render | `PORT`, `JAVA_OPTS` |

Los `APP_MODULE_NAME_*` son obligatorios: cada `*ModuleStartupCheck` falla al arrancar si falta el
suyo. `WOMPI_SIMULATED=true` evita llamadas reales a Wompi en local.

No hay perfiles `dev`/`prod` todavía (I-06). El único perfil de ejecución existente es
`migrar-claves`, que ejecuta la migración de contraseñas a BCrypt:

```powershell
.\mvnw.cmd -pl platform/application -am spring-boot:run "-Dspring-boot.run.profiles=migrar-claves"
```

## Compilar

Desde la raíz del repositorio:

```powershell
# Windows
.\mvnw.cmd clean install

# Linux / macOS / Git Bash
./mvnw clean install
```

`clean install` compila los 11 módulos, ejecuta las pruebas y deja los artefactos en el repositorio
local de Maven. Para empaquetar sin pruebas:

```powershell
.\mvnw.cmd clean package -DskipTests
```

## Probar

```powershell
# Toda la suite (336 pruebas)
.\mvnw.cmd test

# Un módulo concreto y sus dependencias
.\mvnw.cmd -pl backend/api-core -am test

# Una clase concreta
.\mvnw.cmd -pl backend/api-rest -am test "-Dtest=ClaveRestControllerTest"
```

Los reportes quedan en `backend/<módulo>/target/surefire-reports/` y la cobertura (cuando corre
`verify`) en `target/site/jacoco/`. La suite usa JUnit 5, Mockito, MockMvc y H2; **no** requiere
MySQL. Distribución actual: `api-core` 191, `api-auth` 69, `api-rest` 72 y `api-test` 4.

## Ejecutar

La aplicación arranca desde `platform/application` (único módulo ejecutable):

```powershell
.\mvnw.cmd -pl platform/application -am spring-boot:run
```

Alternativa con el jar ya construido:

```powershell
.\mvnw.cmd clean package -DskipTests
java -jar platform/application/target/application-0.0.1-SNAPSHOT.jar
```

Con el `APP_PREFIX` habitual (`/posgrados-project`):

| Recurso | URL local |
|---|---|
| API | `http://localhost:8080/posgrados-project` |
| Health | `http://localhost:8080/posgrados-project/actuator/health` |
| Swagger UI | `http://localhost:8080/posgrados-project/swagger-ui.html` |
| OpenAPI | `http://localhost:8080/posgrados-project/api-docs` |

### Login y consulta autenticada

```bash
curl -X POST http://localhost:8080/posgrados-project/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"<usuario>","password":"<clave>","requestedRole":"SUPER_ADMINISTRADOR"}'
```

La respuesta trae `accessToken` y `refreshToken`. El `requestedRole` debe coincidir con el rol del
usuario; si no, responde `403`. Para una consulta autenticada:

```bash
curl -H "Authorization: Bearer <accessToken>" \
  http://localhost:8080/posgrados-project/api/dev/endpoint/programa/listall
```

Rutas útiles:

| Familia | Prefijo | Origen |
|---|---|---|
| CRUD y catálogos | `/api/dev/endpoint` | `ufps.edu.co.controllers.rest` |
| Casos de uso | `/api/application/case` | `ufps.edu.co.controllers.cases` |
| Autenticación | sin prefijo (`/auth`) | `ufps.edu.co.controllers.auth` |

`api.rest.command-prefix=/api/application/command` está declarado, pero el orden en que
`ApiRestConfiguration` registra los prefijos hace que `controllers.cases.command` quede bajo
`/api/application/case` (p. ej. `/api/application/case/cohorte/createWithPlazo`). Verifica la ruta
real antes de consumir un comando.

## Docker y despliegue

El repositorio incluye una imagen multi-etapa (`Dockerfile`) y la definición de Render
(`render.yaml`).

```powershell
docker build -t posgrados-api .
docker run --rm -p 8080:8080 --env-file .env.properties posgrados-api
```

La imagen compila con Maven y JDK 21, corre con JRE 21 Alpine como usuario `spring`, escucha en
`PORT` (8080 por defecto) y no incluye `.env.properties` ni `target/` (ver `.dockerignore`).

En Render, `render.yaml` define el servicio web `posgrados-api`, el health check
`/posgrados-project/actuator/health` y las variables de entorno; las marcadas con `sync: false` se
cargan en el panel de Render. El despliegue lo hace **Adolfo** desde la rama `render`.

## Flujo de ramas

| Rama | Uso |
|---|---|
| `dev` | Rama de integración. Las funcionalidades salen de `dev` y vuelven a `dev` por pull request. |
| `main` | Rama estable. Se fusiona desde `dev` cuando el trabajo está validado. |
| `render` | Rama desplegada (Dockerfile + `render.yaml`). Adolfo promueve y despliega desde aquí. |

Regla práctica: no trabajar directamente sobre `main` ni sobre `render`; integrar primero en `dev` y
pedir revisión. La rama `render` debe mantenerse alineada con `dev`/`main`; si va por delante, es
deuda de integración que hay que cerrar antes del siguiente despliegue.

## Pruebas de rendimiento

Están en `performance-tests/` y se ejecutan contra un MySQL local, nunca contra producción.

1. Crea una base local llamada `posgrados`.
2. Arranca el backend con la configuración de rendimiento (recrea el esquema: `ddl-auto=create`):

   ```powershell
   .\mvnw.cmd -pl platform/application -am spring-boot:run `
     "-Dspring-boot.run.arguments=--spring.config.additional-location=file:./performance-tests/config/"
   ```

3. Carga el seed (2 000 aspirantes, catálogos, cohortes y usuarios de prueba):

   ```powershell
   mysql -u root -p posgrados < performance-tests/seed/seed-perf.sql
   ```

4. Ejecuta k6:

   ```powershell
   k6 run performance-tests/k6/scenarios.js
   ```

`k6/scenarios.js` tiene 7 escenarios (login, catálogo público, cohortes, programas, aspirantes a
validar, cohortes del director y ranking), 21 minutos en total y umbrales `p95 < 3000 ms`,
`error_rate < 1 %` y `checks > 99 %`. La primera ronda registrada (`resultados/ronda1-all.json`)
terminó con 51 344 iteraciones, 0 fallos y p95 global de 120 ms. Los resultados se guardan en
`performance-tests/resultados/ronda1-all.json`. `config/application-perf.properties` contiene
credenciales locales de ejemplo; **no lo apuntes a Railway ni a ningún esquema de producción**.

## Problemas frecuentes

| Síntoma | Causa / solución |
|---|---|
| `Could not resolve placeholder '...'` | Falta una variable de entorno o el `.env.properties`. |
| `Invalid boolean value '"true"'` o `Cannot load driver class: "..."` | El `.env.properties` tiene los valores entre comillas. Java Properties no las elimina: escribe `ENABLE_API_DOCS=true` y `DB_DRIVER=com.mysql.cj.jdbc.Driver`, sin comillas. |
| `Context path must start with '/' and not end with '/'` | `APP_PREFIX` está entrecomillado o tiene barra final; usa `APP_PREFIX=/posgrados-project`. |
| `app.module.name.xxx is required` | Falta un `APP_MODULE_NAME_*`; el arranque es fail-fast. |
| `Table '...' doesn't exist` | El esquema no existe o no está migrado: `ddl-auto=none` y no hay migraciones versionadas en el repo. |
| `401` / `403` al consumir la API | Token ausente/vencido o `requestedRole` distinto del rol real del usuario. |
| Avisos de JaCoCo `Unsupported class file major version 68` | Estás ejecutando con JDK 24; usa JDK 21. |
| CORS o redirecciones incorrectas detrás de Render/Railway | Revisa `server.forward-headers-strategy=framework` y los orígenes permitidos en `CorsConfiguration`. |

## Documentación adicional

- `BOTS`: mapa operativo resumido (módulos, capas, rutas y seguridad).
- `.agents/`: capa de conocimiento para agentes (arquitectura, entorno, referencia por módulo,
  decisiones y estado del trabajo).
- `.info/`, `notas.md`, `Tasks.txt` y los `SC-*.md`: documentación histórica; puede estar
  desactualizada. Si contradice al código, manda el código.

## Autores

- Gabriel Pinto — UFPS
- Adolfo Alejandro Arenas Ramos — UFPS
