/** Roles del sistema (coinciden con el enum del backend). */
export const ROLES = {
  PREVENTISTA: 'PREVENTISTA',
  TRANSPORTISTA: 'TRANSPORTISTA',
  ADMINISTRATIVO: 'ADMINISTRATIVO',
  JEFE_ADMINISTRATIVO: 'JEFE_ADMINISTRATIVO',
  CLIENTE: 'CLIENTE',
};

/** Etapas del circuito del pedido: Preventor -> Stock -> Facturación -> Logística -> Entrega. */
export const ORDER_STAGES = ['CARGADO', 'PENDIENTE_STOCK', 'PENDIENTE_PAGO', 'FACTURADO', 'EN_REPARTO', 'ENTREGADO'];

export const ORDER_STATE = {
  CARGADO: { label: 'Cargado', kind: 'carg' },
  PENDIENTE_STOCK: { label: 'Pendiente de stock', kind: 'stock' },
  PENDIENTE_PAGO: { label: 'Esperando pago', kind: 'pago' },
  FACTURADO: { label: 'Facturado', kind: 'fact' },
  EN_REPARTO: { label: 'En reparto', kind: 'rep' },
  ENTREGADO: { label: 'Entregado', kind: 'ent' },
  ANULADO: { label: 'Anulado', kind: 'anu' },
};

export const STOP_STATUS = {
  PENDIENTE: { label: 'Pendiente', kind: 'pend' },
  VISITADO: { label: 'Visitado', kind: 'vis' },
  ENTREGADO: { label: 'Entregado', kind: 'ent' },
};

export const PAYMENT_LABEL = {
  EFECTIVO: 'Efectivo',
  TRANSFERENCIA: 'Transferencia',
  BILLETERA_QR: 'Billetera virtual',
  BILLETERA_VIRTUAL: 'Billetera virtual',
};