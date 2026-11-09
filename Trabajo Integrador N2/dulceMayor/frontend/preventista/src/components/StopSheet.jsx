import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Icon, Sheet, StopStatusChip, stopDestination, useDirections, useToast } from '@dulcemayor/shared';
import { preventistaApi } from '../api.js';
import { useCart } from '../context/CartContext.jsx';

/** Hoja inferior con las acciones de un punto de venta: tomar pedido, cómo llegar y marcar visitado. */
export default function StopSheet({ stop, onClose, onChanged }) {
  const navigate = useNavigate();
  const toast = useToast();
  const cart = useCart();
  const directions = useDirections();
  const [busy, setBusy] = useState(false);
  if (!stop) return null;

  const visited = stop.status === 'VISITADO';
  // Un punto de la zona que no está en la ruta de hoy no tiene parada para marcar como visitada.
  const canMarkVisited = !visited && stop.inRoute !== false;

  const takeOrder = () => {
    // El pedido va al comercio (clientId), no a la parada de la ruta (id)
    cart.setPointOfSale({ id: stop.clientId ?? stop.id, name: stop.name, address: stop.address });
    onClose();
    navigate('/pedido', { replace: true });
  };

  const markVisited = async () => {
    setBusy(true);
    try {
      await preventistaApi.markVisited(stop.id);
      toast(`${stop.name} quedó como visitado`);
      onClose();
      await onChanged?.();
    } catch (e) {
      toast(e.message, { error: true });
    } finally {
      setBusy(false);
    }
  };

  return (
    <Sheet open onClose={onClose}>
      <div className="row">
        <div className={`num ${visited ? 'v' : ''}`}>{stop.sequence ?? '•'}</div>
        <div className="grow">
          <h2>{stop.name}</h2>
          <p className="sec small">{stop.address}</p>
        </div>
        <StopStatusChip status={stop.status} />
      </div>
      <p className="small sec" style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
        <Icon name="user" size={14} />
        {stop.contactName} · {stop.phone}
      </p>
      <button className="btn btn-p" onClick={takeOrder}>
        <Icon name="ped" size={20} />
        Tomar pedido acá
      </button>
      <button className="btn btn-s" onClick={() => { onClose(); directions(stopDestination(stop)); }}>
        <Icon name="nav" size={20} />
        Cómo llegar
      </button>
      {canMarkVisited && (
        <button className="btn btn-g" onClick={markVisited} disabled={busy}>
          Marcar como visitado
        </button>
      )}
    </Sheet>
  );
}