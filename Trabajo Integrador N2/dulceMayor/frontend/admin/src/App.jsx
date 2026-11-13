import { Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { AuthProvider, ChangePasswordPage, LoginPage, Screens, ToastProvider, useAuth } from '@dulcemayor/shared';
import { ADMIN_ROLES } from './config.js';
import { OperationsDashboardPage } from './pages/OperationsDashboardPage.jsx';

function AccessDenied() {
  const { user, logout } = useAuth();
  return (
    <main className="admin-access-denied">
      <section className="admin-access-card" role="alert">
        <span aria-hidden="true">403</span>
        <h1>Acceso denegado</h1>
        <p>La cuenta {user?.username ? `"${user.username}"` : 'actual'} no tiene un rol habilitado para el panel administrativo.</p>
        <button className="btn btn-p" onClick={logout}>Cerrar sesi\u00f3n</button>
      </section>
    </main>
  );
}

/**
 * Mismo marco de pantalla que preventista y transportista para el login y el cambio de clave.
 * No usa AppRoot porque admin ya tiene su ToastProvider; la altura fija compensa el height:auto de admin.css.
 */
function StaffScreen({ children }) {
  return (
    <div className="app-root" style={{ height: '100dvh' }}>
      <Screens>{children}</Screens>
    </div>
  );
}

function StaffPasswordPage() {
  const { user, logout } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (!ADMIN_ROLES.includes(user.role)) return <AccessDenied />;
  return <ChangePasswordPage home="/admin/inicio" onLogout={logout} />;
}

function AdminOnly() {
  const { user } = useAuth();
  const location = useLocation();
  if (!user) return <Navigate to="/login" replace state={{ from: location }} />;
  if (user.mustChangePassword) return <Navigate to="/cambiar-clave" replace />;
  if (!ADMIN_ROLES.includes(user.role)) return <AccessDenied />;
  return <OperationsDashboardPage />;
}

function AppRoutes() {
  const { user } = useAuth();
  return (
    <Routes>
      <Route path="/login" element={<StaffScreen><LoginPage title="Centro de mando administrativo" allowedRoles={ADMIN_ROLES} demoUser="admin.mza" home="/admin/inicio" /></StaffScreen>} />
      <Route path="/cambiar-clave" element={<StaffScreen><StaffPasswordPage /></StaffScreen>} />
      <Route path="/acceso-denegado" element={<AccessDenied />} />
      <Route path="/" element={<Navigate to={user ? '/admin/inicio' : '/login'} replace />} />
      <Route path="/admin/*" element={<AdminOnly />} />
      <Route path="*" element={<Navigate to="/acceso-denegado" replace />} />
    </Routes>
  );
}

export default function App() {
  return <AuthProvider><ToastProvider><AppRoutes /></ToastProvider></AuthProvider>;
}
