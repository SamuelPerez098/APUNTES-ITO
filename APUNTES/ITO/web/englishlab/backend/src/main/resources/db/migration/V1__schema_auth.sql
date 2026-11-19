-- =============================================================================
-- English Lab - V1: esquema de autenticacion y usuarios
-- Tablas: roles, users, password_reset_tokens, revoked_tokens
-- =============================================================================

create table roles (
    id bigint generated always as identity primary key,
    name varchar(20) not null unique,
    constraint ck_roles_name check (name in ('ADMIN', 'TEACHER', 'STUDENT'))
);

create table users (
    id bigint generated always as identity primary key,
    name varchar(120) not null,
    email varchar(255) not null,
    password_hash varchar(100) not null,
    role_id bigint not null references roles (id),
    teacher_id bigint references users (id) on delete set null,
    phone varchar(20),
    created_at timestamptz not null default now()
);

-- email unico case-insensitive
create unique index ux_users_email on users (lower(email));
create index ix_users_role_id on users (role_id);
create index ix_users_teacher_id on users (teacher_id);

create table password_reset_tokens (
    id bigint generated always as identity primary key,
    user_id bigint not null references users (id) on delete cascade,
    token_hash varchar(64) not null,
    expires_at timestamptz not null,
    used_at timestamptz,
    created_at timestamptz not null default now()
);

create index ix_prt_user_id on password_reset_tokens (user_id);
create index ix_prt_token_hash on password_reset_tokens (token_hash);

create table revoked_tokens (
    id bigint generated always as identity primary key,
    jti varchar(64) not null unique,
    user_id bigint not null references users (id) on delete cascade,
    expires_at timestamptz not null,
    revoked_at timestamptz not null default now()
);

create index ix_revoked_user_id on revoked_tokens (user_id);
create index ix_revoked_expires_at on revoked_tokens (expires_at);
