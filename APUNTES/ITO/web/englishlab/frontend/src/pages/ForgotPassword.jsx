import { useState } from 'react';
import { Link } from 'react-router-dom';
import AuthLayout from '../layouts/AuthLayout.jsx';
import Input from '../components/Input.jsx';
import Spinner from '../components/Spinner.jsx';
import client, { extractApiError } from '../api/client.js';
import { email as validateEmail, required } from '../lib/validators.js';
import { useToast } from '../components/Toast.jsx';

/** 03 · Recuperación (pageForgot del Figma, HTML:59).
 *  El backend responde siempre 200 (no revela si el correo existe). */
export default function ForgotPassword() {
  const { showToast } = useToast();
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [touched, setTouched] = useState(false);
  const [submitError, setSubmitError] = useState('');
  const [busy, setBusy] = useState(false);
  const [sent, setSent] = useState(false);

  const rule = (v) => required(v, 'El correo') || validateEmail(v);

  function handleChange(e) {
    setEmail(e.target.value);
    if (touched) setError(rule(e.target.value));
  }

  function handleBlur(e) {
    setTouched(true);
    setError(rule(e.target.value));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setTouched(true);
    const err = rule(email);
    setError(err);
    if (err) return;

    setBusy(true);
    setSubmitError('');
    try {
      await client.post('/auth/forgot-password', { email: email.trim() });
      setSent(true);
      showToast('Si el correo existe, enviamos instrucciones de recuperación.');
    } catch (err2) {
      const apiError = extractApiError(err2);
      setSubmitError(apiError.detail);
    } finally {
      setBusy(false);
    }
  }

  return (
    <AuthLayout>
      <h1>Recupera tu acceso.</h1>
      <p className="muted">
        Escribe tu correo para recibir instrucciones para recuperar tu contraseña.
      </p>
      {sent ? (
        <>
          <div className="notice">
            <span>
              <b>Revisa tu bandeja.</b> Si el correo está registrado, te enviamos un enlace para
              restablecer tu contraseña.
            </span>
          </div>
          <div className="auth-links">
            <Link to="/login">Volver al inicio</Link>
          </div>
        </>
      ) : (
        <form onSubmit={handleSubmit} noValidate>
          <Input
            label="Correo electrónico"
            name="email"
            type="email"
            placeholder="nombre@correo.com"
            autoComplete="email"
            value={email}
            onChange={handleChange}
            onBlur={handleBlur}
            error={touched ? error : ''}
          />
          {submitError ? <p className="form-status" role="alert">{submitError}</p> : null}
          <button className="btn wide" type="submit" disabled={busy} style={{ marginTop: 14 }}>
            {busy ? 'Enviando…' : 'Solicitar enlace'}
          </button>
          <div className="auth-links">
            <Link to="/login">Volver al inicio</Link>
          </div>
        </form>
      )}
    </AuthLayout>
  );
}
