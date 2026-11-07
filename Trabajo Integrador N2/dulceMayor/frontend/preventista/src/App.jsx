import { Navigate, Route, Routes } from 'react-router-dom';
import { AppRoot, AuthProvider, ChangePasswordPage, LoginPage, RequireAuth, Screens } from '@dulcemayor/shared';
import { ALLOWED_ROLES, DEMO_USER, HOME } from './config.js';
import { CartProvider } from './context/CartContext.jsx';
import RoutePage from './pages/RoutePage.jsx';
import ZonePage from './pages/ZonePage.jsx';
import CatalogPage from './pages/CatalogPage.jsx';
import OrderPage from './pages/OrderPage.jsx';
import OrderDonePage from './pages/OrderDonePage.jsx';
import OrdersPage from './pages/OrdersPage.jsx';
import OrderDetailPage from './pages/OrderDetailPage.jsx';
import PointOfSaleFormPage from './pages/PointOfSaleFormPage.jsx';

/** App del Preventista: mobile-first, con toma de pedidos y alta de puntos de venta. */
export default function App() {
  return (
    <AppRoot>
      <AuthProvider>
        {/* <Screens> va DENTRO de los providers: la transición solo remonta la pantalla, no el estado (carrito). */}
        <Routes>
          <Route path="/login" element={<Screens><LoginPage title="¡Bienvenido al equipo de campo!" allowedRoles={ALLOWED_ROLES} demoUser={DEMO_USER} home={HOME} /></Screens>} />
          <Route path="/cambiar-clave" element={<Screens><ChangePasswordPage home={HOME} /></Screens>} />
          <Route
            path="/*"
            element={
              <RequireAuth roles={ALLOWED_ROLES}>
                <CartProvider>
                  <Screens>
                    <Routes>
                      <Route path="/ruta" element={<RoutePage />} />
                      <Route path="/zona" element={<ZonePage />} />
                      <Route path="/catalogo" element={<CatalogPage />} />
                      <Route path="/pedido" element={<OrderPage />} />
                      <Route path="/pedido-confirmado" element={<OrderDonePage />} />
                      <Route path="/pedidos" element={<OrdersPage />} />
                      <Route path="/pedidos/:id" element={<OrderDetailPage />} />
                      <Route path="/alta" element={<PointOfSaleFormPage />} />
                      <Route path="*" element={<Navigate to={HOME} replace />} />
                    </Routes>
                  </Screens>
                </CartProvider>
              </RequireAuth>
            }
          />
        </Routes>
      </AuthProvider>
    </AppRoot>
  );
}
