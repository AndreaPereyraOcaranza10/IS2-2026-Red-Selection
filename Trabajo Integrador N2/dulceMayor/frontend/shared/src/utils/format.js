export const fmt = (n) => '$ ' + Math.round(n).toLocaleString('es-AR');

export function formatDateTime(iso) {
  const d = new Date(iso);
  const day = d.toLocaleDateString('es-AR', { day: '2-digit', month: '2-digit' });
  const time = d.toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit', hour12: false });
  return `${day} · ${time} hs`;
}

export const formatShortDate = (iso) =>
  new Date(iso).toLocaleDateString('es-AR', { day: 'numeric', month: 'short' });

export const formatTime = (iso) =>
  new Date(iso).toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit', hour12: false });

export function formatDistance(km) {
  if (km < 1) return `${Math.round(km * 1000)} m`;
  return `${km.toLocaleString('es-AR', { minimumFractionDigits: 1, maximumFractionDigits: 1 })} km`;
}

export const capitalize = (s) => s.charAt(0).toUpperCase() + s.slice(1);

/** Abre la navegación turn-by-turn del dispositivo hacia unas coordenadas. */
export function openNavigation(lat, lng) {
  window.open(`https://www.google.com/maps/dir/?api=1&destination=${lat},${lng}`, '_blank', 'noopener');
}

/** Máscara de CUIT: 20-12345678-9 */
export function maskCuit(value) {
  const d = value.replace(/\D/g, '').slice(0, 11);
  if (d.length > 10) return `${d.slice(0, 2)}-${d.slice(2, 10)}-${d.slice(10)}`;
  if (d.length > 2) return `${d.slice(0, 2)}-${d.slice(2)}`;
  return d;
}
