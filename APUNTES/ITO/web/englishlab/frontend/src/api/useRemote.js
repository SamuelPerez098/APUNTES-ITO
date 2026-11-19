import { useCallback, useEffect, useState } from 'react';
import { extractApiError } from './client.js';

/**
 * Patrón mock/real para paneles cuyo endpoint del backend aún no existe.
 *
 * - Llama a la API real con el cliente JWT.
 * - 2xx            → estado 'real': datos del backend.
 * - 404            → estado 'fallback': el endpoint aún no existe; se usa el
 *                    mock y la UI muestra <MockNotice/>. Cuando el backend
 *                    implemente el endpoint, el panel cambia solo a datos
 *                    reales sin reescribir código.
 * - 401/403/5xx/red → estado 'error': <ErrorState/> con reintento. NUNCA cae
 *                    a mock (un fallo real no se disfraza de datos).
 *
 * Estados: 'loading' | 'real' | 'fallback' | 'error'.
 */
export const REMOTE_STATUS = {
  loading: 'loading',
  real: 'real',
  fallback: 'fallback',
  error: 'error',
};

export function useRemote({ fetcher, fallback, deps = [] }) {
  const [status, setStatus] = useState(REMOTE_STATUS.loading);
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let alive = true;
    setStatus(REMOTE_STATUS.loading);
    (async () => {
      try {
        const res = await fetcher();
        if (!alive) return;
        setData(res?.data ?? res);
        setError(null);
        setStatus(REMOTE_STATUS.real);
      } catch (err) {
        if (!alive) return;
        if (err?.response?.status === 404 && fallback) {
          setData(typeof fallback === 'function' ? fallback() : fallback);
          setError(null);
          setStatus(REMOTE_STATUS.fallback);
        } else {
          setError(extractApiError(err));
          setStatus(REMOTE_STATUS.error);
        }
      }
    })();
    return () => {
      alive = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...deps, reloadKey]);

  const reload = useCallback(() => setReloadKey((k) => k + 1), []);

  return { status, data, error, reload };
}

/**
 * Igual que useRemote, pero para mutaciones (create/update/remove): intenta la
 * API real y, si responde 404 (endpoint pendiente), ejecuta el fallback del
 * mock. Devuelve {mode:'real'|'fallback', data}. Cualquier otro error se
 * propaga para que la página lo muestre como fallo real.
 */
export async function mutateWithFallback(request, fallbackFn) {
  try {
    const res = await request();
    return { mode: 'real', data: res?.data ?? res };
  } catch (err) {
    if (err?.response?.status === 404) {
      return { mode: 'fallback', data: fallbackFn ? fallbackFn() : null };
    }
    throw err;
  }
}
