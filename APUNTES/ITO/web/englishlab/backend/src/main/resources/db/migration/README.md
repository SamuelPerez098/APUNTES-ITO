# Migraciones Flyway

Las tablas se crean **solo** con Flyway (`spring.jpa.hibernate.ddl-auto=validate`).
Nunca editar una migracion ya fusionada a `main`: crear una nueva version.

## Reparto de versiones (evita choques)

| Version                        | Dueno/a | Tablas                                                                |
|--------------------------------|---------|-----------------------------------------------------------------------|
| V1__schema_auth.sql            | A       | roles, users, password_reset_tokens, revoked_tokens                   |
| V2__schema_content.sql         | B       | objectives, objective_prerequisites (N:M), activities (content jsonb) |
| V3__schema_learning.sql        | C       | attempts, learner_skill_state, assignments, notifications_log         |
| V4__categories.sql             | B       | categories, activities.category_id (FK)                               |
| V13__seed_activities_extra.sql | B       | amplia el banco: 10 actividades por objetivo (ids 16-120)             |
| V14__seed_activities_obj1_3.sql | B      | amplía el banco de los objetivos 1–3 a 100 por objetivo, ids 121–390 |
| V15__seed_activities_obj4_6.sql | B      | amplía el banco de los objetivos 4–6 a 100 por objetivo, ids 391–660 |
| V16__seed_activities_obj7_9.sql | B      | amplía el banco de los objetivos 7–9 a 100 por objetivo, ids 661–930 |
| V17__seed_activities_obj10_12.sql | B    | amplía el banco de los objetivos 10–12 a 100 por objetivo, ids 931–1200 |
| V18__b2_objectives_previous_mastery.sql | A/C | objetivos B2 (PASSIV, REPSP), prerrequisitos 13←10 y 14←7, columna `learner_skill_state.previous_mastery` |
| V19__seed_activities_obj13_14.sql | A/C | amplía el banco de los objetivos 13–14 a 100 por objetivo, ids 1201–1400 |
| V10 / V11 / V12                | A/B/C   | seeds de cada modulo                                                  |

Los seeds usan V10+ para no intercalarse con el esquema. V4 se aplica despues de V3
y antes de los seeds: la columna `activities.category_id` se agrega via `ALTER TABLE`
(nunca editando V2).

## Estado actual

Creados: `V1`-`V4` (esquema) y `V10`-`V19` (seeds).
`V13` amplia el banco de actividades para que cada objetivo tenga 10 (ids 16-120),
de modo que la practica libre no repita siempre la misma pregunta.
Ver `docs/er-diagram.md` (diagrama ER) y `docs/README-evaluacion.md` (credenciales del
seed, solo para evaluacion).

## Convenciones PostgreSQL

- PK: `bigint generated always as identity`.
- Fechas: `timestamptz`. Contenido de actividad: `jsonb`.
- Email unico case-insensitive: `create unique index ux_users_email on users (lower(email));`.
- FK con nombre y `on delete` explicito (`cascade` o `set null` segun el caso).
- Indices `ix_<tabla>_<col(s)>` en toda FK y en columnas de filtro (level, skill, type, teacher_id).
- `learner_skill_state`: PK compuesta (user_id, objective_id).
  `objective_prerequisites`: PK compuesta + `CHECK (objective_id <> prerequisite_id)`.
- `activities.category_id` es nullable con `ON DELETE SET NULL`.
- La clave de respuesta vive en `activities.content` (jsonb); nunca viaja al navegador
  (el DTO de estudiante la omite).
- `roles` contiene exactamente ADMIN, TEACHER, STUDENT.
