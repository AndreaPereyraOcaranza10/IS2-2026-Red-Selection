import { BottomNav } from '@dulcemayor/shared';
import { useCart } from '../context/CartContext.jsx';

/** Navegación inferior del preventista (4 destinos). El ítem "Pedido" muestra la cantidad cargada. */
export default function PreventistaNav() {
  const { count } = useCart();
  return (
    <BottomNav
      items={[
        { to: '/ruta', label: 'Ruta', icon: 'ruta', match: ['/ruta', '/zona', '/pedido-confirmado', '/alta'] },
        { to: '/catalogo', label: 'Catálogo', icon: 'cat' },
        { to: '/pedido', label: 'Pedido', icon: 'ped', badge: count },
        { to: '/pedidos', label: 'Mis pedidos', icon: 'list' },
      ]}
    />
  );
}
