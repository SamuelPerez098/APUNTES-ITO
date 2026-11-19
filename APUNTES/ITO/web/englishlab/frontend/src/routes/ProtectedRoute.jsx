import { Navigate, useLocation } from 'react-router-dom';
import { useAuth, homePathForRole } from '../auth/AuthContext.jsx';

/**
 * Guarda de ruta por rol. El rol vive en AuthContext (lo determina el backend
 * al autenticar), NO en la URL. La seguridad real sigue en Spring Security;
 * esto solo es comodidad de navegación (Guía Figma, regla 4).
 */
export default function ProtectedRoute({ allow, children }) {
  const { isAuthenticated, role } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  if (allow && allow.length && !allow.includes(role)) {
    return <Navigate to={homePathForRole(role)} replace />;
  }
  return children;
}
