# README de evaluacion - English Lab (Equipo 15)

> **SOLO PARA EVALUACION / DEMO.**
> Este es el UNICO archivo del repositorio que contiene claves en claro.
> Los `password_hash` de la BD son hashes **BCrypt** reales de estas claves.
> En produccion estas credenciales deben eliminarse o cambiarse.

## Usuarios del seed (V10__seed_auth.sql)

| Rol     | Nombre          | Email                     | Clave         |
|---------|-----------------|---------------------------|---------------|
| ADMIN   | Administrador   | `admin@englishlab.com`    | `Admin123!`   |
| TEACHER | Profesor Uno    | `teacher1@englishlab.com` | `Password1!`  |
| TEACHER | Profesor Dos    | `teacher2@englishlab.com` | `Password1!`  |
| STUDENT | Estudiante Uno  | `student1@englishlab.com` | `Password1!`  |
| STUDENT | Estudiante Dos  | `student2@englishlab.com` | `Password1!`  |
| STUDENT | Estudiante Tres | `student3@englishlab.com` | `Password1!`  |
| STUDENT | Estudiante Cuatro| `student4@englishlab.com` | `Password1!`  |
| STUDENT | Estudiante Cinco| `student5@englishlab.com` | `Password1!`  |
| STUDENT | Estudiante Seis | `student6@englishlab.com` | `Password1!`  |
| STUDENT | Estudiante Siete| `student7@englishlab.com` | `Password1!`  |
| STUDENT | Estudiante Ocho | `student8@englishlab.com` | `Password1!`  |
| STUDENT | Estudiante Nueve| `student9@englishlab.com` | `Password1!`  |

## Vinculacion profesor-estudiante

- **Profesor Uno** (`teacher1`): `student1` a `student5`.
- **Profesor Dos** (`teacher2`): `student6` a `student9`.

## Variables de entorno de Bruno

La coleccion (`bruno/`) no contiene claves: solo variables. Rellena en el entorno
`englishlab-local` estas variables (el resto de valores estan vacios en el repo):

| Variable de Bruno | Valor en la demo |
|-------------------|------------------|
| `adminEmail` | `admin@englishlab.com` |
| `adminPassword` | `Admin123!` |
| `teacherEmail` | `teacher1@englishlab.com` |
| `teacherPassword` | `Password1!` |
| `studentEmail` | `student1@englishlab.com` |
| `studentPassword` | `Password1!` |
| `demoPassword` | `Password1!` (usuarios del seed que no son admin) |
| `newUserPassword` | `Password1!` (usuario nuevo en `register` / `admin/users-create`) |
| `newPassword` | `NewPassword1!` (clave nueva del reset; vale cualquier clave valida) |

## Notas

- Los `password_hash` en `V10__seed_auth.sql` son hashes BCrypt (cost 10) verificados
  contra sus claves en claro antes de commitear.
- Email unico case-insensitive (`lower(email)`).
- Las claves en claro viven **solo** en este documento (`docs/README-evaluacion.md`).
  La coleccion Bruno usa variables de entorno **vacias**: los valores se copian aqui
  antes de ejecutar. `docs/backup.sql` es **solo esquema** (sin hashes ni claves).
