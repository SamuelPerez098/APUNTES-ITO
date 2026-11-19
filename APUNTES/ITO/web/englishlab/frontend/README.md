# English Lab — Frontend (Equipo 15)

Interfaz React + Vite (JavaScript) de English Lab. Paleta, tipografías y layout
portados **exactamente** del prototipo Figma
(`Figma/English_Lab_Figma_Actualizado_Equipo15/English_Lab_Figma_Actualizado.html`).

## Cómo correrlo

```bash
cd frontend
npm install
npm run dev        # http://localhost:5173
npm run build      # build de producción
```

El backend Spring Boot debe estar corriendo en `http://localhost:8080`
(el dev server hace proxy de `/api/*` al backend; también se puede apuntar
directo copiando `.env.example` a `.env` y definiendo `VITE_API_URL`).

## Conectado a la API real (base `/api/v1`)

- `POST /auth/login` — login; el rol (`ADMIN|TEACHER|STUDENT`) viene en la respuesta.
- `POST /auth/register` — registro público (siempre crea estudiante); `400` con
  `fieldErrors` se pinta bajo cada input; `422` correo duplicado.
- `POST /auth/logout` — cierra sesión (el backend invalida el token).
- `POST /auth/forgot-password` — siempre responde 200 (no revela existencia).
- `POST /auth/reset-password` — requiere `?token=` del enlace; `422` token inválido,
  `400` clave débil.
- `GET /health` — disponible para comprobar conectividad.

Errores del backend (ProblemDetail) se mapean bajo el input correspondiente.
La política de contraseña se valida en cliente igual que en el backend:
8+ caracteres, una mayúscula, un número y un carácter especial.

## Mock / pendiente de fase siguiente

Estas pantallas tienen la UI del Figma pero **datos de muestra** marcados con
una nota "datos de muestra — endpoint pendiente" (no se inventa que algo se
guardó; los avisos lo dicen explícitamente):

- Estudiante: `/estudiante/*` (inicio, tareas, actividad, resultado, progreso).
- Profesor: `/profesor/inicio`.
- Compartidas: `/actividades` (banco), `/actividades/nueva` y
  `/actividades/:id/editar`, `/asignaciones/nueva`, `/resultados`.

Endpoints pendientes (NO existen aún): `/admin/users`, `/teacher/*`,
`/content/activities`, `/attempts`, `/recommendations/next`, `/me/objectives`,
`/me/assignments`.

## Patrón mock/real (fase 2a)

`src/api/useRemote.js` define la convención para paneles cuyo endpoint aún no
existe:

- La página llama a la API real (cliente con JWT) con paginación/filtros
  **server-side** (`page`, `size`, `sort` + filtros como parámetros).
- **2xx** → estado `real`: se pinta la respuesta del backend (shape
  `{content, page, size, totalElements, totalPages}`).
- **404** (endpoint aún no implementado) → estado `fallback`: se usan los datos
  de muestra de `src/mock/mockServer.js` (que emulan el MISMO shape de
  contrato) y la página muestra `<MockNotice/>`. Cuando el backend publique el
  endpoint, el panel cambia solo a datos reales.
- **401/403/5xx / fallo de red** → estado `error`: `<ErrorState/>` con
  reintento. Un fallo real NUNCA cae a mock.
- Mutaciones (`create/update/remove`) usan `mutateWithFallback()`: misma regla
  (404 → operación sobre el mock, avisando en el toast que es demo; otros
  errores → fallo real, y los `fieldErrors` del backend se pintan bajo cada
  input).

### Paneles con patrón real-con-fallback

- `/admin/categorias` — conectado a `src/api/categories.js`
  (`GET/POST/PUT/DELETE /admin/categories`, contrato de api-contract.md):
  tabla + paginación + filtro `name` en servidor, alta/edición en modal con
  errores bajo cada input y ConfirmModal para eliminar.
- `/admin/usuarios` — listado conectado a `src/api/users.js`
  (`GET /admin/users` con `page/size/sort/role/text`): tabla + paginación +
  filtros en servidor y desactivación (baja lógica) con ConfirmModal.
  El alta/edición están pendientes del contrato (botón muestra aviso).

### Paneles mock puro (sin cambio en fase 2a)

Estudiante (`/estudiante/*`), profesor (`/profesor/inicio`), compartidas
(`/actividades*`, `/asignaciones/nueva`, `/resultados`).

## Contrato asumido de `users.js` — PENDIENTE DE CONFIRMAR CON EL BACKEND

- `GET /admin/users?page&size&sort&role&text` → mismo shape paginado que
  categorías, con filas `{id, name, email, role: ADMIN|TEACHER|STUDENT, active}`.
- `role` y `text` como parámetros de filtro en servidor (`text` busca en nombre
  o correo).
- `POST /admin/users` / `PUT /admin/users/{id}`: cuerpo asumido
  `{name, email, role, password?}` (contraseña solo en alta; a confirmar si la
  edición permite cambiarla o si el backend envía token de recuperación).
- `DELETE /admin/users/{id}` asumido como **baja lógica** (soft-delete,
  `active=false`), según ui-spec 6.1; si el backend exige eliminación
  definitiva, agregar botón "Eliminar" `.btn rust small` con ConfirmModal.
- Confirmar también si el listing expone `active` o `status` y los nombres
  exactos de parámetros (`text` vs `query`/`search`).

## Decisiones

- Rutas y layout según `docs/ui-spec.md` (secciones 1–4).
- Login **sin selector de rol**: el rol lo determina el backend; React solo
  redirige al panel (`/estudiante|/profesor|/admin`) según `user.role`.
- Sesión en `AuthContext` + `localStorage` (`englishlab.auth`); interceptor
  Axios agrega `Bearer` y en `401` limpia sesión y va a `/login`.
- Prohibido `alert()/confirm()`: confirmaciones con `ConfirmModal`, avisos con
  toasts (auto-cierran a 3.8 s, como el Figma).
- Responsive con los breakpoints del Figma (970 px / 700 px).
- La seguridad real es de Spring Security; las rutas protegidas por rol son
  solo comodidad de navegación.
