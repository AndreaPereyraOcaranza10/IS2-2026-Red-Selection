import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ActionBar, ApiError, Field, Icon, Layout, MapView, USE_MOCK, maskCuit, useToast } from '@dulcemayor/shared';
import { preventistaApi } from '../api.js';

const EMPTY = { name: '', cuit: '', contactName: '', email: '', phone: '', address: '' };

/** Misma validación que el @Valid del backend (el servidor sigue siendo la fuente de verdad). */
function validate(v) {
  const e = {};
  if (!v.name.trim()) e.name = 'Ingresá el nombre del comercio';
  if (v.cuit.replace(/\D/g, '').length !== 11) e.cuit = 'El CUIT tiene que tener 11 dígitos';
  if (!v.contactName.trim()) e.contactName = 'Ingresá el responsable';
  if (!/^\S+@\S+\.\S+$/.test(v.email)) e.email = 'Revisá el correo: falta el @ o el dominio';
  if (!v.phone.trim()) e.phone = 'Ingresá un teléfono';
  if (!v.address.trim()) e.address = 'Ingresá la dirección';
  return e;
}

/** Alta de punto de venta con geolocalización (navigator.geolocation + geocodificación inversa en el backend). */
export default function PointOfSaleFormPage() {
  const navigate = useNavigate();
  const toast = useToast();
  const [v, setV] = useState(EMPTY);
  const [errors, setErrors] = useState({});
  const [coords, setCoords] = useState(null);
  const [busy, setBusy] = useState(false);

  const set = (k) => (e) => setV((s) => ({ ...s, [k]: k === 'cuit' ? maskCuit(e.target.value) : e.target.value }));

  const locate = async () => {
    try {
      const c = await new Promise((resolve, reject) => {
        if (!navigator.geolocation) return reject(new Error('sin geolocalización'));
        navigator.geolocation.getCurrentPosition((p) => resolve({ lat: p.coords.latitude, lng: p.coords.longitude }), reject, { timeout: 8000 });
      }).catch((err) => {
        if (USE_MOCK) return { lat: -32.8895, lng: -68.8458 }; // en demo seguimos con una ubicación de ejemplo
        throw err;
      });
      const geo = await preventistaApi.reverseGeocode(c.lat, c.lng);
      setCoords(c);
      setV((s) => ({ ...s, address: geo.address }));
      toast('Ubicación detectada. Revisá que la dirección esté bien.');
    } catch {
      toast('No pudimos obtener tu ubicación. Escribí la dirección a mano.', { error: true });
    }
  };

  const save = async () => {
    const local = validate(v);
    setErrors(local);
    if (Object.keys(local).length) return toast('Revisá los campos marcados', { error: true });
    setBusy(true);
    try {
      await preventistaApi.createPointOfSale({ ...v, cuit: v.cuit.replace(/\D/g, ''), lat: coords?.lat, lng: coords?.lng });
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
        <Field id="a1" label="Nombre del comercio" autoComplete="organization" placeholder="Ej.: Kiosco La Plaza" value={v.name} onChange={set('name')} error={errors.name} />
        <Field id="a2" label="CUIT" inputMode="numeric" maxLength={13} placeholder="20-12345678-9" value={v.cuit} onChange={set('cuit')} error={errors.cuit} help="11 dígitos. Teclado numérico" />
        <Field id="a3" label="Responsable (nombre y apellido)" autoComplete="name" placeholder="Ej.: Lucía Fernández" value={v.contactName} onChange={set('contactName')} error={errors.contactName} />
        <Field id="a4" label="Correo" type="email" inputMode="email" autoComplete="email" placeholder="comercio@correo.com" value={v.email} onChange={set('email')} error={errors.email} help="Ahí le avisamos cuando llega el reparto" />
        <Field id="a5" label="Teléfono" type="tel" inputMode="tel" autoComplete="tel" placeholder="261 555-0100" value={v.phone} onChange={set('phone')} error={errors.phone} help="Teclado numérico" />
        <Field id="a6" label="Dirección" autoComplete="street-address" placeholder="Calle, número, localidad" value={v.address} onChange={set('address')} error={errors.address} />
        <button className="btn btn-s" onClick={locate}>
          <Icon name="locate" size={20} />
          Usar mi ubicación
        </button>
        {coords && <MapView route={false} pins={[{ id: 'yo', text: '★', lat: coords.lat, lng: coords.lng, status: 'next', label: 'Tu ubicación' }]} />}
      </div>
    </Layout>
  );
}
