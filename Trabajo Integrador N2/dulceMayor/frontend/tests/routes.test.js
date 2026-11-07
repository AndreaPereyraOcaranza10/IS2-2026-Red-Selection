import { beforeEach, describe, expect, it } from 'vitest';
import { createRouteApi, planFromDTO, routeFromDTO, setActor, setAuthToken } from '../shared/src/index.js';
import { mockRequest, resetMock } from '../shared/src/mock/router.js';

const dto = (tipoRuta, estado) => ({
  id: 'r1',
  fecha: '2026-11-06',
  tipoRuta,
  estado: 'EN_CURSO',
  kmRestantes: 4.2,
  paradas: [
    {
      id: 'p1',
      orden: 1,
      clienteId: 'c1',
      comercio: 'Kiosco El Sol',
      responsable: 'Lucía Fernández',
      telefono: null,
      direccion: 'Av. San Martín 1250, Ciudad',
      latitud: -32.89,
      longitud: -68.84,
      estado,
      pedidoId: null,
      fechaHora: '2026-11-06T09:42:00',
      kmDesdeAnterior: 0.5,
      totalPedido: 1000,
    },
  ],
});

describe('Adapter de rutas (DTO del backend -> modelo de las pantallas)', () => {
  it('traduce una ruta de preventa', () => {
    const r = routeFromDTO(dto('PREVENTA', 'COMPLETADA'));
    expect(r).toMatchObject({ id: 'r1', kmRemaining: 4.2 });
    expect(r.stops[0]).toMatchObject({
      id: 'p1',
      sequence: 1,
      name: 'Kiosco El Sol',
      status: 'VISITADO',
      phone: '',
      distanceKm: 0.5,
      lat: -32.89,
    });
    expect(r.stops[0].visitedAt).toBe('2026-11-06T09:42:00');
  });

  it('traduce una ruta de reparto: COMPLETADA es ENTREGADO', () => {
    const r = routeFromDTO(dto('REPARTO', 'COMPLETADA'));
    expect(r.stops[0]).toMatchObject({ status: 'ENTREGADO', total: 1000 });
    expect(r.stops[0].deliveredAt).toBe('2026-11-06T09:42:00');
  });

  it('traduce el plan optimizado: la fuente OSRM o ESTIMADA', () => {
    const base = {
      ruta: dto('PREVENTA', 'PENDIENTE'),
      distanciaKm: 7.5,
      duracionMin: 18,
      geometria: [
        [-32.89, -68.84],
        [-32.9, -68.85],
      ],
    };
    const origin = { lat: -32.88, lng: -68.83 };
    expect(planFromDTO({ ...base, fuente: 'OSRM' }, origin)).toMatchObject({ source: 'osrm', distanceKm: 7.5, durationMin: 18, origin });
    expect(planFromDTO({ ...base, fuente: 'ESTIMADA' }, origin).source).toBe('estimada');
    expect(planFromDTO({ ...base, fuente: 'OSRM' }, origin).geometry).toHaveLength(2);
  });
});

describe('Contrato de rutas contra el backend simulado', () => {
  beforeEach(async () => {
    resetMock();
    const login = await mockRequest('POST', '/api/auth/login', { username: 'despacho.mza', password: 'dulcemayor' });
    const session = await mockRequest('POST', '/api/auth/first-login/change-password', { newPassword: 'Dulce#Mza24' }, login.token);
    setAuthToken(session.token);
    setActor(session.user);
  });

  it('el transportista optimiza y el backend devuelve la ruta reordenada con el recorrido', async () => {
    const routes = createRouteApi('transportista');
    const before = await routes.route();
    const plan = await routes.optimizeRoute({ lat: -32.8895, lng: -68.8458 });
    const pending = plan.route.stops.filter((s) => s.status === 'PENDIENTE');
    expect(plan.geometry).toHaveLength(pending.length + 1); // origen + paradas pendientes
    expect(plan.source).toBe('estimada');
    expect(plan.route.stops.map((s) => s.id)).not.toEqual(before.stops.map((s) => s.id));
    expect((await routes.route()).stops.map((s) => s.id)).toEqual(plan.route.stops.map((s) => s.id)); // quedó guardado
  });

  it('registrar una entrega devuelve la siguiente parada y avisa por correo', async () => {
    const routes = createRouteApi('transportista');
    const route = await routes.route();
    const next = route.stops.find((s) => s.status === 'PENDIENTE');
    const res = await routes.deliver(next.id);
    expect(res.delivered).toMatchObject({ id: next.id, status: 'ENTREGADO' });
    expect(res.emailSent).toBe(true);
    await expect(routes.deliver(next.id)).rejects.toThrow('ya fue registrada');
  });

  it('calcula el trayecto en el backend', async () => {
    const t = await mockRequest(
      'GET',
      '/api/ruteo/trayecto?desdeLat=-32.89&desdeLng=-68.84&hastaLat=-32.9&hastaLng=-68.85',
      undefined,
      'mock.despacho.mza',
    );
    expect(t.distanciaKm).toBeGreaterThan(0);
    expect(t.geometria[0]).toEqual([-32.89, -68.84]);
  });
});
