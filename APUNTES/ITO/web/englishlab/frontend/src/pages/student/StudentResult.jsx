import { Link, useLocation, useParams } from 'react-router-dom';
import PageHeader from '../../components/PageHeader.jsx';
import MockNotice from '../../components/MockNotice.jsx';
import AttemptFeedback from '../../components/AttemptFeedback.jsx';

/** 08 · Resultado (pageResult del Figma, HTML:71).
 *  Muestra lo que devuelve POST /attempts ({correct, score, solution,
 *  explanation, mastery}) por estado de navegación. El cuerpo del resultado vive
 *  en <AttemptFeedback/>, compartido con el modo cola de práctica. Al ser el
 *  final de ese intento, añade el resumen con solo dos datos en porcentaje:
 *  Calificación (score acotado a 0–1) y Dominio del tema (mastery). Si no hay
 *  resultado del servidor (vista directa) se marca como muestra. El envío del
 *  intento no cambia. */
export default function StudentResult() {
  const { intentoId } = useParams();
  const location = useLocation();
  const state = location.state || {};

  const result = state.result || {};
  const isReal = Boolean(state.real);

  return (
    <>
      <PageHeader
        eyebrow="PORTAL / ESTUDIANTE"
        title="Resultado de tu actividad"
        subtitle="La retroalimentación te ayuda a entender cómo mejorar."
      />
      {!isReal ? <MockNotice what={`el intento ${intentoId} (calificación en servidor)`} /> : null}
      <AttemptFeedback
        result={result}
        answer={state.answer}
        activityType={state.activity?.type}
        options={state.activity?.options}
        showSummary={isReal}
      >
        <Link className="btn" to="/estudiante/tareas">
          Mis tareas
        </Link>
        <Link className="btn alt" to="/estudiante/progreso">
          Mi progreso
        </Link>
      </AttemptFeedback>
    </>
  );
}
