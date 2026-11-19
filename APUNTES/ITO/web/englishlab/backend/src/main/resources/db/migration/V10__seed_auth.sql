-- =============================================================================
-- English Lab - V10: seed de autenticacion
-- 3 roles, 1 admin, 2 teachers, 9 students.
-- Los password_hash son hashes BCrypt REALES; las claves en claro se documentan
-- ÚNICAMENTE en docs/README-evaluacion.md (no en este archivo).
-- =============================================================================

insert into roles (id, name) overriding system value values
    (1, 'ADMIN'),
    (2, 'TEACHER'),
    (3, 'STUDENT');

-- admin + teachers (teacher_id null)
insert into users (id, name, email, password_hash, role_id, teacher_id, phone) overriding system value values
    (1, 'Administrador', 'admin@englishlab.com',
        '$2a$10$OflIURyGmGPMJOLgRmLV9OHFkAm1vYSRBb6vzaTWmkcHPfU9K/fM.',
        1, null, null),
    (2, 'Profesor Uno', 'teacher1@englishlab.com',
        '$2a$10$LRNCH8qE0.s31x0W5jrA8ejv7V7oY5NR562FJfWv/VU3xSsEOFV.u',
        2, null, null),
    (3, 'Profesor Dos', 'teacher2@englishlab.com',
        '$2a$10$LRNCH8qE0.s31x0W5jrA8ejv7V7oY5NR562FJfWv/VU3xSsEOFV.u',
        2, null, null);

-- students: 1-5 vinculados al teacher1 (id 2); 6-9 al teacher2 (id 3)
insert into users (id, name, email, password_hash, role_id, teacher_id, phone) overriding system value values
    (4,  'Estudiante Uno',   'student1@englishlab.com', '$2a$10$LRNCH8qE0.s31x0W5jrA8ejv7V7oY5NR562FJfWv/VU3xSsEOFV.u', 3, 2, null),
    (5,  'Estudiante Dos',   'student2@englishlab.com', '$2a$10$LRNCH8qE0.s31x0W5jrA8ejv7V7oY5NR562FJfWv/VU3xSsEOFV.u', 3, 2, null),
    (6,  'Estudiante Tres',  'student3@englishlab.com', '$2a$10$LRNCH8qE0.s31x0W5jrA8ejv7V7oY5NR562FJfWv/VU3xSsEOFV.u', 3, 2, null),
    (7,  'Estudiante Cuatro','student4@englishlab.com', '$2a$10$LRNCH8qE0.s31x0W5jrA8ejv7V7oY5NR562FJfWv/VU3xSsEOFV.u', 3, 2, null),
    (8,  'Estudiante Cinco', 'student5@englishlab.com', '$2a$10$LRNCH8qE0.s31x0W5jrA8ejv7V7oY5NR562FJfWv/VU3xSsEOFV.u', 3, 2, null),
    (9,  'Estudiante Seis',  'student6@englishlab.com', '$2a$10$LRNCH8qE0.s31x0W5jrA8ejv7V7oY5NR562FJfWv/VU3xSsEOFV.u', 3, 3, null),
    (10, 'Estudiante Siete', 'student7@englishlab.com', '$2a$10$LRNCH8qE0.s31x0W5jrA8ejv7V7oY5NR562FJfWv/VU3xSsEOFV.u', 3, 3, null),
    (11, 'Estudiante Ocho',  'student8@englishlab.com', '$2a$10$LRNCH8qE0.s31x0W5jrA8ejv7V7oY5NR562FJfWv/VU3xSsEOFV.u', 3, 3, null),
    (12, 'Estudiante Nueve', 'student9@englishlab.com', '$2a$10$LRNCH8qE0.s31x0W5jrA8ejv7V7oY5NR562FJfWv/VU3xSsEOFV.u', 3, 3, null);

-- avanzar secuencias para que los proximos INSERT (p. ej. registro) usen ids correctos
select setval(pg_get_serial_sequence('roles', 'id'), (select max(id) from roles));
select setval(pg_get_serial_sequence('users', 'id'), (select max(id) from users));
