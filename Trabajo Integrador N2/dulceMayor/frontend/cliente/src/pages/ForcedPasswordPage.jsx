import { Navigate } from "react-router-dom";
import { PasswordChangeForm } from "../components/PasswordChangeForm.jsx";

export function ForcedPasswordPage({ account, onComplete }) {
  if (!account) return <Navigate to="/ingresar" replace />;
  if (!account.mustChangePassword) return <Navigate to="/" replace />;
  return <main className="profile-page password-required-page">
    <div className="profile-heading"><div><span className="section-kicker">SEGURIDAD DE LA CUENTA</span><h1>Cambia tu clave</h1><p>Elige una clave nueva para continuar.</p></div></div>
    <PasswordChangeForm firstLogin onSuccess={onComplete} />
  </main>;
}
