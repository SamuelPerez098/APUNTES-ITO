# English Lab - Plan (texto plano para agentes)

Fuente: English_Lab_Plan_Incrementos_v3.xlsx. Si algo contradice a AGENTS.md, gana AGENTS.md.

## Decisiones que ANULAN al xlsx
- BD: PostgreSQL + Flyway (el xlsx decia MySQL; obsoleto). ddl-auto=validate.
- Nucleo adaptativo: SI entra. Logout: tabla revoked_tokens. Prefijo API: /api/v1/.
- Responsables: A, B, C = los 3 integrantes. Un agente nunca hace commits.

## Calendario (2026)
| Etapa | Fechas | Resultado |
|---|---|---|
| Inc 0 - Cimientos y contrato | 15-16 nov | Repo, tablero, VPS con HTTPS, contrato de API, esqueleto desplegado |
| Inc 1 - Auth, roles y datos | 17-18 nov | Login/registro/logout, roles y todas las tablas con seed |
| Inc 2 - Nucleo adaptativo y actividades | 18-19 nov | Usuarios, vinculo, actividades, intentos y recomendacion; Postfix instalado |
| Inc 3 - Comunicacion y paneles | 20 nov | Correo, SMS y WhatsApp reales; pantallas de estudiante y profesor |
| Inc 4 - Cierre y congelamiento | 21 nov (congela 23:59) | CONGELAMIENTO DE FUNCIONES (21 nov 23:59): Bruno, README, seguridad |
| Margen (colchon) | 22-23 nov | Margen: terminar lo atrasado, absorber lo de quien falle, ensayo |
| Entrega | 24 nov (antes de 23:59; meta 12:00) | Enviar entrega |

Puntos de control: fin del 17 y del 19 nov (commits propios, 1 PR fusionado, tablero al dia).

## Tareas  (A/B/C = responsable; N=nucleo, R=recortable)

### Inc 0 - Cimientos y contrato
- [ ] #1 [A] VPS: Java 21, PostgreSQL (NO MySQL), Nginx; verificar con SELECT version() (N)
- [ ] #2 [A] Dominio + HTTPS con Certbot -- Sin Vercel/Railway (N)
- [ ] #3 [A] Probar puerto 25 abierto y PTR/rDNS -- Si el proveedor lo bloquea, resolverlo HOY (N)
- [ ] #4 [A] Esqueleto Spring Boot como JAR con systemd, /api/v1/health y CORS (N)
- [ ] #5 [B] Figma: logo original y paleta justificada con teoria del color (N)
- [ ] #6 [B] Figma: prototipo navegable de ~8 pantallas (login, registro, objetivos y practica del estudiante, alumnos del profesor, asignar, usuarios y actividades del admin) (N)
- [ ] #7 [B] React + Vite (JavaScript): estructura, Axios con interceptor JWT, layout responsivo (N)
- [ ] #8 [B] Componentes base: modal de confirmacion, input con error bajo el campo, spinner, error de red -- Sin alert()/confirm() (N)
- [ ] #9 [B] Cuenta Twilio, verificar numeros de los 3 y SMS de prueba -- Cuenta de prueba solo envia a verificados (N)
- [ ] #10 [C] Repo publico, .gitignore antes del primer commit, application.properties.example, carpetas backend/ frontend/ bruno/ docs/ (N)
- [ ] #11 [C] Tablero GitHub Projects PUBLICO con 5 columnas y todas las tareas como issues (N)
- [ ] #12 [C] Contrato en docs/api-contract.md: endpoints, JSON, formato JWT y versiones Flyway (V1 A, V2 B, V3 C) -- Los 3, primeras 2 horas (N)
- [ ] #13 [C] Configurar Flyway con ddl-auto=validate (N)
- [ ] #14 [C] Twilio WhatsApp sandbox: unir los numeros de los 3 (N)

### Inc 1 - Auth, roles y datos
- [x] #15 [A] Flyway V1: roles, users (teacher_id, phone), password_reset_tokens, revoked_tokens -- Roles: ADMIN, TEACHER, STUDENT (N)
- [x] #16 [A] Spring Security + JWT: login, registro y logout con invalidacion de token (N)
- [x] #17 [A] BCrypt, Bean Validation en DTOs (8 car., mayuscula, numero, especial) y @ControllerAdvice (400, 401, 403, 404, 422, 500) (N)
- [x] #18 [A] @PreAuthorize por rol y endpoints de recuperacion de contrasena (token de un solo uso con expiracion) (N)
- [x] #19 [A] Seed V10: 1 admin (credenciales fijas), 2 profesores, 9 estudiantes -- Documentar admin en README (N)
- [ ] #20 [B] Login, registro y recuperacion con validaciones bajo cada input (N)
- [ ] #21 [B] AuthContext, rutas protegidas por rol y Navbar (nombre, correo, avatar por defecto, cerrar sesion) (N)
- [x] #22 [B] Flyway V2: objectives, objective_prerequisites (N:M), activities (contenido en JSON) (N)
- [x] #23 [B] Seed V11: 12 objetivos A1-B2 con prerrequisitos y 15 actividades (opcion multiple y completar) -- Pocas pero bien etiquetadas (N)
- [x] #24 [C] Flyway V3: attempts, learner_skill_state, assignments, notifications_log (N)
- [x] #25 [C] Seed V12 (10-15 registros por tabla), diagrama ER y script .sql de respaldo (N)
- [x] #26 [C] Servicio de calificacion en servidor (opcion multiple y completar) con pruebas unitarias -- La clave de respuesta nunca viaja al navegador (N)
- [x] #27 [C] Bruno: login y token JWT en ruta protegida (N)

### Inc 2 - Nucleo adaptativo y actividades
- [x] #28 [A] CRUD de usuarios (solo ADMIN) con Pageable y filtros (rol, texto) -- Paginacion en el SERVIDOR (N)
- [ ] #29 [A] Tabla de usuarios: paginacion server-side, busqueda, filtro por rol y modal de baja (N)
- [x] #30 [A] Profesor vincula estudiante por correo; listado paginado de SUS estudiantes; evitar N+1 -- Sin invitaciones con token en el MVP (N)
- [ ] #31 [A] Instalar Postfix en el VPS con SPF, DKIM y rDNS -- Empezar temprano: es lo mas riesgoso (N)
- [x] #32 [B] CRUD de actividades con DTOs y Bean Validation; DTO de estudiante sin clave de respuesta (N)
- [ ] #33 [B] Tabla de actividades con paginacion server-side y filtros (nivel, habilidad, tipo) y formulario con validaciones (N)
- [ ] #34 [B] Reproductor de actividades (opcion multiple y completar) con feedback y loading (N)
- [x] #35 [C] POST /attempts: califica en servidor, guarda intento y actualiza learner_skill_state (0.65 reciente + 0.35 historico) (N)
- [x] #36 [C] GET /recommendations/next: objetivo de menor dominio con prerrequisitos cumplidos, con motivo explicado (N)
- [x] #37 [C] GET /me/objectives: estado por objetivo (dominado, disponible, bloqueado, recomendado) (N)
- [x] #38 [C] Bruno: intento, recomendacion y errores a proposito (403, 400/422, 404) (N)

### Inc 3 - Comunicacion y paneles
- [ ] #39 [A] JavaMailSender: correos de bienvenida y recuperacion; verificar llegada a bandeja de entrada (N)
- [ ] #40 [A] Perfil: editar nombre/telefono y cambiar contrasena (R)
- [x] #41 [A] Pruebas de autorizacion: un profesor no ve alumnos ajenos; un estudiante solo sus datos (R)
- [ ] #42 [B] Pantalla del estudiante: objetivos con estado y boton 'Practicar lo recomendado' (N)
- [ ] #43 [B] Pantalla del profesor: sus alumnos, dominio por objetivo y modal para asignar actividad (N)
- [ ] #44 [B] SMS al estudiante al crear una asignacion, con feedback visual de exito/error -- Accion real del profesor (N)
- [x] #45 [C] POST /teacher/assignments y GET /me/assignments (solo alumnos vinculados) (N)
- [x] #46 [C] GET /teacher/students/{id}/progress: dominio por objetivo de un alumno (N)
- [ ] #47 [C] WhatsApp al profesor cuando el estudiante completa una asignacion + registro en notifications_log -- Accion real del sistema (N)

### Inc 4 - Cierre y congelamiento
- [ ] #48 [A] Revision: sin secretos en repo ni historial, ddl-auto=validate, BCrypt -- Penalizacion -10 (N)
- [ ] #49 [A] Verificar despliegue final, HTTPS y reinicio de systemd; respaldo de la BD (N)
- [ ] #50 [B] Prototipo Figma final y consistencia visual con la app (N)
- [ ] #51 [B] Revision movil/escritorio y grep de alert()/confirm() (N)
- [ ] #52 [C] Coleccion Bruno completa en el repo (N)
- [ ] #53 [C] README completo (+ seccion 'Trabajo futuro' con lo de la propuesta que no entro) (N)

### Margen (colchon)
- [ ] #54 [A] Colchon: corregir pendientes/errores propios y absorber lo de quien se atrase (N)
- [ ] #55 [B] Colchon: corregir pendientes/errores propios y absorber lo de quien se atrase (N)
- [ ] #56 [C] Colchon: corregir pendientes/errores propios y absorber lo de quien se atrase (N)
- [ ] #57 [A] Ensayo de demo con los 3 roles; cada quien explica TODO el sistema (N)

### Entrega
- [ ] #58 [C] ENTREGA en Classroom: nombres, equipo, repo, URL HTTPS y URL base de la API (antes del 24 nov 23:59; meta 12:00) (N)

## Alcance MVP
- Roles ADMIN, TEACHER, STUDENT: Incluido
- Banco de actividades con editor, etiquetas y paginacion: Incluido (2 tipos: opcion multiple y completar)
- Calificacion en servidor y registro de intentos: Incluido
- Dominio por objetivo (0.65 reciente + 0.35 historico) y prerrequisitos: Incluido
- Recomendacion de la siguiente actividad: Incluido (regla simple)
- Mapa de estudio: Lista de objetivos con estado (sin grafo visual)
- Vinculo profesor-estudiante: Por correo, sin invitaciones con token
- Asignaciones del profesor: Incluido (una actividad a un alumno)
- Diagnostico inicial: Fuera: el dominio inicial es un valor por defecto
- Ordenar palabras, modo evaluacion con tiempo, pistas: Fuera
- Graficas (Recharts), reportes del admin, auditoria: Fuera
- Imagen de perfil subida por el usuario: Fuera: avatar por defecto con iniciales
- Audio/video, speaking, hotspots, Vosk, Whisper, Piper, LanguageTool, H5P, APIs de recursos: Fuera: van a 'Trabajo futuro' del README

## Migraciones Flyway
- V1 (dueno A): roles, users, password_reset_tokens, revoked_tokens
- V2 (dueno B): objectives, objective_prerequisites, categories, activities (con category_id, FK a categories) [N:M objectives-objectives (objective_prerequisites)]
- V3 (dueno C): attempts, learner_skill_state, assignments, notifications_log
- V10/V11/V12 (dueno A/B/C): Seeds de cada modulo (10-15 registros por tabla principal)
- Nota: categories se pliega en V2 (debe existir antes que activities.category_id); si V2/V3 ya se aplicaron, crear V4__categories.sql en su lugar (respaldo).

## Endpoints
- POST /api/v1/auth/login | register | logout | forgot-password | reset-password | A | Publico / autenticado
- GET|POST|PUT|DELETE /api/v1/admin/users (paginado, filtros) | A | ADMIN
- GET|POST|PUT|DELETE /api/v1/admin/categories (paginado, filtros) | B | ADMIN
- POST /api/v1/teacher/students (vincular por correo); GET /api/v1/teacher/students (paginado) | A | TEACHER
- GET|PUT /api/v1/me/profile | A | Autenticado
- GET|POST|PUT|DELETE /api/v1/content/activities (paginado, filtros) | B | ADMIN / TEACHER
- POST /api/v1/attempts | C | STUDENT
- GET /api/v1/recommendations/next | C | STUDENT
- GET /api/v1/me/objectives | C | STUDENT
- POST /api/v1/teacher/assignments | C | TEACHER
- GET /api/v1/me/assignments | C | STUDENT
- GET /api/v1/teacher/students/{id}/progress | C | TEACHER (solo vinculados)

## Contingencia
- Puntos de control: Fin del 17 nov y fin del 19 nov: cada integrante debe tener commits propios, al menos un PR fusionado y tarjetas movidas en el tablero.
- Si alguien no aparece: En el primer punto de control, hablar con esa persona y AVISAR AL PROFESOR el mismo dia. La v4 penaliza con -10 al companero por no comunicarlo, y sin commits la calificacion de quien no trabaja es 0.
- Si falta A: C toma VPS/HTTPS/systemd y JWT/roles (nucleo); B toma usuarios CRUD y Postfix/correo. Recortar: perfil, pruebas de autorizacion formales, respaldo.
- Si falta B: A toma React base, login y rutas protegidas; C toma actividades CRUD y reproductor; Figma se hace entre los dos con 5 pantallas. Recortar: filtros extra, feedback visual.
- Si falta C: A toma Flyway/seed/README; B toma Bruno y asignaciones. Recomendacion queda como 'objetivo de menor dominio' sin explicacion. Recortar: progreso por alumno detallado.
- Orden de recortes: 1) Perfil editable  2) Pruebas de autorizacion formales  3) Respaldo automatico  4) Completar espacios (dejar solo opcion multiple)  5) Motivo explicado de la recomendacion.
- Lo que NO se recorta: PostgreSQL+Flyway+seed, JWT y roles, Bean Validation y @ControllerAdvice, paginacion y filtros en servidor, Postfix/SMS/WhatsApp reales, Figma navegable, Bruno, README, tablero y commits.
- Margen: 22 y 23 nov quedan libres de funciones nuevas: solo correcciones, absorber atrasos y ensayo. Entrega el 24 por la manana.

## Progreso (los agentes actualizan esta seccion)
- #4 esqueleto Spring Boot: compila en local; falta JAR+systemd+prueba en VPS.
- #10 .gitignore creado; falta primer commit humano. #12 docs/api-contract.md creado (revisar). #13 Flyway configurado, sin migraciones aun.
- Inc 1 backend+BD (#15-#19, #22-#26): migraciones V1-V4 + seeds V10-V12 aplicadas; auth JWT completa (register/login/logout con revoked_tokens, recuperacion de clave de un solo uso); calificacion en servidor (opcion multiple y completar) con pruebas unitarias; mvn test 10/0 contra PostgreSQL real (englishlab_test) y curl de login+logout+ruta protegida verificados. Pendiente del incremento: frontend (#20-#21) y Bruno (#27); Postfix/SMS/WhatsApp y despliegue van en Inc 2-3.
- Inc 2-3 backend (#27-#28, #30, #32, #35-#38, #41, #45-#46): CRUD admin de usuarios/categorias/actividades con paginacion y filtros en servidor; vinculo profesor-alumno; nucleo adaptativo completo (/attempts con dominio 0.65/0.35, /me/objectives, /recommendations/next con motivo, asignaciones y progreso con 403 de propiedad); GET /me/activities/{id}; fix de solution en MULTIPLE_CHOICE; JavaMailSender preparado (token de recuperacion fuera de logs salvo flag dev); 54/54 tests contra PostgreSQL real y curl por rol verificado en vivo; Bruno completo con IDs fijos del seed. Frontend cableado y compilando (pendiente click-through del equipo). Pendiente: Postfix+VPS, SMS/WhatsApp (credenciales Twilio), perfil editable, README final y ensayo.
- Practica libre del estudiante (extension aprobada por el equipo): GET /me/objectives/{id}/activities (solo objetivos MASTERED/AVAILABLE/RECOMMENDED; 422 si BLOCKED; DTO resumen sin clave) + boton "Practicar" con modal en Mi progreso. Verificado con curl en vivo y 54/54 tests. El click-through local del equipo paso sin fallos; el correo real queda para Postfix en el VPS (en local, AUTH_LOG_RESET_TOKEN=true saca el token por consola).
- Banco ampliado y sesiones de practica (extension aprobada): V13__seed_activities_extra.sql lleva cada objetivo a 10 actividades (120 en el banco, ids 16-120, checksums de V1-V12 intactos); modal de Mi progreso con "Sesion de practica (10 preguntas)" en cola secuencial barajada, calificada pregunta a pregunta en servidor; fix del formulario de asignacion (ErrorState con reintentar en vez de select vacio silencioso) y aviso "Tu sesion expiro" en login al vencer el JWT. 54/54 tests y curl en vivo verificados.
- Banco a 1.200 actividades y sesiones sin repeticion (extension aprobada): V14-V17 llevan cada objetivo a 100 preguntas (ids 121-1200, cero duplicados validados por script, respuestas MC sin sesgo de posicion); GET /me/objectives/{id}/practice-session elige en servidor primero las nunca intentadas y luego recicla barajado; "Practicar" arranca la sesion de 10 directamente (sin modal), Mi progreso sin secciones de muestra y menu de estudiante sin "07 Resolver actividad"/"08 Resultado". 67/67 tests y curl en vivo verificados.
- Modo RETO y feedback depurado (extension aprobada): POST /attempts con mode NORMAL/CHALLENGE (correcta 1.0, incorrecta -0.5, sin responder 0.0 con solucion incluida; NORMAL sin cambios y vacio=400); botones Practicar/RETO en Mi progreso con temporizador 30s/20s/10s por pregunta y auto-envio al agotarse; feedback por pregunta sin tarjetas de estadisticas y cierre con solo Calificacion % y Dominio del tema % (fuera "Correctas hasta ahora" y boton "Reiniciar"). 67/67 tests y curl en vivo verificados.
- Niveles completos y grafica de progreso (extension aprobada): 2 temas B2 nuevos (PASSIV con prerrequisito PP, REPSP con prerrequisito PAST; 14 objetivos, 3+ por nivel) y V19 con 100 actividades por tema nuevo (banco 1.400); columna previous_mastery (V18) expuesta en /me/objectives para el rango de mejora/empeoramiento; Inicio del estudiante con grafica de barras por nivel (solo CSS) y menu lateral sin numeracion ni pie de marca. 67/67 tests y curl en vivo verificados.
- Ajustes de UI (aprobados): feedback de fallo/sin respuesta con tinte cafe (misma variable --coffee del menu lateral, sin cambiar el color del menu); boton flecha para salir de la actividad con ConfirmModal "Salir de la sesion" (directo en el resumen final); Inicio del estudiante sin tarjetas de stats, solo "Dominio medio: NN%" integrado inline con mini-barra (fuera "Tareas pendientes" y "Objetivos en curso"). Build del frontend en verde, sin alert/confirm.
