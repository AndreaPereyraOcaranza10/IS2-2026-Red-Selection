import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { ActionBar, Icon, Layout, MapView, fmt, formatDistance } from '@dulcemayor/shared';

/**
 * Entrega registrada + próximo punto en el mapa + aviso de mail al próximo comercio.
 * Recibe { delivered, nextStop, emailSent } por location.state (respuesta de POST .../deliver).
 */
export default function DeliveryRegisteredPage() {
  const { state } = useLocation();
  const navigate = useNavigate();
  if (!state?.delivered) return <Navigate to="/reparto" replace />;

  const { delivered, nextStop, emailSent } = state;

  return (
    <Layout
      bar={
        <ActionBar>
          {nextStop && (
            <button className="btn btn-p" onClick={() => navigate(`/entrega/${nextStop.id}`, { replace: true })}>
              <Icon name="nav" size={20} />
              Ir a la siguiente entrega
            </button>
          )}
          <button className={`btn ${nextStop ? 'btn-s' : 'btn-p'}`} onClick={() => navigate('/reparto', { replace: true })}>Volver a la ruta</button>
        </ActionBar>
      }
    >
      <div className="col" style={{ maxWidth: 560, width: '100%', margin: '0 auto' }}>
        <div className="state" style={{ padding: '16px 0' }}>
          <div className="big" style={{ background: 'var(--ok-bg)', color: 'var(--ok)', width: 112, height: 112 }}>
            <Icon name="check" size={64} />
          </div>
          <h1 style={{ fontSize: 28, lineHeight: '34px', fontWeight: 800 }}>¡Entrega registrada!</h1>
          <p className="sec">{delivered.name} · {fmt(delivered.total)}</p>
        </div>
        {nextStop ? (
          <>
            {emailSent && (
              <div className="alert" style={{ background: 'var(--secondary-container)', color: 'var(--secondary-strong)' }}>
                <Icon name="check" />
                <p><b>Se avisó por correo al próximo comercio.</b></p>
              </div>
            )}
            <h2>Próximo punto</h2>
            <MapView
              pins={[
                { id: delivered.id, text: delivered.sequence, lat: delivered.lat, lng: delivered.lng, status: 'vis', label: delivered.name },
                { id: nextStop.id, text: nextStop.sequence, lat: nextStop.lat, lng: nextStop.lng, status: 'next', label: nextStop.name },
              ]}
            />
            <article className="card row">
              <div className="num">{nextStop.sequence}</div>
              <div className="grow">
                <b>{nextStop.name}</b>
                <p className="sec small">{nextStop.address} · {formatDistance(nextStop.distanceKm)}</p>
              </div>
            </article>
          </>
        ) : (
          <div className="alert" style={{ background: 'var(--ok-bg)', color: 'var(--ok)' }}>
            <Icon name="done" />
            <p><b>¡Completaste todas las entregas de hoy!</b></p>
          </div>
        )}
      </div>
    </Layout>
  );
}
