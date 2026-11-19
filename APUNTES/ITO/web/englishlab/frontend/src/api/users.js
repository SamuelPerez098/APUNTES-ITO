import client from './client.js';

/**
 * Servicio de usuarios (rol ADMIN). Contrato confirmado por el coordinator:
 * - GET    /admin/users?page&size&sort=id|name|email|createdAt&role=ADMIN|TEACHER|STUDENT&text=<parcial>
 *          → 200 {content:[{id,name,email,role,teacherId,teacherName,phone,createdAt}],
 *                  page, size, totalElements, totalPages}
 * - POST   /admin/users {name,email,password,role,phone?} → 201
 * - PUT    /admin/users/{id} {name,email,role,phone?} → 200
 * - DELETE /admin/users/{id} → 204
 * Paginación y filtros SIEMPRE en servidor; nunca se filtra/pagina en memoria.
 */

export function listUsers({ page = 0, size = 10, sort = 'name,asc', role, text } = {}) {
  return client.get('/admin/users', {
    params: { page, size, sort, ...(role ? { role } : {}), ...(text ? { text } : {}) },
  });
}

export function createUser(payload) {
  return client.post('/admin/users', payload);
}

export function updateUser(id, payload) {
  return client.put(`/admin/users/${id}`, payload);
}

export function removeUser(id) {
  return client.delete(`/admin/users/${id}`);
}
