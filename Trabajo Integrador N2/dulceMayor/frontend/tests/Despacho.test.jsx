import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';

const api = vi.hoisted(() => ({ dispatchCarriers: vi.fn(), createDispatch: vi.fn() }));
vi.mock('../admin/src/api', () => ({ apiAdmin: api }));

import { DispatchDialog, PendingDispatchCard, repeatedClients } from '../admin/src/pages/DispatchPanel.jsx';

const order = (id, clientName, total = 1000) => ({ id, clientName, total, items: [{}], createdAt: '2026-11-11T10:00:00', invoicedAt: '2026-11-11T10:05:00', status: 'FACTURADO' });

beforeEach(() => {
  api.dispatchCarriers.mockResolvedValue([
    { id: 't1', nombre: 'Juan Gómez', patenteVehiculo: 'AB123CD' },
    { id: 't2', nombre: 'Luis Pérez', patenteVehiculo: null },
  ]);
  api.createDispatch.mockResolvedValue({ transportistaId: 't1', transportista: 'Juan Gómez', pedidoIds: ['p1'], fechaDespacho: '2026-11-11T21:00:00' });
});
afterEach(() => { cleanup(); vi.clearAllMocks(); });

describe('Despacho de pedidos', () => {
  it('repeatedClients detecta comercios con más de un pedido', () => {
    expect(repeatedClients([order('1', 'Kiosco Sol'), order('2', 'Luna'), order('3', 'Kiosco Sol')])).toEqual(['Kiosco Sol']);
    expect(repeatedClients([order('1', 'Kiosco Sol'), order('2', 'Luna')])).toEqual([]);
  });

  it('el diálogo lista los transportistas, exige elegir uno y manda ids al backend', async () => {
    const onDone = vi.fn();
    render(<DispatchDialog orders={[order('p1', 'Kiosco Sol')]} onClose={() => {}} onDone={onDone} />);
    await screen.findByRole('option', { name: /Juan Gómez · AB123CD/ });
    const boton = screen.getByRole('button', { name: /^Despachar$/ });
    expect(boton.disabled).toBe(true);

    fireEvent.change(screen.getByRole('combobox'), { target: { value: 't1' } });
    fireEvent.click(boton);

    await waitFor(() => expect(onDone).toHaveBeenCalled());
    expect(api.createDispatch).toHaveBeenCalledWith('t1', ['p1']);
  });

  it('con dos pedidos del mismo comercio avisa y no deja despachar', async () => {
    render(<DispatchDialog orders={[order('p1', 'Kiosco Sol'), order('p2', 'Kiosco Sol')]} onClose={() => {}} onDone={() => {}} />);
    await screen.findByRole('option', { name: /Juan Gómez/ });
    fireEvent.change(screen.getByRole('combobox'), { target: { value: 't1' } });

    expect(screen.getByRole('alert').textContent).toMatch(/despachos separados/);
    expect(screen.getByRole('button', { name: /^Despachar$/ }).disabled).toBe(true);
  });

  it('muestra el error del backend dentro del diálogo y no lo cierra', async () => {
    api.createDispatch.mockRejectedValue(new Error('El pedido ya no está facturado.'));
    const onDone = vi.fn();
    render(<DispatchDialog orders={[order('p1', 'Kiosco Sol')]} onClose={() => {}} onDone={onDone} />);
    await screen.findByRole('option', { name: /Juan Gómez/ });
    fireEvent.change(screen.getByRole('combobox'), { target: { value: 't1' } });
    fireEvent.click(screen.getByRole('button', { name: /^Despachar$/ }));

    expect((await screen.findByRole('alert')).textContent).toMatch(/ya no está facturado/);
    expect(onDone).not.toHaveBeenCalled();
  });

  it('el panel de pendientes asigna los seleccionados (y "seleccionar todos")', () => {
    const onAssign = vi.fn();
    render(<PendingDispatchCard orders={[order('p1', 'Kiosco Sol'), order('p2', 'Luna')]} onAssign={onAssign} />);
    const asignar = screen.getByRole('button', { name: /Asignar seleccionados/ });
    expect(asignar.disabled).toBe(true);

    fireEvent.click(screen.getByLabelText('Seleccionar todos'));
    fireEvent.click(screen.getByRole('button', { name: /Asignar seleccionados \(2\)/ }));
    expect(onAssign.mock.calls[0][0].map((o) => o.id)).toEqual(['p1', 'p2']);
  });

  it('un pedido en efectivo (sin factura todavía) se muestra como "se factura al despachar"', () => {
    const cash = { ...order('p9', 'Luna'), status: 'PENDIENTE_PAGO', paymentMethod: 'EFECTIVO', invoicedAt: null };
    render(<PendingDispatchCard orders={[order('p1', 'Kiosco Sol'), cash]} onAssign={() => {}} />);
    expect(screen.getByText('Facturado')).toBeTruthy();
    expect(screen.getByText(/se factura al despachar/)).toBeTruthy();
  });

  it('sin pedidos pendientes muestra el vacío', () => {
    render(<PendingDispatchCard orders={[]} onAssign={() => {}} />);
    expect(screen.getByText(/No hay pedidos pendientes de despacho/)).toBeTruthy();
  });
});