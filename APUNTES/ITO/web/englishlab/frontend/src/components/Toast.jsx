import { createContext, useCallback, useContext, useMemo, useRef, useState } from 'react';

/**
 * Toast global (equivalente al toast() del prototipo Figma, HTML:98).
 * Nada de diálogos nativos: los avisos no bloqueantes pasan por aquí.
 */
const ToastContext = createContext({ showToast: () => {} });

let toastId = 0;

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);
  const timers = useRef({});

  const dismiss = useCallback((id) => {
    setToasts((list) => list.filter((t) => t.id !== id));
    clearTimeout(timers.current[id]);
    delete timers.current[id];
  }, []);

  const showToast = useCallback(
    (message, kind = 'info') => {
      const id = ++toastId;
      setToasts((list) => [...list, { id, message, kind }]);
      timers.current[id] = setTimeout(() => dismiss(id), 3800);
    },
    [dismiss]
  );

  const value = useMemo(() => ({ showToast }), [showToast]);

  return (
    <ToastContext.Provider value={value}>
      {children}
      {toasts.map((t) => (
        <div key={t.id} className="toast" role="status">
          {t.message}
        </div>
      ))}
    </ToastContext.Provider>
  );
}

export function useToast() {
  return useContext(ToastContext);
}
