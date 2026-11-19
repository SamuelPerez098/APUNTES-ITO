import { useState } from 'react';
import PageHeader from '../../components/PageHeader.jsx';
import Badge from '../../components/Badge.jsx';
import MockNotice from '../../components/MockNotice.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import Spinner from '../../components/Spinner.jsx';
import { useRemote, REMOTE_STATUS } from '../../api/useRemote.js';
import { listStudents, getStudentProgress } from '../../api/teacher.js';
import { mockStudentsPage } from '../../mock/mockServer.js';
import { useAuth } from '../../auth/AuthContext.jsx';

/** 14 · Resultados (pageTeacherResults del Figma, HTML:83), compartida.
 *  Conectado (ronda 2, pendiente de backend): GET /teacher/students (listado de
 *  alumnos) y GET /teacher/students/{id}/progress (progreso bajo demanda, sin
 *  N+1: se pide solo al pulsar "Ver progreso"). */
export default function TeacherResults() {
  const { role } = useAuth();

  const students = useRemote({
    fetcher: () => listStudents({ page: 0, size: 100, sort: 'name,asc' }),
    fallback: () => mockStudentsPage({ page: 0, size: 100, sort: 'name,asc' }),
  });

  const [selected, setSelected] = useState(null);
  const [progress, setProgress] = useState(null);
  const [progressBusy, setProgressBusy] = useState(false);
  const [progressError, setProgressError] = useState('');

  const rows = Array.isArray(students.data) ? students.data : students.data?.content || [];

  async function openProgress(student) {
    setSelected(student);
    setProgress(null);
    setProgressError('');
    setProgressBusy(true);
    try {
      const { data } = await getStudentProgress(student.id);
      setProgress(data);
    } catch (err) {
      if (err?.response?.status === 404) {
        setProgress({ demo: true });
      } else {
        setProgressError(err?.response?.data?.detail || 'No se pudo cargar el progreso.');
      }
    } finally {
      setProgressBusy(false);
    }
  }

  return (
    <>
      <PageHeader
        eyebrow={role === 'ADMIN' ? 'PORTAL / ADMINISTRADOR' : 'PORTAL / PROFESOR'}
        title="Resultados de estudiantes"
        subtitle="Consulta los intentos y las calificaciones de las actividades asignadas."
      />
      {students.status === REMOTE_STATUS.fallback ? <MockNotice what="los resultados de estudiantes" /> : null}

      {students.status === REMOTE_STATUS.error ? (
        <ErrorState message={students.error?.detail} onRetry={students.reload} />
      ) : students.status === REMOTE_STATUS.loading ? (
        <Spinner />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Estudiante</th>
                <th>Correo</th>
                <th>Acción</th>
              </tr>
            </thead>
            <tbody>
              {rows.length ? (
                rows.map((s) => (
                  <tr key={s.id}>
                    <td>{s.name}</td>
                    <td>{s.email || '—'}</td>
                    <td>
                      <button className="btn small" type="button" onClick={() => openProgress(s)}>
                        Ver progreso
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={3}>
                    <span className="muted">Aún no tienes estudiantes vinculados.</span>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {selected ? (
        <div className="overlay" onClick={(e) => e.target === e.currentTarget && setSelected(null)}>
          <section className="modal" role="dialog" aria-modal="true" aria-labelledby="progress-title">
            <p className="eyebrow">ENGLISH LAB / PROGRESO</p>
            <h2 id="progress-title">{selected.name}</h2>
            {progressBusy ? (
              <Spinner small />
            ) : progressError ? (
              <p className="error" role="alert">{progressError}</p>
            ) : progress?.demo ? (
              <p className="muted">
                Datos de muestra — el endpoint de progreso todavía no existe.
              </p>
            ) : progress ? (
              <ProgressSummary data={progress} />
            ) : null}
            <div className="btn-row" style={{ justifyContent: 'flex-end', marginTop: 24 }}>
              <button className="btn alt" type="button" onClick={() => setSelected(null)}>
                Cerrar
              </button>
            </div>
          </section>
        </div>
      ) : null}
    </>
  );
}

/** Resumen defensivo del progreso: el shape exacto del endpoint está por
 *  confirmar, así que se muestran los campos habituales si existen. */
function ProgressSummary({ data }) {
  const accuracy = data.accuracy ?? data.precision ?? data.mastery;
  const attempts = data.attempts ?? data.totalAttempts ?? data.completed;
  const items = Array.isArray(data.content) ? data.content : Array.isArray(data) ? data : [];
  return (
    <div>
      <div className="grid g2">
        <div className="card">
          <div className="stat-label">Precisión</div>
          <div className="stat-number">
            {accuracy != null ? `${Math.round(Number(accuracy) * 100)}%` : '—'}
          </div>
        </div>
        <div className="card">
          <div className="stat-label">Intentos</div>
          <div className="stat-number">{attempts != null ? attempts : '—'}</div>
        </div>
      </div>
      {items.length ? (
        <div className="table-wrap" style={{ marginTop: 16 }}>
          <table>
            <thead>
              <tr>
                <th>Elemento</th>
                <th>Resultado</th>
              </tr>
            </thead>
            <tbody>
              {items.map((it, i) => (
                <tr key={it.id ?? i}>
                  <td>{it.title ?? it.activityTitle ?? it.name ?? `#${i + 1}`}</td>
                  <td>
                    <Badge color={it.correct || it.mastery >= 0.65 ? 'green' : 'red'}>
                      {it.correct != null ? (it.correct ? 'Correcta' : 'Incorrecta') : it.mastery != null ? `${Math.round(Number(it.mastery) * 100)}%` : '—'}
                    </Badge>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}
    </div>
  );
}
