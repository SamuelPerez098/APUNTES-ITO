import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import PageHeader from '../../components/PageHeader.jsx';
import Badge from '../../components/Badge.jsx';
import MockNotice from '../../components/MockNotice.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import Spinner from '../../components/Spinner.jsx';
import ConfirmModal from '../../components/ConfirmModal.jsx';
import { useToast } from '../../components/Toast.jsx';
import { useRemote, mutateWithFallback, REMOTE_STATUS } from '../../api/useRemote.js';
import { listActivities, removeActivity } from '../../api/activities.js';
import { listCategories } from '../../api/categories.js';
import { mockActivitiesPage, mockRemoveActivity, mockCategoriesPage } from '../../mock/mockServer.js';
import { roleLabel } from '../../lib/format.js';
import { LEVELS, SKILLS, ACTIVITY_TYPE_CODES, activityTypeLabel, skillLabel } from '../../lib/constants.js';
import { useAuth } from '../../auth/AuthContext.jsx';

const PAGE_SIZE = 5;

/** 11 · Banco de actividades (pageBank del Figma, HTML:77), compartida
 *  profesor + admin. Patrón mock/real: GET /content/activities con
 *  page/size/sort + filtros level/skill/type/categoryId/text server-side; si
 *  responde 404 (endpoint pendiente) usa el mock con el mismo shape de
 *  contrato; 401/403/5xx muestran ErrorState real. */
export default function ActivityBank() {
  const { role } = useAuth();
  const navigate = useNavigate();
  const { showToast } = useToast();

  const [filters, setFilters] = useState({ text: '', level: '', skill: '', type: '', categoryId: '' });
  const [applied, setApplied] = useState({ text: '', level: '', skill: '', type: '', categoryId: '' });
  const [page, setPage] = useState(0);
  const [toDelete, setToDelete] = useState(null);
  const [busy, setBusy] = useState(false);

  const { status, data, error, reload } = useRemote({
    fetcher: () =>
      listActivities({
        page,
        size: PAGE_SIZE,
        sort: 'title,asc',
        level: applied.level || undefined,
        skill: applied.skill || undefined,
        type: applied.type || undefined,
        categoryId: applied.categoryId || undefined,
        text: applied.text || undefined,
      }),
    fallback: () =>
      mockActivitiesPage({
        page,
        size: PAGE_SIZE,
        sort: 'title,asc',
        level: applied.level || undefined,
        skill: applied.skill || undefined,
        type: applied.type || undefined,
        categoryId: applied.categoryId || undefined,
        text: applied.text,
      }),
    deps: [page, applied],
  });

  // Opciones del filtro de categoría: se piden a la API (admin) con fallback
  // mock; si fallan, el select queda solo con "Todas las categorías".
  const categoriesRemote = useRemote({
    fetcher: () => listCategories({ page: 0, size: 100, sort: 'name,asc' }),
    fallback: () => mockCategoriesPage({ page: 0, size: 100, sort: 'name,asc' }),
  });
  const categories = categoriesRemote.status === REMOTE_STATUS.real || categoriesRemote.status === REMOTE_STATUS.fallback
    ? categoriesRemote.data?.content || []
    : [];

  const content = data?.content || [];
  const totalPages = data?.totalPages || 1;
  const currentPage = data?.page ?? page;

  function setFilter(name, value) {
    setFilters((f) => ({ ...f, [name]: value }));
  }

  function applyFilter(e) {
    e.preventDefault();
    setApplied({
      text: filters.text.trim(),
      level: filters.level,
      skill: filters.skill,
      type: filters.type,
      categoryId: filters.categoryId,
    });
    setPage(0);
  }

  async function confirmDelete() {
    setBusy(true);
    try {
      const { mode } = await mutateWithFallback(
        () => removeActivity(toDelete.id),
        () => mockRemoveActivity(toDelete.id)
      );
      showToast(
        mode === 'real'
          ? 'Actividad eliminada.'
          : 'Demo (mock): eliminación local; el endpoint /content/activities todavía no existe.'
      );
      setToDelete(null);
      reload();
    } catch (err) {
      showToast(err?.response?.data?.detail || 'No se pudo eliminar la actividad.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <>
      <PageHeader
        eyebrow={role === 'ADMIN' ? 'PORTAL / ADMINISTRADOR' : 'PORTAL / PROFESOR'}
        title="Banco de actividades"
      />
      {status === REMOTE_STATUS.fallback ? <MockNotice what="el banco de actividades" /> : null}

      {status === REMOTE_STATUS.error ? (
        <ErrorState message={error?.detail} onRetry={reload} />
      ) : status === REMOTE_STATUS.loading ? (
        <Spinner />
      ) : (
        <>
          <form className="filters" onSubmit={applyFilter}>
            <input
              className="input search"
              placeholder="Buscar por título"
              aria-label="Buscar por título"
              value={filters.text}
              onChange={(e) => setFilter('text', e.target.value)}
            />
            <select className="input" aria-label="Nivel" value={filters.level} onChange={(e) => setFilter('level', e.target.value)}>
              <option value="">Todos los niveles</option>
              {LEVELS.map((l) => (
                <option key={l}>{l}</option>
              ))}
            </select>
            <select className="input" aria-label="Habilidad" value={filters.skill} onChange={(e) => setFilter('skill', e.target.value)}>
              <option value="">Todas las habilidades</option>
              {SKILLS.map((s) => (
                <option key={s.value} value={s.value}>
                  {s.label}
                </option>
              ))}
            </select>
            <select className="input" aria-label="Tipo" value={filters.type} onChange={(e) => setFilter('type', e.target.value)}>
              <option value="">Todos los tipos</option>
              {ACTIVITY_TYPE_CODES.map((t) => (
                <option key={t} value={t}>
                  {activityTypeLabel(t)}
                </option>
              ))}
            </select>
            <select className="input" aria-label="Categoría" value={filters.categoryId} onChange={(e) => setFilter('categoryId', e.target.value)}>
              <option value="">Todas las categorías</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
            <button className="btn alt small" type="submit">
              Filtrar
            </button>
            <button className="btn olive small" type="button" onClick={() => navigate('/actividades/nueva')}>
              Crear actividad
            </button>
          </form>

          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Título</th>
                  <th>Nivel</th>
                  <th>Categoría</th>
                  <th>Habilidad</th>
                  <th>Tipo</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {content.length ? (
                  content.map((a) => (
                    <tr key={a.id}>
                      <td>{a.title}</td>
                      <td>
                        <Badge color="gray">{a.level}</Badge>
                      </td>
                      <td>{a.categoryName || '—'}</td>
                      <td>{skillLabel(a.skill)}</td>
                      <td>{activityTypeLabel(a.type)}</td>
                      <td>
                        <div className="btn-row">
                          <button className="btn small alt" type="button" onClick={() => navigate(`/actividades/${a.id}/editar`, { state: { activity: a } })}>
                            Editar
                          </button>
                          <button className="btn small rust" type="button" onClick={() => setToDelete(a)}>
                            Eliminar
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={6}>
                      <span className="muted">Sin actividades para este filtro.</span>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          <div className="pagination">
            <span>
              Página {currentPage + 1} de {totalPages}
              {data?.totalElements != null ? ` · ${data.totalElements} actividades` : ''}
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

      <ConfirmModal
        open={Boolean(toDelete)}
        title="Eliminar actividad"
        description={toDelete ? `Esta acción eliminará "${toDelete.title}" del banco.` : ''}
        confirmLabel="Eliminar"
        busy={busy}
        onConfirm={confirmDelete}
        onCancel={() => setToDelete(null)}
      />
      <p className="footer-note">Vista compartida: {roleLabel(role)}.</p>
    </>
  );
}
