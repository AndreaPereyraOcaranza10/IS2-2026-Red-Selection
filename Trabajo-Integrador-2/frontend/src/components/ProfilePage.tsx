import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react';
import { ArrowLeft, BadgeCheck, Check, LoaderCircle, Save, UserRound } from 'lucide-react';
import { api, type CustomerProfile, type LocationOption } from '../lib/api';

const emptyProfile: CustomerProfile = {
  nombre: '', apellido: '', sexo: null, fechaNacimiento: '', tipoDocumento: 'DNI',
  numeroDocumento: '', email: '', telefono: '', tipoTelefono: 'CELULAR',
  provincia: '', departamento: '', localidad: '', codigoPostal: '', calle: '',
  numeroCalle: '', manzanaPiso: '', casaDepartamento: '', referencia: '',
};

function normalizeProfile(data: Partial<CustomerProfile> | null | undefined): CustomerProfile {
  const values = { ...emptyProfile, ...data };
  return Object.fromEntries(Object.entries(values).map(([key, value]) => [key, key === 'sexo' ? value ?? null : value ?? ''])) as CustomerProfile;
}

type ProfilePageProps = { onBack: () => void };

export function ProfilePage({ onBack }: ProfilePageProps) {
  const [profile, setProfile] = useState<CustomerProfile>(emptyProfile);
  const [provinces, setProvinces] = useState<LocationOption[]>([]);
  const [departments, setDepartments] = useState<LocationOption[]>([]);
  const [localities, setLocalities] = useState<LocationOption[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [notice, setNotice] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  useEffect(() => {
    const preserveCurrent = (options: LocationOption[], name: string) => name && !options.some(option => option.name.toLocaleLowerCase() === name.toLocaleLowerCase())
      ? [{ id: '', name }, ...options]
      : options;

    api.profile()
      .then(async data => {
        const loaded = normalizeProfile(data);
        setProfile(loaded);
        const provinceOptions = await api.provinces();
        setProvinces(preserveCurrent(provinceOptions, loaded.provincia));
        const province = provinceOptions.find(option => option.name.toLocaleLowerCase() === loaded.provincia.toLocaleLowerCase());
        if (!province) return;
        const departmentOptions = await api.departments(province.id);
        setDepartments(preserveCurrent(departmentOptions, loaded.departamento));
        const department = departmentOptions.find(option => option.name.toLocaleLowerCase() === loaded.departamento.toLocaleLowerCase());
        if (!department) return;
        const localityOptions = await api.localities(department.id);
        setLocalities(preserveCurrent(localityOptions, loaded.localidad));
      })
      .catch(error => setNotice({ type: 'error', text: error instanceof Error ? error.message : 'No pudimos cargar tu perfil y sus opciones de domicilio.' }))
      .finally(() => setLoading(false));
  }, []);

  const update = (event: ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = event.target;
    setProfile(previous => ({ ...previous, [name]: value }));
  };

  const changeProvince = async (event: ChangeEvent<HTMLSelectElement>) => {
    const name = event.target.value;
    const selected = provinces.find(option => option.name === name);
    setProfile(previous => ({ ...previous, provincia: name, departamento: '', localidad: '', codigoPostal: '' }));
    setDepartments([]);
    setLocalities([]);
    if (!selected?.id) return;
    try { setDepartments(await api.departments(selected.id)); }
    catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : 'No pudimos cargar los departamentos.' }); }
  };

  const changeDepartment = async (event: ChangeEvent<HTMLSelectElement>) => {
    const name = event.target.value;
    const selected = departments.find(option => option.name === name);
    setProfile(previous => ({ ...previous, departamento: name, localidad: '', codigoPostal: '' }));
    setLocalities([]);
    if (!selected?.id) return;
    try { setLocalities(await api.localities(selected.id)); }
    catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : 'No pudimos cargar las localidades.' }); }
  };

  const changeLocality = (event: ChangeEvent<HTMLSelectElement>) => {
    setProfile(previous => ({ ...previous, localidad: event.target.value, codigoPostal: '' }));
  };

  const save = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSaving(true);
    setNotice(null);
    try {
      const saved = await api.updateProfile(profile);
      setProfile(normalizeProfile(saved));
      setNotice({ type: 'success', text: 'Tus datos se guardaron correctamente.' });
    } catch (error) {
      setNotice({ type: 'error', text: error instanceof Error ? error.message : 'No pudimos guardar los cambios.' });
    } finally {
      setSaving(false);
    }
  };

  const initials = `${profile.nombre.charAt(0)}${profile.apellido.charAt(0)}`.trim().toUpperCase() || <UserRound size={20} />;

  return <main className="profile-page">
    <button className="back-link" onClick={onBack}><ArrowLeft size={16} /> Volver al catálogo</button>
    <div className="profile-heading"><div><span className="section-kicker">TU CUENTA MAYORISTA</span><h1>Mi perfil</h1><p>Consultá y actualizá tus datos personales y de contacto.</p></div><div className="profile-avatar">{initials}</div></div>
    {notice && <div className={`profile-notice ${notice.type}`} role="status">{notice.type === 'success' ? <Check size={17} /> : <BadgeCheck size={17} />}{notice.text}</div>}
    {loading ? <div className="profile-loading"><LoaderCircle className="spin" /> Cargando tus datos...</div> : <form className="profile-form" onSubmit={save}>
      <section className="profile-section"><header><span><UserRound /></span><div><h2>Datos personales</h2><p>Esta información identifica al titular de la cuenta.</p></div></header><div className="profile-fields">
        <label>Nombre<input name="nombre" value={profile.nombre} onChange={update} required /></label>
        <label>Apellido<input name="apellido" value={profile.apellido} onChange={update} required /></label>
        <label>Fecha de nacimiento<input name="fechaNacimiento" type="date" value={profile.fechaNacimiento} onChange={update} required /></label>
        <label>Sexo<select name="sexo" value={profile.sexo ?? ''} onChange={update}><option value="">Prefiero no indicarlo</option><option value="FEMENINO">Femenino</option><option value="MASCULINO">Masculino</option><option value="OTRO">Otro</option></select></label>
        <label>Tipo de documento<select name="tipoDocumento" value={profile.tipoDocumento} onChange={update}><option value="DNI">DNI</option><option value="PASAPORTE">Pasaporte</option></select></label>
        <label>Número de documento<input name="numeroDocumento" value={profile.numeroDocumento} onChange={update} required /></label>
      </div></section>
      <section className="profile-section"><header><span><BadgeCheck /></span><div><h2>Contacto</h2><p>Usamos estos datos para novedades de pedidos y facturación.</p></div></header><div className="profile-fields">
        <label>Correo electrónico<input value={profile.email} type="email" readOnly disabled /></label>
        <label>Tipo de teléfono<select name="tipoTelefono" value={profile.tipoTelefono} onChange={update}><option value="CELULAR">Celular</option><option value="FIJO">Fijo</option></select></label>
        <label>Teléfono<input name="telefono" type="tel" value={profile.telefono} onChange={update} required /></label>
      </div></section>
      <section className="profile-section"><header><span><BadgeCheck /></span><div><h2>Domicilio</h2><p>Dirección principal asociada a tu cuenta.</p></div></header><div className="profile-fields">
        <label>Provincia<select name="provincia" value={profile.provincia} onChange={changeProvince} required><option value="">Seleccioná una provincia</option>{provinces.map(option => <option key={option.id || option.name} value={option.name}>{option.name}</option>)}</select></label>
        <label>Departamento<select name="departamento" value={profile.departamento} onChange={changeDepartment} disabled={!profile.provincia} required><option value="">Seleccioná un departamento</option>{departments.map(option => <option key={option.id || option.name} value={option.name}>{option.name}</option>)}</select></label>
        <label>Localidad<select name="localidad" value={profile.localidad} onChange={changeLocality} disabled={!profile.departamento} required><option value="">Seleccioná una localidad</option>{localities.map(option => <option key={option.id || option.name} value={option.name}>{option.name}</option>)}</select></label>
        <label>Código postal<input name="codigoPostal" value={profile.codigoPostal} onChange={update} required /></label>
        <label>Calle<input name="calle" value={profile.calle} onChange={update} required /></label>
        <label>Número<input name="numeroCalle" value={profile.numeroCalle} onChange={update} required /></label>
        <label>Piso / Manzana<input name="manzanaPiso" value={profile.manzanaPiso} onChange={update} /></label>
        <label>Casa / Departamento<input name="casaDepartamento" value={profile.casaDepartamento} onChange={update} /></label>
        <label className="profile-wide">Referencia<textarea name="referencia" value={profile.referencia} onChange={update} rows={3} /></label>
      </div></section>
      <footer className="profile-form-footer"><span><BadgeCheck size={15} /> Tus datos se guardan de forma segura.</span><button className="profile-save" type="submit" disabled={saving}>{saving ? <LoaderCircle className="spin" /> : <Save size={16} />}{saving ? 'Guardando...' : 'Guardar cambios'}</button></footer>
    </form>}
  </main>;
}
