-- =============================================================================
-- English Lab - V12: seed de aprendizaje
-- assignments, attempts, learner_skill_state, notifications_log (10-15 filas c/u).
-- Coherentes con V10 (usuarios) y V11 (objetivos/actividades).
-- Profesores: id 2 -> students 4..8; id 3 -> students 9..12.
-- =============================================================================

insert into assignments (id, teacher_id, student_id, activity_id, status, assigned_at, completed_at) overriding system value values
    (1,  2, 4,  1,  'PENDING',   now() - interval '5 days', null),
    (2,  2, 4,  2,  'COMPLETED', now() - interval '5 days', now() - interval '4 days'),
    (3,  2, 5,  3,  'PENDING',   now() - interval '4 days', null),
    (4,  2, 5,  5,  'COMPLETED', now() - interval '4 days', now() - interval '3 days'),
    (5,  2, 6,  7,  'PENDING',   now() - interval '3 days', null),
    (6,  2, 7,  9,  'COMPLETED', now() - interval '3 days', now() - interval '2 days'),
    (7,  3, 9,  4,  'PENDING',   now() - interval '5 days', null),
    (8,  3, 9,  6,  'PENDING',   now() - interval '4 days', null),
    (9,  3, 10, 8,  'COMPLETED', now() - interval '4 days', now() - interval '3 days'),
    (10, 3, 11, 12, 'PENDING',   now() - interval '3 days', null),
    (11, 3, 12, 14, 'COMPLETED', now() - interval '2 days', now() - interval '1 day'),
    (12, 2, 8,  15, 'PENDING',   now() - interval '2 days', null);

insert into attempts (id, user_id, activity_id, assignment_id, user_answer, is_correct, score, attempted_at) overriding system value values
    (1,  4,  1,  1,  'a',          false, 0.0000, now() - interval '4 days'),
    (2,  4,  2,  2,  'a',          true,  1.0000, now() - interval '4 days'),
    (3,  5,  3,  3,  'is',         false, 0.0000, now() - interval '3 days'),
    (4,  5,  5,  4,  'b',          true,  1.0000, now() - interval '3 days'),
    (5,  6,  7,  5,  'a',          false, 0.0000, now() - interval '2 days'),
    (6,  7,  9,  6,  'b',          true,  1.0000, now() - interval '2 days'),
    (7,  9,  4,  7,  'a',          false, 0.0000, now() - interval '4 days'),
    (8,  9,  6,  8,  'watch',      false, 0.0000, now() - interval '3 days'),
    (9,  10, 8,  9,  'are playing',true,  1.0000, now() - interval '3 days'),
    (10, 11, 12, 10, 'a',          false, 0.0000, now() - interval '2 days'),
    (11, 12, 14, 11, 'b',          true,  1.0000, now() - interval '1 day'),
    (12, 8,  15, 12, 'a',          false, 0.0000, now() - interval '1 day'),
    (13, 6,  2,  null, 'a',        true,  1.0000, now() - interval '2 days');

insert into learner_skill_state (user_id, objective_id, mastery, attempts_count, correct_count, last_attempt_at) values
    (4,  1,  0.1050, 1, 0, now() - interval '4 days'),
    (4,  2,  0.7550, 1, 1, now() - interval '4 days'),
    (5,  2,  0.1050, 1, 0, now() - interval '3 days'),
    (5,  4,  0.7550, 1, 1, now() - interval '3 days'),
    (6,  5,  0.1050, 1, 0, now() - interval '2 days'),
    (6,  2,  0.7550, 1, 1, now() - interval '2 days'),
    (7,  7,  0.7550, 1, 1, now() - interval '2 days'),
    (8,  12, 0.1050, 1, 0, now() - interval '1 day'),
    (9,  3,  0.1050, 1, 0, now() - interval '4 days'),
    (9,  4,  0.1050, 1, 0, now() - interval '3 days'),
    (10, 6,  0.7550, 1, 1, now() - interval '3 days'),
    (11, 9,  0.1050, 1, 0, now() - interval '2 days'),
    (12, 11, 0.7550, 1, 1, now() - interval '1 day');

insert into notifications_log (id, user_id, channel, type, status, error_message, sent_at) overriding system value values
    (1, 4,  'EMAIL',    'WELCOME',             'SENT',   null,                                   now() - interval '6 days'),
    (2, 9,  'EMAIL',    'WELCOME',             'SENT',   null,                                   now() - interval '6 days'),
    (3, 4,  'SMS',      'ASSIGNMENT_CREATED',  'SENT',   null,                                   now() - interval '5 days'),
    (4, 5,  'SMS',      'ASSIGNMENT_CREATED',  'FAILED', 'Twilio error: invalid phone number',   now() - interval '4 days'),
    (5, 3,  'WHATSAPP', 'STUDENT_COMPLETED',   'SENT',   null,                                   now() - interval '3 days'),
    (6, 2,  'EMAIL',    'STUDENT_COMPLETED',   'SENT',   null,                                   now() - interval '2 days'),
    (7, 10, 'SMS',      'ASSIGNMENT_CREATED',  'FAILED', 'Twilio error: timeout',                now() - interval '4 days');

-- avanzar secuencias
select setval(pg_get_serial_sequence('assignments', 'id'), (select max(id) from assignments));
select setval(pg_get_serial_sequence('attempts', 'id'), (select max(id) from attempts));
select setval(pg_get_serial_sequence('notifications_log', 'id'), (select max(id) from notifications_log));
