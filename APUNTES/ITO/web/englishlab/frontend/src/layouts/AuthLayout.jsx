/** Layout de las 4 pantallas públicas (authLayout() del Figma, HTML:56).
 *  Sin menú ni topbar. El arte lateral se oculta en <=970px (media query del Figma). */
export default function AuthLayout({ children }) {
  return (
    <main className="auth-page">
      <section className="auth-zone">
        <div className="auth-art">
          <div>
            <div style={{ fontSize: 12, letterSpacing: '0.2em' }}>EL / APRENDER A TU RITMO</div>
            <h2>Every word brings you closer.</h2>
            <p>
              Practica, equivócate, aprende y continúa. Un espacio para construir confianza en tu
              inglés.
            </p>
          </div>
          <div style={{ zIndex: 1, fontSize: 11, letterSpacing: '0.12em' }}>
            ENGLISH LAB · APRENDE A TU RITMO
          </div>
        </div>
        <div className="auth-form">
          <div className="auth-inner">
            <p className="eyebrow">ENGLISH LAB / ACCESO</p>
            {children}
          </div>
        </div>
      </section>
    </main>
  );
}
