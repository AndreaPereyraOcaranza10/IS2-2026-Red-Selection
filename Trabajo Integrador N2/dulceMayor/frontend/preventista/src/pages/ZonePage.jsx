import { useState } from 'react';
import { Async, Chip, Layout, MapView, StopCard, useAsync } from '@dulcemayor/shared';
import { preventistaApi } from '../api.js';
import PreventistaNav from '../components/PreventistaNav.jsx';
import StopSheet from '../components/StopSheet.jsx';

/** Mapa de mi zona: todos los puntos de venta asignados, visitados y pendientes. */
export default function ZonePage() {
  const zone = useAsync(preventistaApi.zone, []);
  const [selId, setSelId] = useState(null);
  const [sheet, setSheet] = useState(false);

  return (
    <Layout title="Mapa de mi zona" back backTo="/ruta" flush nav={<PreventistaNav />}>
      <Async state={zone}>
        {(points) => {
          const sel = points.find((p) => p.id === selId) || points.find((p) => p.inRoute && p.status === 'PENDIENTE') || points[0];
          const pins = points.map((p) => ({ id: p.id, text: p.sequence ?? '•', lat: p.lat, lng: p.lng, label: p.name, status: p.status === 'VISITADO' ? 'vis' : 'pend' }));
          return (
            <div style={{ position: 'relative', flex: 1, display: 'flex' }}>
              <MapView tall route={false} pins={pins} selectedId={sel.id} onPinClick={setSelId} />
              <div style={{ position: 'absolute', left: 16, top: 16, display: 'flex', gap: 8 }}>
                <Chip kind="vis">Visitado</Chip>
                <Chip kind="pend">Pendiente</Chip>
              </div>
              <div style={{ position: 'absolute', left: 16, right: 16, bottom: 16, maxWidth: 440 }}>
                <StopCard stop={sel} onSelect={() => setSheet(true)} />
              </div>
              {sheet && <StopSheet stop={sel} onClose={() => setSheet(false)} onChanged={zone.reload} />}
            </div>
          );
        }}
      </Async>
    </Layout>
  );
}
