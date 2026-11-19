import client from './client.js';

/**
 * Servicio de aprendizaje del estudiante (rol STUDENT). Contrato (backend):
 * - GET  /me/assignments          → PageResponse de asignaciones; cada una trae
 *                                  ActivityStudentDto (prompt, options, title, type,
 *                                  SIN answer/accepted/explanation).
 * - GET  /me/activities/{id}      → 200 {id, type, level, skill, title, prompt,
 *                                  options:[{id,text}] (null en FILL_BLANK)}; 404 si
 *                                  no existe. Alimenta la práctica libre recomendada.
 * - POST /attempts {activityId, answer, assignmentId?, mode?}
 *        → {correct, score, solution, explanation, mastery}. En MULTIPLE_CHOICE
 *          `solution` es el ID de la opción correcta. `mode` es "NORMAL" (por
 *          defecto) o "CHALLENGE": en RETO la correcta suma 1.0, la incorrecta
 *          −0.5 y la respuesta vacía se califica como "sin responder" (0.0).
 * - GET  /me/objectives           → objetivos del estudiante
 *                                  ({objectiveId,code,name,cefrLevel,skill,mastery,state}).
 * - GET  /me/objectives/{id}/activities → PageResponse de las actividades del
 *                                  objetivo ({id,title,type,level,skill}, SIN prompt/
 *                                  options ni clave); 404 si el objetivo no existe;
 *                                  422 si está BLOCKED por prerrequisitos.
 * - GET  /me/objectives/{id}/practice-session?size=10 → {activityIds:[...]} con
 *                                  las actividades de la sesión; 404 si el objetivo
 *                                  no existe; 422 si está BLOCKED; 403 otro rol.
 *                                  Alimenta el botón "Practicar" de Mi progreso.
 * - GET  /recommendations/next    → {objectiveId, activityId, reason} o
 *                                  {recommendation:null, message}.
 */

export function listAssignments() {
  return client.get('/me/assignments');
}

export function getMyActivity(id) {
  return client.get(`/me/activities/${id}`);
}

export function submitAttempt({ activityId, answer, assignmentId, mode }) {
  return client.post('/attempts', {
    activityId,
    answer,
    // En práctica libre no se envía assignmentId.
    ...(assignmentId != null ? { assignmentId } : {}),
    // `mode` es opcional: el backend asume NORMAL. En RETO se envía CHALLENGE
    // para que el servidor aplique +1.0 / −0.5 / 0.0 (sin respuesta).
    ...(mode != null ? { mode } : {}),
  });
}

export function listObjectives() {
  return client.get('/me/objectives');
}

/**
 * Actividades de un objetivo (paginadas en servidor; `sort` usa la lista blanca
 * del backend).
 */
export function listObjectiveActivities(objectiveId, { page = 0, size = 20, sort = 'title,asc' } = {}) {
  return client.get(`/me/objectives/${objectiveId}/activities`, { params: { page, size, sort } });
}

/**
 * Sesión de práctica de un objetivo: el servidor elige hasta `size` actividades
 * y devuelve {activityIds}. El cliente navega directo al reproductor con esa
 * cola. 422 si el objetivo está bloqueado; 404 si no existe.
 */
export function getPracticeSession(objectiveId, { size = 10 } = {}) {
  return client.get(`/me/objectives/${objectiveId}/practice-session`, { params: { size } });
}

export function nextRecommendation() {
  return client.get('/recommendations/next');
}
