import { ApiError } from './ApiError.js';
import { api } from './http.js';

/**
 * Adapter de puntos de venta, responsables y ubicación (`/api/puntos-venta`, `/api/responsables`, `/api/ubicacion`).
 * Traduce los DTOs en español del backend al modelo que usan las pantallas (inglés) y los errores de validación
 * del backend (`direccion.calle`) a los nombres de campo del formulario (`street`).
 * Las pantallas no conocen ni las URLs ni los nombres del backend.
 */

const unit = (u) => ({ id: u.id, name: u.nombre });

/** Provincia -> departamento -> localidad, siempre desde la API Georef a través del backend. */
export const locationApi = {
  provinces: async () => (await api.get('/api/ubicacion/provincias')).map(unit),
  departments: async (provinceId) => (await api.get(`/api/ubicacion/departamentos?provinciaId=${encodeURIComponent(provinceId)}`)).map(unit),
  localities: async (departmentId) => (await api.get(`/api/ubicacion/localidades?departamentoId=${encodeURIComponent(departmentId)}`)).map(unit),
  /** Provincia y departamento de unas coordenadas; `null` si no se pudieron determinar (el formulario sigue manual). */
  locate: async (lat, lng) => {
    const r = await api.get(`/api/ubicacion/inversa?lat=${lat}&lon=${lng}`);
    return r ? { province: unit(r.provincia), department: unit(r.departamento) } : null;
  },
};

const responsibleFromDTO = (r) => ({
  id: r.id,
  firstName: r.nombre,
  lastName: r.apellido,
  fullName: `${r.nombre} ${r.apellido}`,
  documentType: r.tipoDocumento,
  documentMasked: r.documentoParcial,
  email: r.email,
  phone: r.telefono,
});

const pointFromDTO = (p) => ({
  id: p.id,
  name: p.nombreComercio,
  cuit: p.cuit,
  address: p.direccion.textoCompleto,
  lat: p.direccion.latitud,
  lng: p.direccion.longitud,
  responsible: responsibleFromDTO(p.responsable),
});

/**
 * Formulario -> PuntoVentaCrearDTO. El responsable va de UNA de las dos formas:
 * `responsible.id` (uno ya cargado) o los datos de uno nuevo.
 */
export function pointOfSaleToDTO({ name, cuit, address, responsible }) {
  const text = (s) => s?.trim() || null;
  return {
    nombreComercio: name.trim(),
    cuit: cuit.replace(/\D/g, ''),
    direccion: {
      calle: address.street.trim(),
      numeracion: address.number.trim(),
      barrio: text(address.neighborhood),
      referencia: text(address.reference),
      provinciaId: address.provinceId,
      departamentoId: address.departmentId,
      localidadId: address.localityId,
      latitud: address.lat ?? null,
      longitud: address.lng ?? null,
    },
    ...(responsible.id
      ? { responsableId: responsible.id }
      : {
          responsableNuevo: {
            nombre: responsible.firstName.trim(),
            apellido: responsible.lastName.trim(),
            tipoDocumento: responsible.documentType,
            numeroDocumento: responsible.documentNumber.trim(),
            email: responsible.email.trim(),
            telefono: responsible.phone.replace(/\D/g, ''),
          },
        }),
  };
}

/** Nombre de campo del backend -> nombre de campo del formulario. */
const FIELD = {
  nombreComercio: 'name',
  cuit: 'cuit',
  'direccion.calle': 'street',
  'direccion.numeracion': 'number',
  'direccion.barrio': 'neighborhood',
  'direccion.referencia': 'reference',
  'direccion.provinciaId': 'provinceId',
  'direccion.departamentoId': 'departmentId',
  'direccion.localidadId': 'localityId',
  'direccion.latitud': 'location',
  'direccion.longitud': 'location',
  'responsableNuevo.nombre': 'firstName',
  'responsableNuevo.apellido': 'lastName',
  'responsableNuevo.tipoDocumento': 'documentType',
  'responsableNuevo.numeroDocumento': 'documentNumber',
  'responsableNuevo.email': 'email',
  'responsableNuevo.telefono': 'phone',
};

export const pointOfSaleApi = {
  /** Alta del punto de venta: se guarda en la base y queda asignado al preventista de la sesión. */
  create: async (form) => {
    try {
      return pointFromDTO(await api.post('/api/puntos-venta', pointOfSaleToDTO(form)));
    } catch (e) {
      if (e instanceof ApiError && e.errors.length) {
        const errors = e.errors.map((x) => ({ field: FIELD[x.field] ?? x.field, message: x.message }));
        throw new ApiError(e.status, e.message, { code: e.code, errors });
      }
      throw e;
    }
  },
  /** Responsables ya cargados, por nombre, apellido o documento (mínimo 2 caracteres). */
  searchResponsibles: async (text) => (await api.get(`/api/responsables/buscar?q=${encodeURIComponent(text)}`)).map(responsibleFromDTO),
};