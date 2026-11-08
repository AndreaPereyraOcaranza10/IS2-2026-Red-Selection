import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ActionBar, ApiError, Field, Icon, Layout, MapView, SelectField, USE_MOCK, maskCuit, useOptions, useToast } from '@dulcemayor/shared';
import { preventistaApi } from '../api.js';
import ResponsiblePicker from '../components/ResponsiblePicker.jsx';
import { DEFAULT_PROVINCE_ID } from '../config.js';

const EMPTY = {
  name: '',
  cuit: '',
  provinceId: '',
  departmentId: '',
  localityId: '',
  street: '',
  number: '',
  neighborhood: '',
  reference: '',
  mode: 'EXISTING', // responsable: 'EXISTING' (elegir uno cargado) | 'NEW' (cargar sus datos)
  firstName: '',
  lastName: '',
  documentType: 'DNI',
  documentNumber: '',
  email: '',
  phone: '',
};

const DOCUMENT_TYPES = [
  { id: 'DNI', name: 'DNI' },
  { id: 'PASAPORTE', name: 'Pasaporte' },
];
const RESPONSIBLE_KEYS = ['responsible', 'firstName', 'lastName', 'documentType', 'documentNumber', 'email', 'phone'];

const NAME_RE = /^\p{L}+([ '.\-]\p{L}+)*$/u;
const STREET_RE = /^[\p{L}\d][\p{L}\d .'°º\-]*$/u;
const EMAIL_RE = /^\S+@\S+\.\S+$/;

/** Lo que se puede escribir en cada campo: los nombres no aceptan números; la numeración y el teléfono, solo dígitos. */
const digits = (s, max) => s.replace(/\D/g, '').slice(0, max);
const letters = (s) => s.replace(/[^\p{L} '.\-]/gu, '').slice(0, 60);
const SANITIZE = {
  cuit: maskCuit,
  number: (s) => digits(s, 6),
  phone: (s) => digits(s, 15),
  firstName: letters,
  lastName: letters,
};
const sanitizeDocument = (s, type) => (type === 'DNI' ? digits(s, 8) : s.replace(/[^A-Za-z0-9]/g, '').toUpperCase().slice(0, 9));

/** Misma validación que el @Valid del backend (el servidor sigue siendo la fuente de verdad). */
function validate(v, selected) {
  const e = {};
  if (!v.name.trim()) e.name = 'Ingresá el nombre del comercio';
  if (v.cuit.replace(/\D/g, '').length !== 11) e.cuit = 'El CUIT tiene que tener 11 dígitos';
  if (!v.provinceId) e.provinceId = 'Elegí la provincia';
  if (!v.departmentId) e.departmentId = 'Elegí el departamento';
  if (!v.localityId) e.localityId = 'Elegí la localidad';
  if (!v.street.trim()) e.street = 'Ingresá la calle';
  else if (!STREET_RE.test(v.street.trim())) e.street = 'La calle tiene caracteres no válidos';
  if (!v.number) e.number = 'Ingresá la numeración';
  if (v.mode === 'EXISTING') {
    if (!selected) e.responsible = 'Elegí un responsable de la lista o cargá uno nuevo';
  } else {
    if (!NAME_RE.test(v.firstName.trim())) e.firstName = 'Ingresá el nombre (solo letras)';
    if (!NAME_RE.test(v.lastName.trim())) e.lastName = 'Ingresá el apellido (solo letras)';
    if (v.documentType === 'DNI' ? !/^\d{7,8}$/.test(v.documentNumber) : !/^[A-Z0-9]{6,9}$/.test(v.documentNumber)) {
      e.documentNumber = v.documentType === 'DNI' ? 'El DNI tiene 7 u 8 números' : 'El pasaporte tiene entre 6 y 9 letras o números';
    }
    if (!EMAIL_RE.test(v.email)) e.email = 'Revisá el correo: falta el @ o el dominio';
    if (!/^\d{8,15}$/.test(v.phone)) e.phone = 'Ingresá el teléfono con el código de área, solo números';
  }
  return e;
}

const H2 = { fontSize: 20, lineHeight: '28px' };

/**
 * Alta de punto de venta. Se guarda en la base (POST /api/puntos-venta) y queda asignado al preventista.
 * - Dirección: provincia, departamento y localidad en cascada desde la API Georef (vía backend) + calle y número.
 * - Ubicación: "Usar mi ubicación" guarda el punto exacto del GPS; sin eso, el backend ubica la calle con Georef.
 * - Responsable: uno ya cargado (búsqueda) o uno nuevo con sus datos.
 */
export default function PointOfSaleFormPage() {
  const navigate = useNavigate();
  const toast = useToast();
  const [v, setV] = useState(EMPTY);
  const [selected, setSelected] = useState(null);
  const [errors, setErrors] = useState({});
  const [coords, setCoords] = useState(null);
  const [locating, setLocating] = useState(false);
  const [busy, setBusy] = useState(false);

  const provinces = useOptions(preventistaApi.provinces);
  const departments = useOptions(preventistaApi.departments, v.provinceId, { enabled: !!v.provinceId });
  const localities = useOptions(preventistaApi.localities, v.departmentId, { enabled: !!v.departmentId });

  // Dulce Mayor opera en Mendoza: se preselecciona (se puede cambiar).
  useEffect(() => {
    if (provinces.options.some((p) => p.id === DEFAULT_PROVINCE_ID)) {
      setV((s) => (s.provinceId ? s : { ...s, provinceId: DEFAULT_PROVINCE_ID }));
    }
  }, [provinces.options]);

  const clearErrors = (...keys) => setErrors((e) => Object.fromEntries(Object.entries(e).filter(([k]) => !keys.includes(k))));

  const set = (k) => (e) => {
    const raw = e.target.value;
    const clean = k === 'documentNumber' ? sanitizeDocument(raw, v.documentType) : (SANITIZE[k] ?? ((x) => x))(raw);
    setV((s) => ({ ...s, [k]: clean }));
    clearErrors(k);
  };
  const setProvince = (e) => {
    setV((s) => ({ ...s, provinceId: e.target.value, departmentId: '', localityId: '' }));
    clearErrors('provinceId', 'departmentId', 'localityId');
  };
  const setDepartment = (e) => {
    setV((s) => ({ ...s, departmentId: e.target.value, localityId: '' }));
    clearErrors('departmentId', 'localityId');
  };
  const setDocumentType = (e) => {
    setV((s) => ({ ...s, documentType: e.target.value, documentNumber: '' }));
    clearErrors('documentType', 'documentNumber');
  };
  const setMode = (mode) => {
    setV((s) => ({ ...s, mode }));
    clearErrors(...RESPONSIBLE_KEYS);
  };

  const locate = async () => {
    setLocating(true);
    try {
      const c = await new Promise((resolve, reject) => {
        if (!navigator.geolocation) return reject(new Error('sin geolocalización'));
        navigator.geolocation.getCurrentPosition((p) => resolve({ lat: p.coords.latitude, lng: p.coords.longitude }), reject, { timeout: 8000 });
      }).catch((err) => {
        if (USE_MOCK) return { lat: -32.8895, lng: -68.8458 }; // en demo seguimos con una ubicación de ejemplo
        throw err;
      });
      setCoords(c);
      clearErrors('location');
      // Provincia y departamento del punto; si Georef no responde, el resto se completa a mano.
      const place = await preventistaApi.locate(c.lat, c.lng).catch(() => null);
      if (place) setV((s) => ({ ...s, provinceId: place.province.id, departmentId: place.department.id, localityId: '' }));
      toast(place ? 'Ubicación guardada. Completá la localidad, la calle y el número.' : 'Ubicación guardada. Completá la dirección.');
    } catch {
      toast('No pudimos obtener tu ubicación. Cargá la dirección a mano.', { error: true });
    } finally {
      setLocating(false);
    }
  };

  const save = async () => {
    const local = validate(v, selected);
    setErrors(local);
    if (Object.keys(local).length) return toast('Revisá los campos marcados', { error: true });
    setBusy(true);
    try {
      await preventistaApi.createPointOfSale({
        name: v.name,
        cuit: v.cuit,
        address: {
          street: v.street,
          number: v.number,
          neighborhood: v.neighborhood,
          reference: v.reference,
          provinceId: v.provinceId,
          departmentId: v.departmentId,
          localityId: v.localityId,
          lat: coords?.lat,
          lng: coords?.lng,
        },
        responsible:
          v.mode === 'EXISTING'
            ? { id: selected.id }
            : { firstName: v.firstName, lastName: v.lastName, documentType: v.documentType, documentNumber: v.documentNumber, email: v.email, phone: v.phone },
      });
      toast(`Punto de venta guardado: ${v.name.trim()}`);
      navigate('/ruta', { replace: true });
    } catch (e) {
      if (e instanceof ApiError && e.errors.length) setErrors(e.fieldErrors());
      toast(e.message, { error: true });
    } finally {
      setBusy(false);
    }
  };

  return (
    <Layout
      title="Alta de punto de venta"
      back
      backTo="/ruta"
      bar={
        <ActionBar maxWidth={640}>
          <button className="btn btn-p" onClick={save} disabled={busy}>
            {busy ? 'Guardando…' : 'Guardar punto de venta'}
          </button>
        </ActionBar>
      }
    >
      <div className="col" style={{ maxWidth: 640, width: '100%', margin: '0 auto' }}>
        <h2 style={H2}>Comercio</h2>
        <Field id="pv-name" label="Nombre del comercio" autoComplete="organization" placeholder="Ej.: Kiosco La Plaza" maxLength={100} value={v.name} onChange={set('name')} error={errors.name} />
        <Field id="pv-cuit" label="CUIT" inputMode="numeric" maxLength={13} placeholder="20-12345678-9" value={v.cuit} onChange={set('cuit')} error={errors.cuit} help="11 dígitos. Teclado numérico" />

        <h2 style={H2}>Dirección</h2>
        <SelectField id="pv-province" label="Provincia" options={provinces.options} loading={provinces.loading} value={v.provinceId} onChange={setProvince} error={errors.provinceId || provinces.error?.message} />
        <SelectField id="pv-department" label="Departamento" options={departments.options} loading={departments.loading} disabled={!v.provinceId} value={v.departmentId} onChange={setDepartment} error={errors.departmentId || departments.error?.message} />
        <SelectField id="pv-locality" label="Localidad" options={localities.options} loading={localities.loading} disabled={!v.departmentId} value={v.localityId} onChange={set('localityId')} error={errors.localityId || localities.error?.message} />
        <Field id="pv-street" label="Calle" autoComplete="address-line1" placeholder="Ej.: Av. San Martín" maxLength={100} value={v.street} onChange={set('street')} error={errors.street} />
        <Field id="pv-number" label="Número" inputMode="numeric" placeholder="1250" maxLength={6} value={v.number} onChange={set('number')} error={errors.number} help="Solo números. Teclado numérico" />
        <Field id="pv-neighborhood" label="Barrio (opcional)" maxLength={100} value={v.neighborhood} onChange={set('neighborhood')} error={errors.neighborhood} />
        <Field id="pv-reference" label="Referencia (opcional)" placeholder="Ej.: frente a la plaza" maxLength={200} value={v.reference} onChange={set('reference')} error={errors.reference} />

        <button type="button" className="btn btn-s" onClick={locate} disabled={locating}>
          <Icon name="locate" size={20} />
          {locating ? 'Buscando tu ubicación…' : coords ? 'Actualizar mi ubicación' : 'Usar mi ubicación'}
        </button>
        <p className="hint" style={{ color: errors.location ? 'var(--err)' : undefined }}>
          {errors.location ||
            (coords
              ? 'Guardamos el punto exacto donde estás. Si no estás en el comercio, tocá «Quitar ubicación».'
              : 'Si estás en el comercio, usá tu ubicación para marcarlo con precisión. Si no, lo ubicamos por la calle y el número.')}
        </p>
        {coords && (
          <>
            <MapView route={false} pins={[{ id: 'yo', text: '★', lat: coords.lat, lng: coords.lng, status: 'next', label: 'Tu ubicación' }]} />
            <button type="button" className="btn btn-g" onClick={() => setCoords(null)}>
              Quitar ubicación
            </button>
          </>
        )}

        <h2 style={H2}>Responsable del comercio</h2>
        <div className="row" role="group" aria-label="Cómo cargar al responsable" style={{ gap: 8, flexWrap: 'wrap' }}>
          <button type="button" className="pillbtn" aria-pressed={v.mode === 'EXISTING'} onClick={() => setMode('EXISTING')}>
            Elegir uno existente
          </button>
          <button type="button" className="pillbtn" aria-pressed={v.mode === 'NEW'} onClick={() => setMode('NEW')}>
            Cargar uno nuevo
          </button>
        </div>
        {v.mode === 'EXISTING' ? (
          <ResponsiblePicker
            selected={selected}
            error={errors.responsible}
            onSelect={(r) => {
              setSelected(r);
              clearErrors('responsible');
            }}
            onClear={() => setSelected(null)}
          />
        ) : (
          <>
            <Field id="pv-first" label="Nombre" autoComplete="given-name" placeholder="Ej.: Lucía" value={v.firstName} onChange={set('firstName')} error={errors.firstName} help="Solo letras" />
            <Field id="pv-last" label="Apellido" autoComplete="family-name" placeholder="Ej.: Fernández" value={v.lastName} onChange={set('lastName')} error={errors.lastName} help="Solo letras" />
            <SelectField id="pv-doctype" label="Tipo de documento" allowEmpty={false} options={DOCUMENT_TYPES} value={v.documentType} onChange={setDocumentType} error={errors.documentType} />
            <Field id="pv-doc" label="Número de documento" inputMode={v.documentType === 'DNI' ? 'numeric' : 'text'} placeholder={v.documentType === 'DNI' ? '30123456' : 'AAB123456'} value={v.documentNumber} onChange={set('documentNumber')} error={errors.documentNumber} help={v.documentType === 'DNI' ? 'Solo números, sin puntos' : 'Letras y números'} />
            <Field id="pv-email" label="Correo" type="email" inputMode="email" autoComplete="email" placeholder="comercio@correo.com" value={v.email} onChange={set('email')} error={errors.email} help="Ahí le avisamos cuando llega el reparto" />
            <Field id="pv-phone" label="Teléfono" type="tel" inputMode="tel" autoComplete="tel" placeholder="2615550100" value={v.phone} onChange={set('phone')} error={errors.phone} help="Con código de área, solo números" />
          </>
        )}
      </div>
    </Layout>
  );
}