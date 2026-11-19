# Contrato de API - English Lab

Estado: **backend de Inc 2-3 implementado y verificado** (67/67 tests + curl por rol en vivo; auth Inc 1 igualmente verificada con `mvn test` y `curl`); pendientes SOLO el click-through del frontend (backend verificado; en navegador pendiente de re-prueba con practica libre), Postfix/VPS y SMS/WhatsApp. El resto del documento sigue siendo el baseline acordado para Inc 1-3; cambiar este contrato obliga a avisar a los 3 integrantes.

## Base URL

```
/api/v1
```

Produccion: `https://<dominio>/api/v1` (Nginx termina en HTTPS y reenvia al backend en el puerto 8080).

---

## Implementado en Inc 0

| Metodo | Ruta | Auth | Respuesta |
|--------|------|------|-----------|
| GET | `/api/v1/health` | ninguna (publico) | `200 {"status":"ok"}` |

- Cualquier fallo no previsto responde `500` generico, sin stacktrace al cliente (se loguea en el servidor).
- No hay mas endpoints en este incremento. Auth, intentos, recomendaciones y asignaciones llegan en Inc 1+.

---

## Convenciones del contrato (baseline Inc 1-3)

**Prefijo.** Todas las rutas van bajo `/api/v1/...`. Nada fuera de ese prefijo.

**Autenticacion.** JWT en Bearer:

```
Authorization: Bearer <jwt>
```

Claims minimos:

- `sub`: id del usuario (String).
- `role`: uno de `ADMIN`, `TEACHER`, `STUDENT`.
- `jti`: UUID unico para revocacion.
- `exp`: expiracion.

Sin token o con token invalido/revocado → `401`. Con token valido pero sin permiso → `403`.

**Errores.** Uniformes, con `ProblemDetail` via `@RestControllerAdvice`:

- `400` cuerpo mal formado o validacion de campos (con `fieldErrors`).
- `401` sin autenticacion o token revocado (logout invalida en `revoked_tokens`).
- `403` rol insuficiente (la cadena de seguridad lo genera: `AuthenticationEntryPoint` / `AccessDeniedHandler`).
- `404` recurso inexistente o sin pertenencia al usuario.
- `422` regla de negocio violada (p. ej. alumno ya vinculado, actividad no asignada).
- `500` generico, sin detalles al cliente.

**Paginacion y filtros.** Siempre en servidor: parametros `page`, `size` (tope 100) y `sort` con lista blanca de campos; respuesta `{content, page, size, totalElements, totalPages}`. Los filtros (rol, texto, nivel, habilidad, tipo) via parametros opcionales aplicados en la consulta, jamas filtrando en memoria.

**DTOs.** Entrada y salida siempre con DTOs (records). Nunca se exponen entidades JPA, hashes de contrasena ni la clave de respuesta de una actividad antes de evaluar.

---

## Auth (Inc 1)

Autenticacion por JWT Bearer (HMAC-SHA256). El registro publico crea siempre `STUDENT`; los roles `TEACHER` y `ADMIN` los asigna un admin (Inc 2, `/admin/users`).

| Metodo | Ruta | Auth | Respuesta |
|--------|------|------|-----------|
| POST | `/api/v1/auth/register` | publico | `201` `{token, user}` |
| POST | `/api/v1/auth/login` | publico | `200` `{token, user}` |
| POST | `/api/v1/auth/logout` | Bearer | `204` (token pasa a `revoked_tokens`) |
| POST | `/api/v1/auth/forgot-password` | publico | `200` generico (Inc 1 loguea el token; Inc 3 lo envia por correo) |
| POST | `/api/v1/auth/reset-password` | publico | `200` (token de un solo uso, con expiracion) |

Cuerpos:

- `register`: `{ name, email, password }` -> `201 { token, user }` donde `user = { id, name, email, role }` (nunca el hash). `400` si la clave no cumple la politica; `422` si el email ya existe.
- `login`: `{ email, password }` -> `200 { token, user }`; `401` credenciales invalidas.
- `logout`: invalida el token actual en `revoked_tokens`. `401` si el token ya estaba revocado/invalido.
- `forgot-password`: `{ email }` -> `200` siempre (no revela si el email existe). Genera un token de un solo uso con expiracion en `password_reset_tokens`.
- `reset-password`: `{ token, newPassword }` -> `200`; `400` clave debil; `422` token invalido/expirado/usado.

Politica de contrasena: minimo 8 caracteres, al menos una mayuscula, un numero y un caracter especial.

Token JWT: claims `sub` (id del usuario), `role` (ADMIN|TEACHER|STUDENT), `jti` (UUID unico para revocacion) y `exp`. Secreto via `JWT_SECRET` (sin default en codigo ni en el `.example`); expiracion via `JWT_EXPIRATION_MS` (default 3600000).

---

## Inc 2-3 (backend administrativo, contenido y aprendizaje)

**Convención de paginación (todas las listas):** Request: `page` (0-based), `size` (default 20, máx 100), `sort` con lista blanca por endpoint (`campo,asc|desc`); filtros opcionales por query param aplicados SIEMPRE en la consulta (nunca en memoria). Response: `{ content: [...], page, size, totalElements, totalPages }`.

**ADMIN usuarios — `/api/v1/admin/users` (rol ADMIN):**
- GET `?page&size&sort=id|name|email|createdAt&role=ADMIN|TEACHER|STUDENT&text=<parcial nombre/email>` → 200 PageResponse de `{id, name, email, role, teacherId, teacherName, phone, createdAt}`
- POST `{name, email, password, role, phone?}` → 201; 400 validación; 422 email duplicado
- PUT `/{id}` `{name?, email?, role?, phone?, teacherId?, password?}` → 200; 404; 422 duplicado o teacherId que no es TEACHER
- DELETE `/{id}` → 204; 404 (baja dura: cascadas de V1-V3)

**ADMIN categorías — `/api/v1/admin/categories` (la rúbrica v4 exige CRUD completo de categorías; el modelo suma la tabla `categories` y la columna `activities.category_id`, FK):**
- GET `?page&size&sort=name&text=<parcial nombre>` → 200 página de `{id, name, description}` (lectura ADMIN y TEACHER; filtro `text` aplicado en la consulta, nunca en memoria)
- POST `{name, description?}` → 201 `{id, name, description}` (`name` obligatorio, `description` opcional); 400 validación; 422 nombre duplicado
- PUT `/{id}` → 200 `{id, name, description}`; 404; 422 — DELETE `/{id}` → 204; 404 (escritura solo ADMIN; al borrar, `activities.category_id` queda null)

**Contenido — `/api/v1/content/activities` (ADMIN y TEACHER):**
- GET `?page&size&sort=title&level=A1|A2|B1|B2&skill=grammar|vocabulary&type=MULTIPLE_CHOICE|FILL_BLANK&categoryId=&text=` → 200 PageResponse de `ActivityAdminDto` (contenido completo incl. `answer`/`accepted`: solo gestores)
- POST `{objectiveId, categoryId?, type, level, skill, title, content{...}}` → 201; 400 si `content` no coincide con `type`; 404 objectiveId/categoryId
- PUT `/{id}` → 200; 404 — DELETE `/{id}` → 204; 404

**Teacher estudiantes — `/api/v1/teacher/students` (TEACHER):**
- POST `{email}` → 200 alumno vinculado; 404 email no existe; 422 no es STUDENT; 422 ya tiene profesor
- GET `?page&size&sort=name&text=` → 200 PageResponse SOLO de SUS alumnos (`teacher_id` = id del token), sin N+1

**Núcleo adaptativo y asignaciones:**
- POST `/api/v1/attempts` `{activityId, answer, assignmentId?}` → STUDENT → 200 `{correct, score, solution, explanation, mastery}` (la clave NUNCA viaja antes de calificar; en MULTIPLE_CHOICE `solution` devuelve el ID de la opción correcta —reutilizable directamente como `answer`—; en FILL_BLANK devuelve el texto aceptado); campo opcional `"mode":"NORMAL"|"CHALLENGE"` (default NORMAL) — en CHALLENGE la puntuación es correcta=1.0, incorrecta=-0.5 (penaliza adivinar), sin responder (answer vacío, p. ej. timeout del RETO)=0.0 con solution/explanation incluidos; el score entra tal cual al dominio 0.65/0.35; answer vacío en NORMAL sigue dando 400. Soporte del modo RETO del frontend (30s/20s/10s por pregunta).
- GET `/api/v1/recommendations/next` → STUDENT → 200 `{objectiveId, activityId, reason, message}`; si todo está dominado devuelve el mismo shape con todos los campos en null salvo `message`: `{objectiveId: null, activityId: null, reason: null, message: "..."}`
- GET `/api/v1/me/objectives` → STUDENT → 200 lista `{objectiveId, code, name, cefrLevel, skill, mastery, previousMastery, state}` (state = MASTERED|AVAILABLE|BLOCKED|RECOMMENDED); 14 objetivos (B2: COND2, PASSIV, REPSP). `previousMastery` = dominio antes del último intento (null = sin sesiones previas).
- GET `/api/v1/me/activities/{id}` → STUDENT → 200 `{id, type, level, skill, title, prompt, options:[{id,text}]}` (`options` null en FILL_BLANK); 404 si no existe; 403 para otros roles; JAMÁS `answer`/`accepted`/`explanation`. Nota: es el soporte del botón "Practicar lo recomendado".
- GET /api/v1/me/objectives/{objectiveId}/activities → STUDENT → 200 PageResponse de {id, title, type, level, skill} (sin prompt/options ni clave; el detalle llega con GET /me/activities/{id}); 404 objetivo inexistente; 422 si el objetivo está BLOCKED ("El objetivo está bloqueado por prerrequisitos"); 403 otros roles. Soporte de la práctica libre del estudiante en objetivos desbloqueados (MASTERED/AVAILABLE/RECOMMENDED).
- GET /api/v1/me/objectives/{objectiveId}/practice-session?size=10 → STUDENT → 200 {activityIds:[...]}: hasta 10 actividades del objetivo elegidas por el servidor — primero las NUNCA intentadas por ese estudiante (orden aleatorio) y después las más antiguas, de modo que el banco no se repite hasta agotarse y luego recicla barajado; 404/422-BLOCKED/403 como la lista. Es lo que usa el botón "Practicar".
- POST `/api/v1/teacher/assignments` `{studentId, activityId}` → TEACHER → 201; 403 alumno no vinculado
- GET `/api/v1/me/assignments` → STUDENT → 200 (incluye `ActivityStudentDto` SIN clave)
- GET `/api/v1/teacher/students/{id}/progress` → TEACHER → 200 dominio por objetivo; 403 si el alumno no es suyo

**Notas de comunicación:** forgot-password enviará correo con JavaMailSender cuando SMTP esté configurado (`spring.mail.*` por variables de entorno, sin secretos en el repo) y registrará el envío en `notifications_log` (SENT/FAILED; `PENDING` cuando el envío de correo está deshabilitado y el intento se persiste sin enviar). El token de recuperación NO se loguea salvo `app.auth.log-reset-token=true` (default false). WhatsApp al profesor después con Twilio + `notifications_log`.

---

## Plan de migraciones Flyway

`backend/src/main/resources/db/migration`. Ya existen y están aplicadas: **V1-V4** (esquema), **V10-V19** (seeds; V18 añade además la columna `previous_mastery`), validadas por Flyway en cada arranque.

| Version | Contenido | Tablas | Dueño |
|---------|-----------|--------|-------|
| V1 | Auth | `roles`, `users`, `password_reset_tokens`, `revoked_tokens` | A |
| V2 | Contenido | `objectives`, `objective_prerequisites`, `activities` | B |
| V3 | Aprendizaje | `attempts`, `learner_skill_state`, `assignments`, `notifications_log` | C |
| V4 | Categorías | `categories` y columna `activities.category_id` (FK a `categories`) | B |
| V10 | Seed de objetivos | datos semilla | A |
| V11 | Seed de actividades | datos semilla | B |
| V12 | Seed de usuarios/pruebas | datos semilla | C |
| V13 | Seed extra de actividades — ~10 por objetivo para la práctica libre | A/C |
| V14-V17 | Seeds extra de actividades — 100 por objetivo (banco de 1.200) para la práctica sin repetición | A/C |
| V18 | Objetivos B2 (PASSIV, REPSP) + columna previous_mastery (delta de dominio por sesión) | A/C |
| V19 | Seed actividades de PASSIV y REPSP — 100 por objetivo (banco total 1.400) | A/C |

Notas:

- `ddl-auto=validate`: el esquema lo manda unicamente Flyway.
- `revoked_tokens` es la unica mecanica de logout (sin `token_version`).
- Cada integrante solo toca la version que le corresponde; si necesitas una columna nueva, se agrega una version V13+ en vez de editar una migracion ya aplicada.
- `V4__categories.sql` ya existe y está aplicada (tabla `categories` + `activities.category_id`).

---

## Roles

| Rol | Alcance |
|-----|---------|
| `ADMIN` | Gestion global: usuarios, configuracion, ver todo. |
| `TEACHER` | Ve y gestiona solo a sus alumnos vinculados; crea asignaciones. |
| `STUDENT` | Ve solo sus propios datos, intentos y recomendaciones. |

---

## Resumen: implementado vs planificado

- **Implementado (Inc 0):** `GET /api/v1/health`.
- **Implementado (Inc 1):** auth completa (`## Auth (Inc 1)`: register, login, logout, recuperacion de clave), verificada con `mvn test` y `curl`.
- **Implementado (Inc 2-3):** backend administrativo (usuarios, categorias, actividades), contenido, teacher estudiantes, nucleo adaptativo (práctica libre del estudiante incluida), asignaciones y seeds, verificado con 67/67 tests y curl por rol en vivo; ver `## Inc 2-3 (backend administrativo, contenido y aprendizaje)`. Pendientes SOLO: click-through del frontend, Postfix/VPS y SMS/WhatsApp.
- **Planificado (Inc 1+):** toda la coleccion Bruno con evidencias de errores.
