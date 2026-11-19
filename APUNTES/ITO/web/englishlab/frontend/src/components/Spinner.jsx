/** Spinner de carga (obligatorio por react-ui-rules, con colores del Figma). */
export default function Spinner({ small = false, label = 'Cargando…' }) {
  return (
    <div className="spinner-wrap" role="status" aria-live="polite">
      <span className={`spinner${small ? ' small' : ''}`} aria-hidden="true" />
      {label ? <span>{label}</span> : null}
    </div>
  );
}
