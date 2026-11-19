# Diagrama ER - English Lab

Diagrama entidad-relacion (Mermaid `erDiagram`) del esquema gestionado por Flyway
(V1-V4). 12 tablas: `roles`, `users`, `password_reset_tokens`, `revoked_tokens`,
`objectives`, `objective_prerequisites`, `activities`, `categories`, `assignments`,
`attempts`, `learner_skill_state`, `notifications_log`.

```mermaid
erDiagram
    roles {
        bigint id PK
        varchar name UK
    }

    users {
        bigint id PK
        varchar name
        varchar email
        varchar password_hash
        bigint role_id FK
        bigint teacher_id FK "self (nullable)"
        varchar phone
        timestamptz created_at
    }

    password_reset_tokens {
        bigint id PK
        bigint user_id FK
        varchar token_hash
        timestamptz expires_at
        timestamptz used_at
        timestamptz created_at
    }

    revoked_tokens {
        bigint id PK
        varchar jti UK
        bigint user_id FK
        timestamptz expires_at
        timestamptz revoked_at
    }

    objectives {
        bigint id PK
        varchar code UK
        varchar name
        varchar cefr_level
        varchar skill
        text description
    }

    objective_prerequisites {
        bigint objective_id PK,FK
        bigint prerequisite_id PK,FK
    }

    categories {
        bigint id PK
        varchar name UK
        text description
    }

    activities {
        bigint id PK
        bigint objective_id FK
        bigint category_id FK "nullable"
        varchar type
        varchar level
        varchar skill
        varchar title
        jsonb content
    }

    assignments {
        bigint id PK
        bigint teacher_id FK
        bigint student_id FK
        bigint activity_id FK
        varchar status
        timestamptz assigned_at
        timestamptz completed_at
    }

    attempts {
        bigint id PK
        bigint user_id FK
        bigint activity_id FK
        bigint assignment_id FK "nullable"
        text user_answer
        boolean is_correct
        numeric score
        timestamptz attempted_at
    }

    learner_skill_state {
        bigint user_id PK,FK
        bigint objective_id PK,FK
        numeric mastery
        int attempts_count
        int correct_count
        timestamptz last_attempt_at
    }

    notifications_log {
        bigint id PK
        bigint user_id FK "nullable"
        varchar channel
        varchar type
        varchar status
        text error_message
        timestamptz sent_at
    }

    roles ||--o{ users : "role_id"
    users ||--o{ users : "teacher_id (self, on delete set null)"
    users ||--o{ password_reset_tokens : "user_id (cascade)"
    users ||--o{ revoked_tokens : "user_id (cascade)"
    objectives ||--o{ objective_prerequisites : "objective_id (cascade)"
    objectives ||--o{ objective_prerequisites : "prerequisite_id (cascade)"
    objectives ||--o{ activities : "objective_id (cascade)"
    categories ||--o{ activities : "category_id (set null)"
    users ||--o{ assignments : "teacher_id (cascade)"
    users ||--o{ assignments : "student_id (cascade)"
    activities ||--o{ assignments : "activity_id (cascade)"
    users ||--o{ attempts : "user_id (cascade)"
    activities ||--o{ attempts : "activity_id (cascade)"
    assignments ||--o{ attempts : "assignment_id (set null)"
    users ||--o{ learner_skill_state : "user_id (cascade)"
    objectives ||--o{ learner_skill_state : "objective_id (cascade)"
    users ||--o{ notifications_log : "user_id (set null)"
```

## Notas de integridad

- `users.email` es unico de forma **case-insensitive**: indice unico sobre `lower(email)`.
- `objective_prerequisites` tiene PK compuesta `(objective_id, prerequisite_id)` y
  `CHECK (objective_id <> prerequisite_id)`. El grafo es **aciclico**.
- `learner_skill_state` tiene PK compuesta `(user_id, objective_id)`.
- `activities.content` es `jsonb`; guarda `answer`/`accepted` (la clave) y `explanation`.
  El DTO de estudiante nunca expone esos campos.
- FK con `on delete` explicito: `cascade` para dependientes fuertes, `set null` para
  referencias opcionales (`users.teacher_id`, `attempts.assignment_id`,
  `activities.category_id`, `notifications_log.user_id`).
