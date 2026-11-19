import { Route, Routes, Navigate } from 'react-router-dom';
import { useAuth, homePathForRole } from './auth/AuthContext.jsx';
import ProtectedRoute from './routes/ProtectedRoute.jsx';
import InternalLayout from './layouts/InternalLayout.jsx';

import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import ForgotPassword from './pages/ForgotPassword.jsx';
import ResetPassword from './pages/ResetPassword.jsx';
import NotFound from './pages/NotFound.jsx';

import StudentHome from './pages/student/StudentHome.jsx';
import StudentTasks from './pages/student/StudentTasks.jsx';
import StudentActivity from './pages/student/StudentActivity.jsx';
import StudentResult from './pages/student/StudentResult.jsx';
import StudentProgress from './pages/student/StudentProgress.jsx';

import TeacherHome from './pages/teacher/TeacherHome.jsx';

import ActivityBank from './pages/shared/ActivityBank.jsx';
import ActivityEditor from './pages/shared/ActivityEditor.jsx';
import AssignmentForm from './pages/shared/AssignmentForm.jsx';
import TeacherResults from './pages/shared/TeacherResults.jsx';

import AdminUsers from './pages/admin/AdminUsers.jsx';
import AdminCategories from './pages/admin/AdminCategories.jsx';

/** Si ya hay sesión y visita una pantalla de acceso, mándalo a su panel. */
function PublicOnly({ children }) {
  const { isAuthenticated, role } = useAuth();
  if (isAuthenticated) return <Navigate to={homePathForRole(role)} replace />;
  return children;
}

export default function App() {
  return (
    <Routes>
      {/* Acceso (público, sin menú ni topbar) */}
      <Route path="/login" element={<PublicOnly><Login /></PublicOnly>} />
      <Route path="/registro" element={<PublicOnly><Register /></PublicOnly>} />
      <Route path="/recuperar" element={<PublicOnly><ForgotPassword /></PublicOnly>} />
      <Route path="/restablecer" element={<PublicOnly><ResetPassword /></PublicOnly>} />

      {/* Estudiante */}
      <Route
        path="/estudiante/inicio"
        element={
          <ProtectedRoute allow={['STUDENT']}>
            <InternalLayout crumb="05 · Inicio estudiante"><StudentHome /></InternalLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/estudiante/tareas"
        element={
          <ProtectedRoute allow={['STUDENT']}>
            <InternalLayout crumb="06 · Tareas y práctica"><StudentTasks /></InternalLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/estudiante/actividad/:id"
        element={
          <ProtectedRoute allow={['STUDENT']}>
            <InternalLayout crumb="07 · Resolver actividad"><StudentActivity /></InternalLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/estudiante/resultado/:intentoId"
        element={
          <ProtectedRoute allow={['STUDENT']}>
            <InternalLayout crumb="08 · Resultado"><StudentResult /></InternalLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/estudiante/progreso"
        element={
          <ProtectedRoute allow={['STUDENT']}>
            <InternalLayout crumb="09 · Mi progreso"><StudentProgress /></InternalLayout>
          </ProtectedRoute>
        }
      />
      <Route path="/estudiante/*" element={
        <ProtectedRoute allow={['STUDENT']}>
          <Navigate to="/estudiante/inicio" replace />
        </ProtectedRoute>
      } />

      {/* Profesor */}
      <Route
        path="/profesor/inicio"
        element={
          <ProtectedRoute allow={['TEACHER']}>
            <InternalLayout crumb="10 · Inicio profesor"><TeacherHome /></InternalLayout>
          </ProtectedRoute>
        }
      />
      <Route path="/profesor/*" element={
        <ProtectedRoute allow={['TEACHER']}>
          <Navigate to="/profesor/inicio" replace />
        </ProtectedRoute>
      } />

      {/* Compartidas: profesor + admin */}
      <Route
        path="/actividades"
        element={
          <ProtectedRoute allow={['TEACHER', 'ADMIN']}>
            <InternalLayout crumb="11 · Banco de actividades"><ActivityBank /></InternalLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/actividades/nueva"
        element={
          <ProtectedRoute allow={['TEACHER', 'ADMIN']}>
            <InternalLayout crumb="12 · Crear actividad"><ActivityEditor /></InternalLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/actividades/:id/editar"
        element={
          <ProtectedRoute allow={['TEACHER', 'ADMIN']}>
            <InternalLayout crumb="12 · Crear actividad"><ActivityEditor /></InternalLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/asignaciones/nueva"
        element={
          <ProtectedRoute allow={['TEACHER', 'ADMIN']}>
            <InternalLayout crumb="13 · Asignar tarea"><AssignmentForm /></InternalLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/resultados"
        element={
          <ProtectedRoute allow={['TEACHER', 'ADMIN']}>
            <InternalLayout crumb="14 · Resultados"><TeacherResults /></InternalLayout>
          </ProtectedRoute>
        }
      />

      {/* Admin */}
      <Route
        path="/admin/usuarios"
        element={
          <ProtectedRoute allow={['ADMIN']}>
            <InternalLayout crumb="15 · Administrar usuarios"><AdminUsers /></InternalLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/categorias"
        element={
          <ProtectedRoute allow={['ADMIN']}>
            <InternalLayout crumb="16 · Categorías"><AdminCategories /></InternalLayout>
          </ProtectedRoute>
        }
      />
      <Route path="/admin/*" element={
        <ProtectedRoute allow={['ADMIN']}>
          <Navigate to="/admin/usuarios" replace />
        </ProtectedRoute>
      } />

      {/* Raíz: panel según el rol que devuelve el backend */}
      <Route path="/" element={<HomeRedirect />} />
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}

function HomeRedirect() {
  const { isAuthenticated, role } = useAuth();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return <Navigate to={homePathForRole(role)} replace />;
}
