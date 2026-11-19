# English Lab - Equipo 15 (reglas globales)

## Decisiones cerradas (no reabrir)
- Base de datos: PostgreSQL + Flyway (NO MySQL/MariaDB). ddl-auto=validate.
- El nucleo adaptativo SI entra (dominio 0.65/0.35, prerrequisitos, recomendacion). Ver skill adaptive-mastery.
- Logout: tabla revoked_tokens. Sin token_version.
- Alcance fuera: diagnostico, ordenar palabras, graficas, audio/video, imagen de perfil subida.
- Calendario y reparto: English_Lab_Plan_Incrementos_v3.xlsx (ver docs/plan.md). Alcance y rúbrica: propuesta v4 (.docx). Si hay conflicto, gana la rúbrica salvo las decisiones cerradas de arriba.

## Reglas
- Stack: React+Vite (JS), Java 21 + Spring Boot, PostgreSQL, JWT, Nginx, VPS Contabo.
- API: DTOs siempre, nunca entidades ni la clave de respuesta antes de evaluar.
- Paginacion y filtros en servidor. Sin N+1.
- Prohibido alert()/confirm(). Errores bajo cada input.
- Secretos fuera de Git. Mantener application.properties.example.
- No declarar algo terminado hasta probarlo en el VPS.
- Antes de trabajar, cargar la skill que corresponda (adaptive-mastery, flyway-postgres, spring-api-conventions, jwt-auth-revocation, authz-ownership, grading-tests, react-ui-rules, comms-email-sms-whatsapp, vps-contabo-deploy, repo-hygiene-git-flow, cefr-content, bruno-readme-evidence).
- Las skills de diseno externas no pueden cambiar la paleta ni el layout de Figma.
- Plan en texto plano: docs/plan.md. Leerlo en vez de los .xlsx/.docx. Al terminar una tarea, marcar su casilla y anotar en "Progreso".
- CRUD admin completo: usuarios, categorías (tabla categories) y actividades. Lo exige la rúbrica aunque el Figma no lo muestre.
