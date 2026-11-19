import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Shell from './Shell.jsx';
import ConfirmModal from '../components/ConfirmModal.jsx';
import { useAuth } from '../auth/AuthContext.jsx';
import { roleLabel } from '../lib/format.js';
import client from '../api/client.js';

/**
 * Página base de todas las vistas internas: envuelve el contenido en el shell
 * del Figma y provee las acciones de topbar: Perfil (modal con datos reales
 * del usuario autenticado) y Salir (logout real POST /auth/logout, luego
 * limpieza de token y redirección a /login).
 */
export default function InternalLayout({ crumb, children }) {
  const { user, role, logout } = useAuth();
  const navigate = useNavigate();
  const [showLogout, setShowLogout] = useState(false);
  const [showProfile, setShowProfile] = useState(false);
  const [loggingOut, setLoggingOut] = useState(false);

  async function handleLogout() {
    setLoggingOut(true);
    try {
      await client.post('/auth/logout'); // invalidación real del token (revoked_tokens)
    } catch {
      // aunque el logout de servidor falle, la sesión local se cierra
    } finally {
      logout();
      navigate('/login', { replace: true });
    }
  }

  return (
    <>
      <Shell
        role={role}
        crumb={crumb}
        user={user}
        onProfile={() => setShowProfile(true)}
        onLogout={() => setShowLogout(true)}
      >
        {children}
      </Shell>

      <ConfirmModal
        open={showLogout}
        title="Cerrar sesión"
        description="¿Quieres salir de English Lab? Se cerrará tu sesión en este dispositivo."
        confirmLabel="Salir"
        busy={loggingOut}
        onConfirm={handleLogout}
        onCancel={() => setShowLogout(false)}
      />

      {showProfile ? (
        <div
          className="overlay"
          onClick={(e) => {
            if (e.target === e.currentTarget) setShowProfile(false);
          }}
        >
          <section className="modal" role="dialog" aria-modal="true" aria-labelledby="profile-title">
            <p className="eyebrow">ENGLISH LAB / PERFIL</p>
            <h2 id="profile-title">Mi cuenta</h2>
            <p>
              <b>{user?.name}</b>
              <br />
              {user?.email}
              <br />
              Rol: <b>{roleLabel(role)}</b>
            </p>
            <div className="btn-row" style={{ justifyContent: 'flex-end', marginTop: 24 }}>
              <button className="btn alt" type="button" onClick={() => setShowProfile(false)}>
                Cerrar
              </button>
            </div>
          </section>
        </div>
      ) : null}
    </>
  );
}
