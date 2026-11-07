import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { Icon } from '../components/Icon.jsx';
import { Field } from '../components/Field.jsx';
import { Layout, ActionBar } from '../components/Layout.jsx';
import { useToast } from '../components/Toast.jsx';
import { useAuth } from './AuthContext.jsx';
import { passwordRequirements, passwordStrength, STRENGTH_LABELS } from '../utils/password.js';

/** Cambio de clave obligatorio del primer ingreso. Mientras no se complete, RequireAuth bloquea el resto. */
export function ChangePasswordPage({ home = '/' }) {
  const { user, changeInitialPassword } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const [p1, setP1] = useState('');
  const [p2, setP2] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  if (!user) return <Navigate to="/login" replace />;
  if (!user.mustChangePassword) return <Navigate to={home} replace />;

  const reqs = passwordRequirements(p1, p2);
  const valid = reqs.every((r) => r.ok);
  const score = p1 ? Math.max(1, passwordStrength(p1)) : 0;
  const mismatch = p2 && p1 !== p2;

  const save = async () => {
    setBusy(true);
    setError('');
    try {
      await changeInitialPassword(p1);
      toast('Clave actualizada. ¡Ya podés empezar!');
      navigate(home, { replace: true });
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <Layout
      title="Cambiá tu clave"
      bar={
        <ActionBar>
          <button className="btn btn-p" onClick={save} disabled={!valid || busy}>
            {busy ? 'Guardando…' : 'Guardar y continuar'}
          </button>
        </ActionBar>
      }
    >
      <div style={{ maxWidth: 560, width: '100%', margin: '0 auto', display: 'flex', flexDirection: 'column', gap: 16 }}>
        <div className="alert">
          <Icon name="lock" />
          <p>Es tu primer ingreso y entraste con la clave inicial. Por seguridad tenés que elegir una clave nueva antes de continuar.</p>
        </div>
        {error && (
          <div className="alert" style={{ background: 'var(--err-bg)', color: 'var(--err)' }} role="alert">
            <Icon name="x" />
            <p>{error}</p>
          </div>
        )}
        <Field id="p1" label="Clave nueva" type="password" autoComplete="new-password" placeholder="Ej.: Dulce#Mza24" help="Usá letras, números y un símbolo" value={p1} onChange={(e) => setP1(e.target.value)} />
        <div>
          <div className={`strength s${score}`}>
            {[0, 1, 2, 3].map((i) => (
              <i key={i} className={i < score ? 'on' : ''} />
            ))}
          </div>
          <p className="small" style={{ marginTop: 8 }}>
            <span className="sec">Seguridad:</span> <b>{STRENGTH_LABELS[score]}</b>
          </p>
        </div>
        <Field
          id="p2"
          label="Confirmá la clave nueva"
          type="password"
          autoComplete="new-password"
          value={p2}
          onChange={(e) => setP2(e.target.value)}
          error={mismatch ? 'Las claves no coinciden' : ''}
          ok={!!p2 && !mismatch}
          help="Tienen que coincidir"
        />
        <div className="col" style={{ gap: 6 }}>
          {reqs.map((r) => (
            <div key={r.text} className={`req ${r.ok ? 'ok' : ''}`}>
              <Icon name="check" size={16} />
              {r.text}
            </div>
          ))}
        </div>
      </div>
    </Layout>
  );
}
