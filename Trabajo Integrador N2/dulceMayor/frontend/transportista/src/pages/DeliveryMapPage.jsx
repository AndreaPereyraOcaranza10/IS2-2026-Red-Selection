import { useNavigate } from 'react-router-dom';
import { Async, Chip, Layout, MapView, StopCard, useAsync } from '@dulcemayor/shared';
import { deliveryApi } from '../api.js';
import DeliveryNav from '../components/DeliveryNav.jsx';
import { deliveryPins } from './DeliveryRoutePage.jsx';

/** Mapa del reparto: recorrido completo con la siguiente entrega destacada. */
export default function DeliveryMapPage() {
  const navigate = useNavigate();
  const route = useAsync(deliveryApi.route, []);

  return (
    <Layout title="Mapa del reparto" flush nav={<DeliveryNav />}>
      <Async state={route}>
        {(r) => {
          const next = r.stops.find((s) => s.status === 'PENDIENTE');
          return (
            <div style={{ position: 'relative', flex: 1, display: 'flex' }}>
              <MapView tall pins={deliveryPins(r.stops, next)} selectedId={next?.id} onPinClick={(id) => navigate(`/reparto/${id}`)} />
              <div style={{ position: 'absolute', left: 16, top: 16, display: 'flex', gap: 8 }}>
                <Chip kind="ent">Entregado</Chip>
                <Chip kind="pend">Pendiente</Chip>
              </div>
              {next && (
                <div style={{ position: 'absolute', left: 16, right: 16, bottom: 16, maxWidth: 440 }}>
                  <StopCard stop={next} highlight actionLabel="Ir a la entrega" onAction={() => navigate(`/entrega/${next.id}`)} onSelect={() => navigate(`/reparto/${next.id}`)} />
                </div>
              )}
            </div>
          );
        }}
      </Async>
    </Layout>
  );
}
