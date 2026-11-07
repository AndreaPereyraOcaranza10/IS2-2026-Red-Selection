import { createContext, useContext, useEffect, useState } from 'react';
import { Icon } from './Icon.jsx';
import { Sheet } from './Overlay.jsx';
import { MapView } from './MapView.jsx';
import { USE_MOCK } from '../api/http.js';
import { DEMO_ORIGIN, currentPosition, fetchRoute } from '../utils/routing.js';
import { formatDistance, openNavigation } from '../utils/format.js';

const Ctx = createContext(() => {});

/** Convierte una parada (visita/entrega) en el destino que entiende `useDirections()`. */
export const stopDestination = (s) => ({ lat: s.lat, lng: s.lng, name: s.name, address: s.address, text: s.sequence ?? s.id });

/**
 * "Cómo llegar" dentro de la app: pide la ubicación, calcula la ruta y la dibuja en el mapa
 * con distancia y tiempo. Uso: const directions = useDirections(); directions(stopDestination(stop)).
 */
export function DirectionsProvider({ children }) {
  const [dest, setDest] = useState(null);
  return (
    <Ctx.Provider value={setDest}>
      {children}
      {dest && <DirectionsSheet dest={dest} onClose={() => setDest(null)} />}
    </Ctx.Provider>
  );
}

export const useDirections = () => useContext(Ctx);

function DirectionsSheet({ dest, onClose }) {
  const [state, setState] = useState({ status: 'loading' });

  useEffect(() => {
    let alive = true;
    setState({ status: 'loading' });
    (async () => {
      try {
        const from = await currentPosition().catch((e) => {
          if (USE_MOCK) return DEMO_ORIGIN; // la demo sigue funcionando sin permiso de ubicación
          throw e;
        });
        const route = await fetchRoute(from, dest);
        if (alive) setState({ status: 'ok', from, route });
      } catch (e) {
        if (alive) setState({ status: 'error', message: e.message });
      }
    })();
    return () => {
      alive = false;
    };
  }, [dest]);

  const destPin = { id: 'dest', text: dest.text ?? '★', lat: dest.lat, lng: dest.lng, status: 'pend', label: dest.name };
  const pins = state.status === 'ok' ? [{ id: 'me', text: '★', lat: state.from.lat, lng: state.from.lng, status: 'vis', label: 'Tu ubicación' }, destPin] : [destPin];

  return (
    <Sheet open onClose={onClose}>
      <div>
        <h2 style={{ fontSize: 22, lineHeight: '28px' }}>Cómo llegar</h2>
        <p className="sec small">
          {dest.name}
          {dest.address ? ` · ${dest.address}` : ''}
        </p>
      </div>

      {state.status === 'loading' ? (
        <div className="state" style={{ padding: 16 }} aria-busy="true">
          <div className="sk" style={{ height: 200, width: '100%' }} />
          <p className="sec small">Calculando la ruta…</p>
        </div>
      ) : (
        <>
          <MapView interactive route={false} pins={pins} line={state.status === 'ok' ? state.route.geometry : null} />
          {state.status === 'ok' ? (
            <div className="row" style={{ gap: 8 }}>
              <Icon name="clock" size={20} />
              <b style={{ fontSize: 18 }}>{`${state.route.durationMin} min`}</b>
              <span className="sec">· {formatDistance(state.route.distanceKm)} en auto</span>
            </div>
          ) : (
            <div className="alert" style={{ background: 'var(--err-bg)', color: 'var(--err)' }} role="alert">
              <Icon name="x" />
              <p>{state.message}</p>
            </div>
          )}
        </>
      )}

      <button className="btn btn-s" onClick={() => openNavigation(dest.lat, dest.lng)}>
        <Icon name="nav" size={20} />
        Abrir en Google Maps
      </button>
      <button className="btn btn-g" onClick={onClose}>
        Cerrar
      </button>
    </Sheet>
  );
}
