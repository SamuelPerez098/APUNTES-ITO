import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { clearStoredAuth, readStoredAuth, writeStoredAuth } from '../api/client.js';

/**
 * Estado de sesión: {token, user}. user.role lo determina el backend
 * (ADMIN | TEACHER | STUDENT) en las respuestas de login/register.
 * Nunca se deduce el rol de la URL ni hay selector de rol en el login.
 */
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(readStoredAuth);

  useEffect(() => {
    writeStoredAuth(auth);
  }, [auth]);

  const login = useCallback((token, user) => setAuth({ token, user }), []);
  const register = useCallback((token, user) => setAuth({ token, user }), []);
  /** Cierra sesión en cliente. El logout con invalidación real (revoked_tokens)
   *  llama a POST /auth/logout ANTES de invocar esto. */
  const logout = useCallback(() => setAuth({ token: null, user: null }), []);

  const value = useMemo(
    () => ({
      token: auth.token,
      user: auth.user,
      role: auth.user?.role || null,
      isAuthenticated: Boolean(auth.token),
      login,
      register,
      logout,
    }),
    [auth, login, register, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth debe usarse dentro de <AuthProvider>');
  return ctx;
}

/** Home según el rol que devuelve el backend. */
export function homePathForRole(role) {
  switch (role) {
    case 'ADMIN':
      return '/admin/usuarios';
    case 'TEACHER':
      return '/profesor/inicio';
    case 'STUDENT':
      return '/estudiante/inicio';
    default:
      return '/login';
  }
}
