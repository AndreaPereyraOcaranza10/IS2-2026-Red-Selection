import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react';
import { Building2, LoaderCircle, MapPin, Pencil, Plus, Search, Trash2, X } from 'lucide-react';
import { api, type CompanyBranch, type LocationOption } from '../lib/api';

type BranchForm = Omit<CompanyBranch, 'id' | 'localidad' | 'departamento' | 'provincia'>;
const blankForm = (): BranchForm => ({ razonSocial: '', cuit: '', calle: '', numeracion: '', barrio: '', manzanaPiso: '', casaDepartamento: '', referencia: '', localidadId: '', email: '' });

export function BranchesAdmin() {
  const [branches, setBranches] = useState<CompanyBranch[]>([]);
  const [provinces, setProvinces] = useState<LocationOption[]>([]);
  const [departments, setDepartments] = useState<LocationOption[]>([]);
  const [localities, setLocalities] = useState<LocationOption[]>([]);
  const [provinceId, setProvinceId] = useState('');
  const [departmentId, setDepartmentId] = useState('');
  const [form, setForm] = useState<BranchForm>(blankForm());
  const [editing, setEditing] = useState<string | null>(null);
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  const loadBranches = useCallback(async () => {
    setLoading(true);
    try { setBranches(await api.branches()); setError(''); }
    catch (cause) { setError(cause instanceof Error ? cause.message : 'No se pudieron cargar las sucursales.'); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => {
    void loadBranches();
    api.provinces().then(setProvinces).catch(cause => setError(cause instanceof Error ? cause.message : 'No se pudieron cargar las provincias.'));
  }, [loadBranches]);

  const visible = useMemo(() => branches.filter(branch => `${branch.razonSocial} ${branch.cuit} ${branch.localidad} ${branch.departamento} ${branch.provincia}`.toLowerCase().includes(query.toLowerCase())), [branches, query]);

  const newBranch = () => { setEditing(null); setForm(blankForm()); setProvinceId(''); setDepartmentId(''); setDepartments([]); setLocalities([]); setError(''); setOpen(true); };

  const editBranch = async (branch: CompanyBranch) => {
    setError('');
    setEditing(branch.id);
    setForm({ razonSocial: branch.razonSocial, cuit: branch.cuit, calle: branch.calle, numeracion: branch.numeracion, barrio: branch.barrio || '', manzanaPiso: branch.manzanaPiso || '', casaDepartamento: branch.casaDepartamento || '', referencia: branch.referencia || '', localidadId: branch.localidadId, email: branch.email || '' });
    setOpen(true);
    const province = provinces.find(item => item.name === branch.provincia);
    if (!province) return;
    setProvinceId(province.id);
    const deps = await api.departments(province.id).catch(() => []);
    setDepartments(deps);
    const department = deps.find(item => item.name === branch.departamento);
    if (!department) return;
    setDepartmentId(department.id);
    const locs = await api.localities(department.id).catch(() => []);
    setLocalities(locs);
  };

  const changeProvince = async (id: string) => {
    setProvinceId(id); setDepartmentId(''); setDepartments([]); setLocalities([]); setForm(value => ({ ...value, localidadId: '' }));
    if (id) setDepartments(await api.departments(id).catch(cause => { setError(cause instanceof Error ? cause.message : 'No se pudieron cargar los departamentos.'); return []; }));
  };
  const changeDepartment = async (id: string) => {
    setDepartmentId(id); setLocalities([]); setForm(value => ({ ...value, localidadId: '' }));
    if (id) setLocalities(await api.localities(id).catch(cause => { setError(cause instanceof Error ? cause.message : 'No se pudieron cargar las localidades.'); return []; }));
  };

  const submit = async (event: FormEvent) => {
    event.preventDefault(); setSaving(true); setError('');
    try {
      const saved = editing ? await api.updateBranch(editing, form) : await api.createBranch(form);
      setNotice(editing ? `Sucursal ${saved.razonSocial} actualizada.` : `Sucursal ${saved.razonSocial} creada.`);
      setOpen(false); await loadBranches();
    } catch (cause) { setError(cause instanceof Error ? cause.message : 'No se pudo guardar la sucursal.'); }
    finally { setSaving(false); }
  };

  const remove = async (branch: CompanyBranch) => {
    if (!window.confirm(`Dar de baja la sucursal ${branch.razonSocial}?`)) return;
    try { await api.deleteBranch(branch.id); setNotice(`Sucursal ${branch.razonSocial} dada de baja.`); await loadBranches(); }
    catch (cause) { setError(cause instanceof Error ? cause.message : 'No se pudo dar de baja la sucursal.'); }
  };

  const set = (key: keyof BranchForm, value: string) => setForm(current => ({ ...current, [key]: value }));
  return <>
    <section className="orders-table-card branch-admin-card">
      <header className="table-title"><div><h2>Sucursales registradas</h2><p>Empresas de tipo sucursal segun el modelo Empresa</p></div><b>{branches.length} sucursales</b></header>
      <div className="table-toolbar"><label><Search /><input value={query} onChange={event => setQuery(event.target.value)} placeholder="Buscar por nombre, CUIT o localidad..." /></label><button className="ops-primary" onClick={newBranch}><Plus /> Nueva sucursal</button></div>
      {notice && <div className="ops-notice"><Building2 />{notice}<button onClick={() => setNotice('')} aria-label="Cerrar"><X /></button></div>}
      {error && !open && <div className="ops-notice ops-notice-error" role="alert">{error}<button onClick={() => void loadBranches()}>Reintentar</button></div>}
      {loading ? <div className="ops-loading"><LoaderCircle className="spin" /> Cargando sucursales...</div> : visible.length === 0 ? <div className="ops-empty">{branches.length ? 'No hay sucursales que coincidan con la busqueda.' : 'Todavia no hay sucursales registradas.'}</div> :
        <div className="branch-admin-list">{visible.map(branch => <article className="branch-admin-item" key={branch.id}>
          <span className="branch-admin-icon"><Building2 /></span><div className="branch-admin-details"><h3>{branch.razonSocial}</h3><p><MapPin /> {branch.calle} {branch.numeracion}{branch.barrio ? `, ${branch.barrio}` : ''} · {branch.localidad}, {branch.departamento}, {branch.provincia}</p><small>CUIT {branch.cuit} · {branch.email}</small></div>
          <div className="branch-admin-actions"><button onClick={() => void editBranch(branch)} aria-label={`Editar ${branch.razonSocial}`}><Pencil /> Editar</button><button className="danger" onClick={() => void remove(branch)} aria-label={`Dar de baja ${branch.razonSocial}`}><Trash2 /> Baja</button></div>
        </article>)}</div>}
    </section>
    {open && <div className="manual-modal-backdrop" onMouseDown={event => { if (event.target === event.currentTarget) setOpen(false); }}><section className="manual-modal branch-form-modal" role="dialog" aria-modal="true" aria-labelledby="branch-form-title">
      <header><div><span className="section-kicker">EMPRESA · TIPO SUCURSAL</span><h2 id="branch-form-title">{editing ? 'Editar sucursal' : 'Nueva sucursal'}</h2></div><button onClick={() => setOpen(false)} aria-label="Cerrar"><X /></button></header>
      <form onSubmit={submit}>
        <div className="branch-form-grid">
          <label>Nombre / razon social<input required value={form.razonSocial} onChange={event => set('razonSocial', event.target.value)} /></label>
          <label>CUIT<input required inputMode="numeric" value={form.cuit} onChange={event => set('cuit', event.target.value)} placeholder="30-71234567-8" /></label>
          <label>Email de contacto<input required type="email" value={form.email} onChange={event => set('email', event.target.value)} placeholder="sucursal@dulcemayor.com" /></label>
          <label>Calle<input required value={form.calle} onChange={event => set('calle', event.target.value)} /></label>
          <label>Numeracion<input required value={form.numeracion} onChange={event => set('numeracion', event.target.value)} /></label>
          <label>Barrio<input value={form.barrio || ''} onChange={event => set('barrio', event.target.value)} /></label>
          <label>Provincia<select required value={provinceId} onChange={event => void changeProvince(event.target.value)}><option value="">Seleccionar provincia</option>{provinces.map(item => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label>
          <label>Departamento<select required value={departmentId} disabled={!provinceId} onChange={event => void changeDepartment(event.target.value)}><option value="">Seleccionar departamento</option>{departments.map(item => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label>
          <label>Localidad<select required value={form.localidadId} disabled={!departmentId} onChange={event => set('localidadId', event.target.value)}><option value="">Seleccionar localidad</option>{localities.map(item => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label>
          <label>Manzana / piso<input value={form.manzanaPiso || ''} onChange={event => set('manzanaPiso', event.target.value)} /></label>
          <label>Casa / departamento<input value={form.casaDepartamento || ''} onChange={event => set('casaDepartamento', event.target.value)} /></label>
          <label>Referencia<input value={form.referencia || ''} onChange={event => set('referencia', event.target.value)} /></label>
        </div>
        {error && <p className="manual-error" role="alert">{error}</p>}
        <footer><button type="button" className="ops-secondary" onClick={() => setOpen(false)}>Cancelar</button><button className="ops-primary" disabled={saving}>{saving ? <LoaderCircle className="spin" /> : <Plus />}{saving ? 'Guardando...' : editing ? 'Guardar cambios' : 'Crear sucursal'}</button></footer>
      </form>
    </section></div>}
  </>;
}
