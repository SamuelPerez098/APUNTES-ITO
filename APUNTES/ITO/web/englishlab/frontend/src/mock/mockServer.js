import { MOCK_CATEGORIES, MOCK_STUDENT_TASKS, MOCK_STUDENTS } from './data.js';

/**
 * Servidor mock que EMULA el contrato paginado del backend
 * ({content, page, size, totalElements, totalPages}) y sus mutaciones.
 * Solo se usa cuando la API real responde 404 (endpoint pendiente). Mismo shape
 * que la API real para que el panel no distinga entre modos.
 */

const catRows = [
  ...MOCK_CATEGORIES,
  { id: 4, name: 'Listening', description: 'Comprensión auditiva', activities: 2 },
  { id: 5, name: 'Writing', description: 'Producción escrita', activities: 1 },
  { id: 6, name: 'Speaking', description: 'Producción oral', activities: 1 },
  { id: 7, name: 'Phrasal verbs', description: 'Verbos frasales frecuentes', activities: 3 },
];
let catNextId = catRows.length + 1;

/* Usuarios con el shape del contrato de /admin/users
 * ({id,name,email,role,teacherId,teacherName,phone,createdAt}; sin `active`). */
const userRows = [
  ...MOCK_STUDENTS.map((u) => ({
    id: u.id,
    name: u.name,
    email: u.email,
    role: u.role === 'Profesor' ? 'TEACHER' : u.role === 'Administrador' ? 'ADMIN' : 'STUDENT',
    teacherId: u.role === 'Profesor' ? null : u.id === 1 || u.id === 2 ? 3 : null,
    teacherName: u.role === 'Profesor' ? null : u.id === 1 || u.id === 2 ? 'Sara Ruiz' : null,
    phone: null,
    createdAt: '2026-11-01T10:00:00Z',
  })),
  { id: 5, name: 'María Fernández', email: 'maria@example.com', role: 'TEACHER', teacherId: null, teacherName: null, phone: '+34 600 111 222', createdAt: '2026-11-02T10:00:00Z' },
  { id: 6, name: 'Diego Herrera', email: 'diego@example.com', role: 'STUDENT', teacherId: 5, teacherName: 'María Fernández', phone: null, createdAt: '2026-11-03T10:00:00Z' },
  { id: 7, name: 'Lucía Peña', email: 'lucia@example.com', role: 'STUDENT', teacherId: null, teacherName: null, phone: '+34 600 333 444', createdAt: '2026-11-04T10:00:00Z' },
];
let userNextId = userRows.length + 1;

/* Objetivos de contenido para el select del editor
 * ({id,code,name,cefrLevel,skill}). */
const objectiveRows = [
  { id: 1, code: 'A1-GR-01', name: 'Presente simple', cefrLevel: 'A1', skill: 'grammar' },
  { id: 2, code: 'A2-VO-01', name: 'Rutinas y vocabulario', cefrLevel: 'A2', skill: 'vocabulary' },
  { id: 3, code: 'B1-GR-01', name: 'Condicionales', cefrLevel: 'B1', skill: 'grammar' },
  { id: 4, code: 'B2-GR-01', name: 'Condicionales avanzados', cefrLevel: 'B2', skill: 'grammar' },
];

/* Actividades con el shape del contrato de /content/activities. */
const activityRows = [
  { id: 1, title: 'Present Simple', level: 'A1', skill: 'grammar', type: 'MULTIPLE_CHOICE', categoryId: 1, categoryName: 'Grammar', objectiveId: 1, content: { prompt: 'She ___ a student.', options: ['is', 'are', 'am'], answer: 'is', explanation: 'Tercera persona singular usa "is".' } },
  { id: 2, title: 'Verb to be', level: 'A1', skill: 'grammar', type: 'FILL_BLANK', categoryId: 1, categoryName: 'Grammar', objectiveId: 1, content: { prompt: 'I ___ studying English right now.', accepted: ['am'], explanation: 'Con "I" el verbo to be es "am".' } },
  { id: 3, title: 'Everyday routines', level: 'A2', skill: 'vocabulary', type: 'MULTIPLE_CHOICE', categoryId: 2, categoryName: 'Vocabulary', objectiveId: 2, content: { prompt: 'I ___ my teeth every morning.', options: ['brush', 'brushes', 'brushing'], answer: 'brush', explanation: 'Con "I" el verbo no lleva -s.' } },
  { id: 4, title: 'First conditional', level: 'B1', skill: 'grammar', type: 'MULTIPLE_CHOICE', categoryId: 1, categoryName: 'Grammar', objectiveId: 3, content: { prompt: 'If it rains, I ___ at home.', options: ['stay', 'stays', 'stayed'], answer: 'stay', explanation: 'Primera condicional: if + presente, futuro con will.' } },
  { id: 5, title: 'At the restaurant', level: 'A2', skill: 'vocabulary', type: 'FILL_BLANK', categoryId: 3, categoryName: 'Reading', objectiveId: 2, content: { prompt: 'I would like to ___ a table for two.', accepted: ['book', 'reserve'], explanation: '"Book" o "reserve" son válidos aquí.' } },
  { id: 6, title: 'Second conditional', level: 'B2', skill: 'grammar', type: 'FILL_BLANK', categoryId: 1, categoryName: 'Grammar', objectiveId: 4, content: { prompt: 'If I ___ rich, I would travel.', accepted: ['were', 'was'], explanation: 'Segunda condicional usa pasado simple.' } },
  { id: 7, title: 'Past simple', level: 'A2', skill: 'grammar', type: 'MULTIPLE_CHOICE', categoryId: 1, categoryName: 'Grammar', objectiveId: 2, content: { prompt: 'Yesterday I ___ to the park.', options: ['go', 'went', 'gone'], answer: 'went', explanation: '"Go" es irregular: went en pasado.' } },
  { id: 8, title: 'Travel vocabulary', level: 'B1', skill: 'vocabulary', type: 'MULTIPLE_CHOICE', categoryId: 2, categoryName: 'Vocabulary', objectiveId: 3, content: { prompt: 'We missed the ___ because of the traffic.', options: ['flight', 'fly', 'flew'], answer: 'flight', explanation: '"Flight" es el sustantivo.' } },
];
let activityNextId = activityRows.length + 1;

/** normaliza page/size y corta la página (simulación server-side). */
function paginate(rows, { page = 0, size = 10, sort }) {
  const safeSize = Math.max(1, Number(size) || 10);
  const sorted = [...rows];
  if (sort === 'name,desc') sorted.sort((a, b) => (b.name || '').localeCompare(a.name || ''));
  else if (sort) sorted.sort((a, b) => (a.name || '').localeCompare(b.name || ''));
  const safePage = Math.max(0, Math.min(Number(page) || 0, Math.ceil(sorted.length / safeSize) - 1 || 0));
  const content = sorted.slice(safePage * safeSize, safePage * safeSize + safeSize);
  return {
    content,
    page: safePage,
    size: safeSize,
    totalElements: sorted.length,
    totalPages: Math.max(1, Math.ceil(sorted.length / safeSize)),
  };
}

/* ---------------- Categorías ---------------- */

export function mockCategoriesPage({ page, size, sort, text } = {}) {
  const rows = text
    ? catRows.filter((c) => c.name.toLowerCase().includes(String(text).toLowerCase()))
    : catRows;
  return paginate(rows, { page, size, sort });
}

export function mockCreateCategory({ name, description }) {
  const row = { id: catNextId++, name, description, activities: 0 };
  catRows.push(row);
  return row;
}

export function mockUpdateCategory(id, { name, description }) {
  const row = catRows.find((c) => c.id === Number(id));
  if (!row) return null;
  row.name = name;
  row.description = description;
  return row;
}

export function mockRemoveCategory(id) {
  const index = catRows.findIndex((c) => c.id === Number(id));
  if (index >= 0) catRows.splice(index, 1);
  return null; // como el 204 real
}

/* ---------------- Usuarios ---------------- */

export function mockUsersPage({ page, size, sort, role, text } = {}) {
  let rows = userRows;
  if (role) rows = rows.filter((u) => u.role === role);
  if (text) {
    const q = String(text).toLowerCase();
    rows = rows.filter((u) => u.name.toLowerCase().includes(q) || u.email.toLowerCase().includes(q));
  }
  return paginate(rows, { page, size, sort });
}

export function mockCreateUser(payload) {
  const row = { id: userNextId++, teacherId: null, teacherName: null, phone: null, createdAt: new Date().toISOString(), ...payload };
  userRows.push(row);
  return row;
}

export function mockUpdateUser(id, payload) {
  const row = userRows.find((u) => u.id === Number(id));
  if (!row) return null;
  Object.assign(row, payload);
  return row;
}

export function mockRemoveUser(id) {
  const index = userRows.findIndex((u) => u.id === Number(id));
  if (index >= 0) userRows.splice(index, 1);
  return null; // como el 204 real
}

/* ---------------- Actividades ---------------- */

export function mockActivitiesPage({ page, size, sort, level, skill, type, categoryId, text } = {}) {
  let rows = activityRows;
  if (level) rows = rows.filter((a) => a.level === level);
  if (skill) rows = rows.filter((a) => a.skill === skill);
  if (type) rows = rows.filter((a) => a.type === type);
  if (categoryId) rows = rows.filter((a) => a.categoryId === Number(categoryId));
  if (text) {
    const q = String(text).toLowerCase();
    rows = rows.filter((a) => a.title.toLowerCase().includes(q));
  }
  return paginate(rows, { page, size, sort });
}

export function mockCreateActivity(payload) {
  const row = { id: activityNextId++, categoryName: null, ...payload };
  activityRows.push(row);
  return row;
}

export function mockUpdateActivity(id, payload) {
  const row = activityRows.find((a) => a.id === Number(id));
  if (!row) return null;
  Object.assign(row, payload);
  return row;
}

export function mockRemoveActivity(id) {
  const index = activityRows.findIndex((a) => a.id === Number(id));
  if (index >= 0) activityRows.splice(index, 1);
  return null;
}

export function mockContentObjectivesPage({ page = 0, size = 50 } = {}) {
  return paginate(objectiveRows, { page, size, sort: null });
}

/* ---------------- Profesor (ronda 2) ---------------- */

export function mockStudentsPage({ page, size, sort, text } = {}) {
  let rows = userRows.filter((u) => u.role === 'STUDENT');
  if (text) {
    const q = String(text).toLowerCase();
    rows = rows.filter((u) => u.name.toLowerCase().includes(q) || u.email.toLowerCase().includes(q));
  }
  return paginate(rows, { page, size, sort });
}

export function mockLinkStudent({ email }) {
  const row = userRows.find((u) => u.email === email && u.role === 'STUDENT');
  return row || null;
}

export function mockCreateAssignment(payload) {
  return { id: Date.now(), ...payload };
}

/* ---------------- Estudiante (ronda 2) ---------------- */

export function mockAssignments() {
  return {
    content: MOCK_STUDENT_TASKS.map((t) => ({
      id: t.id,
      activityId: t.id,
      status: t.status === 'Completada' ? 'COMPLETED' : 'PENDING',
      dueDate: t.due,
      activity: {
        id: t.id,
        title: t.title,
        type: t.id === 3 ? 'MULTIPLE_CHOICE' : 'FILL_BLANK',
        prompt: 'Completa la frase con la forma correcta.',
        options: [
          { id: 'a', text: 'am' },
          { id: 'b', text: 'is' },
          { id: 'c', text: 'are' },
        ],
      },
    })),
    page: 0,
    size: 10,
    totalElements: MOCK_STUDENT_TASKS.length,
    totalPages: 1,
  };
}

export function mockObjectives() {
  return [
    { objectiveId: 1, code: 'A1-GR-01', name: 'Presente simple', cefrLevel: 'A1', skill: 'grammar', mastery: 0.65, state: 'IN_PROGRESS' },
    { objectiveId: 2, code: 'A2-VO-01', name: 'Rutinas y vocabulario', cefrLevel: 'A2', skill: 'vocabulary', mastery: 0.4, state: 'LOCKED' },
    { objectiveId: 3, code: 'B1-GR-01', name: 'Condicionales', cefrLevel: 'B1', skill: 'grammar', mastery: 0.2, state: 'LOCKED' },
  ];
}
