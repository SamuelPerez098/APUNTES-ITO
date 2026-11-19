import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import AuthLayout from '../layouts/AuthLayout.jsx';
import Input from '../components/Input.jsx';
import Spinner from '../components/Spinner.jsx';
import client, { extractApiError } from '../api/client.js';
import {
  matchesPassword,
  password as validatePassword,
  required,
} from '../lib/validators.js';
import { PASSWORD_RULE } from '../lib/validators.js';

/** 04 · Nueva contraseña (pageReset del Figma, HTML:60).
 *  Recibe ?token=<token del enlace>; si falta, avisa bajo el formulario. */
export default function ResetPassword() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token') || '';

  const [values, setValues] = useState({ password: '', confirmPassword: '' });
  const [errors, setErrors] = useState({ password: '', confirmPassword: '' });
  const [touched, setTouched] = useState({});
  const [submitError, setSubmitError] = useState('');
  const [busy, setBusy] = useState(false);
  const [done, setDone] = useState(false);

  const rules = {
    password: (v) => validatePassword(v),
    confirmPassword: (v) => validatePassword(v) || matchesPassword(v, values.password),
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setValues((v) => ({ ...v, [name]: value }));
    if (touched[name]) setErrors((errs) => ({ ...errs, [name]: rules[name](value) }));
  };

  const handleBlur = (e) => {
    const { name, value } = e.target;
    setTouched((t) => ({ ...t, [name]: true }));
    setErrors((errs) => ({ ...errs, [name]: rules[name](value) }));
  };

  async function handleSubmit(e) {
    e.preventDefault();
    const nextErrors = {};
    for (const key of Object.keys(rules)) nextErrors[key] = rules[key](values[key]);
    setErrors(nextErrors);
    setTouched({ password: true, confirmPassword: true });
    if (Object.values(nextErrors).some(Boolean)) return;

    setBusy(true);
    setSubmitError('');
    try {
      await client.post('/auth/reset-password', { token, newPassword: values.password });
      setDone(true);
    } catch (err) {
      const apiError = extractApiError(err);
      const fieldErrors = {};
      apiError.fieldErrors.forEach((f) => {
        if (f.field in values) fieldErrors[f.field] = f.message;
      });
      setErrors((prev) => ({ ...prev, ...fieldErrors }));
      if (!Object.keys(fieldErrors).length) {
        setSubmitError(
          apiError.status === 422
            ? 'El enlace no es válido o ya fue utilizado. Solicita uno nuevo en “¿Olvidaste tu contraseña?”.'
            : apiError.detail
        );
      }
    } finally {
      setBusy(false);
    }
  }

  return (
    <AuthLayout>
      <h1>Establecer contraseña.</h1>
      <p className="muted">Crea una nueva contraseña para tu cuenta.</p>
      {done ? (
        <>
          <div className="notice">
            <span>
              <b>Contraseña actualizada.</b> Ya puedes iniciar sesión con tu nueva contraseña.
            </span>
          </div>
          <div className="auth-links">
            <Link to="/login">Ir a iniciar sesión</Link>
          </div>
        </>
      ) : (
        <form onSubmit={handleSubmit} noValidate>
          {!token ? (
            <p className="form-status">
              Falta el token del enlace de recuperación. Abre el enlace que te enviamos por correo.
            </p>
          ) : null}
          <Input
            label="Nueva contraseña"
            name="password"
            type="password"
            placeholder="Nueva contraseña"
            autoComplete="new-password"
            help={PASSWORD_RULE}
            value={values.password}
            onChange={handleChange}
            onBlur={handleBlur}
            error={touched.password ? errors.password : ''}
          />
          <Input
            label="Confirmar contraseña"
            name="confirmPassword"
            type="password"
            placeholder="Repite la contraseña"
            autoComplete="new-password"
            value={values.confirmPassword}
            onChange={handleChange}
            onBlur={handleBlur}
            error={touched.confirmPassword ? errors.confirmPassword : ''}
          />
          {submitError ? <p className="form-status" role="alert">{submitError}</p> : null}
          <button className="btn wide" type="submit" disabled={busy || !token} style={{ marginTop: 14 }}>
            {busy ? 'Guardando…' : 'Guardar contraseña'}
          </button>
          <p className="smallprint" style={{ marginTop: 14 }}>
            <Link to="/login">Volver al inicio</Link>
          </p>
        </form>
      )}
    </AuthLayout>
  );
}
