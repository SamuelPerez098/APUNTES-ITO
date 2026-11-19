# English Lab — Spec de interfaz (desde prototipo Figma HTML)

Fuente: `Figma/English_Lab_Figma_Actualizado_Equipo15/English_Lab_Figma_Actualizado.html` (en adelante HTML)
Guía: `Figma/English_Lab_Figma_Actualizado_Equipo15/English_Lab_Instrucciones_Para_Agentes.md` (en adelante Guía)

## 1. Lista de pantallas (16 vistas, array `screens` HTML:33–38, roles `roleGroups` HTML:39)

| Vista (hash) | Ruta React propuesta | Rol |
|---|---|---|
| `#login` | `/login` | público |
| `#register` | `/registro` | público (estudiante) |
| `#forgot` | `/recuperar` | público |
| `#reset` | `/restablecer` | público |
| `#student-home` | `/estudiante/inicio` | STUDENT |
| `#catalog` | `/estudiante/tareas` | STUDENT |
| `#exercise` | `/estudiante/actividad/:id` | STUDENT |
| `#result` | `/estudiante/resultado/:intentoId` | STUDENT |
| `#progress` | `/estudiante/progreso` | STUDENT |
| `#teacher-home` | `/profesor/inicio` | TEACHER |
| `#bank` | `/actividades` (compartida) | TEACHER + ADMIN |
| `#editor` | `/actividades/nueva` y `/actividades/:id/editar` | TEACHER + ADMIN |
| `#assign` | `/asignaciones/nueva` | TEACHER + ADMIN |
| `#teacher-results` | `/resultados` | TEACHER + ADMIN |
| `#admin-users` | `/admin/usuarios` | ADMIN |
| `#admin-categories` | `/admin/categorias` | ADMIN |

Menús por rol: `roleMenus` HTML:90–94 (admin no ve "home", entra directo al banco). Vistas compartidas: Guía:8 y 19.

## 2. Tokens de diseño (:root HTML:9 y CSS HTML:10–16, 21–23)

- Color fondo app: `--sand #D0C9B2` · tarjetas: `--paper #E4DEC9` · panel claro: `--cream #F7F3EA` · blanco: `--white #FFFCF6` · texto: `--graphite #504C40` · acento/errores: `--rust #A64C40` · ok/progreso: `--olive #75836C` · secundario: `--muted #858071` · líneas: `--line #c4bda8` · sombra: `--shadow 0 14px 36px rgba(55,48,32,.10)`.
- Badges: verde `#3c5a31`/`#dce6d5`, rojo `#8b3e34`/`#f5ddd7`, gris `#e7e1d3`/`#666050`, dorado `#f2e6c9`/`#7e6027` (HTML:14). Notice ok `#e8ecdf`, warn `#f3e1db` (HTML:16).
- Tipografía: cuerpo `'IBM Plex Sans', Inter, system-ui` 15px (HTML:10). Títulos/números en Georgia serif: `page-title` clamp(27px,3.1vw,42px), `stat-number` 38px, `auth-art h2` 45px, `modal h2` 26px (HTML:13,15,16).
- Espacios y radios: canvas 38px padding (23px en móvil), card padding 24px radio 7px, `.btn` min-height 41px radio 5px, `.input` height 43px radio 4px fondo `--cream` borde `#bdb49f`, focus outline 2px `#b8a879`, chips radio 40px, badge radio 3px, `table-wrap` radio 6px, modal padding 28px radio 8px, sidebar 258px fondo `--graphite`, topbar 73px fondo `rgba(247,243,234,.82)`.
- Animación de entrada de pantalla: `@keyframes appear` 0.15s (HTML:12).

## 3. Componentes repetidos (CSS en líneas del `<style>` HTML:7–25)

- **Shell interno** (sidebar + workspace): CSS HTML:11, render en `render()` HTML:96. Marca `brand`+`brand-mark`, `side-foot`. Menú por rol: `navMenu()` HTML:95.
- **Topbar**: `.topbar/.crumb/.chip/.toggle-role` CSS HTML:12; acciones "Perfil"/"Salir" HTML:96.
- **Card**: `.card`, variantes `.light/.dark`, `.card-title/.card-sub/.stat-label/.stat-number` HTML:13.
- **Botones**: `.btn` + `.alt/.rust/.olive/.small/.wide`, `.btn-row` HTML:14 (helper `btn()` HTML:50).
- **Badge**: `.badge` + `.green/.red/.gray/.gold` HTML:14 (helper `tag()` HTML:51).
- **Formularios**: `.form-label/.input/.textarea/.help/.error/.form-2` HTML:15; `.form-status` HTML:21; helpers `input()` HTML:53. Auth: `.auth-page/.auth-zone/.auth-art/.auth-form/.auth-links` HTML:15, layout `authArt()/authLayout()` HTML:55–56.
- **Tabla + paginación**: `.table-wrap/table/th/td/.filters/.pagination` HTML:16; helper `table()` HTML:65, paginación en `pageCatalog` HTML:67 y `pageTeacherResults` HTML:84.
- **Tabs**: `.tabs/.tab` HTML:16, helper `tab()` HTML:64.
- **Modal**: `.overlay/.modal` HTML:16, `renderModal()` HTML:97 (role="dialog", aria-modal).
- **Toast**: `.toast` HTML:16, `toast()` HTML:98 (auto-cierra a los 3.8s).
- **Progreso/meters**: `.progress-track/.progress-fill/.meter/.barline` HTML:16.
- **Ejercicio**: `.exercise/.answer-well/.word-bank/.word/.choice/.notice` HTML:16; `words()` HTML:68, `pageExercise()` HTML:69–70.
- **Listas**: `.list-item/.panel-row/.divider/.smallprint` HTML:16–17.

## 4. Navegación entre pantallas

- Las 4 vistas de acceso (`login/register/forgot/reset`, lista `authViews` HTML:99) se renderizan **sin menú ni topbar** (`render()` HTML:96); enlaces entre ellas en HTML:57–60 (en React: rutas `/login ↔ /registro ↔ /recuperar ↔ /restablecer`).
- Login **sin selector de rol** (Guía:10 y 28). En el HTML el rol se deduce en cliente (`initRole` HTML:46 desde `?rol=` o prefijo de vista; `roleOf()` HTML:100 al navegar) — es solo simulación. En React el rol viene del backend al autenticar (JWT) y vive en `AuthContext`, con rutas protegidas por rol; la seguridad real está en Spring Security (Guía:27).
- `#hash` y `?rol=` son **facilidades de previsualización**, no control de acceso (Guía:19, lógica HTML:42–46, 124). Tras login → redirigir al panel según rol (student/teacher/admin con menús de `roleMenus` HTML:90–94).
- Navegación interna: `navigate()` HTML:101–107 empuja hash y resetea estado; en React esto lo sustituye React Router + estado de sesión.

## 5. Diferencias Figma vs reglas AGENTS.md / react-ui-rules

- **alert/confirm(): NO se usan.** Confirmaciones vía modal `data-modal` (HTML:97) y avisos vía toast (HTML:98). Cumple la prohibición; React debe portarlo como `ConfirmModal` + toasts, sin introducir alert/confirm.
- **Errores bajo cada input: NO cumplen.** El prototipo valida al enviar con **toast** ("Las contraseñas no coinciden", "Revisa los requisitos de tu contraseña", HTML:128–133) y muestra "Servicio no disponible" genérico (HTML:133). Existe `.error` pero vacío (`pageEditor` HTML:80) y `.form-status` definido sin uso (HTML:21). React debe validar en onChange/onBlur con mensaje bajo cada input y regla de contraseña 8+/mayúscula/número/especial (HTML:131 ya la tiene como regex).
- **Responsive: SÍ.** Breakpoints: `970px` (HTML:17) — sidebar 258→212px, g3/g4 a 2 columnas, auth-zone a 1 columna ocultando `auth-art`; `700px` (HTML:18) — sidebar colapsa a nav horizontal, g2/g3/g4/form-2 a 1 columna, filtros full-width, canvas 23px/14px; extra `850px` para `.assign-card` (HTML:23).
- **Altas de usuario y categoría: no existen en el Figma** (Guía:33): `pageAdminUsers` (HTML:85) y `pageAdminCategories` (HTML:87) no tienen botón "Crear". Decisión cerrada: la rúbrica v4 manda y exige CRUD completo — ver sección 6 con el detalle accionable.
- **Datos hardcodeados**: `data` con actividades/estudiantes (HTML:41), stats fijas (HTML:62, 74, 76, 86) y "Página X de 2" con paginación en memoria (`data-page`, HTML:109). React debe consumir la API con paginación/filtros server-side (Guía:29; react-ui-rules).
- **Calificación en cliente**: `check()` HTML:108 compara respuestas hardcodeadas. En React la evaluación es en servidor y la clave nunca viaja al navegador antes de evaluar (Guía:30; skill grading-tests).
- **Enlaces a archivos sueltos**: `recuperar.html`, `registro.html`, `index.html` (HTML:57–59) deben convertirse en rutas SPA.
- Falta en el prototipo: spinner de carga, estado de error de red con reintento, navbar con avatar/iniciales y perfil real ("Perfil" solo abre modal genérico HTML:96) — componentes exigidos por react-ui-rules que React debe añadir sin cambiar paleta ni layout.

## 6. Pantallas y botones faltantes en el Figma (módulo admin CRUD — integrante B frontend)

Decisión cerrada: la rúbrica v4 manda sobre el Figma. CRUD completo (alta, edición, baja) de usuarios, categorías y actividades, con formulario en modal, error bajo cada input (react-ui-rules) y ConfirmModal para borrar. Reutilizar la estética existente, no inventar otra: modal `.overlay/.modal` HTML:16 + `renderModal()` HTML:97, campos `.form-label/.input/.textarea/.error` HTML:15, badges HTML:14.

### 6.1 Usuarios (`pageAdminUsers` HTML:85)
- **Falta "Crear usuario"**: botón en la barra de filtros (HTML:86), alineado a la derecha junto al select de roles; variante `.btn` primaria graphite (la misma familia que "Filtrar", que es `.btn alt small`).
- **Alta/edición en modal**: formulario dentro de `.modal` (HTML:16, 97) con `.form-2` (HTML:15) para nombre/correo, select de rol, contraseña con `.help` de reglas y `.error` bajo cada input. Submit con `.btn wide` (HTML:14).
- **Baja (pendiente de definir con backend)**: hoy solo hay "Desactivar" que abre confirm modal (HTML:85 → `data-modal` → HTML:97) y el estado Activo/Inactivo con badge green/red (HTML:85, 14) sugiere soft-delete. La rúbrica exige baja: proponer ConfirmModal — desactivar como baja lógica (soft-delete) y, si el backend exige eliminación definitiva, botón "Eliminar" `.btn rust small` con ConfirmModal destructivo. Confirmar contrato con el equipo backend antes de implementar.

### 6.2 Categorías (`pageAdminCategories` HTML:87)
- **Falta "Crear categoría"**: botón sobre la tabla (vista sin filtros, HTML:87); variante `.btn` primaria, misma posición que en usuarios (barra superior derecha).
- **ConfirmModal para borrar**: el botón "Eliminar" `.btn rust small` ya abre `data-modal` (HTML:87 → 97); conservarlo, pero el Aceptar debe llamar a la API real (hoy el modal solo muestra toast "no disponible", HTML:109). Alta/edición en el mismo `.modal` con formulario de nombre + descripción y `.error` bajo cada input.

### 6.3 Actividades (banco HTML:77–78, editor HTML:79–80)
- **Baja ya cubierta**: el banco (HTML:78) tiene "Editar" (`.btn small alt`) y "Eliminar" (`.btn small rust` con `data-modal` → ConfirmModal HTML:97). Solo falta conectar el Aceptar a la API y mostrar `.error`/toast de fallo.
- **Falta "Crear actividad" en el banco**: la alta solo se alcanza desde el home del profesor ("Crear actividad", `.btn olive`, HTML:76). Añadir botón "Crear actividad" (`.btn` o `.btn olive small`) en la barra de filtros del banco (HTML:78), coherente con "Filtrar".
- **Editor a pantalla completa ≠ "formulario en modal"**: `pageEditor` (HTML:80) es una página completa (`.card light` en `.canvas`). Para usuarios/categorías el formulario va en modal (sección 6.1–6.2). Para actividades, mantener el editor en pantalla completa (es el layout aprobado del Figma, Guía:25) y usar ConfirmModal para borrar; si el equipo decide exigir modal también aquí, sería un ajuste frente al Figma que debe aprobarse explícitamente.
- **Validación**: el editor ya tiene un `.error` vacío (HTML:80); React debe mostrar el error bajo CADA input mientras se escribe, no un solo bloque al final (react-ui-rules).
