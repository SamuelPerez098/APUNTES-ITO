-- =============================================================================
-- English Lab - V18: dos objetivos B2 nuevos, sus prerrequisitos y la columna
-- previous_mastery (dominio antes del ultimo intento).
-- =============================================================================

-- Salvaguarda: en bases de desarrollo/pruebas pueden existir objetivos creados
-- por los tests con los ids 13 y 14; se eliminan para poder sembrar los dos
-- objetivos B2 con ids fijos. En una base recien migrada es un no-op.
delete from objectives where id in (13, 14);

insert into objectives (id, code, name, cefr_level, skill, description) overriding system value values
    (13, 'PASSIV', 'Passive voice', 'B2', 'grammar',
        'Construcciones pasivas: el foco pasa al objeto que recibe la accion (be + participio).'),
    (14, 'REPSP', 'Reported speech', 'B2', 'grammar',
        'Estilo indirecto: backshift de tiempos y ajuste de pronombres y expresiones de tiempo.');

-- prerrequisitos (el grafo sigue siendo aciclico)
insert into objective_prerequisites (objective_id, prerequisite_id) values
    (13, 10),   -- PASSIV <- PP (present perfect)
    (14, 7);    -- REPSP  <- PAST (past simple)

-- avanzar la secuencia de objectives
select setval(pg_get_serial_sequence('objectives', 'id'), (select max(id) from objectives));

-- dominio previo: se guarda el mastery que habia antes del ultimo intento.
-- Nullable: sin fila de estado no hay valor previo (el DTO devuelve null).
alter table learner_skill_state add column previous_mastery numeric(5, 4);
