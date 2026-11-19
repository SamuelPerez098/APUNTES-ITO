/** Encabezado estándar de pantalla (helper header() del Figma, HTML:52). */
export default function PageHeader({ eyebrow, title, subtitle }) {
  return (
    <>
      <p className="eyebrow">{eyebrow}</p>
      <h1 className="page-title">{title}</h1>
      {subtitle ? <p className="subtitle">{subtitle}</p> : null}
    </>
  );
}
