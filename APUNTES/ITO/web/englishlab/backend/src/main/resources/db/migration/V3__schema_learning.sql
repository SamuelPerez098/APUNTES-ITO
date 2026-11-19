-- =============================================================================
-- English Lab - V3: esquema de aprendizaje
-- Tablas: assignments, attempts, learner_skill_state, notifications_log
-- Orden: assignments antes que attempts por la FK de attempts.assignment_id
-- =============================================================================

create table assignments (
    id bigint generated always as identity primary key,
    teacher_id bigint not null references users (id) on delete cascade,
    student_id bigint not null references users (id) on delete cascade,
    activity_id bigint not null references activities (id) on delete cascade,
    status varchar(20) not null default 'PENDING',
    assigned_at timestamptz not null default now(),
    completed_at timestamptz,
    constraint ck_assignments_status check (status in ('PENDING', 'COMPLETED'))
);

create index ix_assignments_teacher on assignments (teacher_id);
create index ix_assignments_student on assignments (student_id);
create index ix_assignments_activity on assignments (activity_id);

create table attempts (
    id bigint generated always as identity primary key,
    user_id bigint not null references users (id) on delete cascade,
    activity_id bigint not null references activities (id) on delete cascade,
    assignment_id bigint references assignments (id) on delete set null,
    user_answer text not null,
    is_correct boolean not null,
    score numeric(5, 4) not null,
    attempted_at timestamptz not null default now()
);

create index ix_attempts_user on attempts (user_id);
create index ix_attempts_user_activity on attempts (user_id, activity_id);
create index ix_attempts_assignment on attempts (assignment_id);

create table learner_skill_state (
    user_id bigint not null references users (id) on delete cascade,
    objective_id bigint not null references objectives (id) on delete cascade,
    mastery numeric(5, 4) not null default 0.3000,
    attempts_count integer not null default 0,
    correct_count integer not null default 0,
    last_attempt_at timestamptz,
    primary key (user_id, objective_id)
);

create index ix_lss_objective on learner_skill_state (objective_id);

create table notifications_log (
    id bigint generated always as identity primary key,
    user_id bigint references users (id) on delete set null,
    channel varchar(20) not null,
    type varchar(30) not null,
    status varchar(20) not null default 'PENDING',
    error_message text,
    sent_at timestamptz not null default now(),
    constraint ck_notif_channel check (channel in ('EMAIL', 'SMS', 'WHATSAPP')),
    constraint ck_notif_status check (status in ('PENDING', 'SENT', 'FAILED'))
);

create index ix_notif_user on notifications_log (user_id);
