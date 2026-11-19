import { useState } from 'react';
import PageHeader from '../../components/PageHeader.jsx';
import MockNotice from '../../components/MockNotice.jsx';
import Input from '../../components/Input.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import Spinner from '../../components/Spinner.jsx';
import { useToast } from '../../components/Toast.jsx';
import { useRemote, mutateWithFallback, REMOTE_STATUS } from '../../api/useRemote.js';
import { listStudents, linkStudent, createAssignment } from '../../api/teacher.js';
import { listActivities } from '../../api/activities.js';
import { mockStudentsPage, mockLinkStudent, mockCreateAssignment, mockActivitiesPage } from '../../mock/mockServer.js';
import { required } from '../../lib/validators.js';
import { useAuth } from '../../auth/AuthContext.jsx';

/** 13 · Asignar tarea (pageAssign del Figma, HTML:81).
 *  Conectado (ronda 2, pendiente de backend):
 *   - GET /teacher/students (lista de alumnos) y POST /teacher/students {email}
 *     (vincular un alumno existente por correo).
 *   - GET /content/activities (lista de actividades) para el select.
 *   - POST /teacher/assignments {studentId, activityId}.
 *  El campo "fecha límite" se conserva del Figma pero el contrato actual de la
 *  asignación solo lleva studentId + activityId, así que no se envía. */
export default function AssignmentForm() {
  const { role } = useAuth();
  const { showToast } = useToast();

  const students = useRemote({
    fetcher: () => listStudents({ page: 0, size: 100, sort: 'name,asc' }),
    fallback: () => mockStudentsPage({ page: 0, size: 100, sort: 'name,asc' }),
  });
  const activities = useRemote({
    fetcher: () => listActivities({ page: 0, size: 100, sort: 'title,asc' }),
    fallback: () => mockActivitiesPage({ page: 0, size: 100, sort: 'title,asc' }),
  });

  const studentRows = Array.isArray(students.data) ? students.data : students.data?.content || [];
  const activityRows = Array.isArray(activities.data) ? activities.data : activities.data?.content || [];

  const [values, setValues] = useState({ student: '', activity: '', dueDate: '' });
  const [touched, setTouched] = useState({});
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);

  const [linkEmail, setLinkEmail] = useState('');
  const [linkError, setLinkError] = useState('');
  const [linking, setLinking] = useState(false);

  const rules = {
    student: (v) => required(v, 'El estudiante'),
    activity: (v) => required(v, 'La actividad'),
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
    const nextErrors = { student: rules.student(values.student), activity: rules.activity(values.activity) };
    setErrors(nextErrors);
    setTouched({ student: true, activity: true });
    if (Object.values(nextErrors).some(Boolean)) return;

    setBusy(true);
    try {
      const { mode } = await mutateWithFallback(
        () => createAssignment({ studentId: Number(values.student), activityId: Number(values.activity) }),
        () => mockCreateAssignment({ studentId: Number(values.student), activityId: Number(values.activity) })
      );
      showToast(
        mode === 'real'
          ? 'Tarea asignada.'
          : 'Demo (mock): asignación local; el endpoint /teacher/assignments todavía no existe.'
      );
      setValues({ student: '', activity: '', dueDate: '' });
      setTouched({});
    } catch (err) {
      showToast(err?.response?.data?.detail || 'No se pudo asignar la tarea.');
    } finally {
      setBusy(false);
    }
  }

  async function handleLink(e) {
    e.preventDefault();
    const err = required(linkEmail, 'El correo');
    setLinkError(err);
    if (err) return;
    setLinking(true);
    try {
      const { mode } = await mutateWithFallback(
        () => linkStudent({ email: linkEmail.trim() }),
        () => mockLinkStudent({ email: linkEmail.trim() })
      );
      if (mode === 'fallback') {
        showToast('Demo (mock): vinculación local; el endpoint /teacher/students todavía no existe.');
      } else {
        showToast('Estudiante vinculado.');
      }
      setLinkEmail('');
      students.reload();
    } catch (err) {
      const apiError = err?.response?.data || {};
      if (err?.response?.status === 404) showToast('No existe un estudiante con ese correo.');
      else showToast(apiError.detail || 'No se pudo vincular al estudiante.');
    } finally {
      setLinking(false);
    }
  }

  const selectField = (id, label, options, name, value) => (
    <div>
      <label className="form-label" htmlFor={id}>{label}</label>
      <select id={id} className="input" name={name} value={value} onChange={handleChange} onBlur={handleBlur}>
        <option value="">Selecciona…</option>
        {options.map((o) => (
          <option key={o.value} value={o.value}>
            {o.label}
          </option>
        ))}
      </select>
      {touched[name] && errors[name] ? <p className="error" role="alert">{errors[name]}</p> : null}
    </div>
  );

  const usingMock =
    students.status === REMOTE_STATUS.fallback || activities.status === REMOTE_STATUS.fallback;

  /** Listo = datos reales o mock de contrato; loading/error no dejan elegir. */
  const remoteReady = (remote) =>
    remote.status === REMOTE_STATUS.real || remote.status === REMOTE_STATUS.fallback;

  const formReady = remoteReady(students) && remoteReady(activities);

  /** Select con sus tres estados: error → ErrorState con reintento (nunca un
   *  select vacío en silencio), loading → Spinner, real/fallback → opciones. */
  const selectOrState = (remote, { id, label, options, name }) => {
    if (remote.status === REMOTE_STATUS.error) {
      return (
        <ErrorState
          message={remote.error?.detail || `No se pudo cargar la lista de ${label.toLowerCase()}.`}
          onRetry={remote.reload}
        />
      );
    }
    if (!remoteReady(remote)) {
      return (
        <div>
          <label className="form-label" htmlFor={id}>{label}</label>
          <div className="spinner-wrap">
            <Spinner small label="" />
          </div>
        </div>
      );
    }
    return selectField(id, label, options, name, values[name]);
  };

  return (
    <>
      <PageHeader
        eyebrow={role === 'ADMIN' ? 'PORTAL / ADMINISTRADOR' : 'PORTAL / PROFESOR'}
        title="Asignar actividad"
        subtitle="Selecciona a un estudiante, una actividad y una fecha límite."
      />
      {usingMock ? <MockNotice what="la asignación de tareas" /> : null}
      <form className="card light assign-card" onSubmit={handleSubmit} noValidate>
        {selectOrState(students, {
          id: 'assign-student',
          label: 'Estudiante',
          name: 'student',
          options: studentRows.map((s) => ({ value: s.id, label: `${s.name} (${s.email || 'sin correo'})` })),
        })}
        {selectOrState(activities, {
          id: 'assign-activity',
          label: 'Actividad',
          name: 'activity',
          options: activityRows.map((a) => ({ value: a.id, label: `${a.title} · ${a.level || ''}` })),
        })}
        <div>
          <label className="form-label" htmlFor="assign-date">Fecha límite (no se envía aún)</label>
          <input
            id="assign-date"
            className="input"
            type="date"
            name="dueDate"
            value={values.dueDate}
            onChange={handleChange}
            onBlur={handleBlur}
          />
        </div>
        <button className="btn wide" type="submit" style={{ marginTop: 14 }} disabled={busy || !formReady}>
          {busy ? 'Asignando…' : 'Asignar actividad'}
        </button>
        {!formReady && students.status !== REMOTE_STATUS.loading && activities.status !== REMOTE_STATUS.loading ? (
          <p className="muted" style={{ marginTop: 10 }}>
            Revisa los avisos de arriba para cargar las listas.
          </p>
        ) : null}
      </form>

      <form className="card light assign-card" onSubmit={handleLink} noValidate>
        <h3 className="card-title">Vincular estudiante por correo</h3>
        <p className="card-sub">
          Agrega un alumno existente (rol ESTUDIANTE) a tu lista de estudiantes.
        </p>
        <Input
          label="Correo del estudiante"
          name="linkEmail"
          type="email"
          placeholder="estudiante@correo.com"
          value={linkEmail}
          onChange={(e) => {
            setLinkEmail(e.target.value);
            setLinkError('');
          }}
          error={linkError}
        />
        <button className="btn" type="submit" style={{ marginTop: 14 }} disabled={linking}>
          {linking ? 'Vinculando…' : 'Vincular estudiante'}
        </button>
      </form>
    </>
  );
}
