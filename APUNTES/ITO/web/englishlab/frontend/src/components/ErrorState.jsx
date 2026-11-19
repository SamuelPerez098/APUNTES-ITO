/** Estado de error de red/servidor con botón de reintento (react-ui-rules). */
export default function ErrorState({ message, onRetry }) {
  return (
    <div className="network-error" role="alert">
      <div>
        <b>No se pudo cargar la información.</b>
        <div>{message || 'Ocurrió un problema de red o del servidor. Intenta de nuevo.'}</div>
      </div>
      {onRetry ? (
        <button className="btn alt small" type="button" onClick={onRetry}>
          Reintentar
        </button>
      ) : null}
    </div>
  );
}
