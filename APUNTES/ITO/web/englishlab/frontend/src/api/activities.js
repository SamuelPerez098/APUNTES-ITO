import client from './client.js';

/**
 * Servicio del banco de actividades (roles ADMIN/TEACHER). Contrato (coordinator):
 * - GET    /content/activities?page&size&sort=title&level&skill&type&categoryId&text
 *          → 200 {content:[ActivityAdminDto...], page, size, totalElements, totalPages}
 *          (ActivityAdminDto = contenido completo de la actividad)
 * - POST   /content/activities  {objectiveId,categoryId?,type,level,skill,title,content{...}}
 * - PUT    /content/activities/{id}
 * - DELETE /content/activities/{id}
 * El `content` debe cuadrar con `type`:
 *   MULTIPLE_CHOICE → { prompt, options[] (≥3), answer }
 *   FILL_BLANK      → { prompt, accepted }
 * Paginación y filtros SIEMPRE en servidor.
 */

export function listActivities({
  page = 0,
  size = 10,
  sort = 'title,asc',
  level,
  skill,
  type,
  categoryId,
  text,
} = {}) {
  return client.get('/content/activities', {
    params: {
      page,
      size,
      sort,
      ...(level ? { level } : {}),
      ...(skill ? { skill } : {}),
      ...(type ? { type } : {}),
      ...(categoryId ? { categoryId } : {}),
      ...(text ? { text } : {}),
    },
  });
}

export function createActivity(payload) {
  return client.post('/content/activities', payload);
}

export function updateActivity(id, payload) {
  return client.put(`/content/activities/${id}`, payload);
}

export function removeActivity(id) {
  return client.delete(`/content/activities/${id}`);
}

/** Objetivos de contenido para el editor (select de objetivo).
 *  GET /content/objectives?page=0&size=50 →
 *  200 {content:[{id,code,name,cefrLevel,skill}], page, size, totalElements, totalPages} */
export function listContentObjectives({ page = 0, size = 50 } = {}) {
  return client.get('/content/objectives', { params: { page, size } });
}
