import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { api, setActor, setAuthToken, setUnauthorizedHandler } from '../api/http.js';

const KEY = 'dm.session';
const Ctx = createContext(null);

function load() {
  try {
    const s = JSON.parse(sessionStorage.getItem(KEY));
    if (s?.token) {
      setAuthToken(s.token);
      setActor(s.user);
    }
    return s;
  } catch {
    return null;
  }
}

/**
 * Sesión JWT. El backend responde { token, user: { id, employeeId?, username, fullName, role, mustChangePassword } }.
 * El token vive en sessionStorage (se pierde al cerrar la pestaña).
 */
export function AuthProvider({ children }) {
  const [session, setSession] = useState(load);

  const persist = useCallback((s) => {
    setSession(s);
    setAuthToken(s?.token ?? null);
    setActor(s?.user);
    try {
      if (s) sessionStorage.setItem(KEY, JSON.stringify(s));
      else sessionStorage.removeItem(KEY);
    } catch {
      /* almacenamiento no disponible: la sesión vive solo en memoria */
    }
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(() => persist(null));
  }, [persist]);

  const value = useMemo(
    () => ({
      user: session?.user ?? null,
      login: async (username, password) => {
        const res = await api.post('/api/auth/login', { username, password });
        persist(res);
        return res.user;
      },
      /** Cambio de clave obligatorio del primer ingreso (ver regla de negocio `dulcemayor`). */
      changeInitialPassword: async (newPassword) => {
        const res = await api.post('/api/auth/first-login/change-password', { newPassword });
        persist(res);
        return res.user;
      },
      /** Avisa al servidor para que revoque el token (best-effort) y limpia la sesión local. */
      logout: () => {
        if (session?.token) api.post('/api/auth/logout').catch(() => {});
        persist(null);
      },
    }),
    [session, persist],
  );

  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}

export const useAuth = () => useContext(Ctx);

/** Guard de rutas: exige sesión, cambio de clave resuelto y rol permitido para esta app. */
export function RequireAuth({ roles, children }) {
  const { user } = useAuth();
  const location = useLocation();
  if (!user) return <Navigate to="/login" replace state={{ from: location }} />;
  if (user.mustChangePassword) return <Navigate to="/cambiar-clave" replace />;
  if (roles && !roles.includes(user.role)) return <Navigate to="/login" replace />;
  return children;
}