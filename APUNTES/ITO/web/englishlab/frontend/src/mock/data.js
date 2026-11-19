/** Datos de muestra para las pantallas mock (fases siguientes).
 *  Espejo del objeto `data` del prototipo Figma (HTML:41). NADA de esto
 *  viene de la API: cada pantalla mock lo marca con <MockNotice />. */

export const MOCK_STUDENTS = [
  { id: 1, name: 'Ana López', email: 'ana@example.com', role: 'Estudiante', status: 'Activo' },
  { id: 2, name: 'Luis García', email: 'luis@example.com', role: 'Estudiante', status: 'Activo' },
  { id: 3, name: 'Sara Ruiz', email: 'sara@example.com', role: 'Profesor', status: 'Activo' },
  { id: 4, name: 'Pedro Torres', email: 'pedro@example.com', role: 'Estudiante', status: 'Inactivo' },
];

export const MOCK_CATEGORIES = [
  { id: 1, name: 'Grammar', description: 'Reglas gramaticales', activities: 4 },
  { id: 2, name: 'Vocabulary', description: 'Palabras y expresiones', activities: 3 },
  { id: 3, name: 'Reading', description: 'Comprensión lectora', activities: 2 },
];

export const MOCK_STUDENT_TASKS = [
  { id: 1, title: 'Verb to be', level: 'A1', category: 'Grammar', status: 'Pendiente', due: '20 nov' },
  { id: 3, title: 'Daily routines', level: 'A2', category: 'Vocabulary', status: 'Pendiente', due: '22 nov' },
  { id: 4, title: 'First conditional', level: 'B1', category: 'Grammar', status: 'Completada', due: '12 nov' },
];
