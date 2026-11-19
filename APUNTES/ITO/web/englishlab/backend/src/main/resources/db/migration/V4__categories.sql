-- =============================================================================
-- English Lab - V4: categorias de actividades (CRUD admin)
-- Tabla: categories + columna activities.category_id
-- (se aplica despues de V3 y antes de los seeds V10-V12)
-- =============================================================================

create table categories (
    id bigint generated always as identity primary key,
    name varchar(120) not null unique,
    description text
);

alter table activities
    add column category_id bigint references categories (id) on delete set null;

create index ix_activities_category_id on activities (category_id);
