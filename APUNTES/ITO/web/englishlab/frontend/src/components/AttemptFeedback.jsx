/** Normaliza las opciones a [{id,text}]. La API student devuelve `[{id,text}]`;
 *  se aceptan strings/formas antiguas por compatibilidad con el mock. */
function normalizeOptions(options) {
  if (!Array.isArray(options)) return [];
  return options.map((o, i) =>
    typeof o === 'string'
      ? { id: o, text: o }
      : { id: o.id ?? i, text: o.text ?? o.label ?? String(o) }
  );
}

function formatAnswer(value, activityType, options) {
  if (value == null || value === '') return '';
  if (activityType !== 'MULTIPLE_CHOICE' || !options.length) return String(value);
  const found = options.find((o) => String(o.id) === String(value));
  if (!found) return String(value);
  return String(found.id) === String(found.text) ? String(found.text) : `${found.id} — ${found.text}`;
}

/** Calificación del intento en porcentaje. El servidor manda `score` (0–1 en
 *  NORMAL, 1.0 / −0.5 / 0.0 en RETO): se acota a 0–1 y se redondea. */
function scorePercent(result, correct) {
  const raw = result.score != null ? Number(result.score) : NaN;
  if (!Number.isFinite(raw)) return correct ? 100 : 0;
  return Math.round(Math.min(1, Math.max(0, raw)) * 100);
}

/** Cuerpo del resultado de un intento (estética de la pantalla 08). Lo comparten
 *  StudentResult y el modo cola de StudentActivity. No calcula la corrección:
 *  pinta lo que devuelve POST /attempts ({correct, score, solution,
 *  explanation, mastery}). En MULTIPLE_CHOICE `solution` es el id de la opción
 *  correcta ("b"), así que se muestra como "<id> — <texto>".
 *  Durante la revisión de una pregunta no se muestran calificación ni dominio;
 *  el resumen final (dos porcentajes) solo aparece con `showSummary`, que usan
 *  el cierre de sesión y el resultado de un intento individual. */
export default function AttemptFeedback({
  result = {},
  answer: rawAnswer,
  activityType,
  options: rawOptions,
  showSummary = false,
  children,
}) {
  const correct = Boolean(result.correct);
  const explanation = result.explanation;
  const mastery = result.mastery;

  const options = normalizeOptions(rawOptions);
  const solution = formatAnswer(result.solution, activityType, options);
  const answerValue = result.answer ?? rawAnswer;
  const answer = formatAnswer(answerValue, activityType, options);
  /** El servidor devuelve `answer: ''` cuando la pregunta se quedó sin
   *  responder (RETO agotado): se muestra como "Sin respuesta". */
  const hasAnswerValue = answerValue != null;
  const pct = scorePercent(result, correct);
  const masteryPct = mastery != null ? Math.round(Number(mastery) * 100) : null;
  /** Fallo = respuesta incorrecta o "Sin respuesta": el contenedor se tiñe con
   *  el café del menú lateral (.feedback-wrong). El acierto no cambia. */
  const wrong = !correct;

  return (
    <div
      className={`card light${wrong ? ' feedback-wrong' : ''}`}
      style={{ maxWidth: 750, margin: 'auto' }}
    >
      {wrong ? (
        <span className="feedback-wrong-tag">
          {hasAnswerValue && !answer ? 'Sin respuesta' : 'Respuesta incorrecta'}
        </span>
      ) : null}
      <div style={{ fontSize: 39 }}>{correct ? '✓' : '↺'}</div>
      <h2 style={{ font: '35px Georgia, serif', margin: '12px 0' }}>
        {correct ? '¡Muy bien!' : 'Sigue practicando'}
      </h2>
      {hasAnswerValue ? (
        answer ? (
          <p>
            Tu respuesta: <b>{answer}</b>
          </p>
        ) : (
          <p>
            <b>Sin respuesta</b>
          </p>
        )
      ) : null}
      {!correct && solution ? (
        <p>
          Respuesta correcta: <b>{solution}</b>
        </p>
      ) : null}
      <div className="notice">
        <span>
          <b>Explicación:</b>{' '}
          {explanation ||
            'Revisa la explicación de la actividad y vuelve a intentarlo: cada intento ajusta tu dominio del objetivo.'}
        </span>
      </div>
      {showSummary ? (
        <>
          <div className="divider" />
          <div className="grid g2">
            <div className="card">
              <div className="stat-label">Calificación</div>
              <div className="stat-number">{pct}%</div>
            </div>
            <div className="card">
              <div className="stat-label">Dominio del tema</div>
              <div className="stat-number">{masteryPct != null ? `${masteryPct}%` : '—'}</div>
            </div>
          </div>
        </>
      ) : null}
      {children ? <div className="btn-row mt">{children}</div> : null}
    </div>
  );
}
