import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import PreventistaApp from '../preventista/src/App.jsx';
import DeliveryApp from '../transportista/src/App.jsx';
import { resetMock } from '../shared/src/mock/router.js';

// jsdom no puede dibujar Leaflet (sin SVG/canvas): en los tests el mapa es un doble que expone la cantidad de pines.
// El mapa real se verifica en un navegador de verdad.
vi.mock('../shared/src/components/MapView.jsx', async () => {
  const React = await import('react');
  return { MapView: ({ pins, line }) => React.createElement('div', { 'data-testid': 'map', 'data-pins': pins.length, 'data-line': line ? line.length : 0 }) };
});

const mount = (App, path = '/login') =>
  render(
    <MemoryRouter initialEntries={[path]}>
      <App />
    </MemoryRouter>,
  );

const type = (label, value) => fireEvent.change(screen.getByLabelText(label), { target: { value } });
const text = (re) => screen.findByText(re, {}, { timeout: 3000 });

async function loginAndChangePassword() {
  fireEvent.click(await screen.findByRole('button', { name: /^Ingresar$/ }));
  await text('Cambiá tu clave');
  type('Clave nueva', 'Dulce#Mza24');
  type('Confirmá la clave nueva', 'Dulce#Mza24');
  fireEvent.click(screen.getByRole('button', { name: /Guardar y continuar/ }));
}

beforeEach(() => {
  sessionStorage.clear();
  resetMock();
});
afterEach(cleanup);

describe('Preventista', () => {
  it('fuerza el cambio de clave inicial y luego muestra la ruta', async () => {
    mount(PreventistaApp);
    await loginAndChangePassword();
    await text('Mi ruta de hoy');
    expect(await text('Siguiente punto sugerido')).toBeTruthy();
    expect(screen.getAllByText('Drugstore 24 hs').length).toBeGreaterThan(0);
    expect(screen.getByTestId('map').getAttribute('data-pins')).toBe('6'); // los 6 puntos de la ruta
  });

  it('bloquea el acceso a rutas internas si no cambió la clave', async () => {
    mount(PreventistaApp, '/ruta');
    expect(await screen.findByRole('button', { name: /^Ingresar$/ })).toBeTruthy();
  });

  it('toma un pedido con código DULCE10 y propone el siguiente punto', async () => {
    mount(PreventistaApp);
    await loginAndChangePassword();
    await text('Mi ruta de hoy');
    fireEvent.click(screen.getByRole('button', { name: 'Catálogo' }));
    await text(/productos con stock/);
    expect(screen.queryByText('Bombones surtidos')).toBeNull(); // sin stock => oculto
    fireEvent.click(await screen.findByRole('button', { name: 'Agregar Alfajor triple chocolate' }));
    fireEvent.click(screen.getByRole('button', { name: /^\d*\s*Pedido$/ }));
    await text('Nuevo pedido');
    fireEvent.click(screen.getByRole('button', { name: /Elegí el punto de venta/ }));
    fireEvent.click(await screen.findByRole('radio', { name: /Drugstore 24 hs/ }));
    fireEvent.change(screen.getByLabelText('Código de promoción'), { target: { value: 'dulce10' } });
    fireEvent.click(screen.getByRole('button', { name: 'Aplicar' }));
    await text('Código aplicado: 10% de descuento');
    fireEvent.click(screen.getByRole('button', { name: /Confirmar pedido/ }));
    fireEvent.click(await screen.findByRole('button', { name: /Sí, confirmar/ }));
    await text(/Pedido #1043 cargado/);
    expect(await text('Siguiente punto sugerido')).toBeTruthy();
    expect(screen.getAllByText('Autoservicio La Esquina').length).toBeGreaterThan(0); // el más cercano al comercio recién visitado
    expect(await text('Recorrido más eficiente')).toBeTruthy();
    expect(Number(screen.getByTestId('map').getAttribute('data-line'))).toBeGreaterThan(1); // recorrido restante dibujado
  });

  it('muestra cómo llegar dentro de la app, con ruta, tiempo y distancia', async () => {
    mount(PreventistaApp);
    await loginAndChangePassword();
    await text('Mi ruta de hoy');
    fireEvent.click((await screen.findAllByRole('button', { name: /Cómo llegar/ }))[0]);
    await text('Calculando la ruta…');
    expect(await text(/^\d+ min$/)).toBeTruthy();
    expect(screen.getByRole('button', { name: /Abrir en Google Maps/ })).toBeTruthy();
    const maps = screen.getAllByTestId('map');
    expect(maps[maps.length - 1].getAttribute('data-line')).toBe('2'); // ruta dibujada (en demo: línea recta)
    expect(maps[maps.length - 1].getAttribute('data-pins')).toBe('2'); // tu ubicación + destino
    fireEvent.click(screen.getByRole('button', { name: 'Cerrar' }));
    await waitFor(() => expect(screen.queryByRole('button', { name: /Abrir en Google Maps/ })).toBeNull());
  });

  it('optimiza el orden de la ruta, lo guarda y dibuja el recorrido', async () => {
    mount(PreventistaApp);
    await loginAndChangePassword();
    await text('Mi ruta de hoy');
    const before = (await screen.findAllByRole('article')).map((a) => a.textContent);
    fireEvent.click(await screen.findByRole('button', { name: /Optimizar mi ruta/ }));
    expect(await text('Ruta optimizada')).toBeTruthy();
    await text('Siguiente punto sugerido');
    const map = screen.getByTestId('map');
    expect(map.getAttribute('data-pins')).toBe('7'); // 6 paradas + tu ubicación
    expect(Number(map.getAttribute('data-line'))).toBe(5); // origen + 4 paradas pendientes, en orden
    expect(screen.getByRole('button', { name: /Volver a optimizar/ })).toBeTruthy();
    expect(screen.getAllByRole('article').map((a) => a.textContent)).not.toEqual(before); // el orden cambió
  });

  it('muestra al transportista en la trazabilidad de un pedido en reparto', async () => {
    mount(PreventistaApp);
    await loginAndChangePassword();
    await text('Mi ruta de hoy');
    fireEvent.click(screen.getByRole('button', { name: 'Mis pedidos' }));
    fireEvent.click(await screen.findByText('Pedido #1042'));
    expect(await text(/Marcos Giménez/)).toBeTruthy();
  });
});

describe('Transportista', () => {
  it('cambia la clave, entrega y avisa al próximo comercio; no ofrece llamar al cliente', async () => {
    mount(DeliveryApp);
    await loginAndChangePassword();
    await text('Reparto de hoy');
    expect(screen.queryByText('Llamar')).toBeNull();
    fireEvent.click(await screen.findByRole('button', { name: /Ir a la entrega/ }));
    await text('Siguiente entrega');
    await text('Resumen del pedido');
    expect(screen.queryByText(/261 555/)).toBeNull(); // no se muestran teléfonos
    fireEvent.click(await screen.findByRole('button', { name: /Marcar como entregado/ }));
    fireEvent.click(await screen.findByRole('button', { name: /Sí, entregado/ }));
    await text('¡Entrega registrada!');
    expect(await text('Se avisó por correo al próximo comercio.')).toBeTruthy();
  });

  it('optimiza el orden del reparto y lo guarda', async () => {
    mount(DeliveryApp);
    await loginAndChangePassword();
    await text('Reparto de hoy');
    fireEvent.click(await screen.findByRole('button', { name: /Optimizar mi reparto/ }));
    expect(await text('Reparto optimizado')).toBeTruthy();
    expect(screen.getByRole('button', { name: /Volver a optimizar/ })).toBeTruthy();
    expect(Number(screen.getByTestId('map').getAttribute('data-line'))).toBe(5);
  });

  it('muestra el alias cuando el pago es por transferencia', async () => {
    mount(DeliveryApp);
    await loginAndChangePassword();
    await text('Reparto de hoy');
    // 3 (efectivo) -> entregar para llegar al 4 (transferencia)
    fireEvent.click(await screen.findByRole('button', { name: /Ir a la entrega/ }));
    await text('Resumen del pedido');
    fireEvent.click(await screen.findByRole('button', { name: /Marcar como entregado/ }));
    fireEvent.click(await screen.findByRole('button', { name: /Sí, entregado/ }));
    fireEvent.click(await screen.findByRole('button', { name: /Ir a la siguiente entrega/ }));
    expect(await text(/Alias: dulcemayor\.mza/)).toBeTruthy();
  });

  it('rechaza a un usuario con rol de preventista', async () => {
    mount(DeliveryApp);
    type('Usuario', 'preventista.mza');
    fireEvent.click(await screen.findByRole('button', { name: /^Ingresar$/ }));
    expect(await text('Tu usuario no tiene acceso a esta aplicación.')).toBeTruthy();
  });
});