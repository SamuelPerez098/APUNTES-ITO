/**
 * Constantes de dominio compartidas por las pantallas conectadas a la API.
 * Valores alineados con el backend/seed (confirmado por el coordinator).
 */

/** Niveles CEFR (en MAYÚSCULAS, como los usa el backend). */
export const LEVELS = ['A1', 'A2', 'B1', 'B2'];

/** Habilidades (skill) admitidas por el backend/seed (minúsculas). */
export const SKILLS = [
  { value: 'grammar', label: 'Gramática' },
  { value: 'vocabulary', label: 'Vocabulario' },
];

/** Códigos de tipo de actividad (contrato). Solo MULTIPLE_CHOICE y FILL_BLANK. */
export const ACTIVITY_TYPES = {
  MULTIPLE_CHOICE: 'Opción múltiple',
  FILL_BLANK: 'Completar espacios',
};

export const ACTIVITY_TYPE_CODES = Object.keys(ACTIVITY_TYPES);

export function activityTypeLabel(code) {
  return ACTIVITY_TYPES[code] || code || '—';
}

export function skillLabel(code) {
  const found = SKILLS.find((s) => s.value === code);
  return found ? found.label : code || '—';
}
