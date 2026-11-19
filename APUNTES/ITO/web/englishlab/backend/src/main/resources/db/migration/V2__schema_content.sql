-- =============================================================================
-- English Lab - V2: esquema de contenido pedagogico
-- Tablas: objectives, activities, objective_prerequisites
-- (categories y activities.category_id se crean en V4)
-- =============================================================================

create table objectives (
    id bigint generated always as identity primary key,
    code varchar(10) not null,
    name varchar(150) not null,
    cefr_level varchar(2) not null,
    skill varchar(20) not null,
    description text,
    constraint ck_objectives_cefr_level check (cefr_level in ('A1', 'A2', 'B1', 'B2'))
);

create unique index ux_objectives_code on objectives (code);

create table activities (
    id bigint generated always as identity primary key,
    objective_id bigint not null references objectives (id) on delete cascade,
    type varchar(20) not null,
    level varchar(2) not null,
    skill varchar(20) not null,
    title varchar(200) not null,
    content jsonb not null,
    constraint ck_activities_type check (type in ('MULTIPLE_CHOICE', 'FILL_BLANK')),
    constraint ck_activities_level check (level in ('A1', 'A2', 'B1', 'B2'))
);

-- prerrequisitos: N:M self-join, grafo aciclico
create table objective_prerequisites (
    objective_id bigint not null references objectives (id) on delete cascade,
    prerequisite_id bigint not null references objectives (id) on delete cascade,
    primary key (objective_id, prerequisite_id),
    constraint ck_objprereq_distinct check (objective_id <> prerequisite_id)
);

create index ix_objprereq_prerequisite on objective_prerequisites (prerequisite_id);

create index ix_activities_objective_id on activities (objective_id);
create index ix_activities_level on activities (level);
create index ix_activities_skill on activities (skill);
create index ix_activities_type on activities (type);
