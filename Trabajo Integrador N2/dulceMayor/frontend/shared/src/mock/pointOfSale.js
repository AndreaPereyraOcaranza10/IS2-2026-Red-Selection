/**
 * API simulada de puntos de venta, responsables y ubicación, con los mismos DTOs en español y las mismas reglas que
 * el backend (`/api/puntos-venta`, `/api/responsables/buscar`, `/api/ubicacion/**`). Solo se usa en la demo
 * (VITE_USE_MOCK=true); con el backend real estas rutas van directo al servidor.
 */
import { ApiError } from '../api/ApiError.js';
import { NOT_HANDLED } from './routes.js';

const PROVINCES = [
  { id: '06', nombre: 'Buenos Aires' },
  { id: '14', nombre: 'Córdoba' },
  { id: '50', nombre: 'Mendoza' },
  { id: '70', nombre: 'San Juan' },
  { id: '74', nombre: 'San Luis' },
];
const MENDOZA = ['Capital', 'Godoy Cruz', 'Guaymallén', 'Las Heras', 'Maipú', 'Luján de Cuyo'];

const departmentsOf = (provinceId) =>
  (provinceId === '50' ? MENDOZA : ['Departamento Central']).map((nombre, i) => ({ id: `${provinceId}${String(i + 1).padStart(3, '0')}`, nombre }));
const localitiesOf = (departmentId) => {
  const d = departmentsOf(departmentId.slice(0, 2)).find((x) => x.id === departmentId);
  return d ? [{ id: `${d.id}010`, nombre: d.nombre }, { id: `${d.id}020`, nombre: `${d.nombre} Norte` }] : [];
};

const NAME = /^\p{L}+([ '.\-]\p{L}+)*$/u;
const STREET = /^[\p{L}\d][\p{L}\d .'°º\-]*$/u;
const strip = (s) => s.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();

let store = null;
/** Los responsables de ejemplo salen de los contactos de los puntos de venta de la demo. */
const seed = (POINTS) => ({
  responsibles: POINTS.map((p) => {
    const [nombre, ...resto] = p.contactName.split(' ');
    const apellido = resto.join(' ');
    return {
      id: `resp-${p.id}`,
      nombre,
      apellido,
      tipoDocumento: 'DNI',
      documento: String(30111000 + p.id),
      email: `${strip(nombre)}.${strip(apellido).replace(/ /g, '')}@correo.com`,
      telefono: p.phone.replace(/\D/g, ''),
    };
  }),
  cuits: new Set(),
});
/** Vuelve a los datos de ejemplo (lo llama resetMock, después de recortar POINTS). */
export const resetPointOfSaleMock = () => {
  store = null;
};

const responsibleDto = (r) => ({
  id: r.id,
  nombre: r.nombre,
  apellido: r.apellido,
  tipoDocumento: r.tipoDocumento,
  documentoParcial: '*'.repeat(r.documento.length - 3) + r.documento.slice(-3),
  email: r.email,
  telefono: r.telefono,
});

const idParam = (value, what) => {
  if (!/^\d{1,11}$/.test(value ?? '')) throw new ApiError(422, `El identificador de ${what} no es válido`);
  return value;
};

function search(text, s) {
  const term = strip((text ?? '').replace(/[^\p{L}\d ]/gu, '').trim());
  if (term.length < 2) throw new ApiError(422, 'Escribí al menos 2 letras o números para buscar.');
  return s.responsibles
    .filter((r) => strip(`${r.nombre} ${r.apellido}`).includes(term) || strip(`${r.apellido} ${r.nombre}`).includes(term) || r.documento.includes(term))
    .slice(0, 20)
    .map(responsibleDto);
}

/** Mismas validaciones que PuntoVentaCrearDTO / ResponsableCrearDTO (400) y que los services (422, 404). */
function validate(body) {
  const errors = [];
  const bad = (field, message) => errors.push({ field, message });
  const d = body.direccion ?? {};
  if (!body.nombreComercio?.trim()) bad('nombreComercio', 'Ingresá el nombre del comercio');
  if (!/^\d{2}-?\d{8}-?\d$/.test(body.cuit ?? '')) bad('cuit', 'El CUIT tiene que tener 11 dígitos');
  if (!d.calle?.trim()) bad('direccion.calle', 'Ingresá la calle');
  else if (!STREET.test(d.calle.trim())) bad('direccion.calle', 'La calle tiene caracteres no válidos');
  if (!/^\d{1,6}$/.test(d.numeracion ?? '')) bad('direccion.numeracion', 'La numeración solo lleva números');
  [['provinciaId', 'Provincia'], ['departamentoId', 'Departamento'], ['localidadId', 'Localidad']].forEach(([f, label]) => {
    if (!/^\d{1,11}$/.test(d[f] ?? '')) bad(`direccion.${f}`, `${label} no válida`);
  });
  const r = body.responsableNuevo;
  if (r) {
    if (!NAME.test(r.nombre?.trim() ?? '')) bad('responsableNuevo.nombre', 'El nombre solo puede tener letras');
    if (!NAME.test(r.apellido?.trim() ?? '')) bad('responsableNuevo.apellido', 'El apellido solo puede tener letras');
    if (!r.tipoDocumento) bad('responsableNuevo.tipoDocumento', 'Elegí el tipo de documento');
    if (!/^[A-Za-z0-9]{6,9}$/.test(r.numeroDocumento ?? '')) bad('responsableNuevo.numeroDocumento', 'El documento debe tener entre 6 y 9 letras o números');
    if (!/.+@.+\..+/.test(r.email ?? '')) bad('responsableNuevo.email', 'Revisá el correo: falta el @ o el dominio');
    if (!/^\d{8,15}$/.test(r.telefono ?? '')) bad('responsableNuevo.telefono', 'El teléfono solo lleva números (entre 8 y 15)');
  }
  if (errors.length) throw new ApiError(400, 'Datos no válidos', { errors });
}

function create(body, s, { POINTS, db }) {
  validate(body);
  const d = body.direccion;
  const nuevo = body.responsableNuevo;
  if (!!body.responsableId?.trim() === !!nuevo) {
    throw new ApiError(422, 'Elegí un responsable ya cargado o completá los datos de uno nuevo (una sola opción).');
  }
  const digits = body.cuit.replace(/\D/g, '');
  const cuit = `${digits.slice(0, 2)}-${digits.slice(2, 10)}-${digits.slice(10)}`;
  if (s.cuits.has(cuit)) throw new ApiError(422, 'Ya existe un comercio con ese CUIT.');

  const province = PROVINCES.find((x) => x.id === d.provinciaId);
  const department = departmentsOf(d.provinciaId).find((x) => x.id === d.departamentoId);
  const locality = department && localitiesOf(d.departamentoId).find((x) => x.id === d.localidadId);
  if (!province) throw new ApiError(422, 'La provincia no es válida para la selección');
  if (!department) throw new ApiError(422, 'El departamento no es válido para la selección');
  if (!locality) throw new ApiError(422, 'La localidad no es válida para la selección');

  let responsible;
  if (nuevo) {
    if (nuevo.tipoDocumento === 'DNI' && !/^\d{7,8}$/.test(nuevo.numeroDocumento)) throw new ApiError(422, 'El DNI debe tener 7 u 8 números.');
    const doc = nuevo.numeroDocumento.toUpperCase();
    if (s.responsibles.some((r) => r.tipoDocumento === nuevo.tipoDocumento && r.documento === doc)) {
      throw new ApiError(422, 'Ya existe un responsable con ese documento. Elegilo de la lista en lugar de cargarlo de nuevo.');
    }
    responsible = { id: `resp-n${s.responsibles.length + 1}`, nombre: nuevo.nombre.trim(), apellido: nuevo.apellido.trim(), tipoDocumento: nuevo.tipoDocumento, documento: doc, email: nuevo.email.trim().toLowerCase(), telefono: nuevo.telefono };
    s.responsibles.push(responsible);
  } else {
    responsible = s.responsibles.find((r) => r.id === body.responsableId);
    if (!responsible) throw new ApiError(404, `Responsable no encontrado: ${body.responsableId}`, { code: 'NOT_FOUND' });
  }

  const id = POINTS.length + 1;
  const lat = d.latitud ?? -32.89 - id * 0.002; // sin GPS, el backend geocodifica; acá, una posición de ejemplo
  const lng = d.longitud ?? -68.846 + id * 0.002;
  const street = `${d.calle.trim()} ${d.numeracion}`;
  POINTS.push({ id, name: body.nombreComercio.trim(), address: `${street}, ${locality.nombre}`, distanceKm: 1, lat, lng, contactName: `${responsible.nombre} ${responsible.apellido}`, phone: responsible.telefono, paymentMethod: 'EFECTIVO', items: [] });
  db.visitOrder.push(id); // como el backend: el comercio nuevo queda como parada pendiente de la ruta de hoy
  s.cuits.add(cuit);

  return {
    id: String(id),
    nombreComercio: body.nombreComercio.trim(),
    razonSocial: body.nombreComercio.trim(),
    cuit,
    direccion: {
      calle: d.calle.trim(),
      numeracion: d.numeracion,
      barrio: d.barrio?.trim() || null,
      referencia: d.referencia?.trim() || null,
      provinciaId: province.id,
      provinciaNombre: province.nombre,
      departamentoId: department.id,
      departamentoNombre: department.nombre,
      localidadId: locality.id,
      localidadNombre: locality.nombre,
      latitud: lat,
      longitud: lng,
      textoCompleto: `${street}, ${locality.nombre}, ${department.nombre}, ${province.nombre}`,
    },
    responsable: responsibleDto(responsible),
    preventistaId: '1',
  };
}

export function pointOfSaleRoutes(key, p, body, q, ctx) {
  const s = (store ??= seed(ctx.POINTS));
  switch (key) {
    case 'GET /api/ubicacion/provincias':
      return PROVINCES;
    case 'GET /api/ubicacion/departamentos':
      return departmentsOf(idParam(q.get('provinciaId'), 'provincia'));
    case 'GET /api/ubicacion/localidades':
      return localitiesOf(idParam(q.get('departamentoId'), 'departamento'));
    case 'GET /api/ubicacion/inversa':
      return { provincia: PROVINCES.find((x) => x.id === '50'), departamento: departmentsOf('50')[0] };
    case 'GET /api/responsables/buscar':
      return search(q.get('q'), s);
    case 'POST /api/puntos-venta':
      return create(body, s, ctx);
    default:
      return NOT_HANDLED;
  }
}