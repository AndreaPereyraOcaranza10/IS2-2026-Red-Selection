import { Routes, Route } from "react-router-dom";
import Header from "./components/header/Header";
import Home from "./views/Home";
import ClientPage from "./views/ClientPage";
import AdminPage from "./views/AdminPage";
import ClientProfilePage from "./views/ClientProfilePage";
import ErrorPage from "./views/ErrorPage";
import { AuthenticationGuard } from "./components/auth0/AuthenticationGuard";
import CallbackPage from "./components/auth0/CallbackPage";

function App() {
  return (
    <div className="min-h-screen bg-slate-900 text-white">
      {/* 1. Header con botones de Log In / Sign Up o Perfil / Log Out */}
      <Header />

      {/* 2. Enrutador sincronizado con los enlaces en español */}
      <main className="container mx-auto p-4">
        <Routes>
          {/* Ruta pública */}
          <Route path="/" element={<Home />} />
          
          {/* Retorno de Auth0 */}
          <Route path="/callback" element={<CallbackPage />} />

          {/* Rutas protegidas */}
          <Route 
            path="/cliente" 
            element={<AuthenticationGuard component={ClientPage} />} 
          />
          <Route 
            path="/cliente/perfil" 
            element={<AuthenticationGuard component={ClientProfilePage} />} 
          />
          <Route 
            path="/admin" 
            element={<AuthenticationGuard component={AdminPage} />} 
          />

          {/* 404 */}
          <Route path="*" element={<ErrorPage />} />
        </Routes>
      </main>
    </div>
  );
}

export default App;