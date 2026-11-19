import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import AuthLayout from '../layouts/AuthLayout.jsx';
import Input from '../components/Input.jsx';
import Spinner from '../components/Spinner.jsx';
import client, { extractApiError, SESSION_EXPIRED_KEY } from '../api/client.js';
import { useAuth, homePathForRole } from '../auth/AuthContext.jsx';
import { email as validateEmail, required } from '../lib/validators.js';

/** 01 · Inicio de sesión (pageLogin del Figma, HTML:57). Sin selector de rol:
 *  el rol lo devuelve el backend y decide el panel al que se redirige. */
export default function Login() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  const [values, setValues] = useState({ email: '', password: '' });
  const [errors, setErrors] = useState({ email: '', password: '' });
  const [touched, setTouched] = useState({});
  const [submitError, setSubmitError] = useState('');
  const [busy, setBusy] = useState(false);
  const [sessionExpired, setSessionExpired] = useState(false);

  // El interceptor 401 deja una marca antes de redirigir; se consume una sola vez.
  useEffect(() => {
    try {
      if (window.sessionStorage.getItem(SESSION_EXPIRED_KEY) === '1') {
        window.sessionStorage.removeItem(SESSION_EXPIRED_KEY);
        setSessionExpired(true);
      }
    } catch {
      /* sessionStorage no disponible: sin aviso */
    }
  }, []);

  const rules = {
    email: (v) => required(v, 'El correo') || validateEmail(v),
    password: (v) => required(v, 'La contraseña'),
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
    setTouched({ email: true, password: true });
    if (Object.values(nextErrors).some(Boolean)) return;

    setBusy(true);
    setSubmitError('');
    try {
      const { data } = await client.post('/auth/login', {
        email: values.email.trim(),
        password: values.password,
      });
      login(data.token, data.user);
      const from = location.state?.from;
      navigate(from || homePathForRole(data.user?.role), { replace: true });
    } catch (err) {
      const apiError = extractApiError(err);
      const fieldErrors = {};
      apiError.fieldErrors.forEach((f) => {
        if (f.field in values) fieldErrors[f.field] = f.message;
      });
      setErrors((prev) => ({ ...prev, ...fieldErrors }));
      if (!Object.keys(fieldErrors).length) {
        setSubmitError(
          apiError.status === 401
            ? 'Credenciales inválidas. Revisa tu correo y contraseña.'
            : apiError.detail
        );
      }
    } finally {
      setBusy(false);
    }
  }

  return (
    <AuthLayout>
      <h1>Bienvenido de nuevo.</h1>
      <p className="muted">Ingresa para continuar tu aprendizaje.</p>
      {sessionExpired ? (
        <div className="notice" role="status" style={{ margin: '4px 0 16px' }}>
          <span>Tu sesión expiró. Inicia sesión de nuevo.</span>
        </div>
      ) : null}
      <form onSubmit={handleSubmit} noValidate>
        <Input
          label="Correo electrónico"
          name="email"
          type="email"
          placeholder="nombre@correo.com"
          autoComplete="email"
          value={values.email}
          onChange={handleChange}
          onBlur={handleBlur}
          error={touched.email ? errors.email : ''}
        />
        <Input
          label="Contraseña"
          name="password"
          type="password"
          placeholder="Tu contraseña"
          autoComplete="current-password"
          value={values.password}
          onChange={handleChange}
          onBlur={handleBlur}
          error={touched.password ? errors.password : ''}
        />
        <div className="help">
          <Link to="/recuperar">¿Olvidaste tu contraseña?</Link>
        </div>
        {submitError ? <p className="form-status" role="alert">{submitError}</p> : null}
        <button className="btn wide" type="submit" disabled={busy}>
          {busy ? 'Ingresando…' : 'Iniciar sesión'}
        </button>
      </form>
      {busy ? (
        <div className="mt" style={{ display: 'flex', justifyContent: 'center' }}>
          <Spinner small label="" />
        </div>
      ) : null}
      <div className="auth-links">
        <span>
          ¿No tienes cuenta? <Link to="/registro">Regístrate</Link>
        </span>
      </div>
    </AuthLayout>
  );
}
