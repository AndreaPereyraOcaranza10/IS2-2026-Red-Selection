import { beforeEach, describe, expect, it } from 'vitest';
import { ApiError, createRouteApi, locationApi, pointOfSaleApi, pointOfSaleToDTO, setActor, setAuthToken } from '../shared/src/index.js';
import { mockRequest, resetMock } from '../shared/src/mock/router.js';

const address = { street: ' Av. San Martín ', number: '1250', neighborhood: '', reference: ' frente a la plaza ', provinceId: '50', departmentId: '50001', localityId: '50001010', lat: undefined, lng: undefined };
const newResponsible = { firstName: ' Lucía ', lastName: 'Fernández', documentType: 'DNI', documentNumber: '30123456', email: ' Lucia@Correo.com ', phone: '261 555-0100' };
const form = (over = {}) => ({ name: ' Kiosco La Plaza ', cuit: '20-12345678-9', address, responsible: newResponsible, ...over });

describe('Adapter de puntos de venta (formulario -> DTO del backend)', () => {
  it('arma el DTO con el responsable nuevo: limpia espacios, el CUIT queda en dígitos y no manda responsableId', () => {
    const dto = pointOfSaleToDTO(form());
    expect(dto).toMatchObject({ nombreComercio: 'Kiosco La Plaza', cuit: '20123456789' });
    expect(dto.direccion).toEqual({
      calle: 'Av. San Martín', numeracion: '1250', barrio: null, referencia: 'frente a la plaza',
      provinciaId: '50', departamentoId: '50001', localidadId: '50001010', latitud: null, longitud: null,
    });
    expect(dto.responsableNuevo).toEqual({ nombre: 'Lucía', apellido: 'Fernández', tipoDocumento: 'DNI', numeroDocumento: '30123456', email: 'Lucia@Correo.com', telefono: '2615550100' });
    expect(dto).not.toHaveProperty('responsableId');
  });

  it('con un responsable existente manda solo su id', () => {
    const dto = pointOfSaleToDTO(form({ responsible: { id: 'resp-1' } }));
    expect(dto.responsableId).toBe('resp-1');
    expect(dto).not.toHaveProperty('responsableNuevo');
  });

  it('manda las coordenadas del GPS cuando las hay', () => {
    const dto = pointOfSaleToDTO(form({ address: { ...address, lat: -32.89, lng: -68.84 } }));
    expect(dto.direccion).toMatchObject({ latitud: -32.89, longitud: -68.84 });
  });
});

describe('Contrato de puntos de venta contra el backend simulado', () => {
  beforeEach(async () => {
    resetMock();
    const login = await mockRequest('POST', '/api/auth/login', { username: 'preventista.mza', password: 'dulcemayor' });
    const session = await mockRequest('POST', '/api/auth/first-login/change-password', { newPassword: 'Dulce#Mza24' }, login.token);
    setAuthToken(session.token);
    setActor(session.user);
  });

  it('los desplegables bajan en cascada: provincia -> departamento -> localidad', async () => {
    const provinces = await locationApi.provinces();
    expect(provinces).toContainEqual({ id: '50', name: 'Mendoza' });
    const departments = await locationApi.departments('50');
    expect(departments.map((d) => d.name)).toContain('Godoy Cruz');
    const localities = await locationApi.localities(departments[0].id);
    expect(localities[0].id.startsWith(departments[0].id)).toBe(true);
  });

  it('"Usar mi ubicación" devuelve provincia y departamento', async () => {
    expect(await locationApi.locate(-32.89, -68.84)).toMatchObject({ province: { id: '50' }, department: { name: 'Capital' } });
  });

  it('busca responsables ya cargados y exige 2 caracteres', async () => {
    const found = await pointOfSaleApi.searchResponsibles('lucia');
    expect(found).toHaveLength(1);
    expect(found[0]).toMatchObject({ fullName: 'Lucía Fernández', documentType: 'DNI' });
    expect(found[0].documentMasked).toMatch(/^\*+\d{3}$/); // el documento no viaja completo
    await expect(pointOfSaleApi.searchResponsibles('l')).rejects.toThrow('al menos 2');
  });

  it('da de alta un punto de venta con responsable nuevo y queda como parada pendiente de la ruta', async () => {
    const created = await pointOfSaleApi.create(form());
    expect(created).toMatchObject({ name: 'Kiosco La Plaza', cuit: '20-12345678-9', responsible: { fullName: 'Lucía Fernández' } });
    expect(created.address).toBe('Av. San Martín 1250, Capital, Capital, Mendoza');
    const route = await createRouteApi('preventista').route();
    expect(route.stops).toHaveLength(7);
    expect(route.stops.at(-1)).toMatchObject({ name: 'Kiosco La Plaza', status: 'PENDIENTE' });
  });

  it('da de alta un punto de venta con un responsable existente (sin duplicarlo)', async () => {
    const [existing] = await pointOfSaleApi.searchResponsibles('Raúl');
    const created = await pointOfSaleApi.create(form({ responsible: { id: existing.id } }));
    expect(created.responsible.id).toBe(existing.id);
    expect(await pointOfSaleApi.searchResponsibles('Raúl')).toHaveLength(1);
  });

  it('los errores de validación llegan con los nombres de campo del formulario', async () => {
    const bad = form({
      name: ' ',
      address: { ...address, street: '', number: '12a' },
      responsible: { ...newResponsible, firstName: 'Ana2', email: 'sin-arroba', phone: '123' },
    });
    const error = await pointOfSaleApi.create(bad).catch((e) => e);
    expect(error).toBeInstanceOf(ApiError);
    expect(Object.keys(error.fieldErrors()).sort()).toEqual(['email', 'firstName', 'name', 'number', 'phone', 'street']);
  });

  it('rechaza el CUIT repetido, el responsable repetido y las dos formas de responsable a la vez', async () => {
    await pointOfSaleApi.create(form());
    await expect(pointOfSaleApi.create(form())).rejects.toThrow('Ya existe un comercio con ese CUIT');
    await expect(pointOfSaleApi.create(form({ cuit: '27-11111111-4' }))).rejects.toThrow('Ya existe un responsable con ese documento');
    await expect(
      mockRequest('POST', '/api/puntos-venta', { ...pointOfSaleToDTO(form({ cuit: '27-22222222-4' })), responsableId: 'resp-1' }, 'mock.preventista.mza'),
    ).rejects.toThrow('una sola opción');
  });

  it('un DNI con letras se rechaza aunque el formato general del documento sea válido', async () => {
    const bad = form({ cuit: '27-33333333-4', responsible: { ...newResponsible, documentNumber: 'AB123456' } });
    await expect(pointOfSaleApi.create(bad)).rejects.toThrow('El DNI debe tener 7 u 8 números');
  });
});