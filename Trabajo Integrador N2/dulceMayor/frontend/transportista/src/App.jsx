import { Navigate, Route, Routes } from 'react-router-dom';
import { AppRoot, AuthProvider, ChangePasswordPage, LoginPage, RequireAuth, Screens } from '@dulcemayor/shared';
import { ALLOWED_ROLES, DEMO_USER, HOME } from './config.js';
import DeliveryRoutePage from './pages/DeliveryRoutePage.jsx';
import DeliveryMapPage from './pages/DeliveryMapPage.jsx';
import DeliveriesDonePage from './pages/DeliveriesDonePage.jsx';
import NextDeliveryPage from './pages/NextDeliveryPage.jsx';
import DeliveryRegisteredPage from './pages/DeliveryRegisteredPage.jsx';
import DeliveryDetailPage from './pages/DeliveryDetailPage.jsx';

/** App del Transportista: ruta de reparto, entrega y trazabilidad. No toma pedidos ni permite llamar al cliente. */
export default function App() {
  return (
    <AppRoot>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<Screens><LoginPage title="¡Bienvenido al equipo de reparto!" allowedRoles={ALLOWED_ROLES} demoUser={DEMO_USER} home={HOME} /></Screens>} />
          <Route path="/cambiar-clave" element={<Screens><ChangePasswordPage home={HOME} /></Screens>} />
          <Route
            path="/*"
            element={
              <RequireAuth roles={ALLOWED_ROLES}>
                <Screens>
                  <Routes>
                    <Route path="/reparto" element={<DeliveryRoutePage />} />
                    <Route path="/reparto/:id" element={<DeliveryDetailPage />} />
                    <Route path="/pedido/:orderId" element={<DeliveryDetailPage />} />
                    <Route path="/mapa" element={<DeliveryMapPage />} />
                    <Route path="/entregas" element={<DeliveriesDonePage />} />
                    <Route path="/entrega/:id" element={<NextDeliveryPage />} />
                    <Route path="/entrega-registrada" element={<DeliveryRegisteredPage />} />
                    <Route path="*" element={<Navigate to={HOME} replace />} />
                  </Routes>
                </Screens>
              </RequireAuth>
            }
          />
        </Routes>
      </AuthProvider>
    </AppRoot>
  );
}
