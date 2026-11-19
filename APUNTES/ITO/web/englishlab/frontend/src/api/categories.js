import client from './client.js';

/**
 * Servicio de categorías (rol ADMIN). Contrato (coordinator / api-contract.md):
 * - GET    /admin/categories?page&size&sort=name&text=<parcial>
 *          → 200 {content:[{id,name,description}], page, size, totalElements, totalPages}
 * - POST   /admin/categories {name, description?} → 201 {id, name, description}
 * - PUT    /admin/categories/{id} {name, description?} → 200 {id, name, description}
 * - DELETE /admin/categories/{id} → 204
 * Paginación y filtros SIEMPRE en servidor; el filtro `text` (nombre parcial)
 * viaja como parámetro, nunca se filtra en memoria.
 */

export function listCategories({ page = 0, size = 10, sort = 'name,asc', text } = {}) {
  return client.get('/admin/categories', {
    params: { page, size, sort, ...(text ? { text } : {}) },
  });
}

export function createCategory({ name, description }) {
  return client.post('/admin/categories', { name, description });
}

export function updateCategory(id, { name, description }) {
  return client.put(`/admin/categories/${id}`, { name, description });
}

export function removeCategory(id) {
  return client.delete(`/admin/categories/${id}`);
}
