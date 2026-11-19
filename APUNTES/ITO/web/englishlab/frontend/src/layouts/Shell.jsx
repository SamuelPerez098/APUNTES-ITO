import { useEffect } from 'react';

/**
 * Configuración de menú por rol (roleMenus del Figma, HTML:90–94):
 * - Estudiante: 3 entradas (inicio, tareas, progreso). El reproductor (07) y el
 *   resultado (08) NO están en el menú: se llega a ellos desde las tareas y las
 *   sesiones de práctica, pero las rutas siguen existiendo.
 * - Profesor: home + banco/editor/asignar/resultados.
 * - Administrador: SIN "home"; entra directo al banco y añade usuarios/categorías.
 * El menú lateral va SIN numeración (los prefijos "NN · " eran del prototipo).
 * El breadcrumb del topbar conserva su numeración propia (se pasa en App.jsx).
 */
export const NAV_MENUS = {
  STUDENT: [
    { to: '/estudiante/inicio', label: 'Inicio' },
    { to: '/estudiante/tareas', label: 'Tareas y práctica' },
    { to: '/estudiante/progreso', label: 'Mi progreso' },
  ],
  TEACHER: [
    { to: '/profesor/inicio', label: 'Inicio profesor' },
    { to: '/actividades', label: 'Banco de actividades' },
    { to: '/actividades/nueva', label: 'Crear actividad', match: '/actividades/nueva' },
    { to: '/asignaciones/nueva', label: 'Asignar tarea' },
    { to: '/resultados', label: 'Resultados' },
  ],
  ADMIN: [
    { to: '/actividades', label: 'Banco de actividades' },
    { to: '/actividades/nueva', label: 'Crear actividad', match: '/actividades/nueva' },
    { to: '/asignaciones/nueva', label: 'Asignar tarea' },
    { to: '/resultados', label: 'Resultados' },
    { to: '/admin/usuarios', label: 'Administrar usuarios' },
    { to: '/admin/categorias', label: 'Categorías' },
  ],
};

/**
 * Shell interno del Figma (render() HTML:96): sidebar .side con brand + nav
 * por rol, y .topbar con crumb, acciones Perfil/Salir.
 * En móvil (<=700px) la sidebar colapsa a nav horizontal (media query del Figma).
 */
export default function Shell({ role, crumb, user, onProfile, onLogout, children }) {
  const items = NAV_MENUS[role] || [];

  useEffect(() => {
    window.scrollTo(0, 0);
  }, [crumb]);

  return (
    <div className="shell">
      <aside className="side">
        <div className="brand">
          <span className="brand-mark">EL</span>
          <span>
            ENGLISH LAB
            <small>LEARNING SYSTEM</small>
          </span>
        </div>
        <div className="heading">Menú</div>
        <nav className="nav" aria-label="Navegación principal">
          {items.map((item) => (
            <NavLink key={item.to} {...item} />
          ))}
        </nav>
      </aside>
      <section className="workspace">
        <header className="topbar">
          <span className="crumb">ENGLISH LAB / {crumb}</span>
          <div className="top-actions">
            <span className="user-pill">
              <span className="who">
                <b>{user?.name || 'Usuario'}</b>
                <span>{user?.email || ''}</span>
              </span>
              <span className="avatar" aria-hidden="true">
                {avatarInitials(user?.name)}
              </span>
            </span>
            <button className="btn alt small" type="button" onClick={onProfile}>
              Perfil
            </button>
            <button className="btn alt small" type="button" onClick={onLogout}>
              Salir
            </button>
          </div>
        </header>
        <main className="canvas">
          <div className="screen">{children}</div>
        </main>
      </section>
    </div>
  );
}

import { NavLink as RouterNavLink } from 'react-router-dom';
import { initials } from '../lib/format.js';

function NavLink({ to, label, match }) {
  const pattern = match || to;
  return (
    <RouterNavLink to={to} className={({ isActive }) => (isActive && isActiveFor(pattern, to) ? 'active' : '')}>
      {label}
    </RouterNavLink>
  );
}

/* Marca activo si la ruta actual empieza por el patrón (para rutas con :id). */
function isActiveFor(pattern, to) {
  if (pattern === to) return true;
  return window.location.pathname.startsWith(pattern);
}

function avatarInitials(name) {
  return initials(name || '');
}
