import { useState } from 'react';
import PageHeader from '../../components/PageHeader.jsx';
import MockNotice from '../../components/MockNotice.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import Spinner from '../../components/Spinner.jsx';
import ConfirmModal from '../../components/ConfirmModal.jsx';
import Input from '../../components/Input.jsx';
import { useToast } from '../../components/Toast.jsx';
import { useRemote, mutateWithFallback, REMOTE_STATUS } from '../../api/useRemote.js';
import { listUsers, createUser, updateUser, removeUser } from '../../api/users.js';
import { mockUsersPage, mockCreateUser, mockUpdateUser, mockRemoveUser } from '../../mock/mockServer.js';
import { roleLabel } from '../../lib/format.js';
import { required, email as validateEmail, password as validatePassword } from '../../lib/validators.js';

const PAGE_SIZE = 5;
const ROLES = ['ADMIN', 'TEACHER', 'STUDENT'];

/** 15 · Administrar usuarios (pageAdminUsers del Figma, HTML:85).
 *  Patrón mock/real: GET /admin/users con page/size/sort/role/text server-side;
 *  si responde 404 (endpoint pendiente) usa el mock con el mismo shape de
 *  contrato; 401/403/5xx muestran ErrorState real. Alta/edición en modal con
 *  errores bajo cada input; baja (DELETE) con ConfirmModal. */
export default function AdminUsers() {
  const { showToast } = useToast();

  const [textFilter, setTextFilter] = useState('');
  const [roleFilter, setRoleFilter] = useState('');
  const [applied, setApplied] = useState({ text: '', role: '' });
  const [page, setPage] = useState(0);
  const [editing, setEditing] = useState(null); // null | {} (crear) | usuario (editar)
  const [toDelete, setToDelete] = useState(null);
  const [busy, setBusy] = useState(false);

  const { status, data, error, reload } = useRemote({
    fetcher: () =>
      listUsers({
        page,
        size: PAGE_SIZE,
        sort: 'name,asc',
        role: applied.role || undefined,
        text: applied.text || undefined,
      }),
    fallback: () =>
      mockUsersPage({
        page,
        size: PAGE_SIZE,
        sort: 'name,asc',
        role: applied.role || undefined,
        text: applied.text,
      }),
    deps: [page, applied],
  });

  const content = data?.content || [];
  const totalPages = data?.totalPages || 1;
  const currentPage = data?.page ?? page;

  function applyFilter(e) {
    e.preventDefault();
    setApplied({ text: textFilter.trim(), role: roleFilter });
    setPage(0);
  }

  async function saveUser(payload) {
    const isNew = !editing?.id;
    setBusy(true);
    try {
      const { mode } = await mutateWithFallback(
        () => (isNew ? createUser(payload) : updateUser(editing.id, payload)),
        () => (isNew ? mockCreateUser(payload) : mockUpdateUser(editing.id, payload))
      );
      showToast(
        mode === 'real'
          ? 'Usuario guardado.'
          : 'Demo (mock): guardado local; el endpoint /admin/users todavía no existe.'
      );
      setEditing(null);
      reload();
    } catch (err) {
      return err?.response?.data || { detail: 'No se pudo guardar el usuario.' };
    } finally {
      setBusy(false);
    }
    return null;
  }

  async function confirmDelete() {
    setBusy(true);
    try {
      const { mode } = await mutateWithFallback(
        () => removeUser(toDelete.id),
        () => mockRemoveUser(toDelete.id)
      );
      showToast(
        mode === 'real'
          ? 'Usuario eliminado.'
          : 'Demo (mock): eliminación local; el endpoint /admin/users todavía no existe.'
      );
      setToDelete(null);
      reload();
    } catch (err) {
      showToast(err?.response?.data?.detail || 'No se pudo eliminar el usuario.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <>
      <PageHeader
        eyebrow="PORTAL / ADMINISTRADOR"
        title="Administración de usuarios"
        subtitle="Crea y administra cuentas."
      />

      {status === REMOTE_STATUS.fallback ? <MockNotice what="la administración de usuarios" /> : null}

      {status === REMOTE_STATUS.error ? (
        <ErrorState message={error?.detail} onRetry={reload} />
      ) : status === REMOTE_STATUS.loading ? (
        <Spinner />
      ) : (
        <>
          <div className="filters">
            <form className="filters" onSubmit={applyFilter}>
              <input
                className="input search"
                placeholder="Buscar nombre o correo"
                aria-label="Buscar nombre o correo"
                value={textFilter}
                onChange={(e) => setTextFilter(e.target.value)}
              />
              <select
                className="input"
                aria-label="Rol"
                value={roleFilter}
                onChange={(e) => setRoleFilter(e.target.value)}
              >
                <option value="">Todos los roles</option>
                <option value="ADMIN">Administrador</option>
                <option value="TEACHER">Profesor</option>
                <option value="STUDENT">Estudiante</option>
              </select>
              <button className="btn alt small" type="submit">
                Filtrar
              </button>
            </form>
            <button className="btn small" type="button" onClick={() => setEditing({})}>
              Crear usuario
            </button>
          </div>

          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Nombre</th>
                  <th>Correo</th>
                  <th>Rol</th>
                  <th>Teléfono</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {content.length ? (
                  content.map((u) => (
                    <tr key={u.id}>
                      <td>{u.name}</td>
                      <td>{u.email}</td>
                      <td>{roleLabel(u.role)}</td>
                      <td>{u.phone || '—'}</td>
                      <td>
                        <div className="btn-row">
                          <button className="btn alt small" type="button" onClick={() => setEditing(u)}>
                            Editar
                          </button>
                          <button className="btn rust small" type="button" onClick={() => setToDelete(u)}>
                            Eliminar
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={5}>
                      <span className="muted">Sin usuarios para este filtro.</span>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          <div className="pagination">
            <span>
              Página {currentPage + 1} de {totalPages}
              {data?.totalElements != null ? ` · ${data.totalElements} usuarios` : ''}
            </span>
            <div className="btn-row">
              <button
                className="btn alt small"
                type="button"
                disabled={currentPage <= 0}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
              >
                Anterior
              </button>
              <button
                className="btn alt small"
                type="button"
                disabled={currentPage >= totalPages - 1}
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              >
                Siguiente
              </button>
            </div>
          </div>
        </>
      )}

      <UserModal user={editing} busy={busy} onClose={() => setEditing(null)} onSave={saveUser} />

      <ConfirmModal
        open={Boolean(toDelete)}
        title="Eliminar usuario"
        description={toDelete ? `¿Quieres eliminar la cuenta de ${toDelete.name}?` : ''}
        confirmLabel="Eliminar"
        busy={busy}
        onConfirm={confirmDelete}
        onCancel={() => setToDelete(null)}
      />
    </>
  );
}

/** Alta/edición en modal (estética .overlay/.modal del Figma), errores bajo
 *  cada input en tiempo real (react-ui-rules). La contraseña solo se pide en
 *  alta; en edición es opcional (si viene vacía no se envía). */
function UserModal({ user, busy, onClose, onSave }) {
  const isNew = !user?.id;
  const [values, setValues] = useState({
    name: user?.name || '',
    email: user?.email || '',
    password: '',
    role: user?.role || '',
    phone: user?.phone || '',
  });
  const [errors, setErrors] = useState({ name: '', email: '', password: '', role: '', phone: '' });
  const [touched, setTouched] = useState({});
  const [submitError, setSubmitError] = useState('');

  const rules = {
    name: (v) => required(v, 'El nombre'),
    email: (v) => required(v, 'El correo') || validateEmail(v),
    password: (v) => {
      if (!isNew && !String(v || '').trim()) return ''; // en edición es opcional
      return validatePassword(v);
    },
    role: (v) => required(v, 'El rol'),
    phone: (v) => (String(v || '').trim().length > 25 ? 'Máximo 25 caracteres.' : ''),
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setValues((v) => ({ ...v, [name]: value }));
    setSubmitError('');
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
    setTouched({ name: true, email: true, password: true, role: true, phone: true });
    if (Object.values(nextErrors).some(Boolean)) return;

    const payload = {
      name: values.name.trim(),
      email: values.email.trim(),
      role: values.role,
      ...(values.phone.trim() ? { phone: values.phone.trim() } : {}),
    };
    if (isNew || values.password.trim()) payload.password = values.password;

    const apiError = await onSave(payload);
    if (apiError) {
      const fieldErrors = {};
      (apiError.fieldErrors || []).forEach((f) => {
        if (f.field in values) fieldErrors[f.field] = f.message;
      });
      setErrors((prev) => ({ ...prev, ...fieldErrors }));
      setSubmitError(fieldErrors.name || fieldErrors.email ? '' : apiError.detail || 'No se pudo guardar el usuario.');
    }
  }

  if (!user) return null;

  return (
    <div
      className="overlay"
      onClick={(e) => {
        if (e.target === e.currentTarget && !busy) onClose();
      }}
    >
      <section className="modal" role="dialog" aria-modal="true" aria-labelledby="user-modal-title">
        <p className="eyebrow">ENGLISH LAB / {isNew ? 'NUEVO' : 'EDITAR'} USUARIO</p>
        <h2 id="user-modal-title">{isNew ? 'Crear usuario' : 'Editar usuario'}</h2>
        <form onSubmit={handleSubmit} noValidate>
          <Input
            label="Nombre"
            name="name"
            placeholder="Nombre y apellidos"
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
            value={values.email}
            onChange={handleChange}
            onBlur={handleBlur}
            error={touched.email ? errors.email : ''}
          />
          <Input
            label={isNew ? 'Contraseña' : 'Contraseña (opcional)'}
            name="password"
            type="password"
            placeholder={isNew ? 'Mín. 8 caracteres' : 'Dejar vacía para no cambiarla'}
            help="Debe incluir mayúscula, número y carácter especial."
            value={values.password}
            onChange={handleChange}
            onBlur={handleBlur}
            error={touched.password ? errors.password : ''}
          />
          <div>
            <label className="form-label" htmlFor="user-role">Rol</label>
            <select id="user-role" className="input" name="role" value={values.role} onChange={handleChange} onBlur={handleBlur}>
              <option value="">Selecciona…</option>
              {ROLES.map((r) => (
                <option key={r} value={r}>
                  {roleLabel(r)}
                </option>
              ))}
            </select>
            {touched.role && errors.role ? <p className="error" role="alert">{errors.role}</p> : null}
          </div>
          <Input
            label="Teléfono (opcional)"
            name="phone"
            placeholder="+34 600 000 000"
            value={values.phone}
            onChange={handleChange}
            onBlur={handleBlur}
            error={touched.phone ? errors.phone : ''}
          />
          {submitError ? <p className="form-status" role="alert">{submitError}</p> : null}
          <div className="btn-row" style={{ justifyContent: 'flex-end', marginTop: 24 }}>
            <button className="btn alt" type="button" onClick={onClose} disabled={busy}>
              Cancelar
            </button>
            <button className="btn" type="submit" disabled={busy}>
              {busy ? 'Guardando…' : 'Guardar'}
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}
