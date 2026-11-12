import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';

const api = vi.hoisted(() => ({
  orderPayment: vi.fn(), startOrderPayment: vi.fn(), verifyOrderPayment: vi.fn(), simulateOrderPayment: vi.fn(),
}));
vi.mock('../cliente/src/api', () => ({ api }));

import { PaymentPanel } from '../cliente/src/pages/OrdersPage.jsx';

const order = { id: 'p1', status: 'PENDIENTE_PAGO', total: 5000 };
const wallet = (extra = {}) => ({
  id: 'pg1', tipoPago: 'BILLETERA_VIRTUAL', estado: 'PENDIENTE', monto: 5000,
  instrucciones: 'Escaneá el código QR con Mercado Pago', imagenQr: 'data:image/png;base64,AAAA', pasarelaSimulada: true, ...extra,
});

beforeEach(() => { api.orderPayment.mockResolvedValue(undefined); });
afterEach(() => { cleanup(); vi.clearAllMocks(); });

describe('Pago con billetera virtual (QR)', () => {
  it('"Pagar con Mercado Pago" genera el pago y muestra el QR', async () => {
    api.startOrderPayment.mockResolvedValue(wallet());
    render(<PaymentPanel order={order} onPaid={vi.fn()} />);

    fireEvent.click(await screen.findByRole('button', { name: /Pagar con Mercado Pago/ }));

    await waitFor(() => expect(api.startOrderPayment).toHaveBeenCalledWith('p1'));
    const qr = await screen.findByAltText(/Código QR/);
    expect(qr.getAttribute('src')).toBe('data:image/png;base64,AAAA');
  });

  it('si ya hay un QR pendiente lo muestra sin pedir otro', async () => {
    api.orderPayment.mockResolvedValue(wallet());
    render(<PaymentPanel order={order} onPaid={vi.fn()} />);
    expect(await screen.findByAltText(/Código QR/)).toBeTruthy();
    expect(screen.queryByRole('button', { name: /Pagar con Mercado Pago/ })).toBeNull();
  });

  it('"Ya pagué, verificar" avisa que se pagó cuando la pasarela lo acredita', async () => {
    api.orderPayment.mockResolvedValue(wallet());
    api.verifyOrderPayment.mockResolvedValue(wallet({ estado: 'CONFIRMADO', imagenQr: null }));
    const onPaid = vi.fn();
    render(<PaymentPanel order={order} onPaid={onPaid} />);

    fireEvent.click(await screen.findByRole('button', { name: /Ya pagué/ }));

    await waitFor(() => expect(onPaid).toHaveBeenCalled());
  });

  it('si todavía no se acreditó, lo dice y no recarga el pedido', async () => {
    api.orderPayment.mockResolvedValue(wallet());
    api.verifyOrderPayment.mockResolvedValue(wallet());
    const onPaid = vi.fn();
    render(<PaymentPanel order={order} onPaid={onPaid} />);

    fireEvent.click(await screen.findByRole('button', { name: /Ya pagué/ }));

    expect((await screen.findByRole('alert')).textContent).toMatch(/Todavía no vemos el pago/);
    expect(onPaid).not.toHaveBeenCalled();
  });

  it('en modo demo ofrece simular el pago; con pasarela real no', async () => {
    api.orderPayment.mockResolvedValue(wallet());
    api.simulateOrderPayment.mockResolvedValue(wallet({ estado: 'CONFIRMADO', imagenQr: null }));
    const onPaid = vi.fn();
    render(<PaymentPanel order={order} onPaid={onPaid} />);
    fireEvent.click(await screen.findByRole('button', { name: /Simular pago/ }));
    await waitFor(() => expect(onPaid).toHaveBeenCalled());

    cleanup();
    api.orderPayment.mockResolvedValue(wallet({ pasarelaSimulada: false }));
    render(<PaymentPanel order={order} onPaid={vi.fn()} />);
    await screen.findByAltText(/Código QR/);
    expect(screen.queryByRole('button', { name: /Simular pago/ })).toBeNull();
  });

  it('si administración ya acordó efectivo o transferencia, no ofrece pagar con QR', async () => {
    api.orderPayment.mockResolvedValue({ id: 'pg2', tipoPago: 'TRANSFERENCIA', estado: 'PENDIENTE', instrucciones: 'Transferí al alias dulce.mayor' });
    render(<PaymentPanel order={order} onPaid={vi.fn()} />);
    expect((await screen.findByRole('status')).textContent).toMatch(/alias dulce.mayor/);
    expect(screen.queryByRole('button', { name: /Pagar con Mercado Pago/ })).toBeNull();
  });
});
