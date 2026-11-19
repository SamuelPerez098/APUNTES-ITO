/**
 * Aviso discreto para pantallas de fases siguientes: los datos son de muestra
 * porque el endpoint correspondiente del backend aún no existe.
 */
export default function MockNotice({ what }) {
  return (
    <div className="mock-note">
      <span>
        Datos de muestra — {what ? <b>{what}</b> : 'esta vista'} usa contenido de demostración;
        el endpoint del backend todavía no existe.
      </span>
    </div>
  );
}
