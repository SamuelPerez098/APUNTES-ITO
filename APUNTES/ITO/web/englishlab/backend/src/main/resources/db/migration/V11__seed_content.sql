-- =============================================================================
-- English Lab - V11: seed de contenido
-- 6 categorias, 12 objetivos A1-B2 (prerrequisitos aciclicos), 15 actividades.
-- La clave de cada actividad vive SOLO en el jsonb `content` (answer/accepted);
-- jamas viaja al navegador (el DTO de estudiante la omite).
-- =============================================================================

insert into categories (id, name, description) overriding system value values
    (1, 'Verbo to be',          'El verbo to be y los pronombres de sujeto.'),
    (2, 'Tiempos verbales',     'Presente simple y continuo, pasado simple y presente perfecto.'),
    (3, 'Artículos y adjetivos','Artículos a/an/the y comparativos/superlativos.'),
    (4, 'Preposiciones',        'Preposiciones de lugar y tiempo.'),
    (5, 'Modales',              'Verbos modales: can, must, should.'),
    (6, 'Condicionales',        'Primer y segundo condicional.');

insert into objectives (id, code, name, cefr_level, skill, description) overriding system value values
    (1,  'PRON',  'Subject pronouns',               'A1', 'grammar', 'Pronombres personales de sujeto (I, you, he, she, it, we, they).'),
    (2,  'BE',    'Verb to be',                     'A1', 'grammar', 'El verbo to be en presente (am/is/are).'),
    (3,  'ART',   'Articles a/an/the',              'A1', 'grammar', 'Uso de los articulos a, an y the.'),
    (4,  'PS',    'Present simple',                 'A2', 'grammar', 'Presente simple para rutinas, habitos y hechos generales.'),
    (5,  'PREP',  'Prepositions of place and time', 'A2', 'grammar', 'Preposiciones de lugar y tiempo: in, on, at.'),
    (6,  'PC',    'Present continuous',             'A2', 'grammar', 'Presente continuo para acciones en progreso.'),
    (7,  'PAST',  'Past simple',                    'A2', 'grammar', 'Pasado simple regular e irregular.'),
    (8,  'COMP',  'Comparatives and superlatives',  'B1', 'grammar', 'Comparativos y superlativos de adjetivos.'),
    (9,  'MOD',   'Modal verbs',                    'B1', 'grammar', 'Verbos modales: can, must, should.'),
    (10, 'PP',    'Present perfect',                'B1', 'grammar', 'Presente perfecto para experiencias y acciones con relevancia presente.'),
    (11, 'COND1', 'First conditional',              'B1', 'grammar', 'Primer condicional para situaciones reales y posibles.'),
    (12, 'COND2', 'Second conditional',             'B2', 'grammar', 'Segundo condicional para situaciones hipoteticas.');

-- prerrequisitos (grafo aciclico)
insert into objective_prerequisites (objective_id, prerequisite_id) values
    (4, 2),    -- PS    <- BE
    (5, 2),    -- PREP  <- BE
    (6, 4),    -- PC    <- PS
    (7, 4),    -- PAST  <- PS
    (8, 2),    -- COMP  <- BE
    (9, 4),    -- MOD   <- PS
    (10, 7),   -- PP    <- PAST
    (11, 4),   -- COND1 <- PS
    (12, 11);  -- COND2 <- COND1

insert into activities (id, objective_id, type, level, skill, title, content, category_id) overriding system value values
    (1, 1, 'MULTIPLE_CHOICE', 'A1', 'grammar', 'Choose the correct subject pronoun',
        '{"prompt":"___ is my best friend, Maria.","options":[{"id":"a","text":"He"},{"id":"b","text":"She"},{"id":"c","text":"It"},{"id":"d","text":"They"}],"answer":"b","explanation":"Usamos she para referirnos a una mujer o niña."}',
        1),
    (2, 2, 'MULTIPLE_CHOICE', 'A1', 'grammar', 'Choose the correct form of the verb to be',
        '{"prompt":"I ___ a student.","options":[{"id":"a","text":"am"},{"id":"b","text":"is"},{"id":"c","text":"are"},{"id":"d","text":"be"}],"answer":"a","explanation":"Con el sujeto I siempre usamos am."}',
        1),
    (3, 2, 'FILL_BLANK', 'A1', 'grammar', 'Complete with the verb to be',
        '{"prompt":"They ___ my classmates.","accepted":["are"],"explanation":"Con they usamos are."}',
        1),
    (4, 3, 'MULTIPLE_CHOICE', 'A1', 'grammar', 'Choose the correct article',
        '{"prompt":"She has ___ apple for breakfast.","options":[{"id":"a","text":"a"},{"id":"b","text":"an"},{"id":"c","text":"the"},{"id":"d","text":"some"}],"answer":"b","explanation":"Usamos an antes de un sonido vocalico como apple."}',
        3),
    (5, 4, 'MULTIPLE_CHOICE', 'A2', 'grammar', 'Choose the correct present simple form',
        '{"prompt":"She ___ to work every day.","options":[{"id":"a","text":"go"},{"id":"b","text":"goes"},{"id":"c","text":"going"},{"id":"d","text":"gone"}],"answer":"b","explanation":"En tercera persona singular el presente simple añade -s o -es."}',
        2),
    (6, 4, 'FILL_BLANK', 'A2', 'grammar', 'Complete with the present simple',
        '{"prompt":"He ___ TV after dinner.","accepted":["watches"],"explanation":"Tercera persona singular: watch cambia a watches."}',
        2),
    (7, 5, 'MULTIPLE_CHOICE', 'A2', 'grammar', 'Choose the correct preposition',
        '{"prompt":"The meeting is ___ Monday.","options":[{"id":"a","text":"at"},{"id":"b","text":"on"},{"id":"c","text":"in"},{"id":"d","text":"by"}],"answer":"b","explanation":"Con los dias de la semana usamos on."}',
        4),
    (8, 6, 'FILL_BLANK', 'A2', 'grammar', 'Complete with the present continuous',
        '{"prompt":"Look! The children ___ in the garden.","accepted":["are playing"],"explanation":"Presente continuo: am/is/are + verbo terminado en -ing."}',
        2),
    (9, 7, 'MULTIPLE_CHOICE', 'A2', 'grammar', 'Choose the correct past simple form',
        '{"prompt":"Yesterday I ___ a film.","options":[{"id":"a","text":"see"},{"id":"b","text":"saw"},{"id":"c","text":"seen"},{"id":"d","text":"sees"}],"answer":"b","explanation":"Saw es el pasado simple irregular de see."}',
        2),
    (10, 7, 'FILL_BLANK', 'A2', 'grammar', 'Complete with the past simple',
        '{"prompt":"She ___ (visit) her grandmother last week.","accepted":["visited"],"explanation":"Pasado simple regular: se añade -ed al verbo."}',
        2),
    (11, 8, 'MULTIPLE_CHOICE', 'B1', 'grammar', 'Choose the correct comparative',
        '{"prompt":"This book is ___ than that one.","options":[{"id":"a","text":"more interesting"},{"id":"b","text":"most interesting"},{"id":"c","text":"interestinger"},{"id":"d","text":"interesting"}],"answer":"a","explanation":"Los adjetivos largos forman el comparativo con more."}',
        3),
    (12, 9, 'MULTIPLE_CHOICE', 'B1', 'grammar', 'Choose the correct modal verb',
        '{"prompt":"You ___ wear a seatbelt. It is the law.","options":[{"id":"a","text":"can"},{"id":"b","text":"must"},{"id":"c","text":"might"},{"id":"d","text":"could"}],"answer":"b","explanation":"Must expresa obligacion o necesidad."}',
        5),
    (13, 10, 'FILL_BLANK', 'B1', 'grammar', 'Complete with the present perfect',
        '{"prompt":"She ___ (finish) her homework.","accepted":["has finished"],"explanation":"Presente perfecto: have/has + participio (finished)."}',
        2),
    (14, 11, 'MULTIPLE_CHOICE', 'B1', 'grammar', 'Choose the correct first conditional',
        '{"prompt":"If it rains, we ___ at home.","options":[{"id":"a","text":"stay"},{"id":"b","text":"will stay"},{"id":"c","text":"stayed"},{"id":"d","text":"would stay"}],"answer":"b","explanation":"Primer condicional: if + presente simple, will + verbo base."}',
        6),
    (15, 12, 'MULTIPLE_CHOICE', 'B2', 'grammar', 'Choose the correct second conditional',
        '{"prompt":"If I ___ a million dollars, I would travel the world.","options":[{"id":"a","text":"have"},{"id":"b","text":"had"},{"id":"c","text":"will have"},{"id":"d","text":"would have"}],"answer":"b","explanation":"Segundo condicional: if + pasado simple, would + verbo base."}',
        6);

-- avanzar secuencias
select setval(pg_get_serial_sequence('categories', 'id'), (select max(id) from categories));
select setval(pg_get_serial_sequence('objectives', 'id'), (select max(id) from objectives));
select setval(pg_get_serial_sequence('activities', 'id'), (select max(id) from activities));
