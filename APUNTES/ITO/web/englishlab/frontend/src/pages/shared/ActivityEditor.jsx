import { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import PageHeader from '../../components/PageHeader.jsx';
import Input from '../../components/Input.jsx';
import MockNotice from '../../components/MockNotice.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import Spinner from '../../components/Spinner.jsx';
import { useToast } from '../../components/Toast.jsx';
import { mutateWithFallback, useRemote, REMOTE_STATUS } from '../../api/useRemote.js';
import { listActivities, listContentObjectives, createActivity, updateActivity } from '../../api/activities.js';
import { listCategories } from '../../api/categories.js';
import {
  mockCreateActivity,
  mockUpdateActivity,
  mockCategoriesPage,
  mockContentObjectivesPage,
} from '../../mock/mockServer.js';
import { required } from '../../lib/validators.js';
import { useAuth } from '../../auth/AuthContext.jsx';
import { LEVELS, SKILLS, ACTIVITY_TYPE_CODES, activityTypeLabel } from '../../lib/constants.js';

const splitOptions = (s) => String(s || '').split('|').map((x) => x.trim()).filter(Boolean);

function activityToValues(a) {
  const content = a?.content || {};
  return {
    title: a?.title || '',
    level: a?.level || '',
    categoryId: a?.categoryId != null ? String(a.categoryId) : '',
    skill: a?.skill || '',
    objectiveId: a?.objectiveId != null ? String(a.objectiveId) : '',
    type: a?.type || '',
    statement: content.prompt || '',
    options: Array.isArray(content.options) ? content.options.join(' | ') : content.options || '',
    answer: a?.type === 'FILL_BLANK'
      ? (Array.isArray(content.accepted) ? content.accepted.join(' | ') : content.accepted || '')
      : content.answer || '',
    explanation: content.explanation || '',
  };
}

/** 12 · Editor de actividad (pageEditor del Figma, HTML:79), pantalla completa
 *  (no modal, decisión del Figma/Guía). Patrón mock/real: POST/PUT
 *  /content/activities; si el endpoint responde 404 (pendiente) guarda en mock
 *  y lo avisa. Validación bajo cada input y por tipo de ejercicio:
 *  MULTIPLE_CHOICE (prompt + options≥3 + answer), FILL_BLANK (prompt + accepted).
 *  El objetivo se elige de un select alimentado por GET /content/objectives. */
export default function ActivityEditor() {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { showToast } = useToast();
  const { role } = useAuth();

  const [initial, setInitial] = useState(null);
  const [loading, setLoading] = useState(Boolean(id));
  const [loadError, setLoadError] = useState(null);

  // En edición, la actividad llega por `state` desde el banco. Si se accede
  // por URL directa, se busca de forma best-effort en el listado (no existe
  // GET /content/activities/{id} en el contrato).
  useEffect(() => {
    if (!id) {
      setInitial(null);
      setLoading(false);
      return;
    }
    if (location.state?.activity) {
      setInitial(location.state.activity);
      setLoading(false);
      return;
    }
    let alive = true;
    setLoading(true);
    listActivities({ page: 0, size: 100 })
      .then((res) => {
        const found = (res.data?.content || []).find((a) => String(a.id) === String(id));
        if (alive) {
          setInitial(found || null);
          setLoadError(found ? null : { detail: 'No se encontró la actividad solicitada.' });
          setLoading(false);
        }
      })
      .catch((err) => {
        if (alive) {
          setLoadError({ detail: err?.response?.data?.detail || 'No se pudo cargar la actividad.' });
          setLoading(false);
        }
      });
    return () => {
      alive = false;
    };
  }, [id, location.state?.activity]);

  const [values, setValues] = useState(activityToValues(initial));
  const [errors, setErrors] = useState({});
  const [touched, setTouched] = useState({});
  const [submitError, setSubmitError] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (initial !== null) setValues(activityToValues(initial));
  }, [initial]);

  // Opciones del select de categoría: API real con fallback mock (admin).
  // Para TEACHER el endpoint es solo-admin (403) y el select queda vacío.
  const categoriesRemote = useRemote({
    fetcher: () => listCategories({ page: 0, size: 100, sort: 'name,asc' }),
    fallback: () => mockCategoriesPage({ page: 0, size: 100, sort: 'name,asc' }),
  });
  const categories = categoriesRemote.status === REMOTE_STATUS.real || categoriesRemote.status === REMOTE_STATUS.fallback
    ? categoriesRemote.data?.content || []
    : [];

  // Objetivos de contenido para el select de objetivo.
  const objectivesRemote = useRemote({
    fetcher: () => listContentObjectives({ page: 0, size: 50 }),
    fallback: () => mockContentObjectivesPage({ page: 0, size: 50 }),
  });
  const objectives = objectivesRemote.status === REMOTE_STATUS.real || objectivesRemote.status === REMOTE_STATUS.fallback
    ? objectivesRemote.data?.content || []
    : [];

  function validate(v) {
    const e = {};
    e.title = required(v.title, 'El título');
    e.level = required(v.level, 'El nivel');
    e.skill = required(v.skill, 'La habilidad');
    e.objectiveId = required(v.objectiveId, 'El objetivo');
    e.type = required(v.type, 'El tipo de ejercicio');
    e.statement = required(v.statement, 'El enunciado');
    const opts = splitOptions(v.options);
    if (v.type === 'MULTIPLE_CHOICE') {
      e.options = opts.length >= 3 ? '' : 'Escribe al menos 3 opciones separadas por "|".';
      e.answer = required(v.answer, 'La respuesta correcta')
        || (!opts.includes(String(v.answer).trim()) ? 'La respuesta debe ser una de las opciones.' : '');
    } else if (v.type === 'FILL_BLANK') {
      e.options = '';
      e.answer = required(v.answer, 'La respuesta aceptada');
    } else {
      e.options = '';
      e.answer = '';
    }
    return e;
  }

  const handleChange = (e) => {
    const { name, value } = e.target;
    setValues((v) => ({ ...v, [name]: value }));
    setSubmitError('');
    if (touched[name]) setErrors((errs) => ({ ...errs, [name]: validate({ ...values, [name]: value })[name] }));
  };

  const handleBlur = (e) => {
    const { name, value } = e.target;
    setTouched((t) => ({ ...t, [name]: true }));
    setErrors((errs) => ({ ...errs, [name]: validate({ ...values, [name]: value })[name] }));
  };

  function buildContent(type, v) {
    const prompt = String(v.statement).trim();
    const explanation = String(v.explanation).trim();
    if (type === 'MULTIPLE_CHOICE') {
      return { prompt, options: splitOptions(v.options), answer: String(v.answer).trim(), ...(explanation ? { explanation } : {}) };
    }
    return { prompt, accepted: splitOptions(v.answer), ...(explanation ? { explanation } : {}) };
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const nextErrors = validate(values);
    setErrors(nextErrors);
    setTouched({ title: true, level: true, skill: true, objectiveId: true, type: true, statement: true, options: true, answer: true });
    if (Object.values(nextErrors).some(Boolean)) return;

    const payload = {
      title: values.title.trim(),
      level: values.level,
      skill: values.skill,
      objectiveId: Number(values.objectiveId),
      type: values.type,
      ...(values.categoryId ? { categoryId: Number(values.categoryId) } : {}),
      content: buildContent(values.type, values),
    };

    setBusy(true);
    setSubmitError('');
    try {
      const { mode } = await mutateWithFallback(
        () => (id ? updateActivity(id, payload) : createActivity(payload)),
        () => (id ? mockUpdateActivity(id, payload) : mockCreateActivity(payload))
      );
      showToast(
        mode === 'real'
          ? 'Actividad guardada.'
          : 'Demo (mock): guardado local; el endpoint /content/activities todavía no existe.'
      );
      navigate('/actividades');
    } catch (err) {
      const apiError = err?.response?.data || {};
      const fieldErrors = {};
      (apiError.fieldErrors || []).forEach((f) => {
        if (f.field in values) fieldErrors[f.field] = f.message;
      });
      setErrors((prev) => ({ ...prev, ...fieldErrors }));
      setSubmitError(Object.keys(fieldErrors).length ? '' : apiError.detail || 'No se pudo guardar la actividad.');
    } finally {
      setBusy(false);
    }
  }

  if (loading) return <Spinner />;
  if (loadError && !initial) return <ErrorState message={loadError.detail} onRetry={() => navigate('/actividades')} />;

  const selectField = (name, label, options, value) => (
    <div>
      <label className="form-label" htmlFor={`editor-${name}`}>{label}</label>
      <select id={`editor-${name}`} className="input" name={name} value={value} onChange={handleChange} onBlur={handleBlur}>
        <option value="">Selecciona…</option>
        {options.map((o) => (
          <option key={o.value ?? o} value={o.value ?? o}>
            {o.label ?? o}
          </option>
        ))}
      </select>
      {touched[name] && errors[name] ? <p className="error" role="alert">{errors[name]}</p> : null}
    </div>
  );

  return (
    <>
      <PageHeader
        eyebrow={role === 'ADMIN' ? 'PORTAL / ADMINISTRADOR' : 'PORTAL / PROFESOR'}
        title={id ? 'Editar actividad' : 'Crear actividad'}
      />
      <MockNotice what="el editor de actividades (POST/PUT /content/activities)" />
      <form className="card light" style={{ maxWidth: 900 }} onSubmit={handleSubmit} noValidate>
        <div className="form-2">
          <Input
            label="Título"
            name="title"
            placeholder="Ej. Present Simple A1"
            value={values.title}
            onChange={handleChange}
            onBlur={handleBlur}
            error={touched.title ? errors.title : ''}
          />
          {selectField('level', 'Nivel', LEVELS, values.level)}
          {selectField('skill', 'Habilidad', SKILLS, values.skill)}
          {selectField(
            'type',
            'Tipo de ejercicio',
            ACTIVITY_TYPE_CODES.map((t) => ({ value: t, label: activityTypeLabel(t) })),
            values.type
          )}
          {selectField(
            'objectiveId',
            'Objetivo',
            objectives.map((o) => ({ value: o.id, label: `${o.code} — ${o.name} (${o.cefrLevel})` })),
            values.objectiveId
          )}
          {selectField(
            'categoryId',
            'Categoría (opcional)',
            categories.map((c) => ({ value: c.id, label: c.name })),
            values.categoryId
          )}
        </div>
        <div>
          <label className="form-label" htmlFor="statement">Enunciado</label>
          <textarea
            id="statement"
            className="input textarea"
            name="statement"
            placeholder="Escribe la instrucción que verá el estudiante..."
            value={values.statement}
            onChange={handleChange}
            onBlur={handleBlur}
          />
          {touched.statement && errors.statement ? <p className="error" role="alert">{errors.statement}</p> : null}
        </div>
        <Input
          label={values.type === 'FILL_BLANK' ? 'Respuesta(s) aceptada(s)' : 'Oración o respuesta correcta'}
          name="answer"
          placeholder={values.type === 'FILL_BLANK' ? 'am | was' : 'I go to school every day'}
          help={values.type === 'FILL_BLANK' ? 'Separa varias respuestas aceptadas con el carácter |' : undefined}
          value={values.answer}
          onChange={handleChange}
          onBlur={handleBlur}
          error={touched.answer ? errors.answer : ''}
        />
        <Input
          label={values.type === 'FILL_BLANK' ? 'Opciones (no aplica)' : 'Opciones (mínimo 3)'}
          name="options"
          placeholder="am | is | are"
          help={values.type === 'FILL_BLANK' ? 'Este campo no se usa en completar espacios.' : 'Separa las opciones con el carácter |'}
          value={values.options}
          onChange={handleChange}
          onBlur={handleBlur}
          error={touched.options ? errors.options : ''}
        />
        <div>
          <label className="form-label" htmlFor="explanation">Explicación pedagógica</label>
          <textarea
            id="explanation"
            className="input textarea"
            name="explanation"
            placeholder="Explica por qué la respuesta es correcta."
            value={values.explanation}
            onChange={handleChange}
          />
        </div>
        {submitError ? <p className="form-status" role="alert">{submitError}</p> : null}
        <div className="btn-row mt">
          <button className="btn" type="submit" disabled={busy}>
            {busy ? 'Guardando…' : 'Guardar actividad'}
          </button>
          <button className="btn alt" type="button" onClick={() => navigate('/actividades')}>
            Cancelar
          </button>
        </div>
      </form>
    </>
  );
}
