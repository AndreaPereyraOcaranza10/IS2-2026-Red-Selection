import { useNavigate } from 'react-router-dom';
import { Icon } from './Icon.jsx';
import { useAuth } from '../auth/AuthContext.jsx';

/**
 * Cabecera de inicio de la pantalla de ruta: saludo con el nombre del empleado y atajos
 * a lo que puede hacer su rol. `shortcuts` = [{ to, label, icon }].
 */
export function Greeting({ shortcuts = [] }) {
  const navigate = useNavigate();
  const { user } = useAuth() ?? {};
  const nombre = (user?.fullName || user?.username || '').trim().split(/\s+/)[0];
  return (
    <div className="col" style={{ gap: 8 }}>
      <h2 style={{ margin: 0 }}>{nombre ? `¡Hola, ${nombre}!` : '¡Hola!'}</h2>
      {shortcuts.length > 0 && (
        <div className="row" style={{ flexWrap: 'wrap', gap: 8 }}>
          {shortcuts.map((s) => (
            <button key={s.to} className="btn btn-s md auto" onClick={() => navigate(s.to)}>
              <Icon name={s.icon} size={20} />
              {s.label}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
