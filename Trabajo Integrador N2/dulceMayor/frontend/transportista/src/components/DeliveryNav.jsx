import { BottomNav } from '@dulcemayor/shared';

/** Navegación inferior del transportista (3 destinos). No existe ítem de pedidos: no toma pedidos. */
export default function DeliveryNav() {
  return (
    <BottomNav
      items={[
        { to: '/reparto', label: 'Reparto', icon: 'truck', match: ['/reparto', '/entrega', '/entrega-registrada'] },
        { to: '/mapa', label: 'Mapa', icon: 'map' },
        { to: '/entregas', label: 'Entregas', icon: 'done' },
      ]}
    />
  );
}
