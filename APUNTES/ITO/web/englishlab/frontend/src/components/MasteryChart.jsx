import Badge from './Badge.jsx';
import Spinner from './Spinner.jsx';
import ErrorState from './ErrorState.jsx';
import { REMOTE_STATUS } from '../api/useRemote.js';

/**
 * Gráfica de dominio por nivel (Inicio del estudiante). Solo CSS: reutiliza
 * .progress-track / .meter del Figma y añade .mastery-* en styles.css. Sin
 * librerías de gráficas.
 *
 * Datos: GET /me/objectives →
 *   {objectiveId, code, name, cefrLevel, skill, mastery, state, previousMastery}
 * `previousMastery` es el dominio ANTES del último intento (null = sin sesiones
 * previas); el segmento verde/rojo marca cuánto movió ese último intento.
 */
const LEVEL_ORDER = ['A1', 'A2', 'B1', 'B2'];
const BLOCKED = 'BLOCKED';

function toPct(value) {
  const n = Number(value);
  if (!Number.isFinite(n)) return 0;
  return Math.round(Math.min(Math.max(n, 0), 1) * 100);
}

function isBlocked(state) {
  return String(state || '').toUpperCase() === BLOCKED;
}

function label(o, current, delta, blocked) {
  const head = `${o.code ? `${o.code} · ` : ''}${o.name || 'Objetivo'}: ${current}%`;
  const move =
    delta == null
      ? 'primera medición, sin sesiones previas'
      : delta > 0
        ? `+${delta}% desde el último intento`
        : delta < 0
          ? `−${Math.abs(delta)}% desde el último intento`
          : 'sin cambios desde el último intento';
  return `${head}, ${move}${blocked ? '. Bloqueado por prerrequisitos' : ''}`;
}

function ObjectiveBar({ objective }) {
  const current = toPct(objective.mastery);
  const prev = objective.previousMastery == null ? null : toPct(objective.previousMastery);
  const delta = prev == null ? null : current - prev;
  const blocked = isBlocked(objective.state);

  // Segmento del último intento: rango [min, max] sobre la barra.
  const moveStart = delta == null ? 0 : Math.min(current, prev);
  const moveWidth = delta == null ? 0 : Math.abs(delta);

  // Un BLOCKED recién creado tiene mastery 0: el mínimo deja ver la barra tenue.
  const fillWidth = blocked ? Math.max(current, 4) : current;

  return (
    <div className="mastery-row">
      <div className="mastery-label">
        <b>
          {objective.code ? `${objective.code} · ` : ''}
          {objective.name}
        </b>
        <span className="mastery-tags">
          {blocked ? <Badge color="gray">Bloqueado</Badge> : null}
          {delta == null ? (
            <span className="smallprint">nueva</span>
          ) : (
            <span className={`mastery-delta-tag ${delta >= 0 ? 'up' : 'down'}`}>
              {delta > 0 ? `+${delta}%` : delta < 0 ? `−${Math.abs(delta)}%` : '0%'}
            </span>
          )}
          <span className="mastery-pct">{current}%</span>
        </span>
      </div>
      <div
        className="progress-track mastery-track"
        role="img"
        aria-label={label(objective, current, delta, blocked)}
      >
        <div
          className={`mastery-fill${blocked ? ' blocked' : ''}`}
          style={{ width: `${fillWidth}%` }}
        />
        {moveWidth > 0 ? (
          <div
            className={`mastery-delta ${delta > 0 ? 'up' : 'down'}`}
            style={{ left: `${moveStart}%`, width: `${moveWidth}%` }}
          />
        ) : null}
      </div>
    </div>
  );
}

export default function MasteryChart({ status, rows, error, onRetry }) {
  const list = Array.isArray(rows) ? rows : [];

  if (status === REMOTE_STATUS.loading) return <Spinner small />;
  if (status === REMOTE_STATUS.error) {
    return (
      <ErrorState
        message={error?.detail || 'No se pudo cargar tu dominio por objetivo.'}
        onRetry={onRetry}
      />
    );
  }
  if (!list.length) return <p className="muted">Aún no hay objetivos disponibles.</p>;

  const known = new Set(LEVEL_ORDER);
  const extraLevels = list
    .map((o) => String(o.cefrLevel || '').toUpperCase())
    .filter((level, index, all) => level && !known.has(level) && all.indexOf(level) === index);

  const groups = [...LEVEL_ORDER, ...extraLevels]
    .map((level) => ({
      level,
      items: list.filter((o) => String(o.cefrLevel || '').toUpperCase() === level),
    }))
    .filter((group) => group.items.length);

  return (
    <div className="mastery-chart">
      {groups.map((group) => (
        <div className="mastery-group" key={group.level}>
          <div className="mastery-group-head">
            <b>{group.level}</b>
            <span className="smallprint">
              {group.items.length} {group.items.length === 1 ? 'objetivo' : 'objetivos'}
            </span>
          </div>
          {group.items.map((o) => (
            <ObjectiveBar key={o.objectiveId ?? o.code ?? o.name} objective={o} />
          ))}
        </div>
      ))}
      <p className="footer-note">
        El segmento verde/rojo marca el cambio del último intento sobre el dominio previo.
      </p>
    </div>
  );
}
