import { ORDER_STATE, STOP_STATUS } from '../utils/domain.js';

export function Chip({ kind, children }) {
  return <span className={`chip c-${kind}`}>{children}</span>;
}

export function OrderStateChip({ state }) {
  const s = ORDER_STATE[state];
  return <Chip kind={s.kind}>{s.label}</Chip>;
}

export function StopStatusChip({ status }) {
  const s = STOP_STATUS[status];
  return <Chip kind={s.kind}>{s.label}</Chip>;
}
