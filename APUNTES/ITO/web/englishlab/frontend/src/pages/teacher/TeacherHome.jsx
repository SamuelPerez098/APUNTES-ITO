import { useNavigate } from 'react-router-dom';
import PageHeader from '../../components/PageHeader.jsx';
import MockNotice from '../../components/MockNotice.jsx';
import { useRemote, REMOTE_STATUS } from '../../api/useRemote.js';
import { listStudents } from '../../api/teacher.js';
import { mockStudentsPage } from '../../mock/mockServer.js';

/** 10 · Inicio profesor (pageTeacherHome del Figma, HTML:75).
 *  Conectado (ronda 2, pendiente de backend): GET /teacher/students alimenta el
 *  contador de estudiantes. El resto del panel sigue como muestra hasta que
 *  existan endpoints de conteo de actividades/asignaciones. */
export default function TeacherHome() {
  const navigate = useNavigate();

  const students = useRemote({
    fetcher: () => listStudents({ page: 0, size: 1, sort: 'name,asc' }),
    fallback: () => mockStudentsPage({ page: 0, size: 1, sort: 'name,asc' }),
  });

  const studentCount = students.data?.totalElements ?? students.data?.content?.length ?? null;

  return (
    <>
      <PageHeader
        eyebrow="PORTAL / PROFESOR"
        title="Organiza el aprendizaje"
        subtitle="Crea actividades, asígnalas a estudiantes y revisa los resultados."
      />
      {students.status === REMOTE_STATUS.fallback ? <MockNotice what="el panel del profesor" /> : null}
      <div className="grid g3">
        <div className="card">
          <div className="stat-label">Estudiantes</div>
          <div className="stat-number">{studentCount ?? '—'}</div>
        </div>
        <div className="card">
          <div className="stat-label">Actividades disponibles</div>
          <div className="stat-number">8</div>
        </div>
        <div className="card">
          <div className="stat-label">Asignaciones pendientes</div>
          <div className="stat-number">5</div>
        </div>
      </div>
      <div className="grid g2 mt">
        <div className="card dark">
          <p className="eyebrow" style={{ color: '#e7d7bf' }}>CREAR CONTENIDO</p>
          <h2 style={{ font: '30px Georgia, serif' }}>
            Una actividad, muchas oportunidades de aprender.
          </h2>
          <p className="card-sub">
            Diseña ejercicios sencillos con opciones, espacios o palabras para ordenar.
          </p>
          <button className="btn olive" type="button" onClick={() => navigate('/actividades/nueva')}>
            Crear actividad
          </button>
        </div>
        <div className="card">
          <h3 className="card-title">Acciones frecuentes</h3>
          <div className="panel-row">
            <b>Banco de actividades</b>
            <button className="btn small alt" type="button" onClick={() => navigate('/actividades')}>
              Abrir
            </button>
          </div>
          <div className="panel-row">
            <b>Asignar tarea</b>
            <button className="btn small alt" type="button" onClick={() => navigate('/asignaciones/nueva')}>
              Asignar
            </button>
          </div>
          <div className="panel-row">
            <b>Resultados</b>
            <button className="btn small alt" type="button" onClick={() => navigate('/resultados')}>
              Ver
            </button>
          </div>
        </div>
      </div>
    </>
  );
}
