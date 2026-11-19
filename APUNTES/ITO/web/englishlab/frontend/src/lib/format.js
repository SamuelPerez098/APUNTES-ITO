/** Etiquetas de rol y utilidades de presentación. */

export const ROLE_LABELS = {
  ADMIN: 'Administrador',
  TEACHER: 'Profesor',
  STUDENT: 'Estudiante',
};

export function roleLabel(role) {
  return ROLE_LABELS[role] || '—';
}

/** Iniciales para el avatar por defecto. */
export function initials(name = '') {
  const parts = String(name).trim().split(/\s+/).filter(Boolean);
  if (!parts.length) return 'EL';
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return (parts[0][0] + parts[1][0]).toUpperCase();
}
