# Coleccion Bruno de English Lab

Esta carpeta guarda la coleccion de peticiones de la API de English Lab, hecha con
[Bruno](https://usebruno.com/) (no Postman). Los archivos `.bru` usan las secciones
`meta` / `get` / `post` / `put` / `delete` / `body` / `script`, y se agrupan en carpetas
por area de la API.

> Convenciones de la API: base `/api/v1`, autenticacion JWT Bearer, errores uniformes
> con `ProblemDetail` (400/401/403/404/422/500), paginacion y filtros SIEMPRE en servidor.
> Ver `docs/api-contract.md`.

## Como importar la coleccion

1. Abre Bruno.
2. **Open Collection** y selecciona la carpeta `bruno/englishlab/` (contiene el
   `bruno.json`). Bruno importa las carpetas `auth`, `admin`, `content`, `teacher`,
   `learning`, `errors` y el entorno de `environments/`.
3. Selecciona el entorno **englishlab-local** (esquina inferior derecha). Define
   `baseUrl=http://localhost:8080/api/v1` y deja vacios los tokens (`adminToken`,
   `teacherToken`, `studentToken`) y las credenciales (`adminEmail`, `adminPassword`,
   ...).
4. Asegurate de que el backend este levantado en `localhost:8080`.

> **Credenciales:** NO viven en esta coleccion. Estan en `docs/README-evaluacion.md`;
> copialas en el entorno de Bruno (variables vacias) antes de ejecutar. Los `.bru`
> usan variables (`{{adminEmail}}`, `{{adminPassword}}`, `{{newUserPassword}}`, ...).

## Orden de ejecucion recomendado

Los tokens del entorno (`adminToken`, `teacherToken`, `studentToken`) empiezan vacios y
se rellenan **haciendo login**. Ejecuta en este orden:

1. **`auth/health`** (publico) → 200 `{"status":"ok"}` para verificar que el backend responde.
2. **`auth/login`** con `{{adminEmail}}` + `{{adminPassword}}` (por defecto) → rellena `adminToken`.
3. Ya con `adminToken`: `admin/users-list`, `admin/categories-list`, `content/activities-list`,
   `content/objectives-list`, etc.
4. Para rellenar `teacherToken`: en el body de `auth/login` pon `"email": "{{teacherEmail}}"`
   y `"password": "{{teacherPassword}}"`, **y** cambia el target del `setEnvVar` de
   `adminToken` a `teacherToken`. Vuelve a ejecutar login.
5. Para rellenar `studentToken`: body con `{{studentEmail}}` + `{{studentPassword}}`
   y target `studentToken`. Vuelve a ejecutar login.
6. Despues, ya puedes usar `teacher/*` y `learning/*`.
7. Por ultimo ejecuta `errors/*` para capturar las evidencias de errores intencionales.

> Tip: para no pisar un token, rellena los tres haciendo login tres veces (una por rol)
> cambiando body + target del `setEnvVar`. El script de `login` guarda `res.body.token`.

## Tabla de carpetas

| Carpeta    | Requests                                                                 | Token usado        |
|------------|--------------------------------------------------------------------------|--------------------|
| `auth`     | login, register, logout, forgot-password, reset-password, health         | publico (logout: adminToken) |
| `admin`    | users-list/create/update/delete, categories-list/create/update/delete    | `adminToken`       |
| `content`  | activities-list/create/update/delete, objectives-list                    | `adminToken`       |
| `teacher`  | students-list, students-link, assignments-create, student-progress       | `teacherToken`     |
| `learning` | me-objectives, objective-activities, recommendation-next, me-assignments, attempt, attempt-wrong | `studentToken`     |
| `errors`   | 8 evidencias de error intencional (ver abajo)                            | segun el caso      |

## Evidencias de errores intencionales (`errors/`)

Cada request lleva un comentario `// CODIGO ESPERADO`. Debes rellenar el token del rol
indicado antes de ejecutarlas.

| Request | Codigo esperado | Escenario |
|---------|-----------------|-----------|
| `403-estudiante-en-admin` | **403** | `studentToken` accede a `GET /admin/users` (ruta ADMIN). |
| `403-progreso-alumno-ajeno` | **403** | `teacherToken` (teacher1) pide el progreso de student6 (id 9, de teacher2). |
| `422-email-duplicado` | **422** | `register` con `admin@englishlab.com` (email ya existe). |
| `422-estudiante-ya-vinculado` | **422** | teacher1 intenta vincular de nuevo a `student1@englishlab.com` (ya tiene profesor). |
| `422-objetivo-bloqueado` | **422** | `studentToken` pide `GET /me/objectives/12/activities`: COND2 sigue bloqueado porque su prerrequisito COND1 no esta dominado. |
| `404-actividad-inexistente` | **404** | `POST /attempts` con `activityId: 99999` (no existe). |
| `400-clave-debil` | **400** | `register` con `password: "abc"` (no cumple la politica de clave). |
| `401-token-revocado` | **401** | Ejecutar `auth/logout` (revoca `adminToken`) y luego este request con el MISMO token. |

## IDs fijos del seed

Los `.bru` llevan ids reales escritos a mano porque el seed los fija explicitamente
(`insert ... overriding system value` en V10, V11 y V12). Flyway recrea siempre la misma
base de datos con los mismos ids: no hace falta consultar las listas antes de ejecutar.

| Recurso     | Rango | Detalle |
|-------------|-------|---------|
| `users`     | 1..12 | 1 admin, 2 teacher1, 3 teacher2, 4..8 student1..5 (de teacher1), 9..12 student6..9 (de teacher2). |
| `objectives`| 1..12 | 1 PRON, 2 BE, 3 ART, 4 PS, 5 PREP, 6 PC, 7 PAST, 8 COMP, 9 MOD, 10 PP, 11 COND1, 12 COND2. |
| `activities`| 1..15 | La 1 es MULTIPLE_CHOICE A1 del objetivo PRON; su respuesta correcta es la opcion `b`. |
| `categories`| 1..6  | 6 = "Condicionales". Al borrarla, sus actividades quedan con `category_id` null. |

> Ojo: un `POST`/`PUT` si puede cambiar estos ids. Para volver al estado del seed, recrea
> la base de datos y deja que Flyway aplique las migraciones de nuevo.

## Usuarios del seed

Las credenciales de demostracion estan en `docs/README-evaluacion.md`; copialas en el
entorno de Bruno (variables vacias) antes de ejecutar.

Usuarios disponibles: `admin@englishlab.com`, `teacher1@englishlab.com`,
`teacher2@englishlab.com` y `student1@englishlab.com` .. `student9@englishlab.com`.
teacher1 tiene vinculados a student1..student5; teacher2 a student6..student9.
