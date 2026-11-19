import { useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import PageHeader from '../../components/PageHeader.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import Spinner from '../../components/Spinner.jsx';
import AttemptFeedback from '../../components/AttemptFeedback.jsx';
import ConfirmModal from '../../components/ConfirmModal.jsx';
import { extractApiError } from '../../api/client.js';
import { submitAttempt, getMyActivity } from '../../api/learning.js';
import { activityTypeLabel } from '../../lib/constants.js';

/** Normaliza las opciones a [{id,text}]. La API student devuelve `[{id,text}]`
 *  (o null en FILL_BLANK); se aceptan strings por compatibilidad con el mock. */
function normalizeOptions(options) {
  if (!Array.isArray(options)) return [];
  return options.map((o, i) =>
    typeof o === 'string'
      ? { id: o, text: o }
      : { id: o.id ?? i, text: o.text ?? o.label ?? String(o) }
  );
}

function normalizeActivity(raw) {
  if (!raw) return null;
  return {
    id: raw.id,
    type: raw.type,
    level: raw.level,
    skill: raw.skill,
    title: raw.title,
    prompt: raw.prompt,
    options: normalizeOptions(raw.options),
  };
}

/** 07 · Resolver actividad (pageExercise del Figma, HTML:69–70).
 *  Tres modos, todos con el mismo reproductor:
 *   - Asignación: `state.activity` (ActivityStudentDto) + `state.assignmentId`.
 *   - Práctica libre: `state.activityId` o el `:id` de la ruta → GET /me/activities/{id}.
 *   - Cola de práctica: `state.activityIds` (sesión de ~10 preguntas desde "Mi
 *     progreso"). Carga cada actividad con GET /me/activities/{id}, califica con
 *     POST /attempts SIN assignmentId y muestra el resultado en pantalla con
 *     "Siguiente pregunta"; al terminar, resumen con Calificación y Dominio.
 *     Con `state.challengeSeconds` (30/20/10) la sesión es un RETO: cuenta atrás
 *     por pregunta, auto-envío de la respuesta vacía al agotarse y `mode:
 *     'CHALLENGE'` en todos los intentos (correcta +1.0, incorrecta −0.5).
 *  El servidor califica cada intento; el cliente solo cuenta las respuestas
 *  recibidas para el resumen. Solo MULTIPLE_CHOICE y FILL_BLANK. */
export default function StudentActivity() {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const state = location.state || {};

  /** Modo cola: la lista llega en state.activityIds. */
  const queueIds = Array.isArray(state.activityIds)
    ? state.activityIds.filter((value) => value != null)
    : [];
  const queueMode = queueIds.length > 0;
  const total = queueMode ? queueIds.length : 0;

  /** RETO: segundos por pregunta enviados por Mi progreso (30/20/10). */
  const requestedSeconds = Number(state.challengeSeconds);
  const challengeSeconds = Number.isFinite(requestedSeconds) && requestedSeconds > 0
    ? requestedSeconds
    : 0;
  const challengeMode = queueMode && challengeSeconds > 0;

  const [index, setIndex] = useState(0);
  const [results, setResults] = useState([]); // [{result, answer}] ya calificados por el servidor
  const [attempt, setAttempt] = useState(null); // intento calificado de la pregunta actual
  const [finished, setFinished] = useState(false);
  const [remaining, setRemaining] = useState(challengeSeconds);

  const activityId = queueMode ? queueIds[index] : state.activityId ?? id;
  const assignmentId = queueMode ? undefined : state.assignmentId;
  const provided = queueMode ? null : state.activity || null;

  const [activity, setActivity] = useState(() => (provided ? normalizeActivity(provided) : null));
  const [loading, setLoading] = useState(!provided && Boolean(activityId));
  const [loadError, setLoadError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  const [selected, setSelected] = useState('');
  const [typed, setTyped] = useState('');
  const [feedback, setFeedback] = useState('');
  const [busy, setBusy] = useState(false);
  /** Diálogo "¿Salir de la sesión?": la flecha nunca abandona sin confirmar. */
  const [exitOpen, setExitOpen] = useState(false);

  /** Evita doble envío cuando la cuenta atrás y el clic coinciden. */
  const sendingRef = useRef(false);
  /** Clave de la pregunta cuyo tiempo expiró (no se reinicia la cuenta atrás). */
  const expiryRef = useRef(null);
  /** Clave ya auto-enviada al agotarse el tiempo (evita reintentos automáticos). */
  const autoSentRef = useRef(null);
  const questionKey = `${index}:${activityId}`;

  useEffect(() => {
    // Cada pregunta de la cola arranca limpia (y el reintento también).
    setSelected('');
    setTyped('');
    setFeedback('');
    setAttempt(null);
    setLoadError('');

    if (provided) {
      setActivity(normalizeActivity(provided));
      setLoading(false);
      return undefined;
    }
    if (activityId == null) {
      setActivity(null);
      setLoading(false);
      return undefined;
    }

    let alive = true;
    setLoading(true);
    setActivity(null);
    getMyActivity(activityId)
      .then((res) => {
        if (!alive) return;
        setActivity(normalizeActivity(res.data));
        setLoading(false);
      })
      .catch((err) => {
        if (!alive) return;
        setLoadError(err?.response?.status === 404 ? 'notfound' : 'error');
        setLoading(false);
      });
    return () => {
      alive = false;
    };
  }, [activityId, provided, reloadKey]);

  /** Cuenta atrás del RETO: arranca al cargar la pregunta y se detiene al enviar. */
  useEffect(() => {
    if (!challengeMode || attempt || busy || loading || finished || !activity || !activityId) {
      return undefined;
    }
    // El tiempo de esta pregunta ya se agotó: no se reinicia la cuenta atrás.
    if (expiryRef.current === questionKey) return undefined;
    setRemaining(challengeSeconds);
    const startedAt = Date.now();
    const timer = setInterval(() => {
      const left = Math.max(0, challengeSeconds - Math.floor((Date.now() - startedAt) / 1000));
      setRemaining(left);
      if (left === 0) {
        expiryRef.current = questionKey;
        clearInterval(timer);
      }
    }, 250);
    return () => clearInterval(timer);
  }, [challengeMode, challengeSeconds, questionKey, attempt, busy, loading, finished, activity, activityId]);

  const options = activity?.options || [];
  const type = activity?.type;

  function answer() {
    return type === 'MULTIPLE_CHOICE' ? selected : typed.trim();
  }

  /** Avanza en la cola; en la última pregunta cierra la sesión. */
  function advance() {
    setAttempt(null);
    setRemaining(challengeSeconds);
    if (index + 1 >= total) {
      setFinished(true);
      return;
    }
    setIndex((i) => i + 1);
  }

  /** Califica con el servidor. `rawAnswer` vacío = sin respuesta (RETO agotado). */
  async function send(rawAnswer) {
    if (sendingRef.current) return;
    if (!activityId) {
      setFeedback('No hay una actividad concreta para calificar.');
      return;
    }
    sendingRef.current = true;
    setBusy(true);
    setFeedback('');
    try {
      const { data } = await submitAttempt({
        activityId,
        answer: rawAnswer,
        assignmentId,
        mode: challengeMode ? 'CHALLENGE' : undefined,
      });
      if (queueMode) {
        // En la sesión de práctica no se abandona el reproductor: el resultado
        // se muestra con el mismo bloque que la pantalla de resultado.
        setAttempt({ result: data || {}, answer: rawAnswer });
        setResults((prev) => [...prev, { result: data || {}, answer: rawAnswer }]);
      } else {
        navigate(`/estudiante/resultado/${data?.attemptId || data?.id || activityId}`, {
          state: {
            result: data,
            real: true,
            answer: rawAnswer,
            mode: challengeMode ? 'CHALLENGE' : 'NORMAL',
            activity: { type, options },
          },
        });
      }
    } catch (err) {
      setFeedback(extractApiError(err).detail);
    } finally {
      sendingRef.current = false;
      setBusy(false);
    }
  }

  function check() {
    const a = answer();
    // En RETO el servidor califica también la respuesta vacía ("sin responder"),
    // así que no se bloquea el envío manual.
    if (!challengeMode) {
      if (type === 'MULTIPLE_CHOICE' && (a === '' || a == null)) {
        setFeedback('Elige una respuesta.');
        return;
      }
      if (type === 'FILL_BLANK' && !a) {
        setFeedback('Escribe una respuesta.');
        return;
      }
    }
    send(a);
  }

  /** Auto-envío al agotarse el tiempo (solo RETO, una vez por pregunta). */
  useEffect(() => {
    if (!challengeMode || remaining > 0 || finished) return;
    if (expiryRef.current !== questionKey) return;
    if (autoSentRef.current === questionKey) return;
    if (attempt || busy || loading) return;
    // Se marca antes de enviar para no reintentar en automático si el envío
    // falla; el usuario puede reintentar con "Comprobar respuesta".
    autoSentRef.current = questionKey;
    send('');
  }, [challengeMode, remaining, questionKey, attempt, busy, loading, finished]);

  /** Flecha de salida de la actividad: primero confirma, luego vuelve atrás.
   *  En el resumen final (sesión terminada) no hay progreso que perder: la
   *  flecha navega directo. */
  function requestExit() {
    setExitOpen(true);
  }

  function confirmExit() {
    setExitOpen(false);
    navigate(-1);
  }

  /** Resumen final: solo Calificación y Dominio del tema, en porcentaje. */
  const answeredCount = results.length;
  const correctTotal = results.filter((r) => Boolean(r?.result?.correct)).length;
  const unansweredTotal = results.filter((r) => r.answer === '').length;
  const incorrectTotal = Math.max(0, answeredCount - correctTotal - unansweredTotal);
  const denominator = total || 1;
  const scorePercent = challengeMode
    ? Math.round(Math.min(1, Math.max(0, (correctTotal - incorrectTotal * 0.5) / denominator)) * 100)
    : Math.round((correctTotal / denominator) * 100);
  const lastMastery = results[results.length - 1]?.result?.mastery;
  const masteryPercent = lastMastery != null ? Math.round(Number(lastMastery) * 100) : null;

  if (queueMode && finished) {
    return (
      <section className="exercise">
        <div className="exercise-top">
          <button
            className="btn alt small back-btn"
            type="button"
            aria-label="Salir de la actividad"
            title="Salir de la actividad"
            onClick={() => navigate(-1)}
          >
            ←
          </button>
          <div className="page-head">
            <PageHeader
              eyebrow={challengeMode ? 'ACTIVITY PLAYER / RETO' : 'ACTIVITY PLAYER / SESIÓN'}
              title={challengeMode ? 'Sesión de reto' : 'Sesión de práctica'}
              subtitle={
                challengeMode
                  ? 'Cada pregunta suma: correcta +1, incorrecta −0,5, sin respuesta 0.'
                  : 'Repaso rápido para subir tu dominio del objetivo.'
              }
            />
          </div>
        </div>
        <div className="card light" style={{ maxWidth: 750, margin: 'auto' }}>
          <div style={{ fontSize: 39 }}>{scorePercent >= 50 ? '✓' : '↺'}</div>
          <h2 style={{ font: '35px Georgia, serif', margin: '12px 0' }}>
            {challengeMode ? 'Reto completado' : 'Sesión completada'}
          </h2>
          <div className="grid g2">
            <div className="card">
              <div className="stat-label">Calificación</div>
              <div className="stat-number">{scorePercent}%</div>
            </div>
            <div className="card">
              <div className="stat-label">Dominio del tema</div>
              <div className="stat-number">{masteryPercent != null ? `${masteryPercent}%` : '—'}</div>
            </div>
          </div>
          <div className="notice mt">
            <span>
              Cada intento ajustó tu dominio del objetivo. Vuelve a tu progreso para ver el detalle y
              elegir la siguiente sesión.
            </span>
          </div>
          <div className="btn-row mt">
            <button className="btn" type="button" onClick={() => navigate('/estudiante/progreso')}>
              Volver a Mi progreso
            </button>
          </div>
        </div>
      </section>
    );
  }

  if (loading) {
    return (
      <section className="exercise">
        <PageHeader
          eyebrow="ACTIVITY PLAYER"
          title={queueMode ? `Pregunta ${index + 1} de ${total}` : 'Cargando actividad…'}
          subtitle={queueMode ? 'Sesión de práctica' : undefined}
        />
        <Spinner />
      </section>
    );
  }

  if (!activity) {
    return (
      <section className="exercise">
        <PageHeader
          eyebrow="ACTIVITY PLAYER"
          title={queueMode ? `Pregunta ${index + 1} de ${total}` : 'Resolver actividad'}
          subtitle={queueMode ? 'Sesión de práctica' : 'Vista de práctica libre.'}
        />
        <div className="card light">
          {loadError === 'error' || (queueMode && loadError) ? (
            <>
              <ErrorState
                message={
                  loadError === 'notfound'
                    ? 'La actividad solicitada no existe o ya no está disponible para tu cuenta.'
                    : 'No se pudo cargar la actividad.'
                }
                onRetry={() => setReloadKey((k) => k + 1)}
              />
              {queueMode ? (
                <div className="btn-row" style={{ justifyContent: 'flex-end' }}>
                  <button className="btn alt small" type="button" onClick={advance}>
                    {index + 1 >= total ? 'Saltar y terminar la sesión' : 'Saltar esta pregunta'}
                  </button>
                </div>
              ) : null}
            </>
          ) : (
            <div>
              <p className="muted">
                {loadError === 'notfound'
                  ? 'La actividad solicitada no existe o ya no está disponible para tu cuenta.'
                  : 'No hay una actividad para mostrar.'}
              </p>
              <div className="btn-row mt">
                <button className="btn" type="button" onClick={() => navigate('/estudiante/tareas')}>
                  Ir a mis tareas
                </button>
              </div>
            </div>
          )}
        </div>
      </section>
    );
  }

  const questionCard = (
    <div className="card light">
      <div className="barline">
        <b>{activityTypeLabel(type)}</b>
        <span className="tiny">{activity.level ? `Nivel ${activity.level}` : ''}</span>
      </div>
      {activity.prompt ? (
        <h2 style={{ font: '30px/1.35 Georgia, serif', maxWidth: 680, margin: '15px 0 25px' }}>{activity.prompt}</h2>
      ) : null}

      {type === 'MULTIPLE_CHOICE' ? (
        <div>
          {options.map((opt) => (
            <div
              key={opt.id}
              className={`choice${String(selected) === String(opt.id) ? ' selected' : ''}`}
              role="radio"
              aria-checked={String(selected) === String(opt.id)}
              tabIndex={0}
              onClick={() => { setSelected(opt.id); setFeedback(''); }}
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  setSelected(opt.id);
                  setFeedback('');
                }
              }}
            >
              ○&nbsp;&nbsp; {opt.text}
            </div>
          ))}
        </div>
      ) : (
        <div>
          <label className="form-label" htmlFor="answer-text">Tu respuesta</label>
          <input
            id="answer-text"
            className="input"
            placeholder="Escribe la palabra o frase que falta"
            value={typed}
            onChange={(e) => { setTyped(e.target.value); setFeedback(''); }}
          />
        </div>
      )}

      {feedback ? <p className="error" role="alert">{feedback}</p> : null}
      <div className="btn-row mt" style={{ justifyContent: 'flex-end' }}>
        <button className="btn olive" type="button" onClick={check} disabled={busy}>
          {busy ? 'Calificando…' : 'Comprobar respuesta'}
        </button>
      </div>
      {busy ? (
        <div className="mt" style={{ display: 'flex', justifyContent: 'center' }}>
          <Spinner small label="" />
        </div>
      ) : null}
    </div>
  );

  return (
    <section className="exercise">
      <div className="exercise-top">
        <button
          className="btn alt small back-btn"
          type="button"
          aria-label="Salir de la actividad"
          title="Salir de la actividad"
          onClick={requestExit}
        >
          ←
        </button>
        <div className="page-head">
          <PageHeader
            eyebrow={`ACTIVITY PLAYER / ${activity.level || 'A1'}`}
            title={activity.title || 'Resolver actividad'}
            subtitle={
              challengeMode
                ? `Reto · ${challengeSeconds}s por pregunta · respuesta calificada en el servidor.`
                : queueMode
                  ? 'Sesión de práctica · respuesta calificada en el servidor.'
                  : 'Responde y comprueba tu resultado.'
            }
          />
        </div>
      </div>

      {queueMode ? (
        <div style={{ marginBottom: 18 }}>
          <div className="barline">
            <b>Pregunta {index + 1} de {total}</b>
            {challengeMode ? <span className="tiny">{challengeSeconds}s por pregunta</span> : null}
          </div>
          <div className="progress-track">
            <div
              className="progress-fill"
              style={{ width: `${Math.round(((index + (attempt ? 1 : 0)) / total) * 100)}%` }}
            />
          </div>
        </div>
      ) : null}

      {challengeMode && !attempt ? (
        <div style={{ marginBottom: 18 }}>
          <div className="barline">
            <b>Reto · {remaining}s</b>
            <span className="tiny">{busy ? 'Enviando…' : 'Tiempo restante'}</span>
          </div>
          <div className="progress-track" aria-label={`Tiempo restante: ${remaining} segundos`} role="progressbar" aria-valuemin={0} aria-valuemax={challengeSeconds} aria-valuenow={remaining}>
            <div
              className="progress-fill"
              style={{
                width: `${Math.round((remaining / challengeSeconds) * 100)}%`,
                ...(remaining <= 5 ? { background: 'var(--rust)' } : {}),
              }}
            />
          </div>
        </div>
      ) : null}

      {queueMode && attempt ? (
        <AttemptFeedback
          result={attempt.result}
          answer={attempt.answer}
          activityType={type}
          options={options}
        >
          <button className="btn olive" type="button" onClick={advance}>
            Siguiente pregunta
          </button>
        </AttemptFeedback>
      ) : (
        questionCard
      )}

      <ConfirmModal
        open={exitOpen}
        title="¿Salir de la sesión?"
        description="Si sales ahora perderás el progreso no enviado."
        confirmLabel="Salir"
        cancelLabel="Seguir aquí"
        onConfirm={confirmExit}
        onCancel={() => setExitOpen(false)}
      />
    </section>
  );
}
