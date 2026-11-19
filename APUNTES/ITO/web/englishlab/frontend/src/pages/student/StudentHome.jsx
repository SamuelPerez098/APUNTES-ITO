import { useNavigate } from 'react-router-dom';
import PageHeader from '../../components/PageHeader.jsx';
import Badge from '../../components/Badge.jsx';
import MockNotice from '../../components/MockNotice.jsx';
import MasteryChart from '../../components/MasteryChart.jsx';
import { useRemote, REMOTE_STATUS } from '../../api/useRemote.js';
import { listAssignments, listObjectives } from '../../api/learning.js';
import { mockAssignments } from '../../mock/mockServer.js';

const COMPLETED = ['COMPLETED', 'DONE', 'Completada'];

/** Inicio del estudiante (pageStudentHome del Figma, HTML:61).
 *  Tareas recientes: GET /me/assignments (conserva el patrón mock/real).
 *  Dominio por nivel: GET /me/objectives → 14 objetivos con `previousMastery`
 *  (dominio antes del último intento) para la gráfica de barras. */
export default function StudentHome() {
  const navigate = useNavigate();

  const objectives = useRemote({ fetcher: () => listObjectives() });
  const assignments = useRemote({
    fetcher: () => listAssignments(),
    fallback: () => mockAssignments(),
  });

  const taskRows = Array.isArray(assignments.data) ? assignments.data : assignments.data?.content || [];

  const objectiveRows = Array.isArray(objectives.data)
    ? objectives.data
    : objectives.data?.content || [];

  /** Dominio medio: media de `mastery` de los objetivos (se muestra en línea,
   *  sin tarjeta, junto al saludo). */
  const averageMastery = objectiveRows.length
    ? Math.round(
        (objectiveRows.reduce((sum, o) => sum + Number(o.mastery || 0), 0) / objectiveRows.length) * 100
      )
    : null;

  const usingMock = assignments.status === REMOTE_STATUS.fallback;

  return (
    <>
      <PageHeader
        eyebrow="PORTAL / ESTUDIANTE"
        title="Tu espacio para aprender"
        subtitle="Practica un poco cada día. Tus ejercicios, tareas y resultados están aquí."
      />
      {usingMock ? <MockNotice what="el panel del estudiante" /> : null}

      <div className="mastery-inline">
        <span>Dominio medio:</span>
        <b>{averageMastery == null ? '—' : `${averageMastery}%`}</b>
        {averageMastery != null ? (
          <div
            className="progress-track"
            role="img"
            aria-label={`Dominio medio: ${averageMastery}%`}
          >
            <div
              className="progress-fill"
              style={{ width: `${Math.min(100, Math.max(0, averageMastery))}%` }}
            />
          </div>
        ) : null}
      </div>

      <div className="grid g2">
        <div className="card">
          <h3 className="card-title">Dominio por nivel</h3>
          <MasteryChart
            status={objectives.status}
            rows={objectiveRows}
            error={objectives.error}
            onRetry={objectives.reload}
          />
        </div>
        <div className="card">
          <h3 className="card-title">Tareas recientes</h3>
          {taskRows.length ? (
            taskRows.slice(0, 3).map((t) => {
              const done = COMPLETED.includes(t.status);
              return (
                <div className="panel-row" key={t.id}>
                  <div>
                    <b>{t.activity?.title ?? t.activityTitle ?? t.title}</b>
                    <div className="smallprint">Profesor · {t.dueDate ?? t.due ?? 'Sin fecha'}</div>
                  </div>
                  <Badge color={done ? 'green' : 'gold'}>{done ? 'Completada' : 'Pendiente'}</Badge>
                </div>
              );
            })
          ) : (
            <p className="muted">Aún no tienes tareas asignadas.</p>
          )}
          <button className="btn alt small" type="button" onClick={() => navigate('/estudiante/tareas')}>
            Ver todas las tareas
          </button>
        </div>
      </div>
    </>
  );
}
