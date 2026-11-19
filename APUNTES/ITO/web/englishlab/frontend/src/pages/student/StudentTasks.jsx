import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import PageHeader from '../../components/PageHeader.jsx';
import Badge from '../../components/Badge.jsx';
import MockNotice from '../../components/MockNotice.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import Spinner from '../../components/Spinner.jsx';
import { useRemote, REMOTE_STATUS } from '../../api/useRemote.js';
import { listAssignments, listObjectives } from '../../api/learning.js';
import { mockAssignments, mockObjectives } from '../../mock/mockServer.js';
import { skillLabel } from '../../lib/constants.js';

const COMPLETED = ['COMPLETED', 'DONE', 'Completada'];

function stateLabel(state) {
  const map = { IN_PROGRESS: 'En curso', COMPLETED: 'Completado', LOCKED: 'Bloqueado', UNLOCKED: 'Disponible' };
  return map[state] || state || '—';
}

/** 06 · Tareas y práctica libre (pageCatalog del Figma, HTML:66).
 *  Conectado (ronda 2):
 *   - "Mis tareas"     → GET /me/assignments (asignaciones con ActivityStudentDto:
 *                        prompt, options, title, type, SIN answer/accepted).
 *   - "Práctica libre" → GET /me/objectives ({objectiveId,code,name,cefrLevel,skill,mastery,state}).
 *  Patrón mock/real: 404 → mock con <MockNotice/>; 401/403/5xx → <ErrorState/>. */
export default function StudentTasks() {
  const navigate = useNavigate();
  const [tab, setTab] = useState('tasks');

  const { status, data, error, reload } = useRemote({
    fetcher: () => (tab === 'tasks' ? listAssignments() : listObjectives()),
    fallback: () => (tab === 'tasks' ? mockAssignments() : mockObjectives()),
    deps: [tab],
  });

  const raw = Array.isArray(data) ? data : data?.content || [];

  const taskRows = tab === 'tasks'
    ? raw.map((a) => ({
        id: a.id,
        activityId: a.activityId ?? a.activity?.id ?? a.id,
        activity: a.activity || null,
        activityTitle: a.activity?.title ?? a.activityTitle ?? a.title,
        status: a.status,
        dueDate: a.dueDate ?? a.due,
      }))
    : [];

  const objectiveRows = tab === 'free'
    ? raw.map((o) => ({
        objectiveId: o.objectiveId ?? o.id,
        code: o.code,
        name: o.name,
        cefrLevel: o.cefrLevel,
        skill: o.skill,
        mastery: o.mastery,
        state: o.state,
      }))
    : [];

  function openTask(row) {
    const done = COMPLETED.includes(row.status);
    if (done) {
      navigate(`/estudiante/resultado/${row.activityId}`, { state: { assignmentId: row.id, activityId: row.activityId } });
    } else {
      navigate(`/estudiante/actividad/${row.activityId}`, {
        state: {
          assignmentId: row.id,
          activityId: row.activityId,
          activity: row.activity
            ? { id: row.activityId, title: row.activity.title, type: row.activity.type, prompt: row.activity.prompt, options: row.activity.options }
            : null,
        },
      });
    }
  }

  return (
    <>
      <PageHeader
        eyebrow="PORTAL / ESTUDIANTE"
        title="Mis tareas y práctica libre"
        subtitle="Accede a actividades asignadas o elige ejercicios para practicar por tu cuenta."
      />
      {status === REMOTE_STATUS.fallback ? <MockNotice what="el catálogo de tareas" /> : null}

      <div className="tabs">
        <button className={`tab${tab === 'tasks' ? ' active' : ''}`} type="button" onClick={() => setTab('tasks')}>
          Mis tareas
        </button>
        <button className={`tab${tab === 'free' ? ' active' : ''}`} type="button" onClick={() => setTab('free')}>
          Práctica libre
        </button>
      </div>

      {status === REMOTE_STATUS.error ? (
        <ErrorState message={error?.detail} onRetry={reload} />
      ) : status === REMOTE_STATUS.loading ? (
        <Spinner />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                {tab === 'tasks' ? (
                  <>
                    <th>Actividad</th>
                    <th>Tipo</th>
                    <th>Estado</th>
                    <th>Entrega</th>
                    <th>Acción</th>
                  </>
                ) : (
                  <>
                    <th>Objetivo</th>
                    <th>Nivel</th>
                    <th>Habilidad</th>
                    <th>Dominio</th>
                    <th>Estado</th>
                  </>
                )}
              </tr>
            </thead>
            <tbody>
              {tab === 'tasks' ? (
                taskRows.length ? (
                  taskRows.map((r) => (
                    <tr key={r.id}>
                      <td>{r.activityTitle}</td>
                      <td><Badge color="gray">{r.activity?.type || '—'}</Badge></td>
                      <td>
                        <Badge color={COMPLETED.includes(r.status) ? 'green' : 'gold'}>
                          {COMPLETED.includes(r.status) ? 'Completada' : 'Pendiente'}
                        </Badge>
                      </td>
                      <td>{r.dueDate || '—'}</td>
                      <td>
                        <button className="btn small" type="button" onClick={() => openTask(r)}>
                          {COMPLETED.includes(r.status) ? 'Ver resultado' : 'Resolver'}
                        </button>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={5}><span className="muted">No hay tareas asignadas.</span></td>
                  </tr>
                )
              ) : objectiveRows.length ? (
                objectiveRows.map((o) => (
                  <tr key={o.objectiveId}>
                    <td>{o.code ? `${o.code} — ${o.name}` : o.name}</td>
                    <td><Badge color="gray">{o.cefrLevel || '—'}</Badge></td>
                    <td>{skillLabel(o.skill)}</td>
                    <td>
                      <Badge color={Number(o.mastery || 0) >= 0.65 ? 'green' : 'gold'}>
                        {o.mastery != null ? `${Math.round(Number(o.mastery) * 100)}%` : '—'}
                      </Badge>
                    </td>
                    <td><Badge color="gray">{stateLabel(o.state)}</Badge></td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={5}><span className="muted">No hay objetivos para mostrar.</span></td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}
