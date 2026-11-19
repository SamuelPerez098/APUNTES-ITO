import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import PageHeader from '../../components/PageHeader.jsx';
import Badge from '../../components/Badge.jsx';
import Spinner from '../../components/Spinner.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import { useToast } from '../../components/Toast.jsx';
import { useRemote, REMOTE_STATUS } from '../../api/useRemote.js';
import { extractApiError } from '../../api/client.js';
import { listObjectives, getPracticeSession } from '../../api/learning.js';
import { skillLabel } from '../../lib/constants.js';

/** Estados de objetivo desde los que se puede practicar: el backend responde 422
 *  cuando el objetivo está BLOCKED por prerrequisitos. */
const PRACTICABLE_STATES = ['AVAILABLE', 'RECOMMENDED', 'MASTERED'];

/** Preguntas de una sesión de práctica (el backend elige las actividades). */
const PRACTICE_SIZE = 10;

/** Dificultades del RETO: segundos por pregunta. Mismo estilo del diseño, sin
 *  pantalla nueva: la elección aparece bajo el objetivo. */
const CHALLENGE_LEVELS = [
  { seconds: 30, label: 'Fácil' },
  { seconds: 20, label: 'Medio' },
  { seconds: 10, label: 'Experto' },
];

const STATE_LABELS = {
  AVAILABLE: 'Disponible',
  RECOMMENDED: 'Recomendado',
  MASTERED: 'Dominado',
  BLOCKED: 'Bloqueado',
  LOCKED: 'Bloqueado',
  IN_PROGRESS: 'En progreso',
};

function stateLabel(code) {
  return STATE_LABELS[code] || code || '—';
}

/** 09 · Mi progreso (pageProgress del Figma, HTML:73).
 *  Todo real: GET /me/objectives ({objectiveId,code,name,cefrLevel,skill,mastery,
 *  state}) alimenta el dominio por objetivo. "Practicar" pide la sesión al
 *  servidor (GET /me/objectives/{id}/practice-session?size=10 → {activityIds}) y
 *  navega DIRECTO al reproductor con la cola de preguntas en state.activityIds;
 *  no hay modal ni pantalla intermedia. "Reto" abre una mini-selección de
 *  dificultad (30/20/10 s por pregunta) y hace la misma llamada añadiendo
 *  state.challengeSeconds. El servidor califica cada intento. */
export default function StudentProgress() {
  const navigate = useNavigate();
  const { showToast } = useToast();
  /** objectiveId cuya sesión se está pidiendo ("Preparando…"). */
  const [starting, setStarting] = useState(null); // {objectiveId, challengeSeconds|null}
  /** objectiveId con la mini-selección de dificultad del RETO abierta. */
  const [challengeFor, setChallengeFor] = useState(null);
  /** Fallo de la sesión (no 422): se reintenta desde la misma tarjeta. */
  const [sessionError, setSessionError] = useState(null);

  const objectives = useRemote({ fetcher: () => listObjectives() });

  const rows = (Array.isArray(objectives.data) ? objectives.data : objectives.data?.content || [])
    .map((o) => ({
      objectiveId: o.objectiveId ?? o.id,
      code: o.code,
      name: o.name,
      cefrLevel: o.cefrLevel,
      skill: o.skill,
      mastery: o.mastery,
      state: o.state,
    }));

  const stats = rows.length
    ? {
        total: rows.length,
        avg: Math.round(rows.reduce((s, o) => s + Number(o.mastery || 0), 0) / rows.length * 100),
      }
    : { total: 0, avg: 0 };

  /** `challengeSeconds` null/undefined = práctica normal; 30/20/10 = RETO. */
  async function startPractice(objectiveId, challengeSeconds = null) {
    if (starting != null) return;
    setStarting({ objectiveId, challengeSeconds });
    setSessionError(null);
    try {
      const { data } = await getPracticeSession(objectiveId, { size: PRACTICE_SIZE });
      const activityIds = (Array.isArray(data?.activityIds) ? data.activityIds : []).filter(
        (value) => value != null
      );
      if (!activityIds.length) {
        showToast('Este objetivo todavía no tiene actividades para practicar.');
        return;
      }
      navigate(`/estudiante/actividad/${activityIds[0]}`, {
        state: {
          activityIds,
          ...(challengeSeconds != null ? { challengeSeconds } : {}),
        },
      });
    } catch (err) {
      const apiError = extractApiError(err);
      if (apiError.status === 422) {
        // Objetivo BLOCKED por prerrequisitos: el detalle explica el motivo.
        showToast(apiError.detail);
      } else {
        setSessionError({ objectiveId, challengeSeconds, detail: apiError.detail });
      }
    } finally {
      setStarting(null);
    }
  }

  function toggleChallenge(objectiveId) {
    if (starting != null) return;
    setChallengeFor((current) => (current === objectiveId ? null : objectiveId));
  }

  return (
    <>
      <PageHeader
        eyebrow="PORTAL / ESTUDIANTE"
        title="Mi progreso"
        subtitle="Consulta tu dominio por objetivo y lanza una sesión de práctica cuando quieras."
      />

      <div className="grid g3">
        <div className="card">
          <span className="stat-label">Objetivos</span>
          <div className="stat-number">{stats.total}</div>
        </div>
        <div className="card">
          <span className="stat-label">Dominio medio</span>
          <div className="stat-number">{stats.avg}%</div>
        </div>
        <div className="card">
          <span className="stat-label">Recomendado</span>
          <div className="stat-number">{rows.filter((o) => Number(o.mastery || 0) < 0.65).length}</div>
        </div>
      </div>

      {sessionError ? (
        <div className="card mt">
          <ErrorState
            message={sessionError.detail || 'No se pudo preparar la sesión de práctica.'}
            onRetry={() => startPractice(sessionError.objectiveId, sessionError.challengeSeconds)}
          />
        </div>
      ) : null}

      <div className="card mt">
        <h3 className="card-title">Dominio por objetivo</h3>
        {objectives.status === REMOTE_STATUS.loading ? (
          <Spinner small />
        ) : objectives.status === REMOTE_STATUS.error ? (
          <ErrorState
            message={objectives.error?.detail || 'No se pudo cargar tu progreso.'}
            onRetry={objectives.reload}
          />
        ) : rows.length ? (
          rows.map((o) => {
            const canPractice =
              o.objectiveId != null && PRACTICABLE_STATES.includes(String(o.state || '').toUpperCase());
            const startingNormal =
              starting?.objectiveId === o.objectiveId && starting.challengeSeconds == null;
            const startingChallenge =
              starting?.objectiveId === o.objectiveId && starting.challengeSeconds != null;
            const challengeOpen = challengeFor === o.objectiveId;
            return (
              <div key={o.objectiveId}>
                <div className="barline">
                  <b>{o.code ? `${o.code} — ${o.name}` : o.name}</b>
                  <span>{Math.round(Number(o.mastery || 0) * 100)}%</span>
                </div>
                <div className="meter">
                  <div style={{ width: `${Math.round(Number(o.mastery || 0) * 100)}%` }} />
                </div>
                <div
                  className="smallprint"
                  style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 10, flexWrap: 'wrap' }}
                >
                  <span>{skillLabel(o.skill)} · {o.cefrLevel || '—'}</span>
                  {canPractice ? (
                    <div className="btn-row">
                      <button
                        className="btn small"
                        type="button"
                        disabled={starting != null}
                        onClick={() => startPractice(o.objectiveId)}
                      >
                        {startingNormal ? 'Preparando…' : 'Practicar'}
                      </button>
                      <button
                        className="btn small olive"
                        type="button"
                        disabled={starting != null}
                        aria-expanded={challengeOpen}
                        onClick={() => toggleChallenge(o.objectiveId)}
                      >
                        {startingChallenge ? 'Preparando…' : 'Reto'}
                      </button>
                    </div>
                  ) : (
                    <Badge color="gray">{stateLabel(o.state)}</Badge>
                  )}
                </div>
                {canPractice && challengeOpen ? (
                  <div className="btn-row" style={{ marginTop: 10, justifyContent: 'flex-end' }}>
                    <span className="smallprint">Elige dificultad:</span>
                    {CHALLENGE_LEVELS.map((level) => (
                      <button
                        key={level.seconds}
                        className="btn small alt"
                        type="button"
                        disabled={starting != null}
                        onClick={() => startPractice(o.objectiveId, level.seconds)}
                      >
                        {level.seconds}s · {level.label}
                      </button>
                    ))}
                  </div>
                ) : null}
              </div>
            );
          })
        ) : (
          <p className="muted">Aún no hay datos de dominio.</p>
        )}
      </div>
    </>
  );
}
