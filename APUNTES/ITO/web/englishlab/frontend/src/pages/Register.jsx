import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from '../layouts/AuthLayout.jsx';
import Input from '../components/Input.jsx';
import Spinner from '../components/Spinner.jsx';
import client, { extractApiError } from '../api/client.js';
import { useAuth, homePathForRole } from '../auth/AuthContext.jsx';
import {
  email as validateEmail,
  matchesPassword,
  password as validatePassword,
  required,
} from '../lib/validators.js';

/** 02 · Registro (pageRegister del Figma, HTML:58). El registro público
 *  SIEMPRE crea STUDENT; el rol confirma el backend en la respuesta. */
export default function Register() {
  const navigate = useNavigate();
  const { register } = useAuth();

  const [values, setValues] = useState({ name: '', email: '', password: '', confirmPassword: '' });
  const [errors, setErrors] = useState({ name: '', email: '', password: '', confirmPassword: '' });
  const [touched, setTouched] = useState({});
  const [submitError, setSubmitError] = useState('');
  const [busy, setBusy] = useState(false);

  const rules = {
    name: (v) => required(v, 'El nombre'),
    email: (v) => required(v, 'El correo') || validateEmail(v),
    password: (v) => validatePassword(v),
    confirmPassword: (v) => validatePassword(v) || matchesPassword(v, values.password),
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    const next = { ...values, [name]: value };
    setValues(next);
    // Revalidación en vivo: si ya tocó confirmar y cambia la clave, revisa coincidencia.
    setErrors((errs) => ({
      ...errs,
      ...(touched[name] ? { [name]: rules[name](value) } : {}),
      ...(touched.confirmPassword && name === 'password'
        ? { confirmPassword: matchesPassword(next.confirmPassword, value) }
        : {}),
    }));
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
    setTouched(Object.keys(rules).reduce((acc, k) => ({ ...acc, [k]: true }), {}));
    if (Object.values(nextErrors).some(Boolean)) return;

    setBusy(true);
    setSubmitError('');
    try {
      const { data } = await client.post('/auth/register', {
        name: values.name.trim(),
        email: values.email.trim(),
        password: values.password,
      });
      register(data.token, data.user);
      navigate(homePathForRole(data.user?.role), { replace: true });
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
            ? 'Ese correo ya está registrado. Inicia sesión o usa otro correo.'
            : apiError.detail
        );
      }
    } finally {
      setBusy(false);
    }
  }

  return (
    <AuthLayout>
      <h1>Crea tu cuenta.</h1>
      <p className="muted">Regístrate para comenzar a aprender.</p>
      <form onSubmit={handleSubmit} noValidate>
        <Input
          label="Nombre completo"
          name="name"
          placeholder="Nombre y apellidos"
          autoComplete="name"
          value={values.name}
          onChange={handleChange}
          onBlur={handleBlur}
          error={touched.name ? errors.name : ''}
        />
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
          placeholder="Mín. 8 caracteres"
          autoComplete="new-password"
          help="Debe incluir mayúscula, número y carácter especial."
          value={values.password}
          onChange={handleChange}
          onBlur={handleBlur}
          error={touched.password ? errors.password : ''}
        />
        <Input
          label="Confirmar contraseña"
          name="confirmPassword"
          type="password"
          placeholder="Repite tu contraseña"
          autoComplete="new-password"
          value={values.confirmPassword}
          onChange={handleChange}
          onBlur={handleBlur}
          error={touched.confirmPassword ? errors.confirmPassword : ''}
        />
        {submitError ? <p className="form-status" role="alert">{submitError}</p> : null}
        <button className="btn wide" type="submit" disabled={busy} style={{ marginTop: 14 }}>
          {busy ? 'Creando cuenta…' : 'Crear cuenta'}
        </button>
      </form>
      <p className="smallprint">
        ¿Ya tienes cuenta? <Link to="/login">Iniciar sesión</Link>
      </p>
    </AuthLayout>
  );
}
