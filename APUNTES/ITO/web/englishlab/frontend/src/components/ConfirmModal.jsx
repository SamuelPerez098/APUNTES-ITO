/**
 * Modal de confirmación (sustituto obligatorio del confirm nativo).
 * Misma estética que .overlay/.modal del Figma (HTML:97): role="dialog", aria-modal.
 */
export default function ConfirmModal({
  open,
  title,
  description,
  confirmLabel = 'Aceptar',
  cancelLabel = 'Cancelar',
  danger = true,
  busy = false,
  onConfirm,
  onCancel,
}) {
  if (!open) return null;
  return (
    <div
      className="overlay"
      onClick={(e) => {
        if (e.target === e.currentTarget && !busy) onCancel?.();
      }}
    >
      <section className="modal" role="dialog" aria-modal="true" aria-labelledby="confirm-title">
        <p className="eyebrow">ENGLISH LAB / CONFIRMACIÓN</p>
        <h2 id="confirm-title">{title}</h2>
        <p>{description}</p>
        <div className="btn-row" style={{ justifyContent: 'flex-end', marginTop: 24 }}>
          <button className="btn alt" type="button" onClick={onCancel} disabled={busy}>
            {cancelLabel}
          </button>
          <button
            className={`btn${danger ? ' rust' : ''}`}
            type="button"
            onClick={onConfirm}
            disabled={busy}
          >
            {busy ? 'Procesando…' : confirmLabel}
          </button>
        </div>
      </section>
    </div>
  );
}
