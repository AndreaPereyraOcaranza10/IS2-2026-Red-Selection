import { useEffect, useRef } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

/** Mapa base de OpenStreetMap. Para producción con mucho tráfico conviene un proveedor de teselas (VITE_TILE_URL). */
const TILE_URL = import.meta.env.VITE_TILE_URL || 'https://tile.openstreetmap.org/{z}/{x}/{y}.png';
const ATTRIBUTION = '© <a href="https://www.openstreetmap.org/copyright" target="_blank" rel="noopener">OpenStreetMap</a>';
const FILL = { next: '#FFC83D', vis: '#1B7A45', pend: '#C2185B' };

function pinIcon(pin, big) {
  const size = big ? 44 : 34;
  const dot = document.createElement('div');
  dot.className = 'dm-pin-dot';
  dot.textContent = String(pin.text ?? '');
  Object.assign(dot.style, {
    width: `${size}px`,
    height: `${size}px`,
    background: FILL[pin.status] || FILL.pend,
    color: pin.status === 'next' ? '#5C3B00' : '#fff',
    fontSize: big ? '18px' : '15px',
  });
  return L.divIcon({ className: 'dm-pin', html: dot, iconSize: [size, size], iconAnchor: [size / 2, size / 2] });
}

function fit(map, pts) {
  if (!pts.length) return;
  try {
    if (pts.length === 1) map.setView(pts[0], 16);
    else map.fitBounds(L.latLngBounds(pts), { padding: [40, 40], maxZoom: 16 });
  } catch {
    map.setView(pts[0], 14); // contenedor sin tamaño todavía (p. ej. en tests)
  }
}

/**
 * Mapa real (Leaflet + OpenStreetMap) dentro de la app.
 * pins: [{ id, text, lat, lng, status: 'next' | 'vis' | 'pend', label }]
 * route: dibuja la línea punteada uniendo los pines en orden.
 * line: [[lat, lng], ...] trazado por calles (ej. resultado de una ruta); se dibuja sólido y entra en el encuadre.
 * tall: mapa de pantalla completa (permite arrastrar y hacer zoom con la rueda).
 * interactive: fuerza arrastre y zoom aunque el mapa sea chico (ej. dentro de una hoja inferior).
 */
export function MapView({ pins, route = true, line = null, selectedId = null, onPinClick, tall = false, interactive: forceInteractive = false }) {
  const elRef = useRef(null);
  const mapRef = useRef(null);
  const layerRef = useRef(null);
  const clickRef = useRef(onPinClick);
  clickRef.current = onPinClick;

  // Cambia solo si cambian las posiciones: ahí se reencuadra. Los cambios de estado/selección no mueven el mapa.
  const lineKey = line?.length ? `${line.length}:${line[0]}:${line[line.length - 1]}` : '';
  const geoKey = pins.map((p) => `${p.id}:${p.lat}:${p.lng}`).join('|') + `|${lineKey}`;
  const viewKey = pins.map((p) => `${p.id}:${p.text}:${p.status}`).join('|') + `|${selectedId}|${route}|${lineKey}`;
  const interactive = tall || forceInteractive;

  useEffect(() => {
    const map = L.map(elRef.current, {
      attributionControl: false,
      scrollWheelZoom: interactive,
      zoomSnap: 0.5,
      dragging: interactive || !L.Browser.mobile, // en celular, el mapa chico no captura el scroll de la página
    });
    L.tileLayer(TILE_URL, { maxZoom: 19, attribution: ATTRIBUTION }).addTo(map);
    L.control.attribution({ prefix: false, position: 'bottomright' }).addTo(map);
    layerRef.current = L.layerGroup().addTo(map);
    mapRef.current = map;

    let ro;
    if (typeof ResizeObserver !== 'undefined') {
      ro = new ResizeObserver(() => map.invalidateSize());
      ro.observe(elRef.current);
    }
    return () => {
      ro?.disconnect();
      map.remove();
      mapRef.current = null;
    };
  }, [interactive]);

  useEffect(() => {
    const map = mapRef.current;
    if (map) fit(map, [...pins.map((p) => [p.lat, p.lng]), ...(line || [])]);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [geoKey, interactive]);

  useEffect(() => {
    const group = layerRef.current;
    if (!group) return;
    group.clearLayers();
    if (line?.length > 1) L.polyline(line, { color: '#C2185B', weight: 5, opacity: 0.9, lineJoin: 'round' }).addTo(group);
    if (route && pins.length > 1) {
      L.polyline(
        pins.map((p) => [p.lat, p.lng]),
        { color: '#C2185B', weight: 4, dashArray: '10 7', lineCap: 'round', opacity: 0.9 },
      ).addTo(group);
    }
    pins.forEach((p) => {
      const big = p.status === 'next' || p.id === selectedId;
      L.marker([p.lat, p.lng], {
        icon: pinIcon(p, big),
        title: p.label || `Punto ${p.text}`,
        alt: p.label || `Punto ${p.text}`,
        zIndexOffset: big ? 1000 : 0,
        keyboard: true,
      })
        .on('click', () => clickRef.current?.(p.id))
        .addTo(group);
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [viewKey, interactive]);

  return (
    <div className={`map ${tall ? 'tall' : ''}`}>
      <div ref={elRef} className="map-canvas" role="region" aria-label="Mapa" />
    </div>
  );
}