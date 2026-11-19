import client from './client.js';

/**
 * Servicio del profesor (rol TEACHER). Contrato (coordinator):
 * - GET  /teacher/students?page&size&sort=name&text=<parcial>
 *        → 200 {content:[{...}], page, size, totalElements, totalPages} (solo SUS alumnos)
 * - POST /teacher/students {email} → 201 (vincula un alumno existente);
 *        404 el usuario no existe; 422 no es STUDENT; 422 ya tiene profesor.
 * - POST /teacher/assignments {studentId, activityId} → 201
 * - GET  /teacher/students/{id}/progress → progreso del alumno
 * Paginación y filtros SIEMPRE en servidor.
 */

export function listStudents({ page = 0, size = 10, sort = 'name,asc', text } = {}) {
  return client.get('/teacher/students', {
    params: { page, size, sort, ...(text ? { text } : {}) },
  });
}

export function linkStudent({ email }) {
  return client.post('/teacher/students', { email });
}

export function createAssignment({ studentId, activityId }) {
  return client.post('/teacher/assignments', { studentId, activityId });
}

export function getStudentProgress(id) {
  return client.get(`/teacher/students/${id}/progress`);
}
