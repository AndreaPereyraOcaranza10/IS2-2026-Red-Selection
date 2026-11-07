import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { Logo } from '../components/Icon.jsx';
import { Icon } from '../components/Icon.jsx';
import { Field } from '../components/Field.jsx';
import { useAuth } from './AuthContext.jsx';
import { USE_MOCK } from '../api/http.js';
import { INITIAL_PASSWORD } from '../utils/password.js';

/**
 * Login compartido por todas las apps. Cada app lo configura con su título y los roles que admite.
 * demoUser: usuario precargado solo en modo demo (VITE_USE_MOCK=true).
 */
export function LoginPage({ title, allowedRoles, demoUser, home = '/' }) {
  const { user, login, logout } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState(USE_MOCK ? demoUser : '');
  const [password, setPassword] = useState(USE_MOCK ? INITIAL_PASSWORD : '');
  const [show, setShow] = useState(false);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const allowed = user && allowedRoles.includes(user.role);
  if (allowed && !user.mustChangePassword) return <Navigate to={home} replace />;
  if (allowed && user.mustChangePassword) return <Navigate to="/cambiar-clave" replace />;

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      const u = await login(username.trim(), password);
      if (!allowedRoles.includes(u.role)) {
        logout();
        setError('Tu usuario no tiene acceso a esta aplicación.');
        return;
      }
      navigate(u.mustChangePassword ? '/cambiar-clave' : home, { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="login">
      <form className="box" onSubmit={submit} noValidate>
        <Logo />
        <h1 style={{ fontSize: 22, lineHeight: '28px', fontWeight: 700, textAlign: 'center' }}>{title}</h1>
        {error && (
          <div className="alert" style={{ background: 'var(--err-bg)', color: 'var(--err)' }} role="alert">
            <Icon name="x" />
            <p>{error}</p>
          </div>
        )}
        <Field id="u" label="Usuario" value={username} autoComplete="username" onChange={(e) => setUsername(e.target.value)} />
        <Field
          id="k"
          label="Clave"
          type={show ? 'text' : 'password'}
          value={password}
          autoComplete="current-password"
          onChange={(e) => setPassword(e.target.value)}
          help={`Primer ingreso: la clave inicial es «${INITIAL_PASSWORD}»`}
          right={
            <button type="button" className="icon-btn" style={{ marginRight: -12 }} onClick={() => setShow((s) => !s)} aria-label="Mostrar clave">
              <Icon name="eye" />
            </button>
          }
        />
        <button className="btn btn-p" type="submit" disabled={busy || !username || !password}>
          {busy ? 'Ingresando…' : 'Ingresar'}
        </button>
        {USE_MOCK && <p className="hint">Modo demo: la API está simulada en memoria. Usuario de prueba precargado.</p>}
      </form>
    </div>
  );
}
