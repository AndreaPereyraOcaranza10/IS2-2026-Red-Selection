import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';

const admin = vi.hoisted(() => ({
  payments: vi.fn(), confirmPayment: vi.fn(), startOrderPayment: vi.fn(), confirmOrderPayment: vi.fn(),
}));
vi.mock('../admin/src/api', () => ({ apiAdmin: admin }));

import { BillingPaymentsAdminPage } from '../admin/src/pages/BillingPaymentsAdminPage.jsx';
import { OrdersAdminPage } from '../admin/src/pages/OrdersAdminPage.jsx';

const order = (id, status, extra = {}) => ({
  id, status, clientName: 'Kiosco Sol', total: 5000, createdAt: '2026-11-12T10:00:00', paymentMethod: 'A_COORDINAR',
  items: [{ productId: 'a', productName: 'Alfajor', quantity: 2 }], invoiceId: null, ...extra,
});
const inventory = [{ id: 'a', name: 'Alfajor', stock: 10 }];

beforeEach(() => {
  admin.payments.mockResolvedValue([]);
  admin.startOrderPayment.mockResolvedValue({ id: 'pg1', instrucciones: 'Transferí $5000 al alias dulce.mayor.mza' });
  admin.confirmOrderPayment.mockResolvedValue({ id: 'pg1', estado: 'CONFIRMADO' });
});
afterEach(() => { cleanup(); vi.clearAllMocks(); vi.restoreAllMocks(); });

describe('Facturación y pagos (BK-15)', () => {
  it('lista los pedidos que esperan pago y acuerda efectivo sin facturar', async () => {
    const onRefresh = vi.fn().mockResolvedValue();
    render(<BillingPaymentsAdminPage orders={[order('p1', 'PENDIENTE_PAGO')]} loading={false} onRefresh={onRefresh} />);

    expect(screen.getByText('Esperando pago', { selector: 'h2' })).toBeTruthy();
    fireEvent.click(screen.getByRole('button', { name: /Efectivo al entregar/ }));

    await waitFor(() => expect(admin.startOrderPayment).toHaveBeenCalledWith('p1', 'EFECTIVO'));
    expect((await screen.findByRole('status')).textContent).toMatch(/Ya aparece en Despacho/);
    expect(admin.confirmOrderPayment).not.toHaveBeenCalled();
  });

  it('"Indicar alias" muestra las instrucciones de la transferencia', async () => {
    render(<BillingPaymentsAdminPage orders={[order('p1', 'PENDIENTE_PAGO')]} loading={false} onRefresh={vi.fn().mockResolvedValue()} />);
    fireEvent.click(screen.getByRole('button', { name: /Indicar alias/ }));

    expect((await screen.findByRole('status')).textContent).toMatch(/dulce\.mayor\.mza/);
    expect(admin.startOrderPayment).toHaveBeenCalledWith('p1', 'TRANSFERENCIA');
  });

  it('"Transferencia acreditada" pide confirmación y registra el pago del PEDIDO (la factura sale sola)', async () => {
    const onRefresh = vi.fn().mockResolvedValue();
    render(<BillingPaymentsAdminPage orders={[order('p1', 'PENDIENTE_PAGO')]} loading={false} onRefresh={onRefresh} />);

    fireEvent.click(screen.getByRole('button', { name: /Transferencia acreditada/ }));
    fireEvent.click(await screen.findByRole('button', { name: /^Confirmar pago$/ }));

    await waitFor(() => expect(admin.confirmOrderPayment).toHaveBeenCalledWith('p1', 'TRANSFERENCIA'));
    expect(admin.confirmPayment).not.toHaveBeenCalled();   // no es el endpoint por factura
    await waitFor(() => expect(onRefresh).toHaveBeenCalled());
  });

  it('un pedido ya facturado sigue mostrando su factura y su estado de pago', async () => {
    admin.payments.mockResolvedValue([{ estado: 'CONFIRMADO', tipoPago: 'TRANSFERENCIA', numeroFactura: 42 }]);
    render(<BillingPaymentsAdminPage orders={[order('p2', 'FACTURADO', { invoiceId: 'f2' })]} loading={false} onRefresh={vi.fn()} />);

    expect(await screen.findByText('Pagada')).toBeTruthy();
    expect(admin.payments).toHaveBeenCalledWith('f2');
    expect(screen.getByText('No hay pedidos esperando pago.')).toBeTruthy();
  });
});

describe('Pedidos y preparación (BK-15)', () => {
  it('ya no ofrece "Procesar y facturar": un pedido sin stock se reintenta y uno que espera pago solo se anula', () => {
    const onStatusChange = vi.fn().mockResolvedValue();
    const waitingStock = order('p1', 'PENDIENTE_STOCK');
    render(<OrdersAdminPage orders={[waitingStock, order('p2', 'PENDIENTE_PAGO')]} inventory={inventory} loading={false}
      onRefresh={() => {}} onStatusChange={onStatusChange} />);

    expect(screen.queryByText(/Procesar y facturar/)).toBeNull();
    fireEvent.click(screen.getByRole('button', { name: /Reintentar reserva/ }));
    expect(onStatusChange).toHaveBeenCalledWith(waitingStock, 'PENDIENTE_PAGO');
    expect(screen.getByText(/Stock reservado · esperando el pago/)).toBeTruthy();
  });

  it('anular pide confirmación y manda ANULADO', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    const onStatusChange = vi.fn().mockResolvedValue();
    const waiting = order('p2', 'PENDIENTE_PAGO');
    render(<OrdersAdminPage orders={[waiting]} inventory={inventory} loading={false} onRefresh={() => {}} onStatusChange={onStatusChange} />);

    fireEvent.click(screen.getByRole('button', { name: /Anular pedido/ }));
    await waitFor(() => expect(onStatusChange).toHaveBeenCalledWith(waiting, 'ANULADO'));
  });

  it('si el usuario no confirma, no se anula', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(false);
    const onStatusChange = vi.fn();
    render(<OrdersAdminPage orders={[order('p2', 'PENDIENTE_PAGO')]} inventory={inventory} loading={false} onRefresh={() => {}} onStatusChange={onStatusChange} />);

    fireEvent.click(screen.getByRole('button', { name: /Anular pedido/ }));
    expect(onStatusChange).not.toHaveBeenCalled();
  });
});